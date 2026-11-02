package seedu.address.logic.parser;

import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ViewCommand object.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ViewCommand
     * and returns a ViewCommand object for execution.
     *
     * @throws ParseException if the user input is missing, has more than one index, or is not a positive index.
     */
    public ViewCommand parse(String args) throws ParseException {
        return new ViewCommand(ParserUtil.parseSingleIndex(args, ViewCommand.COMMAND_FORMAT));
    }

}
