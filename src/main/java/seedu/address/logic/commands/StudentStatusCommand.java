package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;

/**
 * Changes the status of the student identified by its displayed index.
 */
public abstract class StudentStatusCommand extends Command {

    private static final Logger logger = LogsCenter.getLogger(StudentStatusCommand.class);

    private final Index targetIndex;
    private final StudentStatus targetStatus;

    /**
     * Creates a command that gives the student at {@code targetIndex} the status {@code targetStatus}.
     */
    protected StudentStatusCommand(Index targetIndex, StudentStatus targetStatus) {
        requireNonNull(targetIndex);
        requireNonNull(targetStatus);
        this.targetIndex = targetIndex;
        this.targetStatus = targetStatus;
    }

    /** Returns the message shown when the contact is a guardian. */
    protected abstract String getNotStudentMessage();

    /** Returns the message format shown when the student already has the target status. */
    protected abstract String getAlreadyInStatusMessage();

    /** Returns the message format shown after the status is changed. */
    protected abstract String getSuccessMessage();

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person target = getStudentToUpdate(model);
        Person updated = target.withStatus(targetStatus);
        model.setPerson(target, updated);
        assert updated.getStatus() == targetStatus;
        // LogicManager saves the change afterwards, and discards it if saving fails.
        logger.info(String.format("Prepared status change of contact %s to %s, pending save",
                target.getId(), targetStatus));
        return new CommandResult(String.format(getSuccessMessage(), target.getName()));
    }

    /** Resolves the displayed index, then rejects guardians and repeated status changes in that order. */
    private Person getStudentToUpdate(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, targetIndex.getOneBased()));
        }

        Person target = lastShownList.get(targetIndex.getZeroBased());
        if (target.getRole() != Role.STUDENT) {
            logger.fine("Rejected student status change: target is not a student.");
            throw new CommandException(getNotStudentMessage());
        }
        if (target.getStatus() == targetStatus) {
            logger.fine("Rejected student status change: target already has status " + targetStatus);
            throw new CommandException(String.format(getAlreadyInStatusMessage(), target.getName()));
        }
        return target;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        StudentStatusCommand otherCommand = (StudentStatusCommand) other;
        return targetIndex.equals(otherCommand.targetIndex)
                && targetStatus == otherCommand.targetStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetStatus", targetStatus)
                .toString();
    }
}
