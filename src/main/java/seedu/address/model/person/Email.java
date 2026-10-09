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
