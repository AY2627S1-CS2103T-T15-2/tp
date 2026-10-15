package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a person to the address book.\n"
            + "Parameters: "
            + PREFIX_ROLE + "ROLE "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL]"
            + "\n"
            + "Students may omit phone and email; guardians must provide at least one.\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_ROLE + "student "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact already exists.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE = "Warning: A contact with the same role and name exists. "
            + "This contact may be a duplicate, but it was added.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_PHONE = "Warning: A contact with the same role and name, "
            + "and a phone number matching after ignoring spaces and hyphens exists. "
            + "This contact may be a duplicate, but it was added.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_EMAIL = "Warning: A contact with the same role and name, "
            + "and the same email address exists. This contact may be a duplicate, but it was added.";
    public static final String MESSAGE_CONTACT_METHOD_REQUIRED =
            "A guardian must have at least one contact method: phone or email.";
    public static final String MESSAGE_INVALID_PARAMETER =
            "Invalid parameter. Valid parameters are r/ROLE, n/NAME, p/PHONE, and e/EMAIL.";
    public static final String MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH = MESSAGE_INVALID_PARAMETER
            + " If '/' is part of the name, enclose the entire name in double quotes.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}.
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (toAdd.isGuardianWithoutContactMethod()) {
            throw new CommandException(MESSAGE_CONTACT_METHOD_REQUIRED);
        }

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        boolean hasSimilarPhone = model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> toAdd.hasSameNameAndRole(person) && toAdd.hasSamePhoneIgnoringFormatting(person));
        boolean hasSameEmail = model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> toAdd.hasSameNameAndRole(person) && toAdd.hasSameEmailAddress(person));
        boolean hasSharedEmptyContactField = model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> toAdd.hasSameNameAndRole(person) && hasSharedEmptyContactField(person));

        model.addPerson(toAdd);
        String feedback = String.format(MESSAGE_SUCCESS, Messages.format(toAdd));
        if (hasSimilarPhone) {
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE_PHONE;
        } else if (hasSameEmail) {
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE_EMAIL;
        } else if (hasSharedEmptyContactField) {
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE;
        }
        return new CommandResult(feedback);
    }

    private boolean hasSharedEmptyContactField(Person otherPerson) {
        return toAdd.getPhone().isEmpty() && otherPerson.getPhone().isEmpty()
                || toAdd.getEmail().isEmpty() && otherPerson.getEmail().isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
