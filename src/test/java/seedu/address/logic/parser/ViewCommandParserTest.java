package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private static final String MESSAGE_MISSING_INDEX = "Missing contact index. Use: view INDEX";
    private static final String MESSAGE_MULTIPLE_INDEXES = "Only one contact index is allowed. Use: view INDEX";

    private ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        // Boundary: the smallest valid index
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        // Boundary: the largest valid index
        assertParseSuccess(parser, Integer.toString(Integer.MAX_VALUE),
                new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
        // EP: surrounding spaces
        assertParseSuccess(parser, "  2  ", new ViewCommand(INDEX_SECOND_PERSON));
        // EP: leading zero
        assertParseSuccess(parser, "02", new ViewCommand(INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        // EP: not a number
        assertParseFailure(parser, "a", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        // Boundary: zero, just below the smallest valid index
        assertParseFailure(parser, "0", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        // EP: negative number
        assertParseFailure(parser, "-1", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        // EP: explicit plus sign
        assertParseFailure(parser, "+1", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        // EP: not a whole number
        assertParseFailure(parser, "1.5", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        // Boundary: one more than the largest valid index
        assertParseFailure(parser, Long.toString(Integer.MAX_VALUE + 1L), Messages.MESSAGE_INVALID_CONTACT_INDEX);
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        // EP: empty input
        assertParseFailure(parser, "", MESSAGE_MISSING_INDEX);
        // EP: only spaces
        assertParseFailure(parser, "   ", MESSAGE_MISSING_INDEX);
    }

    @Test
    public void parse_moreThanOneArgument_throwsParseException() {
        // Boundary: two indexes, one more than allowed
        assertParseFailure(parser, "1 2", MESSAGE_MULTIPLE_INDEXES);
        // EP: an index followed by extra text
        assertParseFailure(parser, "1 abc", MESSAGE_MULTIPLE_INDEXES);
    }
}
