package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Verifies actionable load-failure messages through real storage and command execution.
 */
public class DataLoadDiagnosticsTest {
    @TempDir
    public Path temporaryFolder;

    private Path getContactsFile() {
        return temporaryFolder.resolve("contacts.json");
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonAddressBookStorage(getContactsFile()),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private Logic createLogic(StorageManager storage) {
        return new LogicManager(new ModelManager(new AddressBook(), new UserPrefs()), storage);
    }

    /**
     * Loads invalid data through the real storage boundary, as application startup does.
     */
    private StorageManager loadInvalidData(String data) throws Exception {
        Files.writeString(getContactsFile(), data);
        StorageManager storage = createStorage();
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertTrue(storage.isDataFileLoadFailed());
        return storage;
    }

    @Test
    public void startupAndBlockedChange_invalidJson_showSameReasonAndPreserveFile() throws Exception {
        String invalidData = "{invalid json}";
        StorageManager storage = loadInvalidData(invalidData);
        byte[] originalBytes = Files.readAllBytes(getContactsFile());
        Logic logic = createLogic(storage);

        String warning = logic.getStartupMessage().orElseThrow();
        assertTrue(warning.startsWith(LogicManager.MESSAGE_DATA_LOAD_FAILURE));
        assertTrue(warning.contains("Reason: Invalid JSON:"));
        assertFalse(warning.contains("com.fasterxml.jackson"));
        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("add r/student n/Alex Tan"));
        assertEquals(warning, failure.getMessage());
        assertEquals(ListCommand.MESSAGE_NO_CONTACTS, logic.execute("list").getFeedbackToUser());
        assertTrue(logic.getPersonList().isEmpty());
        assertArrayEquals(originalBytes, Files.readAllBytes(getContactsFile()));
    }

    @Test
    public void startup_legacyPunctuationOnlyName_blocksChangesAndPreservesFile() throws Exception {
        String invalidData = "{\"persons\":[{\"name\":\"Alex Tan\",\"role\":\"STUDENT\"},"
                + "{\"name\":\"...\",\"role\":\"STUDENT\"}]}";
        StorageManager storage = loadInvalidData(invalidData);
        byte[] originalBytes = Files.readAllBytes(getContactsFile());
        Logic logic = createLogic(storage);

        String warning = logic.getStartupMessage().orElseThrow();
        assertTrue(warning.contains("Reason: " + Name.MESSAGE_CONSTRAINTS));
        assertEquals(warning, assertThrows(CommandException.class, () ->
                logic.execute("add r/student n/Bea Lim")).getMessage());
        assertTrue(logic.getPersonList().isEmpty());
        assertArrayEquals(originalBytes, Files.readAllBytes(getContactsFile()));

        Files.writeString(getContactsFile(), invalidData.replace("...", "Bea Lim"));
        StorageManager restartedStorage = createStorage();
        assertEquals(2, restartedStorage.readAddressBook().orElseThrow().getPersonList().size());
        assertFalse(restartedStorage.isDataFileLoadFailed());
    }

    @Test
    public void startup_invalidGuardianFields_explainsValidationRule() throws Exception {
        String invalidData = "{\"persons\":[{\"name\":\"Mei Tan\",\"phone\":\"91234567\","
                + "\"role\":\"GUARDIAN\",\"level\":\"Sec 3\"}]}";
        StorageManager storage = loadInvalidData(invalidData);

        String reason = storage.getDataFileLoadFailureReason().orElseThrow();
        assertEquals("Only a student can have a level or subject.", reason);
        assertTrue(createLogic(storage).getStartupMessage().orElseThrow().contains("Reason: " + reason));
        assertEquals(invalidData, Files.readString(getContactsFile()));
    }

    @Test
    public void startup_invalidRelationship_explainsMissingGuardian() throws Exception {
        String invalidData = "{\"persons\":[{\"name\":\"Alex Tan\",\"role\":\"STUDENT\","
                + "\"guardianId\":\"11111111-1111-1111-1111-111111111111\"}]}";
        StorageManager storage = loadInvalidData(invalidData);

        assertTrue(createLogic(storage).getStartupMessage().orElseThrow()
                .contains("Reason: A student link must refer to an existing guardian."));
        assertEquals(invalidData, Files.readString(getContactsFile()));
    }

