package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;

import seedu.address.logic.commands.ChildrenCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed guardian index for a children command.
 */
public class ChildrenCommandParser implements Parser<ChildrenCommand> {
    @Override
    public ChildrenCommand parse(String args) throws ParseException {
        return new ChildrenCommand(RelationshipIndexParser.parse(args, PREFIX_GUARDIAN,
                ChildrenCommand.MESSAGE_USAGE));
    }
}
