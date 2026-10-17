package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ArchiveCommand;

public class ArchiveCommandParserTest {

    private ArchiveCommandParser parser = new ArchiveCommandParser();

    @Test
    public void parse_validArgs_returnsArchiveCommand() {
        assertParseSuccess(parser, "1", new ArchiveCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ArchiveCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", ArchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "a", ArchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0", ArchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", ArchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 2", ArchiveCommand.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 r/student", ArchiveCommand.MESSAGE_INVALID_INDEX);
    }
}
