package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.GuardianChildrenPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Shows all students linked to a guardian, including former students.
 */
public class ChildrenCommand extends Command {
    public static final String COMMAND_WORD = "children";
    public static final String MESSAGE_USAGE = "children g/GUARDIAN_INDEX";
    public static final String MESSAGE_INVALID_ROLE = "Guardian index must refer to a guardian.";
    public static final String MESSAGE_NOT_FOUND = "No contact exists at guardian index %s.";
    public static final String MESSAGE_NO_CHILDREN = "No students are linked to %s. Use list to return to contacts.";
    public static final String MESSAGE_SUCCESS = "Listed %s linked to %s, including former students.";

    private final Index guardianIndex;

    /**
     * Creates a command targeting the guardian's displayed index.
     */
    public ChildrenCommand(Index guardianIndex) {
        this.guardianIndex = requireNonNull(guardianIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (guardianIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, guardianIndex.getOneBased()));
        }
        Person guardian = model.getFilteredPersonList().get(guardianIndex.getZeroBased());
        if (guardian.getRole() != Role.GUARDIAN) {
            throw new CommandException(MESSAGE_INVALID_ROLE);
        }
        model.updateFilteredPersonList(new GuardianChildrenPredicate(guardian.getId()));
        int count = model.getFilteredPersonList().size();
        String feedback = count == 0 ? String.format(MESSAGE_NO_CHILDREN, guardian.getName())
                : String.format(MESSAGE_SUCCESS, count + (count == 1 ? " student" : " students"), guardian.getName());
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ChildrenCommand otherCommand
                && guardianIndex.equals(otherCommand.guardianIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("guardianIndex", guardianIndex).toString();
    }
}
