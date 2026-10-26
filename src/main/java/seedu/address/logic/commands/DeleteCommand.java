package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;

/**
 * Deletes one or more contacts identified using their displayed indexes from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX [INDEX...] or "
            + COMMAND_WORD + " " + PREFIX_NAME + "NAME";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the contacts identified by the index numbers used in the displayed contact list.\n"
            + "Parameters: INDEX [INDEX...] (each must be a unique positive integer), or n/NAME\n"
            + "Example: " + COMMAND_WORD + " 1 3 5";

    public static final String MESSAGE_CONTACT_SUMMARY = "%1$s [%2$s]";
    public static final String MESSAGE_DELETE_CONTACT_SUCCESS = "Deleted contact: " + MESSAGE_CONTACT_SUMMARY + ".";
    public static final String MESSAGE_DELETE_CONTACTS_SUCCESS = "Deleted %1$d contacts:\n%2$s.";
    public static final String MESSAGE_DUPLICATE_INDEX = "Each contact index may appear only once.";
    public static final String MESSAGE_INVALID_TARGET = "Use delete INDEX [INDEX...] or delete n/NAME.";
    public static final String MESSAGE_NAME_NOT_FOUND = "No displayed contact matches name: %s.";
    public static final String MESSAGE_AMBIGUOUS_NAME =
            "Multiple displayed contacts match name: %s. Use delete INDEX to choose a contact:\n%s";
    public static final String MESSAGE_REMOVED_RELATIONSHIP = " Removed 1 student-guardian relationship.";
    public static final String MESSAGE_REMOVED_RELATIONSHIPS = " Removed %d student-guardian relationships.";

    private final List<Index> targetIndexes;
    private final Name targetName;

    /** Creates a command that deletes a unique displayed contact with the given full name. */
    public DeleteCommand(Name targetName) {
        this.targetName = requireNonNull(targetName);
        this.targetIndexes = List.of();
    }

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
        this.targetName = null;
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetName != null) {
            return deleteByName(model, lastShownList);
        }

        for (Index targetIndex : targetIndexes) {
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(String.format(Messages.MESSAGE_CONTACT_NOT_FOUND,
                        targetIndex.getOneBased()));
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

    private CommandResult deleteByName(Model model, List<Person> displayedPersons) throws CommandException {
        List<Person> matches = PersonLookup.findByExactName(displayedPersons, targetName);
        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NAME_NOT_FOUND, targetName));
        }
        Set<ContactId> matchingIds = matches.stream().map(Person::getId).collect(Collectors.toSet());
        List<Index> matchingIndexes = IntStream.range(0, displayedPersons.size())
                .filter(index -> matchingIds.contains(displayedPersons.get(index).getId()))
                .mapToObj(Index::fromZeroBased).toList();
        if (matchingIndexes.size() > 1) {
            String choices = matchingIndexes.stream().map(index -> String.format("%d [%s]",
                    index.getOneBased(), displayedPersons.get(index.getZeroBased()).getRole()))
                    .collect(Collectors.joining("\n"));
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS_NAME, targetName, choices));
        }
        return new DeleteCommand(matchingIndexes.getFirst()).execute(model);
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

        return targetIndexes.equals(otherDeleteCommand.targetIndexes)
                && Objects.equals(targetName, otherDeleteCommand.targetName);
    }

    @Override
    public String toString() {
        if (targetName != null) {
            return new ToStringBuilder(this).add("targetName", targetName).toString();
        }
        return new ToStringBuilder(this)
                .add("targetIndexes", targetIndexes)
                .toString();
    }
}
