package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
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
 * Exercises deletion from the former-student list through LogicManager and JSON storage.
 */
public class DeleteFormerStudentPersistenceTest {

    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person activeStudent = new PersonBuilder().withName("Alex Tan").withPhone("81234567").build();
    private final Person formerStudent = new PersonBuilder().withName("Alex Tan").withPhone("92345678")
            .withGuardianId(guardian.getId()).withStatus(StudentStatus.FORMER).build();

    @Test
    public void execute_deleteFromFormerList_removesDisplayedStudentAndPersists() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute(ListCommand.LIST_FORMER_COMMAND);

        CommandResult result = logic.execute("delete 1");

        assertEquals("Deleted contact: Alex Tan [Student]. Removed 1 student-guardian relationship.",
                result.getFeedbackToUser());
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
        assertFalse(model.getAddressBook().getPersonList().contains(formerStudent));
        assertTrue(model.getAddressBook().getPersonList().contains(activeStudent));
        assertTrue(model.getAddressBook().getPersonList().contains(guardian));

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertFalse(reloaded.getAddressBook().getPersonList().contains(formerStudent));
        assertTrue(reloaded.getAddressBook().getPersonList().contains(activeStudent));
        assertTrue(reloaded.getAddressBook().getPersonList().contains(guardian));
    }

    @Test
    public void execute_outOfRangeIndexInFormerList_preservesData() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);
        logic.execute(ListCommand.LIST_FORMER_COMMAND);
        AddressBook originalContacts = new AddressBook(model.getAddressBook());
        String originalFile = Files.readString(file);

        assertThrows(CommandException.class, String.format(DeleteCommand.MESSAGE_CONTACT_NOT_FOUND, 2), () ->
                logic.execute("delete 2"));

        assertEquals(originalContacts, model.getAddressBook());
        assertEquals(List.of(formerStudent), List.copyOf(model.getFilteredPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }

    @Test
    public void execute_deleteFromFormerListSaveFailure_preservesStateAndFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        createLogic(model, storage).execute(ListCommand.LIST_FORMER_COMMAND);
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
        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute("delete 1"));

        assertEquals(originalContacts, model.getAddressBook());
        assertSame(originalFilter, model.getPersonFilter());
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }

    private Model createModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(activeStudent, formerStudent, guardian));
        return new ModelManager(addressBook, new UserPrefs());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
