package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution. No arguments lists guardians and active students,
     * and "former", ignoring letter case, lists former students.
     *
     * @throws ParseException if the user input contains any other text after the command word.
     */
    public ListCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new ListCommand();
        }
        if (trimmedArgs.equalsIgnoreCase(ListCommand.FORMER_KEYWORD)) {
            return new ListCommand(true);
        }
        throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
