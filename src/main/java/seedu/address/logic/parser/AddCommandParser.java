package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;

/**
 * Parses input arguments and creates a new {@code AddCommand} object.
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final String MESSAGE_MISSING_NAME = "Missing required field: n/NAME.\n\n"
            + AddCommand.MESSAGE_USAGE;
    public static final String MESSAGE_MISSING_ROLE = "Missing required field: r/ROLE.\n\n"
            + AddCommand.MESSAGE_USAGE;
    public static final String MESSAGE_DUPLICATE_PREFIXES =
            "Repeated parameter: %s. Each parameter may appear only once.";
    public static final String MESSAGE_EMPTY_ROLE =
            "Empty role parameter: Role cannot be empty when r/ is specified. Use student or guardian.";
    public static final String MESSAGE_UNEXPECTED_TEXT = "Unexpected text outside a field. "
            + "Use n/NAME, r/ROLE, p/PHONE, e/EMAIL, lv/LEVEL, or sub/SUBJECT.";
    public static final String MESSAGE_GUARDIAN_STUDENT_DETAILS =
            "Only students can have a level or subject. Remove lv/ and sub/ when adding a guardian.";

    /**
     * Parses the given {@code String} of arguments in the context of {@code AddCommand}
     * and returns an {@code AddCommand} object for execution.
     *
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public AddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = tokenizeArguments(args);
        verifyNoDuplicatePrefixes(argMultimap);
        return new AddCommand(parsePerson(argMultimap));
    }

    /**
     * Rejects unknown parameters before tokenizing the supported fields.
     */
    private static ArgumentMultimap tokenizeArguments(String args) throws ParseException {
        String normalizedArguments = normalizeParameterPrefixes(args);
        return ArgumentTokenizer.tokenizeWithQuotedValues(normalizedArguments,
                List.of(PREFIX_NAME, PREFIX_SUBJECT), PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ROLE, PREFIX_LEVEL, PREFIX_SUBJECT);
    }

    /**
     * Parses fields in command-format order, applying role-specific rules before student details.
     */
    private static Person parsePerson(ArgumentMultimap argMultimap) throws ParseException {
        Role role = parseRequiredRole(argMultimap);
        Name name = parseRequiredName(argMultimap);
        Optional<Phone> phone = parseOptionalPhone(argMultimap);
        Optional<Email> email = parseOptionalEmail(argMultimap);
        verifyRoleRequirements(role, phone, email, argMultimap);
        Optional<Level> level = parseOptionalLevel(argMultimap);
        Optional<Subject> subject = parseOptionalSubject(argMultimap);

        return new Person(ContactId.generate(), name, phone, email, role, Optional.empty(),
                StudentStatus.ACTIVE, level, subject);
    }

    private static Role parseRequiredRole(ArgumentMultimap argMultimap) throws ParseException {
        if (!argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_ROLE);
        }
        return parseRole(argMultimap.getValue(PREFIX_ROLE).orElseThrow());
    }

    private static Name parseRequiredName(ArgumentMultimap argMultimap) throws ParseException {
        if (!argMultimap.getValue(PREFIX_NAME).isPresent()) {
            throw new ParseException(MESSAGE_MISSING_NAME);
        }
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_UNEXPECTED_TEXT);
        }
        return parseName(argMultimap.getValue(PREFIX_NAME).orElseThrow());
    }

    private static Optional<Phone> parseOptionalPhone(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            return Optional.of(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).orElseThrow()));
        }
        return Optional.empty();
    }

    private static Optional<Email> parseOptionalEmail(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            return Optional.of(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).orElseThrow()));
        }
        return Optional.empty();
    }

    private static void verifyRoleRequirements(Role role, Optional<Phone> phone, Optional<Email> email,
            ArgumentMultimap argMultimap) throws ParseException {
        if (role == Role.GUARDIAN && phone.isEmpty() && email.isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_CONTACT_METHOD_REQUIRED);
        }

        boolean hasStudentDetails = argMultimap.getValue(PREFIX_LEVEL).isPresent()
                || argMultimap.getValue(PREFIX_SUBJECT).isPresent();
        if (role != Role.STUDENT && hasStudentDetails) {
            throw new ParseException(MESSAGE_GUARDIAN_STUDENT_DETAILS);
        }
    }

    private static Optional<Level> parseOptionalLevel(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_LEVEL).isPresent()) {
            return Optional.of(ParserUtil.parseLevel(argMultimap.getValue(PREFIX_LEVEL).orElseThrow()));
        }
        return Optional.empty();
    }

    private static Optional<Subject> parseOptionalSubject(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_SUBJECT).isPresent()) {
            return Optional.of(ParserUtil.parseSubject(argMultimap.getValue(PREFIX_SUBJECT).orElseThrow()));
        }
        return Optional.empty();
    }

    private static void verifyNoDuplicatePrefixes(ArgumentMultimap argMultimap) throws ParseException {
        Prefix[] prefixes = {PREFIX_ROLE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL, PREFIX_SUBJECT};
        for (Prefix prefix : prefixes) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(String.format(MESSAGE_DUPLICATE_PREFIXES, prefix));
            }
        }
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
     * Returns arguments with recognized prefixes in their canonical case, preserving all field values.
     * Rejects unrecognized parameters in input order before field validation.
     * A prefix is a whitespace-delimited token ending with a slash, outside a quoted name or subject.
     */
    private static String normalizeParameterPrefixes(String args) throws ParseException {
        ArgumentTokenScanner scanner = new ArgumentTokenScanner(args, List.of(PREFIX_NAME, PREFIX_SUBJECT), true);
        StringBuilder normalizedArguments = new StringBuilder(args);
        Prefix currentField = null;
        int currentFieldValueStart = 0;
        while (scanner.hasNext()) {
            String prefix = scanner.getParameterPrefix();
            if (prefix != null) {
                Prefix recognizedPrefix = getRecognizedPrefix(prefix);
                if (recognizedPrefix == null) {
                    String currentFieldValue = args.substring(currentFieldValueStart, scanner.getTokenStart());
                    throw new ParseException(formatUnknownParameterError(prefix, currentField, currentFieldValue));
                }
                normalizedArguments.replace(scanner.getTokenStart(), scanner.getPosition() + 1,
                        recognizedPrefix.getPrefix());
                currentField = recognizedPrefix;
                currentFieldValueStart = scanner.getPosition() + 1;
            }
            scanner.advance();
        }
        return normalizedArguments.toString();
    }

    private static String formatUnknownParameterError(String prefix, Prefix currentField, String currentFieldValue) {
        String errorMessage = String.format(AddCommand.MESSAGE_UNKNOWN_PARAMETER, prefix);
        boolean isNameValueQuoted = currentFieldValue.stripLeading().startsWith("\"");
        if (currentField == PREFIX_NAME && !isNameValueQuoted) {
            errorMessage += AddCommand.MESSAGE_NAME_SLASH_HINT;
        }
        return errorMessage;
    }

    private static Prefix getRecognizedPrefix(String prefix) {
        if (PREFIX_NAME.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_NAME;
        } else if (PREFIX_PHONE.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_PHONE;
        } else if (PREFIX_EMAIL.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_EMAIL;
        } else if (PREFIX_ROLE.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_ROLE;
        } else if (PREFIX_LEVEL.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_LEVEL;
        } else if (PREFIX_SUBJECT.getPrefix().equalsIgnoreCase(prefix)) {
            return PREFIX_SUBJECT;
        }
        return null;
    }
}
