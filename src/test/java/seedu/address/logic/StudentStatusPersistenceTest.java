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
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.model.person.StudentStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises student status changes through LogicManager and JSON storage.
 */
public class StudentStatusPersistenceTest {

    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan")
            .withGuardianId(guardian.getId()).build();
    private final Person unrelatedStudent = new PersonBuilder().withName("Zoe Lim").build();

    @Test
    public void execute_archiveAndReload_preservesStatusDetailsAndGuardian() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel(StudentStatus.ACTIVE);
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute("find Tan");

        logic.execute("archive 1");

        assertStudentState(model, StudentStatus.FORMER);
        Model reloaded = new ModelManager(new JsonAddressBookStorage(file).readAddressBook().orElseThrow(),
                new UserPrefs());
        assertStudentState(reloaded, StudentStatus.FORMER);
    }

    @Test
    public void execute_unarchiveAndReload_preservesStatusDetailsAndGuardian() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel(StudentStatus.FORMER);
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute("find Tan");

        logic.execute("unarchive 1");

        assertStudentState(model, StudentStatus.ACTIVE);
        Model reloaded = new ModelManager(new JsonAddressBookStorage(file).readAddressBook().orElseThrow(),
                new UserPrefs());
        assertStudentState(reloaded, StudentStatus.ACTIVE);
    }

    @Test
    public void execute_archiveAndUnarchiveInAllStatusView_keepsStudentShownAndLinked() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel(StudentStatus.ACTIVE);
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute("list st/all");

        logic.execute("archive " + getDisplayedIndex(model, student.getId()));

        assertStudentState(model, StudentStatus.FORMER);
        assertAllContactsShown(model);

        logic.execute("unarchive " + getDisplayedIndex(model, student.getId()));

        assertStudentState(model, StudentStatus.ACTIVE);
        assertAllContactsShown(model);
    }

    @Test
    public void execute_archiveSaveFailure_preservesContactsFilterAndSavedFile() throws Exception {
        assertSaveFailurePreservesState("archive 1", StudentStatus.ACTIVE);
    }

    @Test
    public void execute_unarchiveSaveFailure_preservesContactsFilterAndSavedFile() throws Exception {
        assertSaveFailurePreservesState("unarchive 1", StudentStatus.FORMER);
    }

    private Model createModel(StudentStatus status) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(guardian, student.withStatus(status), unrelatedStudent));
        return new ModelManager(addressBook, new UserPrefs());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private Person findContact(Model model, ContactId id) {
        return PersonLookup.findById(model.getAddressBook().getPersonList(), id).orElseThrow();
    }

    private int getDisplayedIndex(Model model, ContactId id) {
        List<Person> shownContacts = model.getFilteredPersonList();
        for (int i = 0; i < shownContacts.size(); i++) {
            if (shownContacts.get(i).getId().equals(id)) {
                return i + 1;
            }
        }
        throw new AssertionError("Contact is not shown: " + id);
    }

    private void assertAllContactsShown(Model model) {
        assertEquals(ContactFilter.ALL_CONTACTS, model.getPersonFilter());
        assertEquals(3, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream()
                .anyMatch(person -> person.getId().equals(student.getId())));
    }

    private void assertStudentState(Model model, StudentStatus expectedStatus) {
        Person actualStudent = findContact(model, student.getId());
        assertEquals(expectedStatus, actualStudent.getStatus());
        assertEquals(student.getName(), actualStudent.getName());
        assertEquals(student.getPhone(), actualStudent.getPhone());
        assertEquals(student.getEmail(), actualStudent.getEmail());
        assertEquals(student.getRole(), actualStudent.getRole());
        assertEquals(Optional.of(guardian.getId()), actualStudent.getGuardianId());
        assertEquals(guardian, findContact(model, guardian.getId()));
        assertEquals(unrelatedStudent, findContact(model, unrelatedStudent.getId()));
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    private void assertSaveFailurePreservesState(String command, StudentStatus originalStatus) throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel(originalStatus);
        storage.saveAddressBook(model.getAddressBook());
        createLogic(model, storage).execute("find Tan");
        AddressBook originalContacts = new AddressBook(model.getAddressBook());
        Predicate<Person> originalFilter = model.getPersonFilter();
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        String originalFile = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };
        Logic logic = createLogic(model, failingStorage);

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute(command));

        assertStudentState(model, originalStatus);
        assertEquals(originalContacts, model.getAddressBook());
        assertSame(originalFilter, model.getPersonFilter());
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }
}
