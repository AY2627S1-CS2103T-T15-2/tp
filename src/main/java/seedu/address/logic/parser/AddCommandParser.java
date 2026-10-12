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
        if (argMultimap.getValue(PREFIX_PHONE).isEmpty() && argMultimap.getValue(PREFIX_EMAIL).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_CONTACT_METHOD);
        }
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Optional<Phone> phone = parseOptionalPhone(argMultimap);
        Optional<Email> email = parseOptionalEmail(argMultimap);
        Role role = ParserUtil.parseRole(argMultimap.getValue(PREFIX_ROLE).get());

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

}
