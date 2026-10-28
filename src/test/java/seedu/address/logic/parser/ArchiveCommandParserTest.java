package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ArchiveCommand;

public class ArchiveCommandParserTest {

    private ArchiveCommandParser parser = new ArchiveCommandParser();

    @Test
    public void parse_validArgs_returnsArchiveCommand() {
        assertParseSuccess(parser, "1", new ArchiveCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ArchiveCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "\t01\n", new ArchiveCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingIndex_showsCommandFormat() {
        for (String args : new String[] {"", "  ", "\t\n"}) {
            assertParseFailure(parser, args, "Missing contact index. Use: archive INDEX");
        }
    }

    @Test
    public void parse_extraArguments_explainsSingleIndexAndShowsFormat() {
        for (String args : new String[] {"1 2", "1 r/student", "1\t2", "1\n2"}) {
            assertParseFailure(parser, args, "Only one contact index is allowed. Use: archive INDEX");
        }
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "0", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "-1", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "99999999999", Messages.MESSAGE_INVALID_CONTACT_INDEX);
    }
}
