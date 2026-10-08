package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses student and guardian indexes for a link command.
 */
public class LinkCommandParser implements Parser<LinkCommand> {

    private static final Prefix PREFIX_STUDENT = new Prefix("s/");
    private static final Prefix PREFIX_GUARDIAN = new Prefix("g/");
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    @Override
    public LinkCommand parse(String args) throws ParseException {
        String normalized = " " + args.trim().replaceAll("\\s+", " ");
        Matcher prefixes = PARAMETER_PREFIX.matcher(normalized);
        while (prefixes.find()) {
            String prefix = prefixes.group(1);
            if (!prefix.equals("s/") && !prefix.equals("g/")) {
                throw new ParseException(LinkCommand.MESSAGE_UNKNOWN_PARAMETER);
            }
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalized, PREFIX_STUDENT, PREFIX_GUARDIAN);
        if (arguments.getAllValues(PREFIX_STUDENT).size() > 1
                || arguments.getAllValues(PREFIX_GUARDIAN).size() > 1) {
            throw new ParseException(LinkCommand.MESSAGE_REPEATED_PARAMETER);
        }
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_STUDENT).isEmpty()
                || arguments.getValue(PREFIX_GUARDIAN).isEmpty()) {
            throw new ParseException(LinkCommand.MESSAGE_INVALID_COMMAND);
        }
        Index student = parseIndex(arguments.getValue(PREFIX_STUDENT).get(), LinkCommand.MESSAGE_INVALID_STUDENT_INDEX);
        Index guardian = parseIndex(arguments.getValue(PREFIX_GUARDIAN).get(),
                LinkCommand.MESSAGE_INVALID_GUARDIAN_INDEX);
        return new LinkCommand(student, guardian);
    }

    private Index parseIndex(String value, String errorMessage) throws ParseException {
        if (value.contains(" ")) {
            throw new ParseException(LinkCommand.MESSAGE_INVALID_COMMAND);
        }
        if (!value.matches("[0-9]+")) {
            throw new ParseException(errorMessage);
        }
        try {
            int index = Integer.parseInt(value);
            if (index <= 0) {
                throw new ParseException(errorMessage);
            }
            return Index.fromOneBased(index);
        } catch (NumberFormatException exception) {
            throw new ParseException(errorMessage, exception);
        }
    }
}
