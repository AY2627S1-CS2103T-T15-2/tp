package seedu.address.logic.parser;

import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new UnarchiveCommand object.
 */
public class UnarchiveCommandParser implements Parser<UnarchiveCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the UnarchiveCommand
     * and returns an UnarchiveCommand object for execution.
     *
     * @throws ParseException if the user input is not a single positive index.
     */
    public UnarchiveCommand parse(String args) throws ParseException {
        return new UnarchiveCommand(ParserUtil.parseSingleIndex(args, UnarchiveCommand.COMMAND_FORMAT));
    }

}
