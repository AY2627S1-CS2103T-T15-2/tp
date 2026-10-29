package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private boolean hasDataFileLoadFailed;
    private String dataFileLoadFailureReason;
    private final Map<Path, Optional<byte[]>> observedContents = new HashMap<>();

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        Path source = filePath.toAbsolutePath().normalize();
        try {
            Optional<byte[]> contents = readFileContents(source);
            if (contents.isEmpty()) {
                observedContents.put(source, contents);
                return Optional.empty();
            }
            String json = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(contents.get())).toString();
            JsonSerializableAddressBook data = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
            if (data == null) {
                throw new IOException("The JSON file must contain an object.");
            }
            ReadOnlyAddressBook contacts = data.toModelType();
            observedContents.put(source, contents);
            return Optional.of(contacts);
        } catch (IOException | IllegalValueException | IllegalArgumentException exception) {
            recordDataFileLoadFailure(exception);
            logger.info("Invalid data found in " + filePath + ": " + exception.getMessage());
            throw new DataLoadingException(exception);
        }
    }

    /**
     * Reads one immutable snapshot, distinguishing a missing file from an unreadable existing file.
     */
    private Optional<byte[]> readFileContents(Path path) throws IOException {
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (NoSuchFileException exception) {
            return Optional.empty();
        }
    }

    public boolean isDataFileLoadFailed() {
        return hasDataFileLoadFailed;
    }

    /**
     * Returns the first load failure's explanation, retained until the application restarts.
     */
    public Optional<String> getDataFileLoadFailureReason() {
        return Optional.ofNullable(dataFileLoadFailureReason);
    }

    /**
     * Retains the reason for disabling changes even if a later read succeeds in the same session.
     */
    private void recordDataFileLoadFailure(Exception exception) {
        hasDataFileLoadFailed = true;
        if (dataFileLoadFailureReason != null) {
            return;
        }
        Throwable cause = exception instanceof DataLoadingException ? exception.getCause() : exception;
        if (cause instanceof JsonProcessingException jsonException) {
            dataFileLoadFailureReason = describeJsonFailure(jsonException);
        } else {
            dataFileLoadFailureReason = Optional.ofNullable(cause.getMessage())
                    .orElse("The data file could not be read.");
        }
    }

    /**
     * Removes Jackson's class/source details and gives a recovery action for an empty file.
     */
    private String describeJsonFailure(JsonProcessingException exception) {
        String reason = exception.getOriginalMessage();
        if (reason.startsWith("No content to map")) {
            return "The data file is empty or contains only whitespace. Restore a backup, or delete the empty "
                    + "file and restart TutorRoster to start with an empty contact list.";
        }
        return "Invalid JSON: " + reason;
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        if (hasDataFileLoadFailed) {
            throw new IOException("The data file could not be loaded. Fix the file and restart TutorRoster.");
        }
        Path destination = filePath.toAbsolutePath().normalize();
        Files.createDirectories(destination.getParent());
        if (!observedContents.containsKey(destination)) {
            observedContents.put(destination, readFileContents(destination));
        }
        Path lockPath = destination.resolveSibling(destination.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = acquireWriteLock(channel)) {
            Optional<byte[]> currentContents = readFileContents(destination);
            if (!Arrays.equals(observedContents.get(destination).orElse(null), currentContents.orElse(null))) {
                throw new DataFileConflictException();
            }
            byte[] savedContents = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook))
                    .getBytes(StandardCharsets.UTF_8);
            writeAtomically(destination, savedContents);
            observedContents.put(destination, Optional.of(savedContents));
        }
    }

    /**
     * Serializes cooperating writers around both the snapshot check and atomic replacement.
     * The sibling lock file remains in place so every process locks the same filesystem object.
     */
    private FileLock acquireWriteLock(FileChannel channel) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                throw new DataFileConflictException();
            }
            return lock;
        } catch (OverlappingFileLockException exception) {
            throw new DataFileConflictException();
        }
    }

    /**
     * Publishes exactly the bytes retained as this session's next expected snapshot.
     */
    private void writeAtomically(Path destination, byte[] savedContents) throws IOException {
        Path temporaryFile = Files.createTempFile(destination.getParent(), "addressbook-", ".tmp");
        try {
            Files.write(temporaryFile, savedContents);
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            try {
                deleteTemporaryFile(temporaryFile);
            } catch (IOException exception) {
                logger.warning("Could not remove temporary save file: " + temporaryFile);
            }
        }
    }

    /**
     * Removes the temporary save file, if it still exists.
     */
    void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

}
