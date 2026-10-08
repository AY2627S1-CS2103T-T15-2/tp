package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void read_nullRootOrPerson_rejectsInvalidData() throws Exception {
        Path filePath = testFolder.resolve("contacts.json");
        for (String invalidData : new String[]{"null", "{\"persons\":[null]}"}) {
            Files.writeString(filePath, invalidData);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertTrue(storage.isDataFileLoadFailed());
            assertEquals(invalidData, Files.readString(filePath));
        }
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_legacyAddress_preservesContactsAndOmitsAddress() throws Exception {
        Path filePath = testFolder.resolve("legacyContacts.json");
        var guardian = new seedu.address.testutil.PersonBuilder().withName("Guardian")
                .withRole("guardian").withTags("family").build();
        var student = new seedu.address.testutil.PersonBuilder(ALICE).withGuardianId(guardian.getId()).build();
        AddressBook original = new AddressBook();
        original.setPersons(java.util.List.of(guardian, student));
        String currentJson = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));

        for (String legacyValue : java.util.List.of("\"123 Main Street\"", "\" \"", "null")) {
            String legacyJson = currentJson.replace("\"email\" :", "\"address\" : " + legacyValue + ", \"email\" :");
            Files.writeString(filePath, legacyJson);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
            ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
            assertEquals(original, new AddressBook(loaded));
            assertEquals(guardian.getId(), loaded.getPersonList().get(0).getId());
            assertEquals(student.getId(), loaded.getPersonList().get(1).getId());
            assertEquals(student.getGuardianId(), loaded.getPersonList().get(1).getGuardianId());

            storage.saveAddressBook(loaded);
            assertFalse(Files.readString(filePath).contains("\"address\""));
            assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void saveAddressBook_afterLoadFailure_preservesInvalidFileUntilRestart() throws Exception {
        Path filePath = testFolder.resolve("contacts.json");
        String invalidData = "{invalid json}";
        Files.writeString(filePath, invalidData);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        assertEquals(invalidData, Files.readString(filePath));

        Files.writeString(filePath, "{\"persons\":[]}");
        storage.readAddressBook();
        assertTrue(storage.isDataFileLoadFailed());
        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        assertEquals("{\"persons\":[]}", Files.readString(filePath));

        JsonAddressBookStorage restartedStorage = new JsonAddressBookStorage(filePath);
        restartedStorage.readAddressBook();
        restartedStorage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new AddressBook(restartedStorage.readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_cleanupFailure_doesNotReportSuccessfulSaveAsFailed() throws Exception {
        Path filePath = testFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw new IOException("Simulated temporary file cleanup failure");
            }
        };

        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_failedReplacement_removesTemporaryFileAndPreservesDestination() throws Exception {
        Path destination = testFolder.resolve("contacts.json");
        Files.createDirectory(destination);
        Path existingFile = destination.resolve("keep.txt");
        Files.writeString(existingFile, "Existing data");

        assertThrows(IOException.class, () -> new JsonAddressBookStorage(destination)
                .saveAddressBook(getTypicalAddressBook()));
        assertEquals("Existing data", Files.readString(existingFile));
        try (var remainingFiles = Files.list(testFolder)) {
            assertEquals(java.util.List.of(destination), remainingFiles.toList());
        }
    }
}
