package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;

/**
 * Deletes one or more contacts identified using their displayed indexes from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the contacts identified by the index numbers used in the displayed contact list.\n"
            + "Parameters: INDEX [INDEX...] (each must be a unique positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 3 5";

    public static final String MESSAGE_CONTACT_SUMMARY = "%1$s [%2$s]";
    public static final String MESSAGE_DELETE_CONTACT_SUCCESS = "Deleted contact: " + MESSAGE_CONTACT_SUMMARY + ".";
    public static final String MESSAGE_DELETE_CONTACTS_SUCCESS = "Deleted %1$d contacts:\n%2$s.";
    public static final String MESSAGE_INVALID_INDEX =
            "The contact index must be a positive integer shown in the list.";
    public static final String MESSAGE_DUPLICATE_INDEX = "Each contact index may appear only once.";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No contact exists at index %1$d.";
    public static final String MESSAGE_REMOVED_RELATIONSHIP = " Removed 1 student-guardian relationship.";
    public static final String MESSAGE_REMOVED_RELATIONSHIPS = " Removed %d student-guardian relationships.";

    private final List<Index> targetIndexes;

    /** Creates a command that deletes the contact at {@code targetIndex}. */
    public DeleteCommand(Index targetIndex) {
        this(List.of(targetIndex));
    }

    /**
     * Creates a command that deletes the contacts at {@code targetIndexes}.
     */
    public DeleteCommand(List<Index> targetIndexes) {
        requireNonNull(targetIndexes);
        if (targetIndexes.isEmpty()) {
            throw new IllegalArgumentException("At least one target index is required");
        }
        this.targetIndexes = List.copyOf(targetIndexes);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        for (Index targetIndex : targetIndexes) {
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(String.format(MESSAGE_CONTACT_NOT_FOUND, targetIndex.getOneBased()));
            }
        }

        List<Person> personsToDelete = targetIndexes.stream()
                .map(index -> lastShownList.get(index.getZeroBased()))
                .toList();
        long removedRelationships = countRemovedRelationships(model, personsToDelete);

        for (Person person : personsToDelete) {
            model.deletePerson(person);
        }

        String feedback = formatSuccessMessage(personsToDelete);
        if (removedRelationships == 1) {
            feedback += MESSAGE_REMOVED_RELATIONSHIP;
        } else if (removedRelationships > 1) {
            feedback += String.format(MESSAGE_REMOVED_RELATIONSHIPS, removedRelationships);
        }
        return new CommandResult(feedback);
    }

    private static long countRemovedRelationships(Model model, List<Person> personsToDelete) {
        Set<ContactId> deletedIds = personsToDelete.stream().map(Person::getId).collect(Collectors.toSet());
        // A relationship is removed if either endpoint is deleted, counted once per student.
        return model.getAddressBook().getPersonList().stream()
                .filter(student -> student.getGuardianId().isPresent())
                .filter(student -> deletedIds.contains(student.getId())
                        || deletedIds.contains(student.getGuardianId().get()))
                .count();
    }

    private static String formatSuccessMessage(List<Person> deletedPersons) {
        if (deletedPersons.size() == 1) {
            Person person = deletedPersons.getFirst();
            return String.format(MESSAGE_DELETE_CONTACT_SUCCESS, person.getName(), person.getRole());
        }
        String joinedDescriptions = deletedPersons.stream()
                .map(person -> String.format(MESSAGE_CONTACT_SUMMARY, person.getName(), person.getRole()))
                .collect(Collectors.joining("\n"));
        return String.format(MESSAGE_DELETE_CONTACTS_SUCCESS, deletedPersons.size(), joinedDescriptions);
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

        return targetIndexes.equals(otherDeleteCommand.targetIndexes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndexes", targetIndexes)
                .toString();
    }
}
