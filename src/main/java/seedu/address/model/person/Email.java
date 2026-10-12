package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Invalid email address: Email must contain one @ symbol, "
            + "a non-empty local part, and at least two non-empty domain labels separated by periods, without spaces.";
    public static final String MESSAGE_EMPTY = "Empty email parameter: Email cannot be empty when e/ is specified.";
    private static final String ALPHANUMERIC = "[A-Za-z0-9]+";
    private static final String LOCAL_PART = ALPHANUMERIC + "([._%+\\-]" + ALPHANUMERIC + ")*";
    private static final String DOMAIN_LABEL = ALPHANUMERIC + "(-" + ALPHANUMERIC + ")*";
    private static final String FINAL_DOMAIN_LABEL = "(?=[A-Za-z0-9-]{2,}$)" + DOMAIN_LABEL;
    public static final String VALIDATION_REGEX = LOCAL_PART + "@" + DOMAIN_LABEL
            + "(\\." + DOMAIN_LABEL + ")*\\." + FINAL_DOMAIN_LABEL;

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
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns a specific validation error for {@code test}, or {@code null} if it is valid.
     */
    public static String getValidationErrorMessage(String test) {
        requireNonNull(test);
        if (test.chars().filter(character -> character == '@').count() != 1) {
            return "Email address must contain exactly one @ symbol.";
        }

        String[] addressParts = test.split("@", -1);
        String localPart = addressParts[0];
        String domain = addressParts[1];
        if (localPart.isEmpty()) {
            return "Email local part cannot be empty.";
        }
        if (domain.isEmpty()) {
            return "Email domain cannot be empty.";
        }
        if (test.chars().anyMatch(Character::isWhitespace)) {
            return "Email address cannot contain whitespace.";
        }
        if (!localPart.matches(LOCAL_PART)) {
            return "Email local part contains unsupported characters or invalid punctuation placement.";
        }

        String[] domainLabels = domain.split("\\.", -1);
        if (domainLabels.length < 2) {
            return "Email domain must contain at least two labels separated by a period.";
        }
        for (int i = 0; i < domainLabels.length - 1; i++) {
            if (domainLabels[i].isEmpty()) {
                return "Email domain labels cannot be empty.";
            }
            if (!domainLabels[i].matches(DOMAIN_LABEL)) {
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
        if (!finalLabel.matches(DOMAIN_LABEL)) {
            return "Email's final domain label may contain letters, digits, and internal hyphens only.";
        }
        return null;
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
