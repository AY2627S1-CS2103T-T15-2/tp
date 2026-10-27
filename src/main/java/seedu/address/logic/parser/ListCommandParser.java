package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Level;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    private static final List<Prefix> LIST_PREFIXES =
            List.of(PREFIX_ROLE, PREFIX_STATUS, PREFIX_LEVEL, PREFIX_SUBJECT);
    private static final List<String> STATUS_VALUES =
            List.of(ListCommand.ACTIVE_STATUS, ListCommand.FORMER_STATUS, ListCommand.ALL_STATUS);
    // Matches any word ending in "/" at the start of a token, such as "r/", "n/" or "xyz/".
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution. No arguments lists guardians and active students.
     * {@code r/ROLE} keeps only contacts with that role, and {@code st/STATUS} chooses active contacts,
     * former students or all contacts, ignoring letter case. {@code lv/LEVEL} and {@code sub/SUBJECT} keep only
     * students with that level and subject, ignoring letter case and spaces.
     *
     * @throws ParseException if the user input contains unknown text, a repeated parameter,
     *     an invalid role, status, level or subject, or a guardian with former status, a level or a subject.
     */
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithQuotedValue(args, PREFIX_SUBJECT,
                PREFIX_ROLE, PREFIX_STATUS, PREFIX_LEVEL, PREFIX_SUBJECT);
        throwIfUnknownPrefix(argMultimap.getPreamble());
        for (Prefix prefix : LIST_PREFIXES) {
            for (String value : argMultimap.getAllValues(prefix)) {
                String checkedValue = prefix.equals(PREFIX_SUBJECT)
                        ? ParserUtil.textOutsideQuotedValue(value) : value;
                throwIfUnknownPrefix(prefix.getPrefix() + checkedValue);
            }
        }
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
        Optional<Level> level = parseLevel(argMultimap.getValue(PREFIX_LEVEL));
        Optional<Subject> subject = parseSubject(argMultimap.getValue(PREFIX_SUBJECT));
        boolean isGuardianOnly = role.equals(Optional.of(Role.GUARDIAN));
        if (isGuardianOnly && status.equals(Optional.of(StudentStatus.FORMER))) {
            throw new ParseException(ListCommand.MESSAGE_GUARDIAN_NOT_FORMER);
        }
        if (isGuardianOnly && (level.isPresent() || subject.isPresent())) {
            throw new ParseException(ListCommand.MESSAGE_GUARDIAN_NO_STUDENT_DETAIL);
        }
        return new ListCommand(new ContactFilter(role, status, level, subject));
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

    private static Optional<Level> parseLevel(Optional<String> value) throws ParseException {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ParserUtil.parseLevel(value.get()));
    }

    private static Optional<Subject> parseSubject(Optional<String> value) throws ParseException {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ParserUtil.parseSubject(value.get()));
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
        boolean hasParameters = LIST_PREFIXES.stream().anyMatch(prefix -> argMultimap.getValue(prefix).isPresent());
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