    @Test
    public void startup_duplicateIds_explainsIdentityProblem() throws Exception {
        String duplicateId = "\"id\":\"11111111-1111-1111-1111-111111111111\"";
        String invalidData = "{\"persons\":[{" + duplicateId + ",\"name\":\"Alex\",\"role\":\"STUDENT\"},{"
                + duplicateId + ",\"name\":\"Bea\",\"role\":\"STUDENT\"}]}";
        StorageManager storage = loadInvalidData(invalidData);

        assertTrue(createLogic(storage).getStartupMessage().orElseThrow()
                .contains("Reason: Contacts contain duplicate IDs."));
        assertEquals(invalidData, Files.readString(getContactsFile()));
    }

    @Test
    public void startup_emptyOrWhitespaceOnlyFile_explainsRecoveryAndPreservesBytes() throws Exception {
        for (String emptyData : List.of("", " \t\r\n")) {
            StorageManager storage = loadInvalidData(emptyData);
            Logic logic = createLogic(storage);
            byte[] originalBytes = Files.readAllBytes(getContactsFile());

            String warning = logic.getStartupMessage().orElseThrow();
            assertTrue(warning.contains("The data file is empty or contains only whitespace."));
            assertTrue(warning.contains("Restore a backup, or delete the empty file and restart TutorRoster"));
            assertEquals(warning, assertThrows(CommandException.class, () ->
                    logic.execute("add r/student n/Alex Tan")).getMessage());
            assertArrayEquals(originalBytes, Files.readAllBytes(getContactsFile()));
        }
    }

    @Test
    public void repairedFile_changesStayBlockedUntilRestart() throws Exception {
        StorageManager storage = loadInvalidData("");
        Logic logic = createLogic(storage);
        String originalWarning = logic.getStartupMessage().orElseThrow();
        Files.writeString(getContactsFile(), "{\"persons\":[]}");
        storage.readAddressBook();

        assertTrue(storage.isDataFileLoadFailed());
        assertEquals(Optional.of(originalWarning), logic.getStartupMessage());
        assertEquals(originalWarning, assertThrows(CommandException.class, () ->
                logic.execute("clear")).getMessage());
        assertEquals("{\"persons\":[]}", Files.readString(getContactsFile()));

        StorageManager restartedStorage = createStorage();
        restartedStorage.readAddressBook();
        Logic restartedLogic = createLogic(restartedStorage);
        assertFalse(restartedStorage.isDataFileLoadFailed());
        assertTrue(restartedStorage.getDataFileLoadFailureReason().isEmpty());
        assertTrue(restartedLogic.getStartupMessage().isEmpty());
        restartedLogic.execute("add r/student n/Alex Tan");
        assertEquals(1, restartedStorage.readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void unreadableDataPath_showsIoReasonAndPreservesDirectory() throws Exception {
        Files.createDirectory(getContactsFile());
        StorageManager storage = createStorage();
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        String reason = storage.getDataFileLoadFailureReason().orElseThrow();
        assertFalse(reason.isBlank());
        String warning = createLogic(storage).getStartupMessage().orElseThrow();
        assertTrue(warning.contains("Reason: " + reason));
        assertThrows(java.io.IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertTrue(Files.isDirectory(getContactsFile()));
    }

    @Test
    public void secondLoadFailure_retainsOriginalExplanationUntilRestart() throws Exception {
        StorageManager storage = loadInvalidData("{invalid json}");
        String originalReason = storage.getDataFileLoadFailureReason().orElseThrow();
        Files.writeString(getContactsFile(), "");
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        assertEquals(Optional.of(originalReason), storage.getDataFileLoadFailureReason());
        assertTrue(createLogic(storage).getStartupMessage().orElseThrow().contains("Reason: " + originalReason));
        assertEquals("", Files.readString(getContactsFile()));
    }

    @Test
    public void missingFile_startsNormallyAndSavesFirstContact() throws Exception {
        StorageManager storage = createStorage();
        assertTrue(storage.readAddressBook().isEmpty());
        Logic logic = createLogic(storage);

        assertTrue(logic.getStartupMessage().isEmpty());
        assertTrue(storage.getDataFileLoadFailureReason().isEmpty());
        logic.execute("add r/student n/Alex Tan");
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
    }
}
