package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Subject;

public class ParserUtilTest {

    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "office phone";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_ROLE = "teacher";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_ROLE = "student";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_validQuotedValue_returnsUnquotedName() throws Exception {
        assertEquals(new Name("Randy p/e ratio"), ParserUtil.parseName("\"Randy p/e ratio\""));
        assertEquals(new Name("Ravi s/o Kumar"), ParserUtil.parseName("  \"Ravi s/o Kumar\"  "));
        assertEquals(new Name("///"), ParserUtil.parseName("\"///\""));
        assertEquals(new Name("李小龍"), ParserUtil.parseName("\"李小龍\""));
    }

    @Test
    public void parseName_repeatedSpaces_returnsNormalizedName() throws Exception {
        assertEquals(new Name("Alex Tan"), ParserUtil.parseName("Alex    Tan"));
        assertEquals(new Name("Randy p/e ratio"), ParserUtil.parseName("\"Randy   p/e    ratio\""));
    }

    @Test
    public void parseName_surroundingWhitespace_trimsQuotedAndUnquotedNames() throws Exception {
        for (String whitespace : List.of(WHITESPACE, "\u2003", "\u3000", " \t\u2003\u3000 ")) {
            Name expected = new Name("Alex Tan");
            assertEquals(expected, ParserUtil.parseName(whitespace + "Alex   Tan" + whitespace));
            assertEquals(expected, ParserUtil.parseName(whitespace + "\"" + whitespace
                    + "Alex   Tan" + whitespace + "\"" + whitespace));
            assertEquals(new Name("a".repeat(Name.MAX_LENGTH)), ParserUtil.parseName(
                    "\"" + whitespace + "a".repeat(Name.MAX_LENGTH) + whitespace + "\""));
        }
    }

    @Test
    public void parseName_onlySurroundingWhitespace_reportsBlankName() {
        for (String whitespace : List.of(WHITESPACE, "\u2003", "\u3000", " \t\u2003\u3000 ")) {
            assertThrows(ParseException.class, "Name cannot be blank.", () -> ParserUtil.parseName(whitespace));
            assertThrows(ParseException.class, "Name cannot be blank.", () -> ParserUtil.parseName(
                    "\"" + whitespace + "\""));
        }
    }

    @Test
    public void parseName_unsupportedInternalWhitespace_remainsInvalid() {
        for (String whitespace : List.of("\t", "\n", "\u2003", "\u3000", "\u00a0")) {
            String name = "Alex" + whitespace + "Tan";
            String error = Name.getValidationErrorMessage(name);
            assertThrows(ParseException.class, error, () -> ParserUtil.parseName(name));
            assertThrows(ParseException.class, error, () -> ParserUtil.parseName("\"" + name + "\""));
        }
    }

