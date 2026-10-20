package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

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
            + "hyphens, periods, plus signs and parentheses.";

    private static final String VALIDATION_REGEX = "[\\p{L}\\p{N} .+()-]*[\\p{L}\\p{N}][\\p{L}\\p{N} .+()-]*";

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
        return test.equals(test.trim())
                && test.codePointCount(0, test.length()) <= MAX_LENGTH
                && test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if both subjects are the same after ignoring letter case and spaces,
     * for example "A Math" and "amath".
     */
    public boolean isSameValue(Subject other) {
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
