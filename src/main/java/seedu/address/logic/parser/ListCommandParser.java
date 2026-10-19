package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Optional;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilter;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution. No arguments lists guardians and active students,
     * and {@code st/former}, ignoring the letter case of "former", lists former students.
     *
     * @throws ParseException if the user input contains any other text after the command word.
     */
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STATUS);
        if (!argMultimap.getPreamble().isEmpty() || argMultimap.getAllValues(PREFIX_STATUS).size() > 1) {
            throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
        }

        Optional<String> status = argMultimap.getValue(PREFIX_STATUS);
        if (status.isEmpty()) {
            return new ListCommand();
        }
        if (status.get().trim().equalsIgnoreCase(ListCommand.FORMER_STATUS)) {
            return new ListCommand(ContactFilter.FORMER_STUDENTS);
        }
        throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
