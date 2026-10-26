package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");
    private static final List<Prefix> EDIT_PREFIXES =
            List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL, PREFIX_SUBJECT);

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeWithQuotedValue(args, PREFIX_NAME, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_LEVEL, PREFIX_SUBJECT);

        verifyNoUnknownPrefixes(argMultimap);
        verifyNoRepeatedPrefixes(argMultimap);
        Index index = parseIndex(argMultimap.getPreamble());

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            String phone = argMultimap.getValue(PREFIX_PHONE).get();
            if (phone.trim().equalsIgnoreCase(EditCommand.REMOVE_FIELD_VALUE)) {
                editPersonDescriptor.removePhone();
            } else {
                editPersonDescriptor.setPhone(ParserUtil.parsePhone(phone));
            }
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            String email = argMultimap.getValue(PREFIX_EMAIL).get();
            if (email.trim().equalsIgnoreCase(EditCommand.REMOVE_FIELD_VALUE)) {
                editPersonDescriptor.removeEmail();
            } else {
                editPersonDescriptor.setEmail(ParserUtil.parseEmail(email));
            }
        }

        if (argMultimap.getValue(PREFIX_LEVEL).isPresent()) {
            String level = argMultimap.getValue(PREFIX_LEVEL).get();
            if (isRemoveValue(level)) {
                editPersonDescriptor.removeLevel();
            } else {
                editPersonDescriptor.setLevel(ParserUtil.parseLevel(level));
            }
        }
        if (argMultimap.getValue(PREFIX_SUBJECT).isPresent()) {
            String subject = argMultimap.getValue(PREFIX_SUBJECT).get();
            if (isRemoveValue(subject)) {
                editPersonDescriptor.removeSubject();
            } else {
                editPersonDescriptor.setSubject(ParserUtil.parseSubject(subject));
            }
        }

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    private static boolean isRemoveValue(String value) {
        return value.trim().equalsIgnoreCase(EditCommand.REMOVE_FIELD_VALUE);
    }

    /**
     * Throws if the preamble or any value contains a word ending in {@code /} that is not an edit prefix.
     * Text inside a quoted name is skipped, so it may contain such words.
     */
    private static void verifyNoUnknownPrefixes(ArgumentMultimap argMultimap) throws ParseException {
        throwIfUnknownPrefix(argMultimap.getPreamble());
        for (Prefix prefix : EDIT_PREFIXES) {
            for (String value : argMultimap.getAllValues(prefix)) {
                throwIfUnknownPrefix(prefix.getPrefix() + extractTextOutsideQuotes(prefix, value));
            }
        }
    }

    private static String extractTextOutsideQuotes(Prefix prefix, String value) {
        String trimmedValue = value.trim();
        if (!prefix.equals(PREFIX_NAME) || !trimmedValue.startsWith("\"")) {
            return value;
        }
        // The tokenizer has already rejected a quoted name without a closing quotation mark.
        int closingQuote = trimmedValue.indexOf('"', 1);
        return " " + trimmedValue.substring(closingQuote + 1);
    }

    private static void throwIfUnknownPrefix(String text) throws ParseException {
        Matcher prefixMatcher = PARAMETER_PREFIX.matcher(text);
        while (prefixMatcher.find()) {
            String word = prefixMatcher.group(1);
            if (EDIT_PREFIXES.stream().noneMatch(prefix -> prefix.getPrefix().equals(word))) {
                throw new ParseException(EditCommand.MESSAGE_UNKNOWN_PARAMETER);
            }
        }
    }

    private static void verifyNoRepeatedPrefixes(ArgumentMultimap argMultimap) throws ParseException {
        for (Prefix prefix : EDIT_PREFIXES) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(EditCommand.MESSAGE_REPEATED_PARAMETER);
            }
        }
    }

    private static Index parseIndex(String preamble) throws ParseException {
        if (preamble.isEmpty() || preamble.matches(".*\\s.*")) {
            throw new ParseException(EditCommand.MESSAGE_INVALID_COMMAND);
        }
        try {
            return ParserUtil.parseIndex(preamble);
        } catch (ParseException pe) {
            throw new ParseException(Messages.MESSAGE_INVALID_CONTACT_INDEX, pe);
        }
    }
}
