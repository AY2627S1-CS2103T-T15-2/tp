package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Validates the text rules shared by student levels and subjects.
 */
final class StudentDetailValidator {

    static final String MESSAGE_RESERVED_VALUE =
            "NONE cannot be used as a level or subject. Use lv/NONE or sub/NONE in edit to remove it.";

    /** Value reserved by {@code edit} for removing a level or subject. */
    private static final String RESERVED_VALUE = "NONE";
    private static final String ALLOWED_CHARACTERS_REGEX = "[\\p{L}\\p{M}\\p{N} .+()-]*";
    private static final String SUBJECT_CHARACTERS_REGEX = "[\\p{L}\\p{M}\\p{N} .+()&/-]*";

    private StudentDetailValidator() {
    }

    /**
     * Returns the first failed constraint, preserving length, character, content and spacing priority.
     *
     * @param value The student detail to validate.
     * @param maximumLength Maximum number of Unicode code points allowed.
     */
    static ValidationResult validate(String value, int maximumLength) {
        return validateValue(value, maximumLength, ALLOWED_CHARACTERS_REGEX);
    }

    /** Validates a subject, allowing slashes and ampersands in addition to the shared characters. */
    static ValidationResult validateSubject(String value, int maximumLength) {
        return validateValue(value, maximumLength, SUBJECT_CHARACTERS_REGEX);
    }

    private static ValidationResult validateValue(String value, int maximumLength, String allowedCharactersRegex) {
        requireNonNull(value);
        if (value.codePointCount(0, value.length()) > maximumLength) {
            return ValidationResult.TOO_LONG;
        }
        if (!value.matches(allowedCharactersRegex)) {
            return ValidationResult.INVALID_CHARACTERS;
        }
        if (!value.matches(".*[\\p{L}\\p{N}].*")) {
            return ValidationResult.MISSING_ALPHANUMERIC;
        }
        if (!value.equals(value.trim())) {
            return ValidationResult.SURROUNDING_SPACES;
        }
        if (value.equalsIgnoreCase(RESERVED_VALUE)) {
            return ValidationResult.RESERVED_VALUE;
        }
        return ValidationResult.VALID;
    }

    /**
     * The outcome of checking a student detail, independent of its field-specific message.
     */
    enum ValidationResult {
        VALID, TOO_LONG, INVALID_CHARACTERS, MISSING_ALPHANUMERIC, SURROUNDING_SPACES, RESERVED_VALUE
    }
}
