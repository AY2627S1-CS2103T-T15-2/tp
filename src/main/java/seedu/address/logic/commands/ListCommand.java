package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Lists the contacts that match a {@code ContactFilter}. By default, guardians and active students are listed.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String FORMER_STATUS = "former";
    public static final String LIST_FORMER_COMMAND = COMMAND_WORD + " " + PREFIX_STATUS + FORMER_STATUS;

    public static final String COMMAND_FORMAT = COMMAND_WORD + " [" + PREFIX_ROLE + "ROLE] ["
            + PREFIX_STATUS + FORMER_STATUS + "]";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid list command. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_REPEATED_PARAMETER = "Each of r/ and st/ may appear only once.";
    public static final String MESSAGE_GUARDIAN_NOT_FORMER =
            "Guardians cannot be former. Remove r/guardian or st/former.";
    public static final String MESSAGE_SUCCESS = "Listed %1$s";
    public static final String MESSAGE_NO_CONTACTS = "No contacts found.";
    public static final String MESSAGE_ONLY_FORMER_STUDENTS = "No active contacts found. Use "
            + LIST_FORMER_COMMAND + " to see former students.";
    public static final String MESSAGE_NO_FORMER_STUDENTS = "No former students found.";
    public static final String MESSAGE_NO_GUARDIANS = "No guardians found.";
    public static final String MESSAGE_NO_STUDENTS = "No students found.";
    public static final String MESSAGE_NO_ACTIVE_STUDENTS = "No active students found. Use " + COMMAND_WORD + " "
            + PREFIX_ROLE + "student " + PREFIX_STATUS + FORMER_STATUS + " to see former students.";

    private final ContactFilter filter;

    /**
     * Creates a ListCommand that shows guardians and active students.
     */
    public ListCommand() {
        this(ContactFilter.ACTIVE_CONTACTS);
    }

    /**
     * Creates a ListCommand that shows the contacts matching {@code filter}.
     */
    public ListCommand(ContactFilter filter) {
        requireNonNull(filter);
        this.filter = filter;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(filter);

        int contactCount = model.getFilteredPersonList().size();
        if (filter.isFormerOnly()) {
            return new CommandResult(contactCount == 0
                    ? MESSAGE_NO_FORMER_STUDENTS
                    : String.format(MESSAGE_SUCCESS, Messages.formatFormerStudentCount(contactCount)));
        }
        if (contactCount == 0 && filter.getRole().isPresent()) {
            return new CommandResult(getNoMatchingRoleMessage(model, filter.getRole().get()));
        }
        if (contactCount == 0) {
            // The active view hides only former students, so any saved contact here must be one.
            assert model.getAddressBook().getPersonList().stream().allMatch(Person::isFormerStudent);
            boolean hasFormerStudents = !model.getAddressBook().getPersonList().isEmpty();
            return new CommandResult(hasFormerStudents ? MESSAGE_ONLY_FORMER_STUDENTS : MESSAGE_NO_CONTACTS);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatContactCount(contactCount)));
    }

    /**
     * Returns the message shown when no active contact has {@code role}. For students, the message suggests
     * listing former students if there are any.
     */
    private static String getNoMatchingRoleMessage(Model model, Role role) {
        if (role == Role.GUARDIAN) {
            return MESSAGE_NO_GUARDIANS;
        }
        assert role == Role.STUDENT;
        boolean hasFormerStudents = model.getAddressBook().getPersonList().stream()
                .anyMatch(Person::isFormerStudent);
        return hasFormerStudents ? MESSAGE_NO_ACTIVE_STUDENTS : MESSAGE_NO_STUDENTS;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ListCommand otherListCommand)) {
            return false;
        }

        return filter.equals(otherListCommand.filter);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filter", filter)
                .toString();
    }
}
