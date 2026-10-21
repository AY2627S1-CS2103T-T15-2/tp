package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(trimmedArgs, CliSyntax.PREFIX_NAME);
        if (arguments.getValue(CliSyntax.PREFIX_NAME).isPresent()) {
            if (!arguments.getPreamble().isEmpty()
                    || arguments.getAllValues(CliSyntax.PREFIX_NAME).size() != 1) {
                throw new ParseException(DeleteCommand.MESSAGE_INVALID_TARGET);
            }
            return new DeleteCommand(ParserUtil.parseName(arguments.getValue(CliSyntax.PREFIX_NAME).get()));
        }
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX);
        }

        List<Index> indexes = new ArrayList<>();
        Set<Integer> seenIndexes = new HashSet<>();
        for (String indexText : trimmedArgs.split("\\s+")) {
            Index index;
            try {
                index = ParserUtil.parseIndex(indexText);
            } catch (ParseException pe) {
                throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX, pe);
            }
            if (!seenIndexes.add(index.getZeroBased())) {
                throw new ParseException(DeleteCommand.MESSAGE_DUPLICATE_INDEX);
            }
            indexes.add(index);
        }
        return new DeleteCommand(indexes);
    }

}
