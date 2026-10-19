package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.ContactFilter;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_former_returnsListFormerCommand() {
        assertParseSuccess(parser, " st/former", new ListCommand(ContactFilter.FORMER_STUDENTS));
        assertParseSuccess(parser, "  st/FORMER  ", new ListCommand(ContactFilter.FORMER_STUDENTS));
    }

    @Test
    public void parse_extraText_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " all", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " former", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/former 1", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/formers", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/active", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/former st/former", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " r/student", ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
