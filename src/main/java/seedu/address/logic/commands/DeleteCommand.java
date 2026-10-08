package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a contact identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the contact identified by the index number used in the displayed contact list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_CONTACT_SUCCESS = "Deleted contact: %1$s [%2$s].";
    public static final String MESSAGE_REMOVED_RELATIONSHIP = " Removed 1 student-guardian relationship.";
    public static final String MESSAGE_REMOVED_RELATIONSHIPS = " Removed %d student-guardian relationships.";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        long removedRelationships = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getGuardianId().filter(personToDelete.getId()::equals).isPresent()).count();
        if (personToDelete.getGuardianId().isPresent()) {
            removedRelationships++;
        }
        model.deletePerson(personToDelete);
        String feedback = String.format(MESSAGE_DELETE_CONTACT_SUCCESS, personToDelete.getName(),
                personToDelete.getRole());
        if (removedRelationships == 1) {
            feedback += MESSAGE_REMOVED_RELATIONSHIP;
        } else if (removedRelationships > 1) {
            feedback += String.format(MESSAGE_REMOVED_RELATIONSHIPS, removedRelationships);
        }
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
