package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void getValidationErrorMessage_distinguishesCharactersFromPunctuation() {
        for (String local : List.of("alex!tan", "alex'", "aléx", "alex..tan!")) {
            assertEquals(Email.MESSAGE_LOCAL_CHARACTERS,
                    Email.getValidationErrorMessage(local + "@example.com"));
        }
        for (String local : List.of(".alex", "alex.", "alex..tan", ".", "..", "_..+")) {
            assertEquals("Email local part cannot start or end with a period, or contain consecutive periods.",
                    Email.getValidationErrorMessage(local + "@example.com"));
        }
        for (String domain : List.of("exam_ple.com", "example.c_m", "exämple.com", "exam--ple!.com")) {
            assertEquals(Email.MESSAGE_DOMAIN_CHARACTERS, Email.getValidationErrorMessage("alex@" + domain));
        }
        for (String domain : List.of("-example.com", "example-.com", "exam--ple.com", "example.-com",
                "example.com-", "example.co--m")) {
            assertEquals("Email domain hyphens must appear singly between letters or digits.",
                    Email.getValidationErrorMessage("alex@" + domain));
        }
    }

    @Test
    public void constructor_supportedLocalPunctuation_preservesEmail() {
        for (String local : List.of("mei__tan", "mei+_tan", "mei++__tan", "mei%+tan", "_mei", "mei_",
                "+mei", "mei+", "%mei", "mei%", "-mei", "mei-", "++__", "_._", "mei._tan", "mei_.tan")) {
            String email = local + "@example.com";
            assertTrue(Email.isValidEmail(email), email);
            assertEquals(email, new Email(email).value);
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void constructor_emailWithUppercaseLetters_preservesLetterCase() {
        Email email = new Email("Valid.Email@Example.com");
        assertEquals("Valid.Email@Example.com", email.value);
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertTrue(Email.isValidEmail("-peterjack@example.com")); // local part starts with a hyphen
        assertTrue(Email.isValidEmail("peterjack-@example.com")); // local part ends with a hyphen
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // consecutive special characters
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain label starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain label ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack%school@example.c")); // final domain label is too short

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack%1190@example.com")); // percentage sign in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertFalse(Email.isValidEmail("a@bc")); // domain needs at least two labels
        assertFalse(Email.isValidEmail("test@localhost")); // domain needs at least two labels
        assertFalse(Email.isValidEmail("123@145")); // domain needs at least two labels
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void constructor_maximumLength_preservesEmail() {
        String email = "a".repeat(64) + "@" + "b".repeat(63)
                + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertEquals(Email.MAX_LENGTH, email.length());
        assertEquals(email, new Email(email).value);
    }

    @Test
    public void constructor_oneOverMaximumLength_rejected() {
        String email = "a".repeat(64) + "@" + "b".repeat(63)
                + "." + "c".repeat(63) + "." + "d".repeat(62);
        assertEquals(Email.MAX_LENGTH + 1, email.length());
        assertEquals(Email.MESSAGE_TOO_LONG, Email.getValidationErrorMessage(email));
        assertThrows(IllegalArgumentException.class, () -> new Email(email));
    }

    @Test
    public void constructor_localPartLength_checksBoundary() {
        String valid = "a".repeat(64) + "@example.com";
        String invalid = "a" + valid;
        assertEquals(valid, new Email(valid).value);
        assertEquals(Email.MESSAGE_LOCAL_PART_TOO_LONG, Email.getValidationErrorMessage(invalid));
        assertThrows(IllegalArgumentException.class, () -> new Email(invalid));
    }

    @Test
    public void constructor_domainLabelLength_checksEveryLabel() {
        for (String valid : List.of("a@" + "b".repeat(63) + ".com", "a@example." + "b".repeat(63),
                "a@example." + "b".repeat(63) + ".com")) {
            assertEquals(valid, new Email(valid).value);
        }
        for (String invalid : List.of("a@" + "b".repeat(64) + ".com", "a@example." + "b".repeat(64),
                "a@example." + "b".repeat(64) + ".com")) {
            assertEquals(Email.MESSAGE_DOMAIN_LABEL_TOO_LONG, Email.getValidationErrorMessage(invalid));
            assertThrows(IllegalArgumentException.class, () -> new Email(invalid));
        }
    }

    @Test
    public void isValidEmail_extremelyLongValues_reportsLengthError() {
        for (String value : List.of("a.".repeat(10_000) + "a@example.com",
                "a.".repeat(10_000) + "@example.com",
                "a.".repeat(10_000) + "a!@example.com",
                "alex@" + "a-".repeat(10_000) + ".com",
                "alex@" + "a.".repeat(10_000) + ".com",
                "alex@example." + "a-".repeat(10_000))) {
            assertFalse(Email.isValidEmail(value));
            assertEquals(Email.MESSAGE_TOO_LONG, Email.getValidationErrorMessage(value));
            assertThrows(IllegalArgumentException.class, () -> new Email(value));
        }
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email.com");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email.com")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email.com")));
    }

    @Test
    public void isSameEmail() {
        Email email = new Email("Valid.Email@Example.com");

        // same object -> returns true
        assertTrue(email.isSameEmail(email));

        // same value -> returns true
        assertTrue(email.isSameEmail(new Email("Valid.Email@Example.com")));

        // same value with different letter case -> returns true
        assertTrue(email.isSameEmail(new Email("valid.email@example.com")));
        assertTrue(email.isSameEmail(new Email("VALID.EMAIL@EXAMPLE.COM")));

        // different value -> returns false
        assertFalse(email.isSameEmail(new Email("other.email@example.com")));

        // null -> returns false
        assertFalse(email.isSameEmail(null));
    }
}
