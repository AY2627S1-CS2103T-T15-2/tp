package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_former_returnsListFormerCommand() {
        assertParseSuccess(parser, " former", new ListCommand(true));
        assertParseSuccess(parser, "  FORMER  ", new ListCommand(true));
    }

    @Test
    public void parse_extraText_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " all", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " former 1", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " formers", ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
