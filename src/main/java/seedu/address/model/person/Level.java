package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a student's level in TutorRoster, such as "Sec 3" or "JC1".
 * Guarantees: immutable; is valid as declared in {@link #isValidLevel(String)}
 */
public class Level {

    public static final int MAX_LENGTH = 30;
    public static final String MESSAGE_EMPTY = "Empty level parameter: Level cannot be empty when lv/ is specified.";
    public static final String MESSAGE_CONSTRAINTS = "Level must contain 1 to " + MAX_LENGTH
            + " characters, including at least one letter or digit. It may contain letters, digits, spaces, "
            + "hyphens, periods, plus signs and parentheses.";

    public static final String MESSAGE_TOO_LONG = "Level cannot exceed " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_INVALID_CHARACTERS =
            "Level contains unsupported characters. Use letters, digits, spaces, hyphens, "
                    + "periods, plus signs and parentheses.";
    public static final String MESSAGE_MISSING_ALPHANUMERIC = "Level must contain at least one letter or digit.";

    private static final String ALLOWED_CHARACTERS_REGEX = "[\\p{L}\\p{N} .+()-]*";

    public final String value;

    /**
     * Constructs a {@code Level}.
     *
     * @param level A valid level.
     */
    public Level(String level) {
        requireNonNull(level);
        checkArgument(isValidLevel(level), MESSAGE_CONSTRAINTS);
        value = level;
    }

    /**
     * Returns true if {@code test} is a valid level. It must not have leading or trailing spaces.
     */
    public static boolean isValidLevel(String test) {
        requireNonNull(test);
        return getValidationErrorMessage(test) == null;
    }

    /**
     * Returns the failed constraint for {@code test}, or {@code null} for a valid, trimmed value.
     */
    public static String getValidationErrorMessage(String test) {
        requireNonNull(test);
        if (test.codePointCount(0, test.length()) > MAX_LENGTH) {
            return MESSAGE_TOO_LONG;
        }
        if (!test.matches(ALLOWED_CHARACTERS_REGEX)) {
            return MESSAGE_INVALID_CHARACTERS;
        }
        if (!test.matches(".*[\\p{L}\\p{N}].*")) {
            return MESSAGE_MISSING_ALPHANUMERIC;
        }
        if (!test.equals(test.trim())) {
            return "Level cannot start or end with spaces.";
        }
        return null;
    }

    /**
     * Returns true if both levels are the same after ignoring letter case and spaces,
     * for example "Sec 3" and "sec3".
     */
    public boolean isSameValue(Level other) {
        return other != null && normalize(value).equals(normalize(other.value));
    }

    private static String normalize(String text) {
        return text.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Level otherLevel)) {
            return false;
        }

        return value.equals(otherLevel.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
