package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Requires explicit confirmation before creating a command that clears the entire roster. */
public class ClearCommandParser implements Parser<ClearCommand> {
    @Override
    public ClearCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.strip().equalsIgnoreCase(ClearCommand.CONFIRMATION_KEYWORD)) {
            throw new ParseException(ClearCommand.MESSAGE_USAGE);
        }
        return new ClearCommand();
    }
}
