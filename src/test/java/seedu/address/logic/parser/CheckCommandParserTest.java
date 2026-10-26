package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CheckCommand;

public class CheckCommandParserTest {
    private final CheckCommandParser parser = new CheckCommandParser();

    @Test
    public void parse_supportedSections_acceptsCaseAndSurroundingWhitespace() {
        assertParseSuccess(parser, "", new CheckCommand(CheckCommand.Section.ALL));
        assertParseSuccess(parser, " Guardians ", new CheckCommand(CheckCommand.Section.GUARDIANS));
        assertParseSuccess(parser, "details", new CheckCommand(CheckCommand.Section.DETAILS));
        assertParseSuccess(parser, "DUPLICATES", new CheckCommand(CheckCommand.Section.DUPLICATES));
        assertParseSuccess(new AddressBookParser()::parseCommand, "check details",
                new CheckCommand(CheckCommand.Section.DETAILS));
    }

    @Test
    public void parse_unknownOrMultipleSections_rejectsArguments() {
        assertParseFailure(parser, "all", CheckCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "guardians details", CheckCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Alex", CheckCommand.MESSAGE_USAGE);
    }
}
