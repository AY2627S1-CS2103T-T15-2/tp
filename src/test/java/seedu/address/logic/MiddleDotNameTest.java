package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests middle-dot names through command execution and saved-data loading. */
public class MiddleDotNameTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addAndEditMiddleDotName_preservesSpellingAcrossReloads() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage contactsStorage = new JsonAddressBookStorage(contactsFile);
        StorageManager storage = new StorageManager(contactsStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        String addedName = "玛丽\u00b7居里";
        logic.execute("add r/student n/" + addedName);
        ContactId originalId = logic.getPersonList().getFirst().getId();
        assertEquals(addedName, logic.getPersonList().getFirst().getName().fullName);
        assertEquals(logic.getPersonList(), contactsStorage.readAddressBook().orElseThrow().getPersonList());

        String editedName = "让\u00b7皮埃尔";
        logic.execute("edit 1 n/\"" + editedName + "\"");
        assertEquals(editedName, logic.getPersonList().getFirst().getName().fullName);
        JsonAddressBookStorage reloadedStorage = new JsonAddressBookStorage(contactsFile);
        assertEquals(logic.getPersonList(), reloadedStorage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(originalId, logic.getPersonList().getFirst().getId());

        logic.execute("find " + editedName);
        assertEquals(1, logic.getFilteredPersonList().size());
    }
}
