package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
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

/**
 * Exercises editing student details through LogicManager and JSON storage.
 */
public class EditStudentDetailsPersistenceTest {

    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan").withLevel("Sec 3").withSubject("Math")
            .withStatus(StudentStatus.FORMER).withGuardianId(guardian.getId()).build();
    private final Person unrelatedStudent = new PersonBuilder().withName("Zoe Lim").build();

    @Test
    public void execute_setDetailsAndReload_preservesContactIdentityStatusAndGuardian() throws Exception {
        Person expected = new PersonBuilder(student).withLevel("Sec 4").withSubject("Physics").build();
        assertEditPersists("edit 1 lv/Sec 4 sub/Physics", expected);
    }

    @Test
    public void execute_removeLevelAndReload_preservesSubject() throws Exception {
        assertEditPersists("edit 1 lv/NONE", new PersonBuilder(student).withoutLevel().build());
    }

    @Test
    public void execute_removeSubjectAndReload_preservesLevel() throws Exception {
        assertEditPersists("edit 1 sub/none", new PersonBuilder(student).withoutSubject().build());
    }

    @Test
    public void execute_setDetailsSaveFailure_preservesContactsFilterAndSavedFile() throws Exception {
        assertSaveFailurePreservesState("edit 1 lv/Sec 4 sub/Physics");
    }

    @Test
    public void execute_removeDetailsSaveFailure_preservesContactsFilterAndSavedFile() throws Exception {
        assertSaveFailurePreservesState("edit 1 lv/NONE sub/NONE");
    }

    private Model createModel() {
        return new ModelManager(addressBookWith(student), new UserPrefs());
    }

    private AddressBook addressBookWith(Person expectedStudent) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(guardian, expectedStudent, unrelatedStudent));
        return addressBook;
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertEditPersists(String command, Person expectedStudent) throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute("find Alex");

        logic.execute(command);

        assertContactsEqual(addressBookWith(expectedStudent), model.getAddressBook());
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertContactsEqual(addressBookWith(expectedStudent), reloaded);
    }

    private void assertSaveFailurePreservesState(String command) throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        createLogic(model, storage).execute("find Alex");
        AddressBook originalContacts = new AddressBook(model.getAddressBook());
        Predicate<Person> originalFilter = model.getPersonFilter();
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        byte[] originalFile = Files.readAllBytes(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };
        Logic logic = createLogic(model, failingStorage);

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute(command));

        assertContactsEqual(originalContacts, model.getAddressBook());
        assertSame(originalFilter, model.getPersonFilter());
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertArrayEquals(originalFile, Files.readAllBytes(file));
    }

    private void assertContactsEqual(ReadOnlyAddressBook expected, ReadOnlyAddressBook actual) {
        assertEquals(expected, actual);
        // Person.equals compares contact data but deliberately excludes IDs.
        assertEquals(expected.getPersonList().stream().map(Person::getId).toList(),
                actual.getPersonList().stream().map(Person::getId).toList());
    }
}
