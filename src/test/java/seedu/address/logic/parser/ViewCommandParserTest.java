package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  2  ", new ViewCommand(INDEX_SECOND_PERSON));
        assertParseSuccess(parser, "02", new ViewCommand(INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "0", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "-1", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1.5", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "99999999999", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1 2", Messages.MESSAGE_INVALID_CONTACT_INDEX);
    }
}
