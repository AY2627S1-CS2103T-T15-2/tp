package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Level;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class UndoDeletionTest {
    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
    private final Person former = new PersonBuilder().withName("Zoe Tan").withGuardianId(guardian.getId())
            .withStatus(StudentStatus.FORMER).withLevel("Sec 4").withSubject("Math").build();

    private Model createModel() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(guardian, student, former));
        return new ModelManager(book, new UserPrefs());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_undoGuardianDeletion_restoresHiddenLinksIdsAndSavedData() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("delete n/Mei Tan");
        logic.execute("find Alex");

        CommandResult result = logic.execute("undo");

        assertEquals(Optional.of(guardian.getId()), result.getSelectedContactId());
        assertEquals("Restored 1 deleted contact(s).", result.getFeedbackToUser());
        assertEquals(List.of(guardian, student, former), List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(List.of(guardian.getId(), student.getId(), former.getId()),
                model.getAddressBook().getPersonList().stream().map(Person::getId).toList());
        assertSame(Model.PREDICATE_SHOW_ACTIVE_CONTACTS, model.getPersonFilter());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () ->
                createLogic(model, storage).execute("undo"));
    }

    @Test
    public void execute_undoBulkDeletion_restoresWholeBatch() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete 2 1");
        logic.execute("undo");
        assertEquals(List.of(guardian.getId(), student.getId(), former.getId()),
                model.getAddressBook().getPersonList().stream().map(Person::getId).toList());
        assertEquals(List.of(guardian, student, former), List.copyOf(model.getAddressBook().getPersonList()));
    }

    @Test
    public void execute_interveningCommands_preservesOrInvalidatesHistory() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete 1");
        assertThrows(ParseException.class, () -> logic.execute("undo 1"));
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        logic.execute("undo");
        logic.execute("delete 1");
        logic.execute("edit 1 n/New Guardian");
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
    }

    @Test
    public void execute_undoSaveFailure_keepsDeletedStateAndAllowsRetry() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        boolean[] failSave = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave[0]) {
                    throw new IOException("Simulated disk failure");
                }
                super.saveAddressBook(book);
            }
        };
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("delete 2");
        List<Person> deletedState = List.copyOf(model.getAddressBook().getPersonList());
        String deletedFile = Files.readString(file);
        failSave[0] = true;

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute("undo"));
        assertEquals(deletedState, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(deletedFile, Files.readString(file));

        failSave[0] = false;
        logic.execute("undo");
        assertEquals(List.of(guardian, student, former), List.copyOf(model.getAddressBook().getPersonList()));
    }

    @Test
    public void execute_secondDeletion_replacesUndoOpportunity() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete 1");
        logic.execute("delete 1");
        logic.execute("undo");
        assertEquals(List.of(guardian.getId(), former.getId()), model.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
        assertTrue(model.getAddressBook().getPersonList().stream()
                .noneMatch(person -> person.getId().equals(student.getId())));
    }

    @Test
    public void execute_otherSuccessfulChanges_invalidateUndoHistory() throws Exception {
        for (String command : List.of("add r/student n/New Student", "edit 1 n/Renamed Student", "clear",
                "archive 1", "unarchive 4", "link s/1 g/3")) {
            Model model = createModel();
            model.addPerson(new PersonBuilder().withName("Sarah Tan").withRole("guardian").build());
            model.addPerson(new PersonBuilder().withName("Temporary Contact").build());
            Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
            logic.execute("delete n/Temporary Contact");
            logic.execute("find Tan");

            logic.execute(command);

            assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
        }
    }

    @Test
    public void execute_failedInterveningSaves_preserveOriginalUndoAndSavedFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        boolean[] failSave = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave[0]) {
                    throw new IOException("Simulated disk failure");
                }
                super.saveAddressBook(book);
            }
        };
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("delete n/Alex Tan");
        List<Person> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        String originalFile = Files.readString(file);
        failSave[0] = true;
        for (String command : List.of("delete n/Mei Tan", "edit 1 n/Changed Name", "clear")) {
            assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute(command));
            assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
            assertEquals(originalFile, Files.readString(file));
        }
        failSave[0] = false;

        logic.execute("undo");

        assertEquals(List.of(guardian.getId(), student.getId(), former.getId()),
                model.getAddressBook().getPersonList().stream().map(Person::getId).toList());
        assertEquals(List.of(guardian, student, former), List.copyOf(model.getAddressBook().getPersonList()));
    }

    @Test
    public void execute_formerStudentAfterReadOnlyCommands_restoresFilterAndSelection() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("list st/former");
        logic.execute("delete 1");
        logic.execute("list");
        logic.execute("view 1");
        logic.execute("help");
        logic.execute("find Tan");

        CommandResult result = logic.execute("undo");

        assertEquals(Optional.of(former.getId()), result.getSelectedContactId());
        assertEquals(List.of(former), List.copyOf(model.getFilteredPersonList()));
        assertEquals(List.of(former.getId()), model.getFilteredPersonList().stream().map(Person::getId).toList());
        assertEquals(former.getGuardianId(), model.getFilteredPersonList().getFirst().getGuardianId());
    }

    @Test
    public void execute_undoInLevelAndSubjectView_restoresBothCriteria() throws Exception {
        Person sec3Math = new PersonBuilder().withName("Cal Lim").withLevel("Sec 3").withSubject("Math").build();
        Person otherSec3Math = new PersonBuilder().withName("Dan Lim").withLevel("Sec 3").withSubject("Math").build();
        Person sec3Physics = new PersonBuilder().withName("Eve Lim").withLevel("Sec 3").withSubject("Physics")
                .build();
        Person sec4Math = new PersonBuilder().withName("Fay Lim").withLevel("Sec 4").withSubject("Math").build();
        AddressBook book = new AddressBook();
        book.setPersons(List.of(sec3Math, otherSec3Math, sec3Physics, sec4Math, guardian));
        Model model = new ModelManager(book, new UserPrefs());
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        ContactFilter sec3MathFilter = new ContactFilter(Optional.empty(), Optional.of(StudentStatus.ACTIVE),
                Optional.of(new Level("Sec 3")), Optional.of(new Subject("Math")));
        logic.execute("list lv/Sec 3 sub/Math");
        logic.execute("delete 1");
        // Show every contact, so that the filter after undo can only come from the deletion
        logic.execute("list st/all");

        CommandResult result = logic.execute("undo");

        assertEquals(sec3MathFilter, model.getPersonFilter());
        assertEquals(List.of(sec3Math, otherSec3Math), List.copyOf(model.getFilteredPersonList()));
        assertEquals(Optional.of(sec3Math.getId()), result.getSelectedContactId());
        assertEquals(5, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_sameNameContactlessStudents_restoresBothOriginalIdentities() throws Exception {
        Model model = new ModelManager();
        Person first = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
        Person second = new PersonBuilder(first).withId(ContactId.generate()).build();
        model.addPerson(first);
        model.addPerson(second);
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete 2");

        logic.execute("undo");

        assertEquals(List.of(first.getId(), second.getId()), model.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void execute_invalidDataFile_blocksUndoAndPreservesFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Files.writeString(file, "{invalid json}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Logic logic = createLogic(new ModelManager(), storage);

        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_LOAD_FAILURE, () -> logic.execute("undo"));

        assertEquals("{invalid json}", Files.readString(file));
    }
}
