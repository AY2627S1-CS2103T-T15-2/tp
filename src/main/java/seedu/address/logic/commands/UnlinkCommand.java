package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Removes a student's guardian relationship while retaining both contacts.
 */
public class UnlinkCommand extends Command {
    public static final String COMMAND_WORD = "unlink";
    public static final String MESSAGE_USAGE = "unlink s/STUDENT_INDEX";
    public static final String MESSAGE_INVALID_ROLE = "Student index must refer to a student.";
    public static final String MESSAGE_NOT_FOUND = "No contact exists at student index %s.";
    public static final String MESSAGE_NO_LINK = "%s has no guardian link to remove.";
    public static final String MESSAGE_SUCCESS = "Removed the guardian link from %s. Both contacts were kept.";

    private final Index studentIndex;

    /**
     * Creates a command targeting the student's displayed index.
     */
    public UnlinkCommand(Index studentIndex) {
        this.studentIndex = requireNonNull(studentIndex);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, studentIndex.getOneBased()));
        }
        Person student = model.getFilteredPersonList().get(studentIndex.getZeroBased());
        if (student.getRole() != Role.STUDENT) {
            throw new CommandException(MESSAGE_INVALID_ROLE);
        }
        if (student.getGuardianId().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_LINK, student.getName()));
        }
        model.setPerson(student, student.withGuardianId(Optional.empty()));
        return new CommandResult(String.format(MESSAGE_SUCCESS, student.getName()), student.getId());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnlinkCommand otherCommand
                && studentIndex.equals(otherCommand.studentIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).toString();
    }
}
