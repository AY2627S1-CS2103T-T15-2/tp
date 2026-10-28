package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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
                ArgumentTokenizer.tokenizeWithQuotedValues(args, List.of(PREFIX_NAME, PREFIX_SUBJECT), PREFIX_NAME,
                        PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL, PREFIX_SUBJECT);

        verifyNoUnknownPrefixes(argMultimap);
        verifyNoRepeatedPrefixes(argMultimap);
        Index index = parseIndex(argMultimap.getPreamble());

        EditPersonDescriptor descriptor = parseEditedFields(argMultimap);
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, descriptor);
    }

    /** Parses values in command-format order so the first reported field error remains consistent. */
    private static EditPersonDescriptor parseEditedFields(ArgumentMultimap argMultimap) throws ParseException {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).orElseThrow()));
        }
        parseOptionalField(argMultimap.getValue(PREFIX_PHONE), ParserUtil::parsePhone,
                descriptor::setPhone, descriptor::removePhone);
        parseOptionalField(argMultimap.getValue(PREFIX_EMAIL), ParserUtil::parseEmail,
                descriptor::setEmail, descriptor::removeEmail);
        parseOptionalField(argMultimap.getValue(PREFIX_LEVEL), ParserUtil::parseLevel,
                descriptor::setLevel, descriptor::removeLevel);
        parseOptionalField(argMultimap.getValue(PREFIX_SUBJECT), ParserUtil::parseSubject,
                descriptor::setSubject, descriptor::removeSubject);
        return descriptor;
    }

    /** Keeps an omitted field unchanged, removes NONE, or validates and sets a supplied value. */
    private static <T> void parseOptionalField(Optional<String> value, FieldParser<T> parser,
            Consumer<T> setter, Runnable remover) throws ParseException {
        if (value.isEmpty()) {
            return;
        }
        String fieldValue = value.orElseThrow();
        if (isRemoveValue(fieldValue)) {
            remover.run();
        } else {
            setter.accept(parser.parse(fieldValue));
        }
    }

    private static boolean isRemoveValue(String value) {
        return value.trim().equalsIgnoreCase(EditCommand.REMOVE_FIELD_VALUE);
    }

    /**
     * Throws if the preamble or any value contains a word ending in {@code /} that is not an edit prefix.
     * Text inside a quoted name or subject is skipped, so it may contain such words.
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
        return prefix.equals(PREFIX_NAME) || prefix.equals(PREFIX_SUBJECT)
                ? ParserUtil.textOutsideQuotedValue(value) : value;
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

    /** Validates one field value while preserving the parser's checked error. */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }
}
