package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsSubstringPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "", FindCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "     ", FindCommand.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new NameContainsSubstringPredicate("Alex Tan"));

        // no leading and trailing whitespaces
        assertParseSuccess(parser, "Alex Tan", expectedFindCommand);

        // leading, trailing and multiple internal whitespaces
        assertParseSuccess(parser, " \n Alex \n \t Tan  \t", expectedFindCommand);
    }

}
