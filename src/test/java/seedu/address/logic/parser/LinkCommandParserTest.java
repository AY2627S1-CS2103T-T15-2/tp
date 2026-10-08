package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LinkCommand;

public class LinkCommandParserTest {

    private final LinkCommandParser parser = new LinkCommandParser();

    @Test
    public void parse_validIndexes_acceptsEitherOrderWhitespaceAndLeadingZeroes() {
        LinkCommand expected = new LinkCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);
        for (String arguments : new String[]{" s/1 g/2", "g/2 s/1", " s/001 g/02 ", "\ts/ 1\tg/ 2\n"}) {
            assertParseSuccess(parser, arguments, expected);
        }
    }

    @Test
    public void parse_missingParametersOrExtraText_rejectsInput() {
        for (String arguments : new String[]{"", " ", "s/1", "g/2", "extra s/1 g/2", "s/1 g/2 extra",
            "s/1 2 g/2", "1 2"}) {
            assertParseFailure(parser, arguments, LinkCommand.MESSAGE_INVALID_COMMAND);
        }
    }

    @Test
    public void parse_unknownOrRepeatedParameters_reportsSpecificError() {
        for (String arguments : new String[]{"s/1 g/2 r/student", "x/1 s/1 g/2", "S/1 g/2", "s/1 G/2"}) {
            assertParseFailure(parser, arguments, LinkCommand.MESSAGE_UNKNOWN_PARAMETER);
        }
        assertParseFailure(parser, "s/1 s/2 g/2", LinkCommand.MESSAGE_REPEATED_PARAMETER);
        assertParseFailure(parser, "s/1 g/2 g/3", LinkCommand.MESSAGE_REPEATED_PARAMETER);
    }

    @Test
    public void parse_invalidIndexes_handlesEmptyZeroNegativeDecimalAndOverflow() {
        for (String value : new String[]{"", "0", "00", "-1", "+1", "1.5", "Alex", "2147483648", "9".repeat(50)}) {
            assertParseFailure(parser, "s/" + value + " g/2", LinkCommand.MESSAGE_INVALID_STUDENT_INDEX);
            assertParseFailure(parser, "s/1 g/" + value, LinkCommand.MESSAGE_INVALID_GUARDIAN_INDEX);
        }
    }

    @Test
    public void parseCommand_link_dispatchesToLinkParser() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals(new LinkCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                new AddressBookParser().parseCommand("link s/1 g/2"));
    }
}
