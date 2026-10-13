package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

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

    public static final String MESSAGE_MISSING_NAME = "Missing required field: n/NAME.\n\n"
            + AddCommand.MESSAGE_USAGE;
    public static final String MESSAGE_MISSING_ROLE = "Missing required field: r/ROLE.\n\n"
            + AddCommand.MESSAGE_USAGE;
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
        if (!argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_ROLE);
        }
        Role role = parseRole(argMultimap.getValue(PREFIX_ROLE).orElseThrow());
        if (!argMultimap.getValue(PREFIX_NAME).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_NAME);
        }
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
        }
        Name name = parseName(argMultimap.getValue(PREFIX_NAME).orElseThrow());
        Optional<Phone> phone = Optional.empty();
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            phone = Optional.of(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).orElseThrow()));
        }
        Optional<Email> email = Optional.empty();
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            email = Optional.of(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).orElseThrow()));
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