    @Test
    public void parseName_malformedQuotes_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_NAME_QUOTES, ()
                -> ParserUtil.parseName("\""));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_NAME_QUOTES, ()
                -> ParserUtil.parseName("\"Randy"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_NAME_QUOTES, ()
                -> ParserUtil.parseName("Randy\""));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_NAME_QUOTES, ()
                -> ParserUtil.parseName("\"Ran\"dy\""));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_NAME_QUOTES, ()
                -> ParserUtil.parseName("\"\"Randy\"\""));
    }

    @Test
    public void parseName_emptyQuotedValue_throwsParseException() {
        assertThrows(ParseException.class, "Name cannot be blank.", () -> ParserUtil.parseName("\"\""));
        assertThrows(ParseException.class, "Name cannot be blank.", () -> ParserUtil.parseName("\"   \""));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseRole_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseRole(INVALID_ROLE));
    }

    @Test
    public void parseRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRole(null));
    }

    @Test
    public void parseRole_validValueWithDifferentCase_returnsRole() throws Exception {
        assertEquals(Role.STUDENT, ParserUtil.parseRole(VALID_ROLE));
    }

    @Test
    public void parseRole_validValueWithWhitespaceAndDifferentCase_returnsRole() throws Exception {
        assertEquals(Role.GUARDIAN, ParserUtil.parseRole(WHITESPACE + "GuArDiAn" + WHITESPACE));
    }

    @Test
    public void parseEmail_maximumLength_checksAfterTrimming() throws Exception {
        String email = "a".repeat(64) + "@" + "b".repeat(63)
                + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertEquals(email, ParserUtil.parseEmail(" \t" + email + " \r\n").value);
        assertThrows(ParseException.class, Email.MESSAGE_TOO_LONG, ()
                -> ParserUtil.parseEmail(" a" + email + " "));
    }

    @Test
    public void parseEmail_componentLengths_reportsSpecificErrors() {
        assertThrows(ParseException.class, Email.MESSAGE_LOCAL_PART_TOO_LONG, ()
                -> ParserUtil.parseEmail("a".repeat(65) + "@example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_DOMAIN_LABEL_TOO_LONG, ()
                -> ParserUtil.parseEmail("a@" + "b".repeat(64) + ".com"));
    }

    @Test
    public void parseEmail_emptyOrWhitespace_reportsExactMessage() {
        String expectedMessage = "Empty email parameter: Email cannot be empty when e/ is specified.";
        for (String value : List.of("", " ", "\t\r\n")) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseEmail(value));
        }
    }

    @Test
    public void parseEmail_invalidExamples_reportsExactMessage() {
        assertThrows(ParseException.class, "Email domain must contain at least two labels separated by a period.", ()
                -> ParserUtil.parseEmail("alex@example"));
        assertThrows(ParseException.class, Email.MESSAGE_WHITESPACE, ()
                -> ParserUtil.parseEmail("alex @example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_LOCAL_PART_EMPTY, ()
                -> ParserUtil.parseEmail("@example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_DOMAIN_LABEL_EMPTY, ()
                -> ParserUtil.parseEmail("alex@.com"));
        assertThrows(ParseException.class, Email.MESSAGE_DOMAIN_LABEL_EMPTY, ()
                -> ParserUtil.parseEmail("alex@example..com"));
        assertThrows(ParseException.class, "Email address must contain exactly one @ symbol.", ()
                -> ParserUtil.parseEmail("alex@@example.com"));
        assertThrows(ParseException.class,
                Email.MESSAGE_DOMAIN_CHARACTERS, ()
                -> ParserUtil.parseEmail("alex@exam_ple.com"));
        assertThrows(ParseException.class,
                Email.MESSAGE_LOCAL_CHARACTERS, ()
                -> ParserUtil.parseEmail("alex!@example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_WHITESPACE, ()
                -> ParserUtil.parseEmail("alex\t@example.com"));
        assertThrows(ParseException.class, Email.MESSAGE_DOMAIN_LABEL_EMPTY, ()
                -> ParserUtil.parseEmail("alex@example.com."));
        assertThrows(ParseException.class, Email.MESSAGE_FINAL_LABEL_TOO_SHORT, ()
                -> ParserUtil.parseEmail("a@b.c"));
    }

    @Test
    public void parsePhone_invalidExamples_reportsSpecificMessage() {
        assertThrows(ParseException.class, Phone.MESSAGE_TOO_FEW_DIGITS, () -> ParserUtil.parsePhone("office"));
        assertThrows(ParseException.class, Phone.MESSAGE_TOO_FEW_DIGITS, () -> ParserUtil.parsePhone("12"));
        assertThrows(ParseException.class, Phone.MESSAGE_INVALID_CHARACTERS, ()
                -> ParserUtil.parsePhone("911/2222"));
        assertThrows(ParseException.class, Phone.MESSAGE_INVALID_CHARACTERS, ()
                -> ParserUtil.parsePhone("911\t2222"));
    }

    @Test
    public void parseEmail_validExamples_preservesTrimmedValue() throws Exception {
        for (String value : List.of("alex.tan@example.com", "alex+tuition@example.com",
                "Alex_Tan%25@Example.COM", "alex%school@example.com", "alex++__tan@example.com",
                "_alex-@example.com")) {
            assertEquals(value, ParserUtil.parseEmail(" \t" + value + " \r\n").value);
        }
        for (String value : List.of(".alex@example.com", "alex..tan@example.com", "alex.@example.com",
                "alex@-example.com", "alex@example-.com", "a@b.c")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseEmail(value));
        }
    }

    @Test
    public void parseLevel_validValueWithExtraSpaces_returnsTrimmedAndCollapsedLevel() throws Exception {
        assertEquals(new Level("Sec 3"), ParserUtil.parseLevel("  Sec   3  "));
    }

    @Test
    public void parseStudentDetails_invalidValues_reportsFailedConstraint() {
        assertThrows(ParseException.class, "Level cannot exceed 30 characters.", ()
                -> ParserUtil.parseLevel("a".repeat(31)));
        assertThrows(ParseException.class, "Subject cannot exceed 30 characters.", ()
                -> ParserUtil.parseSubject("a".repeat(31)));
        assertThrows(ParseException.class, "Level must contain at least one letter or digit.", ()
                -> ParserUtil.parseLevel(" -. + () "));
        assertThrows(ParseException.class, "Subject must contain at least one letter or digit.", ()
                -> ParserUtil.parseSubject(" -. + () "));
        assertThrows(ParseException.class, Level.MESSAGE_INVALID_CHARACTERS, ()
                -> ParserUtil.parseLevel("Sec\t3"));
        assertThrows(ParseException.class, Subject.MESSAGE_INVALID_CHARACTERS, ()
                -> ParserUtil.parseSubject("Math # Science"));
    }

    @Test
    public void parseLevel_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Level.MESSAGE_INVALID_CHARACTERS, () -> ParserUtil.parseLevel("Sec #3"));
    }

    @Test
    public void parseLevel_emptyOrWhitespace_reportsEmptyParameter() {
        String expectedMessage = "Empty level parameter: Level cannot be empty when lv/ is specified.";
        for (String value : List.of("", " ", "\t\r\n")) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseLevel(value));
        }
    }

    @Test
    public void parseLevel_none_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_RESERVED_REMOVE_VALUE, () ->
                ParserUtil.parseLevel("none"));
    }

    @Test
    public void parseSubject_validValueWithExtraSpaces_returnsTrimmedAndCollapsedSubject() throws Exception {
        assertEquals(new Subject("Pure Chem"), ParserUtil.parseSubject(" Pure  Chem "));
    }

    @Test
    public void parseSubject_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Subject.MESSAGE_INVALID_CHARACTERS, () ->
                ParserUtil.parseSubject("Math # Sci"));
    }

    @Test
    public void parseSubject_emptyOrWhitespace_reportsEmptyParameter() {
        String expectedMessage = "Empty subject parameter: Subject cannot be empty when sub/ is specified.";
        for (String value : List.of("", " ", "\t\r\n")) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseSubject(value));
        }
    }

    @Test
    public void parseSubject_none_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_RESERVED_REMOVE_VALUE, () ->
                ParserUtil.parseSubject("NONE"));
    }
}
