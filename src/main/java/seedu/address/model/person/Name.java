package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 70;
    public static final String MESSAGE_CONSTRAINTS =
            "Names must contain 1 to 70 characters and cannot be blank. "
                    + "Names may contain letters, digits, spaces, apostrophes, hyphens, periods, and slashes.";

    public static final String VALIDATION_REGEX = "[\\p{L}\\p{M}\\p{N}\\p{Pd} './’]+";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);

        int characterCount = test.codePointCount(0, test.length());
        boolean hasValidLength = characterCount >= 1 && characterCount <= MAX_LENGTH;
        boolean isNotBlank = !test.isBlank();
        boolean containsOnlyAllowedCharacters = test.matches(VALIDATION_REGEX);

        return hasValidLength && isNotBlank && containsOnlyAllowedCharacters;
    }

    /**
     * Returns true if both names are the same, ignoring letter case.
     */
    public boolean isSameName(Name otherName) {
        return otherName != null && fullName.equalsIgnoreCase(otherName.fullName);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
