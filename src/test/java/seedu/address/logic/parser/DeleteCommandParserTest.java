package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.person.Name;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_name_returnsNameDeleteCommand() {
        assertParseSuccess(parser, "n/Alice Tan", new DeleteCommand(new Name("Alice Tan")));
        assertParseSuccess(parser, "  n/Alice Tan  ", new DeleteCommand(new Name("Alice Tan")));
        assertParseFailure(parser, "1 n/Alice Tan", DeleteCommand.MESSAGE_INVALID_TARGET);
        assertParseFailure(parser, "n/Alice n/Bob", DeleteCommand.MESSAGE_INVALID_TARGET);
        assertParseFailure(parser, "n/", "Name cannot be blank.");
    }

    @Test
    public void parseCommand_nameThroughApplicationParser_returnsNameDeleteCommand() throws Exception {
        assertParseSuccess(new AddressBookParser()::parseCommand, "delete n/Alice Tan",
                new DeleteCommand(new Name("Alice Tan")));
    }

    @Test
    public void parse_quotedNameContainingPrefix_preservesName() {
        assertParseSuccess(parser, "n/\"Ravi n/o Kumar\"",
                new DeleteCommand(new Name("Ravi n/o Kumar")));
        assertParseFailure(parser, "n/\"Ravi n/o Kumar\" n/Alice", DeleteCommand.MESSAGE_INVALID_TARGET);
        assertParseFailure(parser, "n/\"Ravi n/o Kumar", ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTED_VALUE);
    }

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "1 2", new DeleteCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
        assertParseSuccess(parser, "  2   1  ", new DeleteCommand(List.of(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON)));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1 a", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1 0", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "0", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "-1", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "1.5", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "99999999999", Messages.MESSAGE_INVALID_CONTACT_INDEX);
        assertParseFailure(parser, "", Messages.MESSAGE_INVALID_CONTACT_INDEX);
    }

    @Test
    public void parse_duplicateIndex_throwsParseException() {
        assertParseFailure(parser, "1 1", DeleteCommand.MESSAGE_DUPLICATE_INDEX);
        assertParseFailure(parser, "01 1", DeleteCommand.MESSAGE_DUPLICATE_INDEX);
    }
}
