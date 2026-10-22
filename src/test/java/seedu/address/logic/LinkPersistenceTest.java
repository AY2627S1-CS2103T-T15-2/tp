package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LinkPersistenceTest {

    @TempDir
    public Path temporaryFolder;

    private final Person student = new PersonBuilder().withName("Alex Tan").build();
    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();

    private Model createModel() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(guardian, student));
        return new ModelManager(book, new UserPrefs());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private Person findContact(Model model, ContactId id) {
        return PersonLookup.findById(model.getAddressBook().getPersonList(), id).orElseThrow();
    }

    @Test
    public void execute_deleteStudentByName_persistsDeletionAndRetainsGuardian() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");

        assertEquals("Deleted contact: Alex Tan [Student]. Removed 1 student-guardian relationship.",
                logic.execute("delete n/  aLeX   tAn").getFeedbackToUser());

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(guardian.getId()), model.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
        assertEquals(List.of(guardian.getId()), reloaded.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void execute_deleteGuardianByName_persistsClearedStudentLink() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");

        assertEquals("Deleted contact: Mei Tan [Guardian]. Removed 1 student-guardian relationship.",
                logic.execute("delete n/Mei Tan").getFeedbackToUser());

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(student.getId()), reloaded.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
        assertTrue(findContact(model, student.getId()).getGuardianId().isEmpty());
        assertTrue(findContact(reloaded, student.getId()).getGuardianId().isEmpty());
    }

    @Test
    public void execute_deleteByNameSaveFailure_preservesContactsFilterAndFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");
        logic.execute("find Alex");
        List<Person> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        String originalFile = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () ->
                createLogic(model, failingStorage).execute("delete n/Alex Tan"));

        assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertEquals(Optional.of(guardian.getId()), findContact(model, student.getId()).getGuardianId());
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_ambiguousOrMissingName_preservesContactsAndFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        model.addPerson(new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build());
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        List<Person> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        String originalFile = Files.readString(file);

        assertThrows(CommandException.class, () -> logic.execute("delete n/Alex Tan"));
        assertThrows(CommandException.class, String.format(DeleteCommand.MESSAGE_NAME_NOT_FOUND, "Unknown Name"), () ->
                logic.execute("delete n/Unknown Name"));

        assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_linkSecondContactlessStudentWithSameName_updatesOnlyDisplayedTarget() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan");
        logic.execute("add r/student n/Alex Tan");
        logic.execute("add r/guardian n/Mei Tan p/98765432");
        ContactId firstStudentId = model.getFilteredPersonList().get(0).getId();
        ContactId secondStudentId = model.getFilteredPersonList().get(1).getId();
        ContactId guardianId = model.getFilteredPersonList().get(2).getId();

        assertEquals(Optional.of(secondStudentId), logic.execute("link s/2 g/3").getSelectedContactId());

        assertTrue(findContact(model, firstStudentId).getGuardianId().isEmpty());
        assertEquals(Optional.of(guardianId), findContact(model, secondStudentId).getGuardianId());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(findContact(reloaded, firstStudentId).getGuardianId().isEmpty());
        assertEquals(Optional.of(guardianId), findContact(reloaded, secondStudentId).getGuardianId());
        assertEquals(3, reloaded.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_sameNameStudentsSharingGuardian_editDeleteAndReloadPreserveOtherStudent() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan");
        logic.execute("add r/student n/Alex Tan");
        logic.execute("add r/guardian n/Mei Tan e/mei@example.com");
        ContactId firstStudentId = model.getFilteredPersonList().get(0).getId();
        ContactId secondStudentId = model.getFilteredPersonList().get(1).getId();
        ContactId guardianId = model.getFilteredPersonList().get(2).getId();
        logic.execute("link s/2 g/3");
        logic.execute("link s/1 g/3");

        logic.execute("edit 2 n/Bea Tan");

        assertEquals("Alex Tan", findContact(model, firstStudentId).getName().fullName);
        assertEquals("Bea Tan", findContact(model, secondStudentId).getName().fullName);
        assertEquals(Optional.of(guardianId), findContact(model, firstStudentId).getGuardianId());
        assertEquals(Optional.of(guardianId), findContact(model, secondStudentId).getGuardianId());
        logic.execute("edit 2 n/Alex Tan");
        logic.execute("delete 2");

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(PersonLookup.findById(reloaded.getAddressBook().getPersonList(), secondStudentId).isEmpty());
        assertEquals(Optional.of(guardianId), findContact(reloaded, firstStudentId).getGuardianId());
        assertEquals(guardianId, findContact(reloaded, guardianId).getId());
        assertEquals(2, reloaded.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_linkEditAndReload_preservesIdsAndRelationship() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link g/02 s/01");
        logic.execute("edit 1 n/Zoe Tan");
        logic.execute("edit 1 n/Bea Tan p/91234567");
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(Optional.of(guardian.getId()), findContact(reloaded, student.getId()).getGuardianId());
        assertEquals("Zoe Tan", findContact(reloaded, student.getId()).getName().fullName);
        assertEquals("Bea Tan", findContact(reloaded, guardian.getId()).getName().fullName);
        assertEquals("91234567", findContact(reloaded, guardian.getId()).getPhone().get().value);
        assertEquals(List.of(guardian.getId(), student.getId()), reloaded.getFilteredPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void execute_saveFailure_keepsContactsFilterAndSavedFileUnchanged() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage workingStorage = new JsonAddressBookStorage(file);
        Model model = createModel();
        workingStorage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(file);
        model.updateFilteredPersonList(person -> person.getName().fullName.endsWith("Tan"));
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };
        Logic logic = createLogic(model, failingStorage);
        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute("link s/1 g/2"));
        assertTrue(findContact(model, student.getId()).getGuardianId().isEmpty());
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_failedReplacement_preservesOldGuardian() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        model.addPerson(new PersonBuilder().withName("Sarah Tan").withRole("guardian").build());
        createLogic(model, storage).execute("link s/1 g/2");
        String originalFile = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };
        assertThrows(CommandException.class, () -> createLogic(model, failingStorage).execute("link s/1 g/3"));
        assertEquals(Optional.of(guardian.getId()), findContact(model, student.getId()).getGuardianId());
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_filteredLink_preservesFilterAfterSave() throws Exception {
        Model model = createModel();
        model.addPerson(new PersonBuilder().withName("Unrelated Contact").build());
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("find Tan");
        logic.execute("link s/1 g/2");
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(Optional.of(guardian.getId()), findContact(model, student.getId()).getGuardianId());
        logic.execute("list");
        assertEquals(3, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_deleteGuardian_clearsLinksWithoutDeletingStudents() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");
        logic.execute("delete 2");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(findContact(model, student.getId()).getGuardianId().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().getFirst().getGuardianId().isEmpty());
    }

    @Test
    public void execute_deleteLinkedStudent_persistsDeletionAndRetainsGuardian() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");

        logic.execute("delete 1");

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(PersonLookup.findById(reloaded.getAddressBook().getPersonList(), student.getId()).isEmpty());
        assertEquals(guardian.getId(), reloaded.getAddressBook().getPersonList().getFirst().getId());
        assertEquals(1, reloaded.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_bulkDelete_persistsAllDeletions() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = createModel();
        Person retainedStudent = new PersonBuilder().withName("Zoe Tan").build();
        model.addPerson(retainedStudent);
        Logic logic = createLogic(model, storage);
        logic.execute("link s/1 g/2");

        logic.execute("delete 2 1");

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(retainedStudent.getId()), reloaded.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void execute_bulkDeleteSaveFailure_keepsEveryContactAndSavedFileUnchanged() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage workingStorage = new JsonAddressBookStorage(file);
        Model model = createModel();
        createLogic(model, workingStorage).execute("link s/1 g/2");
        String originalFile = Files.readString(file);
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () ->
                createLogic(model, failingStorage).execute("delete 2 1"));

        assertEquals(originalPersons, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(Optional.of(guardian.getId()), findContact(model, student.getId()).getGuardianId());
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_deleteSaveFailure_keepsContactsRelationshipsAndSavedFileUnchanged() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage workingStorage = new JsonAddressBookStorage(file);
        Model model = createModel();
        createLogic(model, workingStorage).execute("link s/1 g/2");
        String originalFile = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };

        Logic failingLogic = createLogic(model, failingStorage);
        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () ->
                failingLogic.execute("delete 1"));

        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(Optional.of(guardian.getId()), findContact(model, student.getId()).getGuardianId());
        assertEquals(guardian.getId(), findContact(model, guardian.getId()).getId());
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_invalidDataFile_disablesChangesAndProtectsFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Files.writeString(file, "{invalid json}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Logic logic = createLogic(new ModelManager(), storage);
        assertEquals(Optional.of(LogicManager.MESSAGE_DATA_LOAD_FAILURE), logic.getStartupMessage());
        assertThrows(CommandException.class,
                LogicManager.MESSAGE_DATA_LOAD_FAILURE, () -> logic.execute("link s/1 g/2"));
        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_LOAD_FAILURE, () -> logic.execute("clear"));
        logic.execute("list");
        assertEquals("{invalid json}", Files.readString(file));
    }

    @Test
    public void execute_readOnlyCommands_doNotCreateDataFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Logic logic = createLogic(new ModelManager(), new JsonAddressBookStorage(file));
        logic.execute("list");
        logic.execute("help");
        assertFalse(Files.exists(file));
    }
}
