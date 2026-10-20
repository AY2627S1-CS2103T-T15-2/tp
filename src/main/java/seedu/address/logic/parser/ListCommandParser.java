package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    private static final List<Prefix> LIST_PREFIXES = List.of(PREFIX_ROLE, PREFIX_STATUS);
    // Matches any word ending in "/" at the start of a token, such as "r/", "n/" or "xyz/".
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution. No arguments lists guardians and active students.
     * {@code r/ROLE} keeps only contacts with that role, and {@code st/former}, ignoring the letter case
     * of "former", lists former students instead of active ones.
     *
     * @throws ParseException if the user input contains unknown text, a repeated parameter,
     *     an invalid role or status, or the combination of guardians and former students.
     */
    public ListCommand parse(String args) throws ParseException {
        throwIfUnknownPrefix(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ROLE, PREFIX_STATUS);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
        }
        for (Prefix prefix : LIST_PREFIXES) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(ListCommand.MESSAGE_REPEATED_PARAMETER);
            }
        }

        Optional<Role> role = parseRole(argMultimap.getValue(PREFIX_ROLE));
        StudentStatus status = parseStatus(argMultimap.getValue(PREFIX_STATUS));
        if (role.equals(Optional.of(Role.GUARDIAN)) && status == StudentStatus.FORMER) {
            throw new ParseException(ListCommand.MESSAGE_GUARDIAN_NOT_FORMER);
        }
        return new ListCommand(new ContactFilter(role, status));
    }

    private static void throwIfUnknownPrefix(String args) throws ParseException {
        Matcher prefixMatcher = PARAMETER_PREFIX.matcher(args);
        while (prefixMatcher.find()) {
            String word = prefixMatcher.group(1);
            if (LIST_PREFIXES.stream().noneMatch(prefix -> prefix.getPrefix().equals(word))) {
                throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
            }
        }
    }

    private static Optional<Role> parseRole(Optional<String> value) throws ParseException {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (value.get().isBlank()) {
            throw new ParseException(AddCommandParser.MESSAGE_EMPTY_ROLE);
        }
        return Optional.of(ParserUtil.parseRole(value.get()));
    }

    private static StudentStatus parseStatus(Optional<String> value) throws ParseException {
        if (value.isEmpty()) {
            return StudentStatus.ACTIVE;
        }
        if (value.get().trim().equalsIgnoreCase(ListCommand.FORMER_STATUS)) {
            return StudentStatus.FORMER;
        }
        throw new ParseException(ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
