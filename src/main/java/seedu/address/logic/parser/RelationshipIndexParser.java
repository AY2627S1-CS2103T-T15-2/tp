package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses one required relationship index, rejecting additional or repeated arguments.
 */
final class RelationshipIndexParser {
    private RelationshipIndexParser() {
    }

    static Index parse(String args, Prefix prefix, String usage) throws ParseException {
        requireNonNull(args);
        String normalized = " " + args.trim().replaceAll("\\s+", " ");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalized, prefix);
        if (!arguments.getPreamble().isEmpty() || arguments.getAllValues(prefix).size() != 1) {
            throw new ParseException("Invalid " + usage.split(" ")[0] + " command. Use: " + usage
                    + ", with lowercase " + prefix + " given once.");
        }
        return ParserUtil.parseSingleIndex(arguments.getValue(prefix).orElseThrow(), usage);
    }
}
