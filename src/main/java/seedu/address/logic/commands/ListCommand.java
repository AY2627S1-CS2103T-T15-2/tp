package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Lists the contacts that match a {@code ContactFilter}. By default, guardians and active students are listed.
 * The filter can also choose a role, a status, a level and a subject.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String ACTIVE_STATUS = "active";
    public static final String FORMER_STATUS = "former";
    public static final String ALL_STATUS = "all";
    public static final String LIST_FORMER_COMMAND = COMMAND_WORD + " " + PREFIX_STATUS + FORMER_STATUS;

    public static final String COMMAND_FORMAT = COMMAND_WORD + " [" + PREFIX_ROLE + "ROLE] [" + PREFIX_STATUS
            + "STATUS] [" + PREFIX_LEVEL + "LEVEL] [" + PREFIX_SUBJECT + "SUBJECT]";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid list command. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_SUGGESTION = "Invalid list command. Did you mean: %1$s?";
    public static final String MESSAGE_INVALID_STATUS = "Invalid status. Status must be " + ACTIVE_STATUS + ", "
            + FORMER_STATUS + ", or " + ALL_STATUS + ".";
    public static final String MESSAGE_REPEATED_PARAMETER = "Each of " + PREFIX_ROLE + ", " + PREFIX_STATUS + ", "
            + PREFIX_LEVEL + " and " + PREFIX_SUBJECT + " may appear only once.";
    public static final String MESSAGE_GUARDIAN_NOT_FORMER =
            "Guardians cannot be former. Remove r/guardian or st/former.";
    public static final String MESSAGE_GUARDIAN_NO_STUDENT_DETAIL = "Guardians have no level or subject. Remove "
            + PREFIX_ROLE + "guardian, or remove " + PREFIX_LEVEL + " and " + PREFIX_SUBJECT + ".";
    public static final String MESSAGE_SUCCESS = "Listed %1$s";
    public static final String MESSAGE_FORMER_STUDENTS_HIDDEN = "%1$s hidden. Add " + PREFIX_STATUS + ALL_STATUS
            + " to include them.";
    public static final String MESSAGE_NO_CONTACTS = "No contacts found.";
    public static final String MESSAGE_ONLY_FORMER_STUDENTS = "No active contacts found. Use "
            + LIST_FORMER_COMMAND + " to see former students.";
    public static final String MESSAGE_NO_FORMER_STUDENTS = "No former students found.";
    public static final String MESSAGE_NO_GUARDIANS = "No guardians found.";
    public static final String MESSAGE_NO_STUDENTS = "No students found.";
    public static final String MESSAGE_NO_ACTIVE_STUDENTS = "No active students found. Use " + COMMAND_WORD + " "
            + PREFIX_ROLE + "student " + PREFIX_STATUS + FORMER_STATUS + " to see former students.";

    private static final Logger logger = LogsCenter.getLogger(ListCommand.class);

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
        int hiddenFormerStudentCount = countHiddenFormerStudents(model);
        logger.fine("Listing contacts with " + filter + ": " + contactCount + " shown, "
                + hiddenFormerStudentCount + " former students hidden");

        // Checked before the status, so that an empty st/former lv/LEVEL result does not suggest that
        // there are no former students at all.
        if (contactCount == 0 && filter.hasStudentDetail()) {
            return new CommandResult(hiddenFormerStudentCount == 0
                    ? MESSAGE_NO_CONTACTS
                    : MESSAGE_NO_CONTACTS + " " + formatHiddenFormerStudents(hiddenFormerStudentCount));
        }
        if (filter.isFormerOnly()) {
            return new CommandResult(contactCount == 0
                    ? MESSAGE_NO_FORMER_STUDENTS
                    : String.format(MESSAGE_SUCCESS, Messages.formatFormerStudentCount(contactCount)));
        }
        if (contactCount == 0 && filter.getRole().isPresent()) {
            return new CommandResult(getNoMatchingRoleMessage(model, filter.getRole().get()));
        }
        if (contactCount == 0) {
            // Without a role, level or subject, a filter hides at most the former students,
            // so any saved contact here must be one.
            assert model.getAddressBook().getPersonList().stream().allMatch(Person::isFormerStudent);
            boolean hasFormerStudents = !model.getAddressBook().getPersonList().isEmpty();
            return new CommandResult(hasFormerStudents ? MESSAGE_ONLY_FORMER_STUDENTS : MESSAGE_NO_CONTACTS);
        }
        String feedback = String.format(MESSAGE_SUCCESS, Messages.formatContactCount(contactCount));
        if (hiddenFormerStudentCount > 0) {
            feedback += ". " + formatHiddenFormerStudents(hiddenFormerStudentCount);
        }
        return new CommandResult(feedback);
    }

    private static String formatHiddenFormerStudents(int hiddenFormerStudentCount) {
        assert hiddenFormerStudentCount > 0;
        return String.format(MESSAGE_FORMER_STUDENTS_HIDDEN,
                Messages.formatFormerStudentCount(hiddenFormerStudentCount));
    }

    /**
     * Returns the number of former students that this command's filter hides but that would otherwise match it,
     * or 0 if the filter does not hide former students.
     */
    private int countHiddenFormerStudents(Model model) {
        if (!filter.hidesFormerStudents()) {
            return 0;
        }
        ContactFilter anyStatusFilter = filter.withAnyStatus();
        return Math.toIntExact(model.getAddressBook().getPersonList().stream()
                .filter(anyStatusFilter)
                .filter(Person::isFormerStudent)
                .count());
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
