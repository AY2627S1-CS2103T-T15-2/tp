package seedu.address.storage;

import java.io.IOException;

/**
 * Reports that a session cannot safely replace contact data observed by another writer.
 */
public class DataFileConflictException extends IOException {
    public static final String MESSAGE = "Contact data changed or is being saved by another session. "
            + "No changes were saved. Close other copies and restart TutorRoster to load the latest data.";

    public DataFileConflictException() {
        super(MESSAGE);
    }
}
