package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_removedContactFieldPrefixes_failure() {
        String fields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB;
        assertParseFailure(parser, " t/friends" + fields, AddCommand.MESSAGE_INVALID_PARAMETER);
        assertParseFailure(parser, fields + " t/friends", AddCommand.MESSAGE_INVALID_PARAMETER);

        assertParseFailure(parser, " a/123 Main Street" + fields, AddCommand.MESSAGE_INVALID_PARAMETER);
        assertParseFailure(parser, fields + " a/123 Main Street", AddCommand.MESSAGE_INVALID_PARAMETER);
    }

    @Test
    public void parse_unknownParameter_failure() {
        String fields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB;
        assertParseFailure(parser, fields + " x/value", AddCommand.MESSAGE_INVALID_PARAMETER);
        assertParseFailure(parser, NAME_DESC_BOB + " a/15" + PHONE_DESC_BOB + ROLE_DESC_BOB,
                AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        assertParseFailure(parser, NAME_DESC_BOB + " s/o Kumar" + PHONE_DESC_BOB + ROLE_DESC_BOB,
                AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        assertParseFailure(parser, ROLE_DESC_BOB + " " + PREFIX_NAME + "\"Alex Tan\" a/15",
                AddCommand.MESSAGE_INVALID_PARAMETER);
        assertParseFailure(parser, ROLE_DESC_BOB + " " + PREFIX_NAME + "\"Ravi s/o Kumar\" a/15",
                AddCommand.MESSAGE_INVALID_PARAMETER);
        assertParseFailure(parser, " " + PREFIX_NAME + "Ravi s/o Kumar" + PHONE_DESC_BOB + ROLE_DESC_BOB,
                AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        assertParseFailure(parser, " " + PREFIX_NAME + "ravi/so" + PHONE_DESC_BOB + ROLE_DESC_BOB,
                AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        assertParseFailure(parser, " " + PREFIX_NAME + "Alex / Tan" + PHONE_DESC_BOB + ROLE_DESC_BOB,
                AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_roleWithDifferentCaseAndWhitespace_success() {
        Person expectedPerson = new PersonBuilder(AMY).build();
        String otherFields = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY;

        assertParseSuccess(parser, otherFields + " " + PREFIX_ROLE + "sTuDenT",
                new AddCommand(expectedPerson));
        assertParseSuccess(parser, otherFields + " " + PREFIX_ROLE + "           student            ",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // multiple roles
        assertParseFailure(parser, ROLE_DESC_AMY + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid role
        assertParseFailure(parser, INVALID_ROLE_DESC + validExpectedPersonString,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);

        // invalid role
        assertParseFailure(parser, validExpectedPersonString + INVALID_ROLE_DESC,
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB,
                AddCommandParser.MESSAGE_MISSING_NAME);

        // an email without its prefix becomes part of the phone value
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ROLE_DESC_BOB,
                Phone.MESSAGE_INVALID_CHARACTERS);

        // missing role prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " " + VALID_ROLE_BOB, AddCommandParser.MESSAGE_MISSING_ROLE);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB,
                AddCommandParser.MESSAGE_MISSING_ROLE);

        // empty command: role is the first required parameter in the documented format
        assertParseFailure(parser, "", AddCommandParser.MESSAGE_MISSING_ROLE);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB, "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes.");

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + ROLE_DESC_BOB, Phone.MESSAGE_TOO_FEW_DIGITS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + ROLE_DESC_BOB, "Email address must contain exactly one @ symbol.");

        // invalid role
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_ROLE_DESC, Role.MESSAGE_CONSTRAINTS);

        // an invalid role is reported before a missing name
        assertParseFailure(parser, INVALID_ROLE_DESC, Role.MESSAGE_CONSTRAINTS);

        // empty role
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " " + PREFIX_ROLE, AddCommandParser.MESSAGE_EMPTY_ROLE);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + " " + PREFIX_ROLE + "   ", AddCommandParser.MESSAGE_EMPTY_ROLE);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + ROLE_DESC_BOB,
                "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes.");

        // invalid fields are checked in documented parameter order, regardless of input order
        assertParseFailure(parser, INVALID_EMAIL_DESC + INVALID_PHONE_DESC + INVALID_NAME_DESC + INVALID_ROLE_DESC,
                Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INVALID_EMAIL_DESC + INVALID_PHONE_DESC + INVALID_NAME_DESC + ROLE_DESC_BOB,
                "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes.");
        assertParseFailure(parser, INVALID_EMAIL_DESC + INVALID_PHONE_DESC + NAME_DESC_BOB + ROLE_DESC_BOB,
                Phone.MESSAGE_TOO_FEW_DIGITS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB,
                AddCommandParser.MESSAGE_UNEXPECTED_TEXT);
    }

    @Test
    public void parse_emailInvalidValues_reportsSpecificErrors() {
        String[][] invalidEmailCases = {
            {"@example.com", "Email local part cannot be empty."},
            {"alex@", "Email domain cannot be empty."},
            {"alex @example.com", "Email address cannot contain whitespace."},
            {"alex-@example.com", "Email local part contains unsupported characters or invalid punctuation placement."},
            {"alex@example", "Email domain must contain at least two labels separated by a period."},
            {"alex@.com", "Email domain labels cannot be empty."},
            {"alex@example..com", "Email domain labels cannot be empty."},
            {"alex@exam_ple.com", "Email domain labels may contain letters, digits, and internal hyphens only."},
            {"alex@example.c", "Email's final domain label must contain at least two characters."}
        };

        for (String[] testCase : invalidEmailCases) {
            String arguments = NAME_DESC_AMY + PHONE_DESC_AMY + " e/" + testCase[0] + ROLE_DESC_AMY;
            assertParseFailure(parser, arguments, testCase[1]);
        }
    }

    @Test
    public void parse_validNameVariations_success() {
        assertNameParsesSuccessfully("\"Randy p/e ratio\"", "Randy p/e ratio");
        assertNameParsesSuccessfully("\"ravi s/o\"", "ravi s/o");
        assertNameParsesSuccessfully("\"Ravi s/o Kumar\"", "Ravi s/o Kumar");
        assertNameParsesSuccessfully("\"Alex / Tan\"", "Alex / Tan");
        assertNameParsesSuccessfully("\"///\"", "///");
        assertNameParsesSuccessfully("...", "...");
        assertNameParsesSuccessfully("Mary-Anne O’Brien", "Mary-Anne O’Brien");
        assertNameParsesSuccessfully("李小龍", "李小龍");
        assertNameParsesSuccessfully("\"Alex    Tan\"", "Alex Tan");
        assertNameParsesSuccessfully("a".repeat(Name.MAX_LENGTH), "a".repeat(Name.MAX_LENGTH));
    }

    @Test
    public void parse_multipleInvalidFields_reportsFirstFieldInParameterOrder() {
        assertParseFailure(parser, NAME_DESC_AMY + " " + PREFIX_ROLE + "teacher p/12",
                Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_AMY + " p/12 " + PREFIX_ROLE + "teacher",
                Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_malformedQuotedName_failure() {
        String otherFields = PHONE_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB;

        assertParseFailure(parser, " " + PREFIX_NAME + "\"Randy p/e ratio" + otherFields,
                ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTED_VALUE);
        assertParseFailure(parser, " " + PREFIX_NAME + "Randy\"" + otherFields,
                ParserUtil.MESSAGE_INVALID_NAME_QUOTES);
        assertParseFailure(parser, " " + PREFIX_NAME + "\"Ran\"dy\"" + otherFields,
                ParserUtil.MESSAGE_INVALID_NAME_QUOTES);
        assertParseFailure(parser, " " + PREFIX_NAME + "\"\"Randy\"\"" + otherFields,
                ParserUtil.MESSAGE_INVALID_NAME_QUOTES);
    }

    @Test
    public void parse_emptyOrUnsupportedName_failure() {
        String otherFields = PHONE_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB;

        assertParseFailure(parser, " " + PREFIX_NAME + "\"\"" + otherFields, "Name cannot be blank.");
        assertParseFailure(parser, " " + PREFIX_NAME + "\"   \"" + otherFields, "Name cannot be blank.");
        assertParseFailure(parser, " " + PREFIX_NAME + "Alex@Tan" + otherFields,
                "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes.");
        assertParseFailure(parser, " " + PREFIX_NAME + "a".repeat(Name.MAX_LENGTH + 1) + otherFields,
                "Name cannot exceed " + Name.MAX_LENGTH + " characters.");
    }

    private void assertNameParsesSuccessfully(String nameArgument, String expectedName) {
        Person expectedPerson = new PersonBuilder(BOB).withName(expectedName).build();
        String input = " " + PREFIX_NAME + nameArgument + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ROLE_DESC_BOB;

        assertParseSuccess(parser, input, new AddCommand(expectedPerson));
    }


    @Test
    public void parse_emailOmitted_success() {
        Person expected = new PersonBuilder(AMY).withoutEmail().build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY,
                new AddCommand(expected));
    }

    @Test
    public void parse_phoneOmitted_success() {
        Person expected = new PersonBuilder(AMY).withoutPhone().build();
        assertParseSuccess(parser, NAME_DESC_AMY + EMAIL_DESC_AMY + ROLE_DESC_AMY,
                new AddCommand(expected));
    }

    @Test
    public void parse_studentPhoneAndEmailOmitted_success() {
        Person expected = new PersonBuilder(AMY).withoutPhone().withoutEmail().build();
        assertParseSuccess(parser, NAME_DESC_AMY + ROLE_DESC_AMY, new AddCommand(expected));
    }

    @Test
    public void parse_guardianPhoneAndEmailOmitted_failure() {
        assertParseFailure(parser, NAME_DESC_AMY + ROLE_DESC_BOB,
                AddCommand.MESSAGE_CONTACT_METHOD_REQUIRED);
    }

    @Test
    public void parse_guardianWithOneContactMethod_success() {
        Person phoneOnlyGuardian = new PersonBuilder(BOB).withoutEmail().build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ROLE_DESC_BOB,
                new AddCommand(phoneOnlyGuardian));

        Person emailOnlyGuardian = new PersonBuilder(BOB).withoutPhone().build();
        assertParseSuccess(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + ROLE_DESC_BOB,
                new AddCommand(emailOnlyGuardian));
    }

    @Test
    public void parse_phoneEmpty_failure() {
        String fields = NAME_DESC_AMY + EMAIL_DESC_AMY + ROLE_DESC_AMY;
        assertParseFailure(parser, fields + " p/", Phone.MESSAGE_EMPTY);
        assertParseFailure(parser, fields + " p/   ", Phone.MESSAGE_EMPTY);
        assertParseFailure(parser, NAME_DESC_AMY + " p/   " + EMAIL_DESC_AMY + ROLE_DESC_AMY,
                Phone.MESSAGE_EMPTY);
    }

    @Test
    public void parse_emailEmpty_failure() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY;
        assertParseFailure(parser, fields + " e/", Email.MESSAGE_EMPTY);
        assertParseFailure(parser, fields + " e/   ", Email.MESSAGE_EMPTY);
        assertParseFailure(parser, NAME_DESC_AMY + " e/   " + PHONE_DESC_AMY + ROLE_DESC_AMY,
                Email.MESSAGE_EMPTY);
        assertParseFailure(parser, fields + " e/ e/alex@example.com",
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);
    }

    @Test
    public void parse_emailWithPercentageSign_success() {
        Person expected = new PersonBuilder(AMY).withEmail("alex%school@example.com").build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY
                + " e/alex%school@example.com" + ROLE_DESC_AMY, new AddCommand(expected));
    }

    @Test
    public void parse_phoneWithLabelsAndMultipleNumbers_success() {
        String phoneDetails = "(HP) +65 1234 5678 1111-3333 (Office)";
        Person expected = new PersonBuilder(AMY).withoutEmail().withPhone(phoneDetails).build();
        assertParseSuccess(parser, NAME_DESC_AMY + " p/" + phoneDetails + ROLE_DESC_AMY,
                new AddCommand(expected));
    }

    @Test
    public void parse_emailAtMaximumLength_acceptsTrimmedValue() {
        String email = "a".repeat(64) + "@" + "b".repeat(63)
                + "." + "c".repeat(63) + "." + "d".repeat(61);
        Person expected = new PersonBuilder(AMY).withoutPhone().withEmail(email).build();
        assertParseSuccess(parser, NAME_DESC_AMY + " e/  " + email + "  " + ROLE_DESC_AMY,
                new AddCommand(expected));
    }

    @Test
    public void parse_emailExceedingMaximumLength_reportsLengthError() {
        for (String localPart : List.of("a".repeat(243), "a.".repeat(10_000) + "a")) {
            assertParseFailure(parser, NAME_DESC_AMY + " e/" + localPart + "@example.com" + ROLE_DESC_AMY,
                    Email.MESSAGE_TOO_LONG);
        }
    }

    @Test
    public void parse_emailWithManySeparators_reportsInvalidPunctuation() {
        String localPart = "a.".repeat(32);
        assertParseFailure(parser, NAME_DESC_AMY + " e/" + localPart + "@example.com" + ROLE_DESC_AMY,
                "Email local part contains unsupported characters or invalid punctuation placement.");
    }

    @Test
    public void parse_phoneWithNonAsciiNumerals_requiresThreeAsciiDigits() {
        String numerals = "\u0661\u0662\u0663";
        assertParseFailure(parser, NAME_DESC_AMY + " p/12 " + numerals + ROLE_DESC_AMY,
                Phone.MESSAGE_TOO_FEW_DIGITS);
        String phone = "(手机) 123 " + numerals;
        Person expected = new PersonBuilder(AMY).withoutEmail().withPhone(phone).build();
        assertParseSuccess(parser, NAME_DESC_AMY + " p/" + phone + ROLE_DESC_AMY, new AddCommand(expected));
    }

    @Test
    public void parse_phoneAtMaximumLength_success() {
        String phoneDetails = "1".repeat(Phone.MAX_LENGTH);
        Person expected = new PersonBuilder(AMY).withPhone(phoneDetails).withoutEmail().build();
        assertParseSuccess(parser, NAME_DESC_AMY + " p/" + phoneDetails + ROLE_DESC_AMY,
                new AddCommand(expected));
    }

    @Test
    public void parse_phoneExceedingMaximumLength_failure() {
        String phoneDetails = "1".repeat(Phone.MAX_LENGTH + 1);
        assertParseFailure(parser, NAME_DESC_AMY + " p/" + phoneDetails + ROLE_DESC_AMY,
                Phone.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_phoneWithSlash_failure() {
        assertParseFailure(parser, NAME_DESC_AMY + " p/911/2222" + ROLE_DESC_AMY,
                Phone.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_phoneWithForbiddenCharacters_failure() {
        assertParseFailure(parser, NAME_DESC_AMY + " p/912@456" + ROLE_DESC_AMY,
                Phone.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, NAME_DESC_AMY + " p/912\t456" + ROLE_DESC_AMY,
                Phone.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_studentWithLevelAndSubject_success() {
        Person expectedPerson = new PersonBuilder(AMY).withLevel("Sec 3").withSubject("A-Math").build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ROLE_DESC_AMY
                + " " + PREFIX_LEVEL + "Sec  3 " + PREFIX_SUBJECT + "A-Math", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_studentWithOnlySubject_success() {
        Person expectedPerson = new PersonBuilder(AMY).withSubject("Math").build();
        assertParseSuccess(parser, " " + PREFIX_SUBJECT + "Math" + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ROLE_DESC_AMY, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_guardianWithLevelOrSubject_failure() {
        String guardian = " " + PREFIX_ROLE + "guardian" + NAME_DESC_BOB + PHONE_DESC_BOB;
        assertParseFailure(parser, guardian + " " + PREFIX_LEVEL + "Sec 3",
                AddCommandParser.MESSAGE_GUARDIAN_STUDENT_DETAILS);
        assertParseFailure(parser, guardian + " " + PREFIX_SUBJECT + "Math",
                AddCommandParser.MESSAGE_GUARDIAN_STUDENT_DETAILS);
    }

    @Test
    public void parse_repeatedLevelOrSubject_failure() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY;
        assertParseFailure(parser, fields + " " + PREFIX_LEVEL + "Sec 3 " + PREFIX_LEVEL + "Sec 4",
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);
        assertParseFailure(parser, fields + " " + PREFIX_SUBJECT + "Math " + PREFIX_SUBJECT + "Physics",
                AddCommandParser.MESSAGE_DUPLICATE_PREFIXES);
    }

    @Test
    public void parse_invalidLevelOrSubject_failure() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY;
        assertParseFailure(parser, fields + " " + PREFIX_LEVEL + "Sec #3", Level.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, fields + " " + PREFIX_SUBJECT + "Math & Sci", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, fields + " " + PREFIX_LEVEL + "NONE",
                ParserUtil.MESSAGE_RESERVED_REMOVE_VALUE);
    }

    @Test
    public void parse_emptyLevel_reportsEmptyParameter() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY;
        for (String value : List.of("", "   ", "\t\r\n")) {
            assertParseFailure(parser, fields + " " + PREFIX_LEVEL + value,
                    "Empty level parameter: Level cannot be empty when lv/ is specified.");
        }
    }

    @Test
    public void parse_emptySubject_reportsEmptyParameter() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + ROLE_DESC_AMY;
        for (String value : List.of("", "   ", "\t\r\n")) {
            assertParseFailure(parser, fields + " " + PREFIX_SUBJECT + value,
                    "Empty subject parameter: Subject cannot be empty when sub/ is specified.");
        }
    }
}
