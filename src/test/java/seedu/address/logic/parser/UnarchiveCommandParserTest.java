package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnarchiveCommand;

public class UnarchiveCommandParserTest {

    private UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validArgs_returnsUnarchiveCommand() {
        assertParseSuccess(parser, "1", new UnarchiveCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new UnarchiveCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", UnarchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "a", UnarchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0", UnarchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", UnarchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 2", UnarchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 r/student", UnarchiveCommand.MESSAGE_INVALID_INDEX);
    }
}
