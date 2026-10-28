package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a student's subject in TutorRoster, such as "Math" or "A-Math".
 * Guarantees: immutable; is valid as declared in {@link #isValidSubject(String)}
 */
public class Subject {

    public static final int MAX_LENGTH = 30;
    public static final String MESSAGE_EMPTY =
            "Empty subject parameter: Subject cannot be empty when sub/ is specified.";
    public static final String MESSAGE_CONSTRAINTS = "Subject must contain 1 to " + MAX_LENGTH
            + " characters, including at least one letter or digit. It may contain letters, digits, spaces, "
            + "hyphens, periods, plus signs, parentheses, slashes and ampersands.";

    public static final String MESSAGE_TOO_LONG = "Subject cannot exceed " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_INVALID_CHARACTERS =
            "Subject contains unsupported characters. Use letters, digits, spaces, hyphens, "
                    + "periods, plus signs, parentheses, slashes and ampersands.";
    public static final String MESSAGE_MISSING_ALPHANUMERIC = "Subject must contain at least one letter or digit.";

    public final String value;

    /**
     * Constructs a {@code Subject}.
     *
     * @param subject A valid subject.
     */
    public Subject(String subject) {
        requireNonNull(subject);
        checkArgument(isValidSubject(subject), MESSAGE_CONSTRAINTS);
        value = subject;
    }

    /**
     * Returns true if {@code test} is a valid subject. It must not have leading or trailing spaces.
     */
    public static boolean isValidSubject(String test) {
        requireNonNull(test);
        return getValidationErrorMessage(test) == null;
    }

    /**
     * Returns the failed constraint for {@code test}, or {@code null} for a valid, trimmed value.
     */
    public static String getValidationErrorMessage(String test) {
        return switch (StudentDetailValidator.validateSubject(test, MAX_LENGTH)) {
            case TOO_LONG -> MESSAGE_TOO_LONG;
            case INVALID_CHARACTERS -> MESSAGE_INVALID_CHARACTERS;
            case MISSING_ALPHANUMERIC -> MESSAGE_MISSING_ALPHANUMERIC;
            case SURROUNDING_SPACES -> "Subject cannot start or end with spaces.";
            case VALID -> null;
        };
    }

    /**
     * Returns true if both subjects are canonically equivalent after ignoring letter case and spaces,
     * for example "A Math" and "amath".
     */
    public boolean isSameValue(Subject other) {
        return other != null && StudentDetailNormalizer.normalizeForComparison(value)
                .equals(StudentDetailNormalizer.normalizeForComparison(other.value));
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
        if (!(other instanceof Subject otherSubject)) {
            return false;
        }

        return value.equals(otherSubject.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
