package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
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
        assertEquals("91234567", findContact(reloaded, guardian.getId()).getPhone().value);
        assertEquals(List.of(guardian.getId(), student.getId()), reloaded.getFilteredPersonList().stream()
                .map(Person::getId).toList());
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

}
