package seedu.address.model.person;

import java.util.Locale;

/**
 * Represents a person's role in the address book.
 */
public enum Role {
    STUDENT("Student"),
    GUARDIAN("Guardian");

    public static final String MESSAGE_CONSTRAINTS = "Role must be student or guardian.";

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns true if {@code test} represents a supported role.
     */
    public static boolean isValidRole(String test) {
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
     * Returns the role represented by {@code value}, ignoring letter case and surrounding whitespace.
     *
     * @throws IllegalArgumentException if {@code value} does not represent a supported role.
     */
    public static Role fromString(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
