package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void isSameNameForDuplicateWarning_caseDependentComposition_matchesBothDirections() {
        Name uppercase = new Name("J\u030can");
        Name lowercase = new Name("j\u030can");
        Name composed = new Name("\u01f0an");
        // EP: only lowercase j with caron has a precomposed character.
        assertTrue(uppercase.isSameName(lowercase));
        assertTrue(uppercase.isSameNameForDuplicateWarning(lowercase));
        assertTrue(lowercase.isSameNameForDuplicateWarning(uppercase));
        assertTrue(uppercase.isSameNameForDuplicateWarning(composed));
        assertTrue(composed.isSameNameForDuplicateWarning(uppercase));
    }

    @Test
    public void isSameNameForDuplicateWarning_preservesCaseRulesAndWordBoundaries() {
        Name name = new Name("\u00c9lise Tan");
        // EP: equivalent representation and case, including surrounding/repeated legacy spaces.
        assertTrue(name.isSameNameForDuplicateWarning(new Name(" E\u0301LISE  TAN ")));
        assertTrue(new Name("I").isSameNameForDuplicateWarning(new Name("\u0131")));
        assertTrue(new Name(" \u0130 ").isSameNameForDuplicateWarning(new Name("i")));
        // EP: different accent, word boundary, compatibility character, or absent name.
        assertFalse(name.isSameNameForDuplicateWarning(new Name("Elise Tan")));
        assertFalse(name.isSameNameForDuplicateWarning(new Name("\u00c9liseTan")));
        assertFalse(name.isSameNameForDuplicateWarning(new Name("\uff25lise Tan")));
        assertFalse(name.isSameNameForDuplicateWarning(null));
    }

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
        assertTrue(Name.isValidName("Alex, Tan")); // comma
        assertTrue(Name.isValidName("李小龍")); // Unicode letters
        assertTrue(Name.isValidName("Jose\u0301")); // combining mark
        assertTrue(Name.isValidName("a".repeat(Name.MAX_LENGTH))); // maximum length
    }

    @Test
    public void validation_noLettersOrDigits_rejectsPunctuationAndCombiningMarks() {
        for (String name : List.of("...", "-", "---", "'", "///", "'-./’", " . - ’ ", "\u0301", "-\u0301.",
                "(", ")", ",", "(),")) {
            assertFalse(Name.isValidName(name));
            assertEquals(Name.MESSAGE_MISSING_ALPHANUMERIC, Name.getValidationErrorMessage(name));
            assertThrows(IllegalArgumentException.class, () -> new Name(name));
        }
    }

    @Test
    public void validation_unicodeLettersAndNumbers_remainValid() {
        for (String name : List.of("O’Brien", "Mary-Anne", "李小龍", "किरण", "123", "Jose\u0301",
                "\uD801\uDC00", "\uD835\uDFD8", "²")) {
            assertTrue(Name.isValidName(name));
            assertEquals(name, new Name(name).fullName);
        }
    }

    @Test
    public void validation_multipleErrors_preservesErrorPriority() {
        assertEquals("Name cannot be blank.", Name.getValidationErrorMessage(" "));
        assertEquals("Name cannot exceed " + Name.MAX_LENGTH + " characters.",
                Name.getValidationErrorMessage(".".repeat(Name.MAX_LENGTH + 1)));
        assertEquals("Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                + "hyphens, periods, middle dots (·), parentheses, commas, and slashes.",
                Name.getValidationErrorMessage("!"));
    }

    @Test
    public void constructor_parenthesesAndCommas_preservesSpelling() {
        for (String name : List.of("Tan Wei Ming (David)", "Lee Ah Kow, Jr.", "Alex (Al), Jr.")) {
            assertEquals(name, new Name(name).fullName);
        }
    }

    @Test
    public void constructor_apostropheVariants_preservesOriginalSpelling() {
        for (String apostrophe : List.of("'", "\u2018", "\u2019")) {
            String name = "O" + apostrophe + "Brien";
            assertTrue(Name.isValidName(name));
            assertEquals(name, new Name(name).fullName);
            assertFalse(Name.isValidName(apostrophe));
            assertEquals(Name.MESSAGE_MISSING_ALPHANUMERIC, Name.getValidationErrorMessage(apostrophe));
        }
    }

    @Test
    public void constructor_middleDotNames_preservesOriginalSpelling() {
        String name = "玛丽\u00b7居里";
        assertTrue(Name.isValidName(name));
        assertEquals(name, new Name(name).fullName);
        assertTrue(Name.isValidName("L\u00b7L"));
        assertFalse(Name.isValidName("玛丽\u2022居里")); // a bullet is not a middle dot
        assertFalse(Name.isValidName("\u00b7"));
        assertEquals(Name.MESSAGE_MISSING_ALPHANUMERIC, Name.getValidationErrorMessage("\u00b7"));
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
    public void replaceCurlyApostrophes() {
        // null -> throws NullPointerException
        assertThrows(NullPointerException.class, () -> Name.replaceCurlyApostrophes(null));

        // right single quotation mark -> straight apostrophe
        assertEquals("O'Brien", Name.replaceCurlyApostrophes("O’Brien"));

        // left single quotation mark -> straight apostrophe
        assertEquals("O'Brien", Name.replaceCurlyApostrophes("O‘Brien"));

        // several curly apostrophes -> all replaced
        assertEquals("D'Souza 'Ali'", Name.replaceCurlyApostrophes("D’Souza ‘Ali’"));

        // straight apostrophe and other characters -> unchanged
        assertEquals("Nur 'Aisyah-Tan", Name.replaceCurlyApostrophes("Nur 'Aisyah-Tan"));
    }

    @Test
    public void removeAccents() {
        // null -> throws NullPointerException
        assertThrows(NullPointerException.class, () -> Name.removeAccents(null));

        // precomposed accented letters -> base letters
        assertEquals("Elise", Name.removeAccents("\u00c9lise"));
        assertEquals("Zoe Nunez", Name.removeAccents("Zo\u00eb N\u00fa\u00f1ez"));

        // letter followed by a combining mark -> base letter
        assertEquals("Elise", Name.removeAccents("E\u0301lise"));

        // letters with a stroke -> base letters, keeping letter case
        assertEquals("Soren Lukasz", Name.removeAccents("S\u00f8ren \u0141ukasz"));
        assertEquals("OLE DOKOVIC", Name.removeAccents("\u00d8LE \u0110OKOVI\u0106"));

        // ligatures and sharp s -> two letters
        assertEquals("Aesa Strauss", Name.removeAccents("\u00c6sa Strau\u00df"));

        // letter with a stroke and an accent -> base letter
        assertEquals("o", Name.removeAccents("\u01ff"));

        // no accents -> unchanged
        assertEquals("Alex O'Brien-Tan 2", Name.removeAccents("Alex O'Brien-Tan 2"));

        // EP: Greek and Cyrillic accents -> base letters
        assertEquals("\u03b1\u03bd\u03bd\u03b1", Name.removeAccents("\u03ac\u03bd\u03bd\u03b1")); // άννα
        assertEquals("\u0438\u0432\u0430\u043d", Name.removeAccents("\u0439\u0432\u0430\u043d")); // йван

        // EP: Devanagari vowel signs are part of the word -> kept
        assertEquals("\u0915\u093f\u0930\u0923", Name.removeAccents("\u0915\u093f\u0930\u0923")); // किरण
        assertEquals("\u093f", Name.removeAccents("\u093f")); // vowel sign ि on its own

        // Boundary: a combining mark with no letter before it -> kept, as there is no accented letter
        assertEquals("\u0301", Name.removeAccents("\u0301"));
        assertEquals("A \u0301", Name.removeAccents("A \u0301"));

        // EP: each kind of combining mark is removed after a Latin letter and kept after a Devanagari letter
        assertEquals("A", Name.removeAccents("A\u20dd")); // enclosing circle
        assertEquals("\u0915\u20dd", Name.removeAccents("\u0915\u20dd"));
        assertEquals("a", Name.removeAccents("a\u0903")); // spacing mark (visarga)
        assertEquals("\u0915\u094d\u0937", Name.removeAccents("\u0915\u094d\u0937")); // non-spacing virama in क्ष

        // EP: a character outside the Basic Multilingual Plane -> unchanged, and marks after it are kept
        assertEquals("\ud801\udc00", Name.removeAccents("\ud801\udc00")); // Deseret capital long I
        assertEquals("\ud801\udc00\u0301", Name.removeAccents("\ud801\udc00\u0301"));
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
