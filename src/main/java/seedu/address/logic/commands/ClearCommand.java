package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears all contacts from TutorRoster.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRMATION_KEYWORD = "confirm";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " " + CONFIRMATION_KEYWORD;
    public static final String MESSAGE_USAGE = "This will permanently delete all contacts and student-guardian links. "
            + "Use " + COMMAND_FORMAT + " to proceed. This cannot be undone.";
    public static final String MESSAGE_SUCCESS = "All contacts have been cleared.";


    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
