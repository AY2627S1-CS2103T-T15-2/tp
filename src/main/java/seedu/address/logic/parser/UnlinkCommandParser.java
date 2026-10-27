package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student index for a unlink command.
 */
public class UnlinkCommandParser implements Parser<UnlinkCommand> {
    @Override
    public UnlinkCommand parse(String args) throws ParseException {
        return new UnlinkCommand(RelationshipIndexParser.parse(args, PREFIX_STUDENT,
                UnlinkCommand.MESSAGE_USAGE));
    }
}
