package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.model.person.StudentStatus;

/**
 * Marks the student identified by its displayed index as a former student.
 */
public class ArchiveCommand extends StudentStatusCommand {

    public static final String COMMAND_WORD = "archive";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Marks the student identified by the index number used in the displayed contact list"
            + " as a former student.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Marked %1$s as a former student.";
    public static final String MESSAGE_ALREADY_FORMER = "%1$s is already a former student.";
    public static final String MESSAGE_NOT_STUDENT = "Only students can be archived.";

    /**
     * Creates an ArchiveCommand to mark the student at {@code targetIndex} as a former student.
     */
    public ArchiveCommand(Index targetIndex) {
        super(targetIndex, StudentStatus.FORMER);
    }

    @Override
    protected String getNotStudentMessage() {
        return MESSAGE_NOT_STUDENT;
    }

    @Override
    protected String getAlreadyInStatusMessage() {
        return MESSAGE_ALREADY_FORMER;
    }

    @Override
    protected String getSuccessMessage() {
        return MESSAGE_SUCCESS;
    }
}
