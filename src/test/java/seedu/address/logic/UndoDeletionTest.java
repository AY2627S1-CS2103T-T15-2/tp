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

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
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
    public void execute_bulkDeletion_thenUndo_restoresWholeBatch() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete 2 1");
        logic.execute("undo");
        assertEquals(List.of(guardian.getId(), student.getId(), former.getId()),
                model.getAddressBook().getPersonList().stream().map(Person::getId).toList());
        assertEquals(List.of(guardian, student, former), List.copyOf(model.getAddressBook().getPersonList()));
    }

    @Test
    public void execute_failedCommands_preserveHistory_successfulEditInvalidatesIt() throws Exception {
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
}
