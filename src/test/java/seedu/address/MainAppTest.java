package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    private StorageManager createStorage(Path contactsFile) {
        return new StorageManager(new JsonAddressBookStorage(contactsFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    @Test
    public void initModelManager_missingFile_startsEmptyAndSavesFirstContact() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        StorageManager storage = createStorage(contactsFile);
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(contactsFile));
        assertTrue(logic.getStartupMessage().isEmpty());

        logic.execute("add n/Alex Tan p/91234567 e/alex@example.com a/Kent Ridge r/student");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(Files.exists(contactsFile));
        Model reloaded = new MainApp().initModelManager(createStorage(contactsFile), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(model.getAddressBook().getPersonList().getFirst().getId(),
                reloaded.getAddressBook().getPersonList().getFirst().getId());
    }

    @Test
    public void initModelManager_invalidFile_startsEmptyAndDisablesChanges() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        String invalidData = "{invalid json}";
        Files.writeString(contactsFile, invalidData);
        StorageManager storage = createStorage(contactsFile);
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(Optional.of(LogicManager.MESSAGE_DATA_LOAD_FAILURE), logic.getStartupMessage());
        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_LOAD_FAILURE, () -> logic.execute("clear"));
        assertEquals(invalidData, Files.readString(contactsFile));
    }

    @Test
    public void initModelManager_validFile_restoresContactsAndGuardianRelationship() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        AddressBook contacts = new AddressBook();
        contacts.setPersons(List.of(student, guardian));
        StorageManager storage = createStorage(contactsFile);
        storage.saveAddressBook(contacts);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(contacts, model.getAddressBook());
        assertEquals(student.getId(), model.getAddressBook().getPersonList().getFirst().getId());
        assertEquals(Optional.of(guardian.getId()),
                model.getAddressBook().getPersonList().getFirst().getGuardianId());
        assertFalse(storage.isDataFileLoadFailed());
    }
}
