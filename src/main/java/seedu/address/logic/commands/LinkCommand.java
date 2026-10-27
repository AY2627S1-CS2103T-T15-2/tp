package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.model.person.Role;

/**
 * Links a student to a guardian using their indexes in the displayed contact list.
 */
public class LinkCommand extends Command {

    public static final String COMMAND_WORD = "link";
    public static final String MESSAGE_USAGE = "link s/STUDENT_INDEX g/GUARDIAN_INDEX";
    public static final String COMMAND_FORMAT = MESSAGE_USAGE + " or link s/STUDENT_INDEX g/NONE";
    public static final String REMOVE_GUARDIAN_VALUE = "NONE";
    public static final String MESSAGE_UNLINKED = "Removed guardian link from %s.";
    public static final String MESSAGE_NOT_LINKED = "%s has no guardian linked.";
    public static final String MESSAGE_INVALID_COMMAND = "Invalid link command. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_REPEATED_PARAMETER = "Each of s/ and g/ may appear only once.";
    public static final String MESSAGE_INVALID_STUDENT_INDEX =
            "Student index must be a positive integer shown in the list.";
    public static final String MESSAGE_INVALID_GUARDIAN_INDEX =
            "Guardian value must be a positive integer shown in the list, or NONE to remove the guardian link.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "No contact exists at student index %s.";
    public static final String MESSAGE_GUARDIAN_NOT_FOUND = "No contact exists at guardian index %s.";
    public static final String MESSAGE_NOT_STUDENT = "Student index must refer to a student.";
    public static final String MESSAGE_NOT_GUARDIAN = "Guardian index must refer to a guardian.";
    public static final String MESSAGE_SUCCESS = "Linked student %s to guardian %s.";
    public static final String MESSAGE_REPLACED = "Changed %s’s guardian from %s to %s.";
    public static final String MESSAGE_DUPLICATE_LINK = "%s is already linked to %s.";
    public static final String MESSAGE_MISSING_OLD_GUARDIAN =
            "The student's existing guardian could not be found. No changes were made.";

    private final Index studentIndex;
    private final Optional<Index> guardianIndex;

    /**
     * Creates a link command using the displayed student and guardian indexes.
     */
    public LinkCommand(Index studentIndex, Index guardianIndex) {
        this(studentIndex, Optional.of(guardianIndex));
    }

    private LinkCommand(Index studentIndex, Optional<Index> guardianIndex) {
        requireAllNonNull(studentIndex, guardianIndex);
        this.studentIndex = studentIndex;
        this.guardianIndex = guardianIndex;
    }

    /** Creates a command that removes only the displayed student's guardian link. */
    public static LinkCommand unlink(Index studentIndex) {
        return new LinkCommand(studentIndex, Optional.empty());
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentIndex.getOneBased()));
        }
        if (guardianIndex.isEmpty()) {
            return unlinkStudent(model, displayedPersons.get(studentIndex.getZeroBased()));
        }
        Index guardianTarget = guardianIndex.orElseThrow();
        if (guardianTarget.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(String.format(MESSAGE_GUARDIAN_NOT_FOUND, guardianTarget.getOneBased()));
        }

        Person student = displayedPersons.get(studentIndex.getZeroBased());
        Person guardian = displayedPersons.get(guardianTarget.getZeroBased());
        if (student.getRole() != Role.STUDENT) {
            throw new CommandException(MESSAGE_NOT_STUDENT);
        }
        if (guardian.getRole() != Role.GUARDIAN) {
            throw new CommandException(MESSAGE_NOT_GUARDIAN);
        }
        if (student.getGuardianId().filter(guardian.getId()::equals).isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_LINK, student.getName(), guardian.getName()));
        }

        String feedback = String.format(MESSAGE_SUCCESS, student.getName(), guardian.getName());
        if (student.getGuardianId().isPresent()) {
            Person oldGuardian = PersonLookup.findById(model.getAddressBook().getPersonList(),
                    student.getGuardianId().get())
                    .orElseThrow(() -> new CommandException(MESSAGE_MISSING_OLD_GUARDIAN));
            feedback = String.format(MESSAGE_REPLACED, student.getName(), oldGuardian.getName(), guardian.getName());
        }
        model.setPerson(student, student.withGuardianId(Optional.of(guardian.getId())));
        return new CommandResult(feedback, student.getId());
    }

    private CommandResult unlinkStudent(Model model, Person student) throws CommandException {
        if (student.getRole() != Role.STUDENT) {
            throw new CommandException(MESSAGE_NOT_STUDENT);
        }
        if (student.getGuardianId().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NOT_LINKED, student.getName()));
        }
        model.setPerson(student, student.withGuardianId(Optional.empty()));
        return new CommandResult(String.format(MESSAGE_UNLINKED, student.getName()), student.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LinkCommand otherCommand)) {
            return false;
        }
        return studentIndex.equals(otherCommand.studentIndex) && guardianIndex.equals(otherCommand.guardianIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex)
                .add("guardianIndex", guardianIndex).toString();
    }
}
