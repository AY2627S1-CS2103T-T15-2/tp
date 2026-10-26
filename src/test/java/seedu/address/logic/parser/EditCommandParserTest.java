package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_localEmailPunctuation_success() {
        for (String email : List.of("mei__tan@example.com", "mei+_tan@example.com", "mei++__tan@example.com",
                "_mei-@example.com")) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail(email).build();
            assertParseSuccess(parser, "1 e/" + email, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_emailExceedingMaximumLength_reportsLengthError() {
        String email = "a".repeat(64) + "@" + "b".repeat(63)
                + "." + "c".repeat(63) + "." + "d".repeat(62);
        assertParseFailure(parser, "1 e/" + email, Email.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, NAME_DESC_AMY, EditCommand.MESSAGE_INVALID_COMMAND);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", EditCommand.MESSAGE_INVALID_COMMAND);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_CONTACT_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_CONTACT_INDEX);

        // words and decimals
        assertParseFailure(parser, "abc" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1.5" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_CONTACT_INDEX);

        // index too large for an int
        assertParseFailure(parser, "99999999999" + NAME_DESC_AMY, Messages.MESSAGE_INVALID_CONTACT_INDEX);
    }

    @Test
    public void parse_leadingZeroIndex_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        assertParseSuccess(parser, "02" + NAME_DESC_AMY, new EditCommand(INDEX_SECOND_PERSON, descriptor));
    }

    @Test
    public void parse_extraPreamble_failure() {
        assertParseFailure(parser, "1 some random string", EditCommand.MESSAGE_INVALID_COMMAND);
        assertParseFailure(parser, "1 2" + NAME_DESC_AMY, EditCommand.MESSAGE_INVALID_COMMAND);
        assertParseFailure(parser, "1\t2" + NAME_DESC_AMY, EditCommand.MESSAGE_INVALID_COMMAND);
    }

    @Test
    public void parse_unknownParameter_failure() {
        // unknown prefix in place of a field
        assertParseFailure(parser, "1 r/guardian", EditCommand.MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 a/123 Main Street", EditCommand.MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 t/", EditCommand.MESSAGE_UNKNOWN_PARAMETER);

        // unknown prefix after a field
        assertParseFailure(parser, "1 n/Alex a/15", EditCommand.MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1" + PHONE_DESC_AMY + " t/friends", EditCommand.MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1" + EMAIL_DESC_AMY + " x/1", EditCommand.MESSAGE_UNKNOWN_PARAMETER);

        // unknown prefix after a quoted name
        assertParseFailure(parser, "1 n/\"Alex Tan\" a/15", EditCommand.MESSAGE_UNKNOWN_PARAMETER);

        // unknown prefix is reported before an invalid index
        assertParseFailure(parser, "0 r/guardian", EditCommand.MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_slashInsideWord_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("AC/DC").build();
        assertParseSuccess(parser, "1 n/AC/DC", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC,
                "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes."); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_TOO_FEW_DIGITS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC,
                "Email address must contain exactly one @ symbol."); // invalid email

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_TOO_FEW_DIGITS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_PHONE_AMY,
                "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                        + "hyphens, periods, and slashes.");
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB
                + EMAIL_DESC_AMY + NAME_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY)
                .build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PARAMETER);

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PARAMETER);

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_BOB + EMAIL_DESC_BOB;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PARAMETER);

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PARAMETER);

        // repeated name
        assertParseFailure(parser, "1 n/A n/B", EditCommand.MESSAGE_REPEATED_PARAMETER);
    }

    @Test
    public void parse_emptyEmail_failure() {
        assertParseFailure(parser, "1 e/", Email.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 e/   ", Email.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 e/alex@example",
                "Email domain must contain at least two labels separated by a period.");
    }

    @Test
    public void parse_emailNone_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutEmail().build();
        assertParseSuccess(parser, "1 e/NONE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 e/ NONE ", new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).withoutEmail().build();
        assertParseSuccess(parser, "1" + NAME_DESC_AMY + " e/NONE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_phoneNone_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutPhone().build();
        assertParseSuccess(parser, "1 p/NONE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 p/ NONE ", new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withoutPhone().withoutEmail().build();
        assertParseSuccess(parser, "1 p/NONE e/NONE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_phoneNoneAnyCase_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutPhone().build();
        assertParseSuccess(parser, "1 p/none", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 p/nOnE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_emptyPhone_failure() {
        assertParseFailure(parser, "1 p/", Phone.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 p/   ", Phone.MESSAGE_EMPTY);
    }

    @Test
    public void parse_emailNoneAnyCase_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutEmail().build();
        assertParseSuccess(parser, "1 e/none", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 e/None", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_quotedName_success() {
        Index targetIndex = INDEX_FIRST_PERSON;

        // quoted name containing a prefix is kept whole
        String userInput = targetIndex.getOneBased() + " n/\"Randy p/e ratio\"" + PHONE_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Randy p/e ratio")
                .withPhone(VALID_PHONE_AMY).build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));

        // quoted name containing the email prefix is kept whole
        userInput = targetIndex.getOneBased() + " n/\"Tan e/x Lim\"" + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withName("Tan e/x Lim").withEmail(VALID_EMAIL_AMY).build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));

        // a name with s/o must be quoted, since s/ would otherwise read as a parameter
        descriptor = new EditPersonDescriptorBuilder().withName("Ravi s/o Kumar").build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " n/\"Ravi s/o Kumar\"",
                new EditCommand(targetIndex, descriptor));
        assertParseFailure(parser, targetIndex.getOneBased() + " n/Ravi s/o Kumar",
                EditCommand.MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_malformedQuotedName_failure() {
        assertParseFailure(parser, "1 n/\"Randy p/e ratio", ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTED_VALUE);
        assertParseFailure(parser, "1 n/Randy\"", ParserUtil.MESSAGE_INVALID_NAME_QUOTES);
        assertParseFailure(parser, "1 n/\"Ran\"dy\"", ParserUtil.MESSAGE_INVALID_NAME_QUOTES);
    }

    @Test
    public void parse_levelAndSubject_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withLevel("Sec 4")
                .withSubject("A-Math").build();
        assertParseSuccess(parser, "1 lv/ Sec  4 sub/A-Math",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_levelAndSubjectNone_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutLevel().withoutSubject()
                .build();
        assertParseSuccess(parser, "1 lv/NONE sub/none", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 lv/ None  sub/nOnE", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidLevelOrSubject_failure() {
        assertParseFailure(parser, "1 lv/Sec #3", Level.MESSAGE_INVALID_CHARACTERS);
        assertParseFailure(parser, "1 sub/Math & Science", Subject.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_emptyLevel_reportsEmptyParameter() {
        for (String value : List.of("", "   ", "\t\r\n")) {
            assertParseFailure(parser, "1 lv/" + value,
                    "Empty level parameter: Level cannot be empty when lv/ is specified.");
        }
    }

    @Test
    public void parse_emptySubject_reportsEmptyParameter() {
        for (String value : List.of("", "   ", "\t\r\n")) {
            assertParseFailure(parser, "1 sub/" + value,
                    "Empty subject parameter: Subject cannot be empty when sub/ is specified.");
        }
    }

    @Test
    public void parse_repeatedLevelOrSubject_failure() {
        assertParseFailure(parser, "1 lv/Sec 3 lv/Sec 4", EditCommand.MESSAGE_REPEATED_PARAMETER);
        assertParseFailure(parser, "1 sub/Math sub/Physics", EditCommand.MESSAGE_REPEATED_PARAMETER);
    }
}
