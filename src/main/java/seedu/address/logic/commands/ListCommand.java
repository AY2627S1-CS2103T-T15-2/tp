package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Person;

/**
 * Lists the contacts that match a {@code ContactFilter}. By default, guardians and active students are listed.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String FORMER_STATUS = "former";
    public static final String LIST_FORMER_COMMAND = COMMAND_WORD + " " + PREFIX_STATUS + FORMER_STATUS;

    public static final String MESSAGE_INVALID_FORMAT = "Invalid list command. Use: " + COMMAND_WORD
            + " or " + LIST_FORMER_COMMAND;
    public static final String MESSAGE_SUCCESS = "Listed %1$s";
    public static final String MESSAGE_NO_CONTACTS = "No contacts found.";
    public static final String MESSAGE_ONLY_FORMER_STUDENTS = "No active contacts found. Use "
            + LIST_FORMER_COMMAND + " to see former students.";
    public static final String MESSAGE_NO_FORMER_STUDENTS = "No former students found.";

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
            return new CommandResult(MESSAGE_NO_CONTACTS);
        }
        if (contactCount == 0) {
            // The active view hides only former students, so any saved contact here must be one.
            assert model.getAddressBook().getPersonList().stream().allMatch(Person::isFormerStudent);
            boolean hasFormerStudents = !model.getAddressBook().getPersonList().isEmpty();
            return new CommandResult(hasFormerStudents ? MESSAGE_ONLY_FORMER_STUDENTS : MESSAGE_NO_CONTACTS);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatContactCount(contactCount)));
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
