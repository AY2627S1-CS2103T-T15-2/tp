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

    public static final String MESSAGE_MISSING_NAME = "Missing required field: n/NAME.";
    public static final String MESSAGE_MISSING_ROLE = "Missing required field: r/ROLE.";
    public static final String MESSAGE_MISSING_CONTACT_METHOD =
            "Missing contact method. Specify p/PHONE, e/EMAIL, or both.";
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
        String nameValue = argMultimap.getValue(PREFIX_NAME).get();
        if (!nameValue.startsWith("\"") && nameValue.contains("/")) {
            throw new ParseException(AddCommand.MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH);
        }
        Name name = ParserUtil.parseName(nameValue);
        Optional<Phone> phone = parseOptionalPhone(argMultimap);
        Optional<Email> email = parseOptionalEmail(argMultimap);
        Role role = ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());

        if (role == Role.GUARDIAN && phone.isEmpty() && email.isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_CONTACT_METHOD_REQUIRED);
        }

        Person person = new Person(name, phone, email, role);

        return new AddCommand(person);
    }

    private static Optional<Phone> parseOptionalPhone(ArgumentMultimap argumentMultimap) throws ParseException {
        Optional<String> phone = argumentMultimap.getValue(PREFIX_PHONE);
        return phone.isPresent() ? Optional.of(ParserUtil.parsePhone(phone.get())) : Optional.empty();
    }

    private static Optional<Email> parseOptionalEmail(ArgumentMultimap argumentMultimap) throws ParseException {
        Optional<String> email = argumentMultimap.getValue(PREFIX_EMAIL);
        return email.isPresent() ? Optional.of(ParserUtil.parseEmail(email.get())) : Optional.empty();
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
