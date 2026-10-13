package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final String MESSAGE_MISSING_NAME = "Missing required field: n/NAME.";
    public static final String MESSAGE_MISSING_ROLE = "Missing required field: r/ROLE.";
    public static final String MESSAGE_EMPTY_ROLE =
            "Empty role parameter: Role cannot be empty when r/ is specified. Use student or guardian.";
    public static final String MESSAGE_UNEXPECTED_TEXT = "Unexpected text outside a field. "
            + "Use n/NAME, r/ROLE, p/PHONE, or e/EMAIL.";

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        Optional<String> parameterError = getParameterError(args);
        if (parameterError.isPresent()) {
            throw new ParseException(parameterError.get());
        }

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeWithQuotedValue(args, PREFIX_NAME, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_ROLE);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ROLE);
        if (!argMultimap.getValue(PREFIX_NAME).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_NAME);
        }
        if (!argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_ROLE);
        }
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
        }
        Name name = null;
        Role role = null;
        Optional<Phone> phone = Optional.empty();
        Optional<Email> email = Optional.empty();
        for (Prefix prefix : getRecognizedPrefixesInOrder(args)) {
            String value = argMultimap.getValue(prefix).orElseThrow();
            if (prefix.equals(PREFIX_NAME)) {
                name = parseName(value);
            } else if (prefix.equals(PREFIX_ROLE)) {
                role = parseRole(value);
            } else if (prefix.equals(PREFIX_PHONE)) {
                phone = Optional.of(ParserUtil.parsePhone(value));
            } else if (prefix.equals(PREFIX_EMAIL)) {
                email = Optional.of(ParserUtil.parseEmail(value));
            }
        }

        if (role == Role.GUARDIAN && phone.isEmpty() && email.isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_CONTACT_METHOD_REQUIRED);
        }

        Person person = new Person(name, phone, email, role);

        return new AddCommand(person);
    }

    private static Name parseName(String value) throws ParseException {
        if (!value.startsWith("\"") && value.contains("/")) {
            throw new ParseException(AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        }
        return ParserUtil.parseName(value);
    }

    private static Role parseRole(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_ROLE);
        }
        return ParserUtil.parseRole(value);
    }

    /**
     * Returns the recognized parameter prefixes in the order they appear outside a quoted name.
     */
    private static List<Prefix> getRecognizedPrefixesInOrder(String args) {
        List<Prefix> prefixes = new ArrayList<>();
        boolean isInsideQuotedName = false;
        for (int i = 0; i < args.length(); i++) {
            if (isInsideQuotedName) {
                if (args.charAt(i) == '"') {
                    isInsideQuotedName = false;
                }
                continue;
            }
            if (args.charAt(i) != ' ') {
                continue;
            }

            int prefixStart = i + 1;
            Prefix prefix = getRecognizedPrefixAt(args, prefixStart);
            if (prefix == null) {
                continue;
            }
            prefixes.add(prefix);
            if (prefix.equals(PREFIX_NAME)) {
                int valueStart = prefixStart + PREFIX_NAME.getPrefix().length();
                while (valueStart < args.length() && args.charAt(valueStart) == ' ') {
                    valueStart++;
                }
                if (valueStart < args.length() && args.charAt(valueStart) == '"') {
                    isInsideQuotedName = true;
                    i = valueStart;
                }
            }
        }
        return prefixes;
    }

    private static Prefix getRecognizedPrefixAt(String args, int prefixStart) {
        if (args.startsWith(PREFIX_ROLE.getPrefix(), prefixStart)) {
            return PREFIX_ROLE;
        } else if (args.startsWith(PREFIX_NAME.getPrefix(), prefixStart)) {
            return PREFIX_NAME;
        } else if (args.startsWith(PREFIX_PHONE.getPrefix(), prefixStart)) {
            return PREFIX_PHONE;
        } else if (args.startsWith(PREFIX_EMAIL.getPrefix(), prefixStart)) {
            return PREFIX_EMAIL;
        }
        return null;
    }

    /**
     * Returns whether the arguments contain an unrecognized parameter prefix. A prefix is a slash
     * following a whitespace-delimited token, outside a quoted value.
     */
    private static Optional<String> getParameterError(String args) {
        int tokenStart = 0;
        boolean isInsideQuotedValue = false;
        boolean tokenHasSlash = false;
        Prefix currentField = null;
        for (int i = 0; i < args.length(); i++) {
            char current = args.charAt(i);
            if (current == '"') {
                isInsideQuotedValue = !isInsideQuotedValue;
            } else if (!isInsideQuotedValue && Character.isWhitespace(current)) {
                tokenStart = i + 1;
                tokenHasSlash = false;
            } else if (!isInsideQuotedValue && current == '/' && i > tokenStart && !tokenHasSlash) {
                tokenHasSlash = true;
                String prefix = args.substring(tokenStart, i + 1);
                Prefix recognizedPrefix = getRecognizedPrefix(prefix);
                if (recognizedPrefix == null) {
                    String errorMessage = currentField == PREFIX_NAME
                            ? AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH
                            : AddCommand.MESSAGE_INVALID_PARAMETER;
                    return Optional.of(errorMessage);
                }
                currentField = recognizedPrefix;
            }
        }
        return Optional.empty();
    }

    private static Prefix getRecognizedPrefix(String prefix) {
        if (PREFIX_NAME.getPrefix().equals(prefix)) {
            return PREFIX_NAME;
        } else if (PREFIX_PHONE.getPrefix().equals(prefix)) {
            return PREFIX_PHONE;
        } else if (PREFIX_EMAIL.getPrefix().equals(prefix)) {
            return PREFIX_EMAIL;
        } else if (PREFIX_ROLE.getPrefix().equals(prefix)) {
            return PREFIX_ROLE;
        }
        return null;
    }

}
