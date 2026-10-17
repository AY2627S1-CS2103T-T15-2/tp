package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_CONTACTS;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;

/**
 * Lists guardians and active students in the address book to the user, or former students only.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String FORMER_KEYWORD = "former";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid list command. Use: " + COMMAND_WORD
            + " or " + COMMAND_WORD + " " + FORMER_KEYWORD;
    public static final String MESSAGE_SUCCESS = "Listed %1$s";
    public static final String MESSAGE_NO_CONTACTS = "No contacts found.";
    public static final String MESSAGE_ONLY_FORMER_STUDENTS = "No active contacts found. Use "
            + COMMAND_WORD + " " + FORMER_KEYWORD + " to see former students.";
    public static final String MESSAGE_NO_FORMER_STUDENTS = "No former students found.";

    private final boolean isFormerList;

    /**
     * Creates a ListCommand that shows guardians and active students.
     */
    public ListCommand() {
        this(false);
    }

    /**
     * Creates a ListCommand that shows former students only if {@code isFormerList} is true,
     * or guardians and active students otherwise.
     */
    public ListCommand(boolean isFormerList) {
        this.isFormerList = isFormerList;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(isFormerList
                ? PREDICATE_SHOW_FORMER_STUDENTS
                : PREDICATE_SHOW_ACTIVE_CONTACTS);

        int contactCount = model.getFilteredPersonList().size();
        if (isFormerList) {
            return new CommandResult(contactCount == 0
                    ? MESSAGE_NO_FORMER_STUDENTS
                    : String.format(MESSAGE_SUCCESS, formatFormerStudentCount(contactCount)));
        }
        if (contactCount == 0) {
            boolean hasFormerStudents = !model.getAddressBook().getPersonList().isEmpty();
            return new CommandResult(hasFormerStudents ? MESSAGE_ONLY_FORMER_STUDENTS : MESSAGE_NO_CONTACTS);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatContactCount(contactCount)));
    }

    /**
     * Returns {@code count} followed by "former student" if {@code count} is 1,
     * or "former students" otherwise.
     */
    private static String formatFormerStudentCount(int count) {
        return count + (count == 1 ? " former student" : " former students");
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

        return isFormerList == otherListCommand.isFormerList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isFormerList", isFormerList)
                .toString();
    }
}
