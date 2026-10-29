package seedu.address.logic.parser;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ArchiveCommand object.
 */
public class ArchiveCommandParser implements Parser<ArchiveCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ArchiveCommand
     * and returns an ArchiveCommand object for execution.
     *
     * @throws ParseException if the user input is not a single positive index.
     */
    public ArchiveCommand parse(String args) throws ParseException {
        return new ArchiveCommand(ParserUtil.parseSingleIndex(args, ArchiveCommand.COMMAND_FORMAT));
    }

}
