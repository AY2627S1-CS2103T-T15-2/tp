package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests unquoted parentheses and commas in names through Add, Edit and saved-data loading.
 */
public class ParenthesesAndCommaNameTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addAndEditParentheses_preservesNameAndIdOnReload() throws Exception {
        assertNameSurvivesAddEditAndReload("Tan Wei Ming (David)", "Alex Tan (Al)");
    }

    @Test
    public void execute_addAndEditCommas_preservesNameAndIdOnReload() throws Exception {
        assertNameSurvivesAddEditAndReload("Lee Ah Kow, Jr.", "Alex Tan, Jr.");
    }

    private void assertNameSurvivesAddEditAndReload(String addedName, String editedName) throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage contactsStorage = new JsonAddressBookStorage(contactsFile);
        StorageManager storage = new StorageManager(contactsStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        logic.execute("add r/student n/" + addedName);
        ContactId originalId = logic.getPersonList().getFirst().getId();
        assertEquals(addedName, logic.getPersonList().getFirst().getName().fullName);
        assertSavedContact(contactsFile, addedName, originalId);

        logic.execute("edit 1 n/" + editedName);
        assertEquals(editedName, logic.getPersonList().getFirst().getName().fullName);
        assertEquals(originalId, logic.getPersonList().getFirst().getId());
        assertSavedContact(contactsFile, editedName, originalId);
    }

    private void assertSavedContact(Path contactsFile, String expectedName, ContactId expectedId) throws Exception {
        JsonAddressBookStorage reloadedStorage = new JsonAddressBookStorage(contactsFile);
        List<Person> contacts = reloadedStorage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(1, contacts.size());
        assertEquals(expectedName, contacts.getFirst().getName().fullName);
        assertEquals(expectedId, contacts.getFirst().getId());
    }
}
