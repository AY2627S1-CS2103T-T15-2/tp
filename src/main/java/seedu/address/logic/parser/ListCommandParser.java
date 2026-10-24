package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.List;
import java.util.Locale;
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
    private static final List<String> STATUS_VALUES =
            List.of(ListCommand.ACTIVE_STATUS, ListCommand.FORMER_STATUS, ListCommand.ALL_STATUS);
    // Matches any word ending in "/" at the start of a token, such as "r/", "n/" or "xyz/".
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution. No arguments lists guardians and active students.
     * {@code r/ROLE} keeps only contacts with that role, and {@code st/STATUS} chooses active contacts,
     * former students or all contacts, ignoring letter case.
     *
     * @throws ParseException if the user input contains unknown text, a repeated parameter,
     *     an invalid role or status, or the combination of guardians and former students.
     */
    public ListCommand parse(String args) throws ParseException {
        throwIfUnknownPrefix(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ROLE, PREFIX_STATUS);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(getInvalidFormatMessage(argMultimap));
        }
        for (Prefix prefix : LIST_PREFIXES) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(ListCommand.MESSAGE_REPEATED_PARAMETER);
            }
        }

        Optional<Role> role = parseRole(argMultimap.getValue(PREFIX_ROLE));
        Optional<StudentStatus> status = parseStatus(argMultimap.getValue(PREFIX_STATUS));
        if (role.equals(Optional.of(Role.GUARDIAN)) && status.equals(Optional.of(StudentStatus.FORMER))) {
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

    /**
     * Returns the status chosen by {@code value}: {@code ACTIVE} if {@code value} is empty, or an empty
     * {@code Optional} for "all", which matches every status.
     *
     * @throws ParseException if {@code value} is blank or not "active", "former" or "all".
     */
    private static Optional<StudentStatus> parseStatus(Optional<String> value) throws ParseException {
        if (value.isEmpty()) {
            return Optional.of(StudentStatus.ACTIVE);
        }
        String status = value.get().trim().toLowerCase(Locale.ROOT);
        if (status.equals(ListCommand.ACTIVE_STATUS)) {
            return Optional.of(StudentStatus.ACTIVE);
        }
        if (status.equals(ListCommand.FORMER_STATUS)) {
            return Optional.of(StudentStatus.FORMER);
        }
        if (status.equals(ListCommand.ALL_STATUS)) {
            return Optional.empty();
        }
        throw new ParseException(ListCommand.MESSAGE_INVALID_STATUS);
    }

    /**
     * Returns the message for text before the first prefix. If the whole input is a single role or status
     * value, such as "former" or "student", the message suggests the command with the matching prefix.
     */
    private static String getInvalidFormatMessage(ArgumentMultimap argMultimap) {
        boolean hasParameters = argMultimap.getValue(PREFIX_ROLE).isPresent()
                || argMultimap.getValue(PREFIX_STATUS).isPresent();
        if (hasParameters) {
            return ListCommand.MESSAGE_INVALID_FORMAT;
        }
        String word = argMultimap.getPreamble().trim().toLowerCase(Locale.ROOT);
        if (STATUS_VALUES.contains(word)) {
            return String.format(ListCommand.MESSAGE_SUGGESTION, ListCommand.COMMAND_WORD + " " + PREFIX_STATUS + word);
        }
        if (Role.isValidRole(word)) {
            return String.format(ListCommand.MESSAGE_SUGGESTION, ListCommand.COMMAND_WORD + " " + PREFIX_ROLE + word);
        }
        return ListCommand.MESSAGE_INVALID_FORMAT;
    }
}
