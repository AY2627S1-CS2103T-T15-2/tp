package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_CONTACTS;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a student or guardian contact to TutorRoster.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student or guardian contact.\n"
            + "Parameters: "
            + PREFIX_ROLE + "ROLE "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_LEVEL + "LEVEL] "
            + "[" + PREFIX_SUBJECT + "SUBJECT]"
            + "\n"
            + "Students may omit phone and email; guardians must provide at least one.\n"
            + "Only students can have a level and subject.\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_ROLE + "student "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_LEVEL + "Sec 3 "
            + PREFIX_SUBJECT + "Math";

    public static final String MESSAGE_SUCCESS = "Added contact: %1$s [%2$s].";
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
            "Invalid parameter. Valid parameters are r/ROLE, n/NAME, p/PHONE, e/EMAIL, lv/LEVEL, "
                    + "and sub/SUBJECT.";
    public static final String MESSAGE_UNKNOWN_PARAMETER =
            "Invalid parameter: %s. Valid parameters are r/ROLE, n/NAME, p/PHONE, e/EMAIL, lv/LEVEL, "
                    + "and sub/SUBJECT.";
    public static final String MESSAGE_NAME_SLASH_HINT =
            " If '/' is part of the name, enclose the entire name in double quotes.";
    public static final String MESSAGE_INVALID_PARAMETER_OR_NAME_SLASH = MESSAGE_INVALID_PARAMETER
            + MESSAGE_NAME_SLASH_HINT;

    private static final Logger logger = LogsCenter.getLogger(AddCommand.class);

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
        verifyContactCanBeAdded(model);
        String feedback = createSuccessFeedback(model);
        addContactToModel(model);
        logger.info("Prepared ADD for contact " + toAdd.getId() + ", pending save.");
        return new CommandResult(feedback, toAdd.getId());
    }

    private void verifyContactCanBeAdded(Model model) throws CommandException {
        if (toAdd.isGuardianWithoutContactMethod()) {
            logger.info("Rejected ADD: guardian has no contact method.");
            throw new CommandException(MESSAGE_CONTACT_METHOD_REQUIRED);
        }

        if (model.hasPerson(toAdd)) {
            logger.info("Rejected ADD: an exact duplicate contact exists.");
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
    }

    private void addContactToModel(Model model) {
        model.addPerson(toAdd);
        assert model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getId().equals(toAdd.getId()))
                : "Model must contain the added contact ID after addPerson returns.";
        model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_CONTACTS);
    }

    /**
     * Builds feedback using existing contacts, before the new contact can match itself.
     */
    private String createSuccessFeedback(Model model) {
        String feedback = String.format(MESSAGE_SUCCESS, toAdd.getName(), toAdd.getRole());
        DuplicateWarning warning = DuplicateWarning.classify(toAdd, model.getAddressBook().getPersonList());
        if (warning == DuplicateWarning.PHONE) {
            logger.info("ADD possible-duplicate warning selected: phone matches ignoring spaces and hyphens.");
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE_PHONE;
        } else if (warning == DuplicateWarning.EMAIL) {
            logger.info("ADD possible-duplicate warning selected: email matches ignoring case.");
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE_EMAIL;
        } else if (warning == DuplicateWarning.GENERAL) {
            logger.info("ADD possible-duplicate warning selected: a contact method is absent from both contacts.");
            feedback += "\n" + MESSAGE_POSSIBLE_DUPLICATE;
        }
        return feedback;
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
