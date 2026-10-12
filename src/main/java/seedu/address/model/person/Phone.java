package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone details must contain at least 3 digits and cannot contain control characters, '@' or '/'.";
    public static final String MESSAGE_TOO_FEW_DIGITS = "Phone number must contain at least 3 digits.";
    public static final String MESSAGE_INVALID_CHARACTERS =
            "Phone number cannot contain control characters, '@', or '/'.";
    public static final String MESSAGE_EMPTY = "Empty phone parameter: Phone cannot be empty when p/ is specified.";
    public static final int MINIMUM_DIGIT_COUNT = 3;
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        return !hasInvalidCharacters(test) && getDigitCount(test) >= MINIMUM_DIGIT_COUNT;
    }

    /**
     * Returns true if the phone details contain a prohibited character.
     */
    public static boolean hasInvalidCharacters(String test) {
        requireNonNull(test);
        for (int i = 0; i < test.length(); i++) {
            char character = test.charAt(i);
            if (Character.isISOControl(character) || character == '@' || character == '/') {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of digits in the phone details.
     */
    public static int getDigitCount(String test) {
        requireNonNull(test);
        int digitCount = 0;
        for (int i = 0; i < test.length(); i++) {
            if (Character.isDigit(test.charAt(i))) {
                digitCount++;
            }
        }
        return digitCount;
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
