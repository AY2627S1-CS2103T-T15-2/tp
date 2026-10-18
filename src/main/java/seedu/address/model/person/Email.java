package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final int MAX_LENGTH = 254;
    public static final int MAX_LOCAL_PART_LENGTH = 64;
    public static final int MAX_DOMAIN_LABEL_LENGTH = 63;
    public static final String MESSAGE_CONSTRAINTS =
            "Invalid email address: Email must contain one @ symbol, "
            + "a non-empty local part, and at least two non-empty domain labels "
            + "separated by periods, without spaces. Email cannot exceed " + MAX_LENGTH + " characters; "
            + "the local part cannot exceed " + MAX_LOCAL_PART_LENGTH + " characters, and each domain label "
            + "cannot exceed " + MAX_DOMAIN_LABEL_LENGTH + " characters.";
    public static final String MESSAGE_TOO_LONG =
            "Email address cannot exceed " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_LOCAL_PART_TOO_LONG =
            "Email local part cannot exceed " + MAX_LOCAL_PART_LENGTH + " characters.";
    public static final String MESSAGE_DOMAIN_LABEL_TOO_LONG =
            "Email domain labels cannot exceed " + MAX_DOMAIN_LABEL_LENGTH + " characters.";
    public static final String MESSAGE_EMPTY =
            "Empty email parameter: Email cannot be empty when e/ is specified.";
    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return getValidationErrorMessage(test) == null;
    }

    /**
     * Returns a specific validation error for {@code test}, or {@code null} if it is valid.
     */
    public static String getValidationErrorMessage(String test) {
        requireNonNull(test);
        if (test.length() > MAX_LENGTH) {
            return MESSAGE_TOO_LONG;
        }
        if (test.chars().filter(character -> character == '@').count() != 1) {
            return "Email address must contain exactly one @ symbol.";
        }

        String[] addressParts = test.split("@", -1);
        String localPart = addressParts[0];
        String domain = addressParts[1];
        if (localPart.isEmpty()) {
            return "Email local part cannot be empty.";
        }
        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return MESSAGE_LOCAL_PART_TOO_LONG;
        }
        if (domain.isEmpty()) {
            return "Email domain cannot be empty.";
        }
        if (test.chars().anyMatch(Character::isWhitespace)) {
            return "Email address cannot contain whitespace.";
        }
        if (!isValidPart(localPart, "._%+-")) {
            return "Email local part contains unsupported characters or invalid punctuation placement.";
        }

        String[] domainLabels = domain.split("\\.", -1);
        for (String label : domainLabels) {
            if (label.length() > MAX_DOMAIN_LABEL_LENGTH) {
                return MESSAGE_DOMAIN_LABEL_TOO_LONG;
            }
        }
        if (domainLabels.length < 2) {
            return "Email domain must contain at least two labels separated by a period.";
        }
        for (int i = 0; i < domainLabels.length - 1; i++) {
            if (domainLabels[i].isEmpty()) {
                return "Email domain labels cannot be empty.";
            }
            if (!isValidPart(domainLabels[i], "-")) {
                return "Email domain labels may contain letters, digits, and internal hyphens only.";
            }
        }
        String finalLabel = domainLabels[domainLabels.length - 1];
        if (finalLabel.isEmpty()) {
            return "Email domain labels cannot be empty.";
        }
        if (finalLabel.length() < 2) {
            return "Email's final domain label must contain at least two characters.";
        }
        if (!isValidPart(finalLabel, "-")) {
            return "Email's final domain label may contain letters, digits, and internal hyphens only.";
        }
        return null;
    }

    /**
     * Returns true if {@code part} contains only ASCII letters and digits, with each character from
     * {@code allowedPunctuation} appearing singly between two letters or digits.
     * Uses a loop instead of a regex so long inputs cannot overflow the stack.
     */
    private static boolean isValidPart(String part, String allowedPunctuation) {
        boolean isPreviousAlphanumeric = false;
        for (int i = 0; i < part.length(); i++) {
            char character = part.charAt(i);
            if (isAsciiAlphanumeric(character)) {
                isPreviousAlphanumeric = true;
            } else if (isPreviousAlphanumeric && allowedPunctuation.indexOf(character) >= 0) {
                isPreviousAlphanumeric = false;
            } else {
                return false;
            }
        }
        return isPreviousAlphanumeric;
    }

    private static boolean isAsciiAlphanumeric(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || (character >= '0' && character <= '9');
    }

    /**
     * Returns true if both email addresses are the same, ignoring letter case.
     */
    public boolean isSameEmail(Email otherEmail) {
        return otherEmail != null && value.equalsIgnoreCase(otherEmail.value);
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
