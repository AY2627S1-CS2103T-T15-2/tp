package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.DeletionHistory;
import seedu.address.logic.DeletionSnapshot;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonDisplayOrderComparator;

/** Restores the last deletion on a candidate model; LogicManager consumes history after saving. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = "Use undo without any arguments.";
    public static final String MESSAGE_UNAVAILABLE = "No deletion is available to undo.";
    public static final String MESSAGE_SUCCESS = "Restored %s:\n%s.";

    private final DeletionHistory history;

    /** Creates an undo command using the current session's deletion history. */
    public UndoCommand(DeletionHistory history) {
        this.history = requireNonNull(history);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        DeletionSnapshot snapshot = history.getLatest()
                .orElseThrow(() -> new CommandException(MESSAGE_UNAVAILABLE));
        model.setAddressBook(snapshot.copyAddressBook());
        model.updateFilteredPersonList(snapshot.getFilter());
        String feedback = formatSuccessMessage(snapshot, model);
        Optional<Person> selected = model.getFilteredPersonList().stream()
                .filter(person -> snapshot.getDeletedIds().contains(person.getId())).findFirst();
        return selected.map(person -> new CommandResult(feedback, person.getId()))
                .orElseGet(() -> new CommandResult(feedback));
    }

    /** Describes deleted contacts and links restored from either deleted endpoint, including hidden students. */
    private static String formatSuccessMessage(DeletionSnapshot snapshot, Model model) {
        Set<ContactId> deletedIds = Set.copyOf(snapshot.getDeletedIds());
        List<Person> restoredContacts = model.getAddressBook().getPersonList().stream()
                .filter(person -> deletedIds.contains(person.getId()))
                .sorted(new PersonDisplayOrderComparator()).toList();
        String descriptions = restoredContacts.stream()
                .map(person -> String.format("%s [%s]", person.getName(), person.getRole()))
                .collect(Collectors.joining("\n"));
        String feedback = String.format(MESSAGE_SUCCESS, Messages.formatContactCount(restoredContacts.size()),
                descriptions);
        long restoredRelationships = RelationshipCount.countAffected(model.getAddressBook().getPersonList(), deletedIds);
        if (restoredRelationships > 0) {
            feedback += " Restored " + restoredRelationships + (restoredRelationships == 1
                    ? " student-guardian relationship." : " student-guardian relationships.");
        }
        return feedback;
    }
}
