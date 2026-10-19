package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // no digits
        assertFalse(Phone.isValidPhone("HP 12 (Office)")); // fewer than 3 digits
        assertFalse(Phone.isValidPhone("91\n1234")); // control characters
        assertFalse(Phone.isValidPhone("amy123@example.com")); // @ is reserved for email values
        assertFalse(Phone.isValidPhone("911/2222")); // slash is reserved for command prefixes
        assertFalse(Phone.isValidPhone("1".repeat(Phone.MAX_LENGTH + 1))); // exceeds maximum length

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("+65-9123-4567"));
        assertTrue(Phone.isValidPhone("(HP) +65 1234 5678"));
        assertTrue(Phone.isValidPhone("HP+651234567 Office: 1111-3333"));
        assertTrue(Phone.isValidPhone("1".repeat(Phone.MAX_LENGTH))); // exactly maximum length
    }

    @Test
    public void isValidPhone_withThreeDigitsAndOtherText_isValid() {
        assertTrue(Phone.isValidPhone("联系人 HP 电话: +65 123"));
        assertTrue(Phone.isValidPhone("(HP) +65-123"));
    }

    @Test
    public void isValidPhone_withFewerThanThreeDigits_isInvalid() {
        assertFalse(Phone.isValidPhone("(HP) +65"));
        assertFalse(Phone.isValidPhone("办公室 12"));
    }

    @Test
    public void isValidPhone_nonAsciiNumerals_doNotCountTowardsMinimum() {
        for (String numerals : List.of("\u0661\u0662\u0663", "\uff11\uff12\uff13",
                "\uD835\uDFCF\uD835\uDFD0\uD835\uDFD1")) {
            assertFalse(Phone.isValidPhone(numerals));
            assertFalse(Phone.isValidPhone("12 " + numerals));
            assertTrue(Phone.isValidPhone("123 " + numerals));
        }
    }

    @Test
    public void isValidPhone_withTabOrCarriageReturn_isInvalid() {
        assertFalse(Phone.isValidPhone("911\t2222"));
        assertFalse(Phone.isValidPhone("911\r2222"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}
