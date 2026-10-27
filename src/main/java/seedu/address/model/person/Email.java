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
    public static final String MESSAGE_LOCAL_CHARACTERS =
            "Email local part contains unsupported characters. Use ASCII letters (A-Z, a-z), digits (0-9), "
                    + "and '.', '_', '%', '+', or '-'.";
    public static final String MESSAGE_LOCAL_PUNCTUATION =
            "Email local part cannot start or end with a period, or contain consecutive periods.";
    public static final String MESSAGE_DOMAIN_CHARACTERS =
            "Email domain labels contain unsupported characters. Use ASCII letters (A-Z, a-z), digits (0-9), "
                    + "and hyphens only.";
    public static final String MESSAGE_DOMAIN_PUNCTUATION =
            "Email domain hyphens must appear singly between letters or digits.";
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
        String structureError = getAddressStructureError(test);
        if (structureError != null) {
            return structureError;
        }
        String[] addressParts = test.split("@", -1);
        return getAddressPartsError(addressParts[0], addressParts[1]);
    }

    private static String getAddressStructureError(String address) {
        if (address.length() > MAX_LENGTH) {
            return MESSAGE_TOO_LONG;
        }
        if (address.chars().filter(character -> character == '@').count() != 1) {
            return "Email address must contain exactly one @ symbol.";
        }
        return null;
    }

    /**
     * Checks presence, local-part length and whitespace before either part's character rules.
     */
    private static String getAddressPartsError(String localPart, String domain) {
        if (localPart.isEmpty()) {
            return "Email local part cannot be empty.";
        }
        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return MESSAGE_LOCAL_PART_TOO_LONG;
        }
        if (domain.isEmpty()) {
            return "Email domain cannot be empty.";
        }
        if (localPart.chars().anyMatch(Character::isWhitespace) || domain.chars().anyMatch(Character::isWhitespace)) {
            return "Email address cannot contain whitespace.";
        }
        String localPartError = getLocalPartError(localPart);
        return localPartError != null ? localPartError : getDomainError(domain);
    }

    private static String getLocalPartError(String localPart) {
        if (hasUnsupportedCharacters(localPart, "._%+-")) {
            return MESSAGE_LOCAL_CHARACTERS;
        }
        if (!isValidLocalPart(localPart)) {
            return MESSAGE_LOCAL_PUNCTUATION;
        }
        return null;
    }

    /**
     * Checks every label's length before validating label count and contents in order.
     */
    private static String getDomainError(String domain) {
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
            String labelError = getDomainLabelError(domainLabels[i]);
            if (labelError != null) {
                return labelError;
            }
        }
        return getFinalDomainLabelError(domainLabels[domainLabels.length - 1]);
    }

    private static String getFinalDomainLabelError(String finalLabel) {
        if (finalLabel.isEmpty()) {
            return "Email domain labels cannot be empty.";
        }
        if (finalLabel.length() < 2) {
            return "Email's final domain label must contain at least two characters.";
        }
        return getDomainLabelError(finalLabel);
    }

    /**
     * Checks period placement after the local part's characters have been validated.
     * Other supported punctuation can appear consecutively and at either end.
     */
    private static boolean isValidLocalPart(String localPart) {
        return !localPart.startsWith(".") && !localPart.endsWith(".") && !localPart.contains("..");
    }

    private static String getDomainLabelError(String label) {
        if (hasUnsupportedCharacters(label, "-")) {
            return MESSAGE_DOMAIN_CHARACTERS;
        }
        return isValidPart(label, "-") ? null : MESSAGE_DOMAIN_PUNCTUATION;
    }

    private static boolean hasUnsupportedCharacters(String part, String allowedPunctuation) {
        for (int i = 0; i < part.length(); i++) {
            char character = part.charAt(i);
            if (!isAsciiAlphanumeric(character) && allowedPunctuation.indexOf(character) < 0) {
                return true;
            }
        }
        return false;
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
