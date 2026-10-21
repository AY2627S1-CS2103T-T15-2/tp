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
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
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
import seedu.address.model.person.StudentStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises relationship changes across filtered lists, student status and bulk deletion.
 */
public class RelationshipLifecyclePersistenceTest {

    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person activeStudent = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail()
            .withGuardianId(guardian.getId()).withLevel("Sec 3").withSubject("Math").build();
    private final Person formerStudent = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail()
            .withGuardianId(guardian.getId()).withStatus(StudentStatus.FORMER)
            .withLevel("Sec 4").withSubject("Science").build();
    private final Person unrelatedStudent = new PersonBuilder().withName("Zoe Lim").build();

    @Test
    public void execute_bulkDeleteGuardianAndActiveStudent_clearsHiddenFormerLinkAndPersists() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        Logic logic = createLogic(model, storage);
        logic.execute("list");
        assertEquals(List.of(activeStudent, guardian, unrelatedStudent), List.copyOf(model.getFilteredPersonList()));

        CommandResult result = logic.execute("delete 2 1");

        assertTrue(result.getFeedbackToUser().endsWith(String.format(DeleteCommand.MESSAGE_REMOVED_RELATIONSHIPS, 2)));
        assertEquals(List.of(unrelatedStudent), List.copyOf(model.getFilteredPersonList()));
        assertRetainedContacts(model);
        Model reloaded = reload(file);
        assertRetainedContacts(reloaded);
        createLogic(reloaded, new JsonAddressBookStorage(file)).execute("list st/former");
        assertEquals(List.of(formerStudent.withGuardianId(Optional.empty())),
                List.copyOf(reloaded.getFilteredPersonList()));
    }

    @Test
    public void execute_bulkDeleteSaveFailure_preservesHiddenLinksFilterAndFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        createLogic(model, storage).execute("list");
        Predicate<Person> originalFilter = model.getPersonFilter();
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        List<Person> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        List<ContactId> originalIds = contactIds(model);
        String originalFile = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Simulated disk failure");
            }
        };

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () ->
                createLogic(model, failingStorage).execute("delete 2 1"));

        assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(originalIds, contactIds(model));
        assertSame(originalFilter, model.getPersonFilter());
        assertEquals(originalDisplay, List.copyOf(model.getFilteredPersonList()));
        assertEquals(originalFile, Files.readString(file));
        Model reloaded = reload(file);
        assertEquals(originalContacts, List.copyOf(reloaded.getAddressBook().getPersonList()));
        assertEquals(originalIds, contactIds(reloaded));
    }

    @Test
    public void execute_relinkFormerStudent_preservesStatusDetailsAndSameNameSibling() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        Person replacement = new PersonBuilder().withName("Sarah Tan").withRole("guardian").build();
        model.addPerson(replacement);
        Logic logic = createLogic(model, storage);
        logic.execute("find Tan");
        int studentIndex = model.getFilteredPersonList().indexOf(formerStudent) + 1;
        int guardianIndex = model.getFilteredPersonList().indexOf(replacement) + 1;
        Predicate<Person> originalFilter = model.getPersonFilter();

        CommandResult result = logic.execute("link s/" + studentIndex + " g/" + guardianIndex);

        assertEquals(Optional.of(formerStudent.getId()), result.getSelectedContactId());
        assertSame(originalFilter, model.getPersonFilter());
        Person expected = formerStudent.withGuardianId(Optional.of(replacement.getId()));
        assertEquals(expected, PersonLookup.findById(model.getAddressBook().getPersonList(), formerStudent.getId())
                .orElseThrow());
        Model reloaded = reload(file);
        assertEquals(expected, PersonLookup.findById(reloaded.getAddressBook().getPersonList(), formerStudent.getId())
                .orElseThrow());
        assertEquals(activeStudent,
                PersonLookup.findById(reloaded.getAddressBook().getPersonList(), activeStudent.getId()).orElseThrow());
        assertFalse(reloaded.getFilteredPersonList().contains(expected));
    }

    @Test
    public void execute_bulkDeleteInvalidIndex_doesNotRemoveValidTargetOrRelationships() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = createModel();
        storage.saveAddressBook(model.getAddressBook());
        List<Person> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        String originalFile = Files.readString(file);

        assertThrows(CommandException.class, String.format(DeleteCommand.MESSAGE_CONTACT_NOT_FOUND, 99), () ->
                createLogic(model, storage).execute("delete 2 99"));

        assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(originalFile, Files.readString(file));
    }

    private Model createModel() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(guardian, formerStudent, unrelatedStudent, activeStudent));
        return new ModelManager(book, new UserPrefs());
    }

    private List<ContactId> contactIds(Model model) {
        return model.getAddressBook().getPersonList().stream().map(Person::getId).toList();
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private Model reload(Path file) throws Exception {
        return new ModelManager(new JsonAddressBookStorage(file).readAddressBook().orElseThrow(), new UserPrefs());
    }

    private void assertRetainedContacts(Model model) {
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(formerStudent.withGuardianId(Optional.empty()),
                PersonLookup.findById(model.getAddressBook().getPersonList(), formerStudent.getId()).orElseThrow());
        assertEquals(unrelatedStudent,
                PersonLookup.findById(model.getAddressBook().getPersonList(), unrelatedStudent.getId()).orElseThrow());
        assertTrue(PersonLookup.findById(model.getAddressBook().getPersonList(), guardian.getId()).isEmpty());
        assertTrue(PersonLookup.findById(model.getAddressBook().getPersonList(), activeStudent.getId()).isEmpty());
    }
}
