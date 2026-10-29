package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataFileConflictException;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Guards against lost updates between independently loaded sessions sharing one contact file.
 */
public class ConcurrentSaveTest {
    @TempDir
    public Path temporaryFolder;

    private Path getContactsFile() {
        return temporaryFolder.resolve("contacts.json");
    }

    private Logic openSession() throws Exception {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(getContactsFile());
        var loaded = contacts.readAddressBook().orElseGet(AddressBook::new);
        return new LogicManager(new ModelManager(loaded, new UserPrefs()),
                new StorageManager(contacts, new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private List<String> readSavedNames() throws Exception {
        return new JsonAddressBookStorage(getContactsFile()).readAddressBook().orElseThrow()
                .getPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    @Test
    public void twoInitiallyEmptySessions_staleSavePreservesOtherSessionAndLiveModel() throws Exception {
        Logic first = openSession();
        Logic second = openSession();
        first.execute("add r/student n/Alice");
        String saved = Files.readString(getContactsFile());

        CommandException failure = assertThrows(CommandException.class, () ->
                second.execute("add r/student n/Bob"));

        assertEquals(DataFileConflictException.MESSAGE, failure.getMessage());
        assertEquals(saved, Files.readString(getContactsFile()));
        assertTrue(second.getPersonList().isEmpty());
        assertTrue(second.getFilteredPersonList().isEmpty());
        assertEquals(List.of("Alice"), readSavedNames());
        assertThrows(CommandException.class, () -> second.execute("add r/student n/Bob"));
        assertEquals(saved, Files.readString(getContactsFile()));

        Logic restarted = openSession();
        restarted.execute("add r/student n/Bob");
        assertEquals(List.of("Alice", "Bob"), readSavedNames());
    }

    @Test
    public void twoLoadedSessions_staleDeletionPreservesDataAndFilter() throws Exception {
        Logic first = openSession();
        first.execute("add r/student n/Alice");
        first.execute("add r/student n/Bob");
        Logic second = openSession();
        second.execute("find Bob");
        first.execute("add r/student n/Carol");
        String saved = Files.readString(getContactsFile());

        assertThrows(CommandException.class, () -> second.execute("delete 1"));

        assertEquals(List.of("Bob"), second.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals(2, second.getPersonList().size());
        assertEquals(saved, Files.readString(getContactsFile()));
        assertEquals(List.of("Alice", "Bob", "Carol"), readSavedNames());
        assertThrows(CommandException.class, () -> second.execute("undo"));
    }

    @Test
    public void simultaneousSaves_onlyOneSessionPublishesItsCandidate() throws Exception {
        Logic first = openSession();
        Logic second = openSession();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var alice = executor.submit(() -> saveAfterSignal(first, "Alice", start));
            var bob = executor.submit(() -> saveAfterSignal(second, "Bob", start));
            start.countDown();

            boolean aliceSaved = alice.get(10, TimeUnit.SECONDS);
            boolean bobSaved = bob.get(10, TimeUnit.SECONDS);
            assertTrue(aliceSaved != bobSaved);
            assertEquals(List.of(aliceSaved ? "Alice" : "Bob"), readSavedNames());
            assertEquals(aliceSaved ? 1 : 0, first.getPersonList().size());
            assertEquals(bobSaved ? 1 : 0, second.getPersonList().size());
        }
    }

    private boolean saveAfterSignal(Logic logic, String name, CountDownLatch start) throws Exception {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        try {
            logic.execute("add r/student n/" + name);
            return true;
        } catch (CommandException exception) {
            assertEquals(DataFileConflictException.MESSAGE, exception.getMessage());
            return false;
        }
    }

    @Test
    public void overlappingWriteLock_rejectsSaveAndAllowsRetryAfterRelease() throws Exception {
        Logic session = openSession();
        Path lockFile = temporaryFolder.resolve("contacts.json.lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertEquals(DataFileConflictException.MESSAGE, assertThrows(CommandException.class, () ->
                    session.execute("add r/student n/Alice")).getMessage());
            assertFalse(Files.exists(getContactsFile()));
            assertTrue(session.getPersonList().isEmpty());
        }

        session.execute("add r/student n/Alice");
        session.execute("add r/student n/Bob");
        assertEquals(List.of("Alice", "Bob"), readSavedNames());
    }

    @Test
    public void conflictingUndo_retainsHistoryForSuccessfulRetry() throws Exception {
        Logic session = openSession();
        session.execute("add r/student n/Alice");
        session.execute("add r/student n/Bob");
        session.execute("delete 1");
        String beforeConflict = Files.readString(getContactsFile());
        Logic other = openSession();
        other.execute("add r/student n/Carol");

        assertEquals(DataFileConflictException.MESSAGE, assertThrows(CommandException.class, () ->
                session.execute("undo")).getMessage());
        assertEquals(List.of("Bob", "Carol"), readSavedNames());
        assertEquals(1, session.getPersonList().size());

        Files.writeString(getContactsFile(), beforeConflict);
        session.execute("undo");
        assertEquals(List.of("Alice", "Bob"), readSavedNames());
    }

    @Test
    public void externallyDeletedFile_isNotRecreatedFromStaleData() throws Exception {
        Logic session = openSession();
        session.execute("add r/student n/Alice");
        Files.delete(getContactsFile());

        assertEquals(DataFileConflictException.MESSAGE, assertThrows(CommandException.class, () ->
                session.execute("add r/student n/Bob")).getMessage());

        assertFalse(Files.exists(getContactsFile()));
        assertEquals(1, session.getPersonList().size());
    }

    @Test
    public void explicitFilePaths_trackIndependentSnapshots() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(getContactsFile());
        Path other = temporaryFolder.resolve("other.json");
        storage.readAddressBook(getContactsFile());
        storage.readAddressBook(other);
        storage.saveAddressBook(new AddressBook(), getContactsFile());
        storage.saveAddressBook(new AddressBook(), other);
        storage.saveAddressBook(new AddressBook(), getContactsFile());
        assertTrue(storage.readAddressBook(other).orElseThrow().getPersonList().isEmpty());
        assertTrue(storage.readAddressBook(getContactsFile()).orElseThrow().getPersonList().isEmpty());
    }
}
