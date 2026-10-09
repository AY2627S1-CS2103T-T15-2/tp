package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // unsupported punctuation only
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("Alex@Tan")); // unsupported at sign
        assertFalse(Name.isValidName("Alex!")); // unsupported exclamation mark
        assertFalse(Name.isValidName("Alex~Tan")); // unsupported tilde
        assertFalse(Name.isValidName("Alex_Tan")); // unsupported underscore
        assertFalse(Name.isValidName("Alex, Tan")); // unsupported comma
        assertFalse(Name.isValidName("\"Alex\"")); // quotation marks are syntax, not part of a name
        assertFalse(Name.isValidName("a".repeat(Name.MAX_LENGTH + 1))); // exceeds maximum length

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Ravi s/o Kumar")); // slash
        assertTrue(Name.isValidName("Mary-Anne O'Brien")); // hyphen and straight apostrophe
        assertTrue(Name.isValidName("Mary-Anne O’Brien")); // typographic apostrophe
        assertTrue(Name.isValidName("J. R. R. Tolkien")); // periods
        assertTrue(Name.isValidName("李小龍")); // Unicode letters
        assertTrue(Name.isValidName("Jose\u0301")); // combining mark
        assertTrue(Name.isValidName("///")); // punctuation only
        assertTrue(Name.isValidName("...")); // punctuation only
        assertTrue(Name.isValidName("'-./’")); // supported punctuation only
        assertTrue(Name.isValidName("a".repeat(Name.MAX_LENGTH))); // maximum length
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void isSameName() {
        Name name = new Name("Valid Name");

        // same object -> returns true
        assertTrue(name.isSameName(name));

        // same value -> returns true
        assertTrue(name.isSameName(new Name("Valid Name")));

        // same value with different letter case -> returns true
        assertTrue(name.isSameName(new Name("valid name")));
        assertTrue(name.isSameName(new Name("VALID NAME")));

        // different value -> returns false
        assertFalse(name.isSameName(new Name("Other Valid Name")));

        // null -> returns false
        assertFalse(name.isSameName(null));
    }
}
