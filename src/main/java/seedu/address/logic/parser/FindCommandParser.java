package seedu.address.logic.parser;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsSubstringPredicate;

/**
 * Parses input arguments and creates a new FindCommand object.
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * Leading and trailing whitespace is removed, and consecutive internal whitespace
     * is reduced to a single space.
     *
     * @throws ParseException if the search term is empty after removing whitespace.
     */
    public FindCommand parse(String args) throws ParseException {
        String searchTerm = args.trim().replaceAll("\\s+", " ");
        if (searchTerm.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_FORMAT);
        }

        return new FindCommand(new NameContainsSubstringPredicate(searchTerm));
    }

}
