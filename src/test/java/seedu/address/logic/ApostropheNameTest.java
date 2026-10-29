package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
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

/** Tests pasted apostrophes through command execution and saved-data loading. */
public class ApostropheNameTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addAndEditApostropheVariants_preservesSpellingAcrossReloads() throws Exception {
        for (String apostrophe : List.of("'", "\u2018", "\u2019")) {
            Path dataFolder = Files.createTempDirectory(temporaryFolder, "apostrophe-");
            Path contactsFile = dataFolder.resolve("contacts.json");
            JsonAddressBookStorage contactsStorage = new JsonAddressBookStorage(contactsFile);
            StorageManager storage = new StorageManager(contactsStorage,
                    new JsonUserPrefsStorage(dataFolder.resolve("preferences.json")));
            Logic logic = new LogicManager(new ModelManager(), storage);

            String addedName = "O" + apostrophe + "Brien";
            logic.execute("add r/student n/" + addedName);
            ContactId originalId = logic.getPersonList().getFirst().getId();
            assertEquals(addedName, logic.getPersonList().getFirst().getName().fullName);
            assertEquals(addedName, contactsStorage.readAddressBook().orElseThrow()
                    .getPersonList().getFirst().getName().fullName);

            String editedName = "D" + apostrophe + "Souza";
            logic.execute("edit 1 n/" + editedName);
            assertEquals(editedName, logic.getPersonList().getFirst().getName().fullName);
            JsonAddressBookStorage reloadedStorage = new JsonAddressBookStorage(contactsFile);
            List<Person> reloadedContacts = reloadedStorage.readAddressBook().orElseThrow().getPersonList();
            assertEquals(logic.getPersonList(), reloadedContacts);
            assertEquals(originalId, reloadedContacts.getFirst().getId());
            assertEquals(originalId, logic.getPersonList().getFirst().getId());

            logic.execute("find D'Souza");
            assertEquals(1, logic.getFilteredPersonList().size());
        }
    }
}
