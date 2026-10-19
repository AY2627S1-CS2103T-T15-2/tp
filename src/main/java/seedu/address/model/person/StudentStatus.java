package seedu.address.model.person;

import java.util.Locale;

/**
 * Represents whether a student is currently being taught or is a former student.
 * Guardians are always {@code ACTIVE}.
 */
public enum StudentStatus {
    ACTIVE("Active"),
    FORMER("Former");

    public static final String MESSAGE_CONSTRAINTS = "Invalid status. Status must be active or former.";

    private final String displayName;

    StudentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns true if {@code test} represents a supported status.
     */
    public static boolean isValidStatus(String test) {
        if (test == null) {
            return false;
        }

        try {
            valueOf(test.trim().toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Returns the status represented by {@code value}, ignoring letter case and surrounding whitespace.
     *
     * @throws IllegalArgumentException if {@code value} does not represent a supported status.
     */
    public static StudentStatus fromString(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
