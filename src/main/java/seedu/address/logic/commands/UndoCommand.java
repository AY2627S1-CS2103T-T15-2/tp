package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.logic.DeletionHistory;
import seedu.address.logic.DeletionSnapshot;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Restores the last deletion on a candidate model; LogicManager consumes history after saving. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = "Use undo without any arguments.";
    public static final String MESSAGE_UNAVAILABLE = "No deletion is available to undo.";
    public static final String MESSAGE_SUCCESS = "Restored %d deleted contact(s).";

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
        String feedback = String.format(MESSAGE_SUCCESS, snapshot.getDeletedIds().size());
        Optional<Person> selected = model.getFilteredPersonList().stream()
                .filter(person -> snapshot.getDeletedIds().contains(person.getId())).findFirst();
        return selected.map(person -> new CommandResult(feedback, person.getId()))
                .orElseGet(() -> new CommandResult(feedback));
    }
}
