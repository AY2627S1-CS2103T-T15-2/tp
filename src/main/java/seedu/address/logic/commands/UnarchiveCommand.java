package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.model.person.StudentStatus;

/**
 * Marks the former student identified by its displayed index as an active student again.
 */
public class UnarchiveCommand extends StudentStatusCommand {

    public static final String COMMAND_WORD = "unarchive";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Marks the former student identified by the index number used in the displayed contact list"
            + " as an active student.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Marked %1$s as an active student.";
    public static final String MESSAGE_ALREADY_ACTIVE = "%1$s is already an active student.";
    public static final String MESSAGE_NOT_STUDENT = "Only students can be unarchived.";

    /**
     * Creates an UnarchiveCommand to mark the former student at {@code targetIndex} as an active student.
     */
    public UnarchiveCommand(Index targetIndex) {
        super(targetIndex, StudentStatus.ACTIVE);
    }

    @Override
    protected String getNotStudentMessage() {
        return MESSAGE_NOT_STUDENT;
    }

    @Override
    protected String getAlreadyInStatusMessage() {
        return MESSAGE_ALREADY_ACTIVE;
    }

    @Override
    protected String getSuccessMessage() {
        return MESSAGE_SUCCESS;
    }
}
