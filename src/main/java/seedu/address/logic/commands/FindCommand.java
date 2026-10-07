package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsSubstringPredicate;

/**
 * Finds and lists all contacts in the address book whose name contains the search term.
 * Matching is case-insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid find command. Use: " + COMMAND_WORD + " NAME";
    public static final String MESSAGE_SUCCESS = "Found %1$s matching: %2$s.";
    public static final String MESSAGE_NO_MATCHES = "No contacts found matching: %1$s";

    private final NameContainsSubstringPredicate predicate;

    public FindCommand(NameContainsSubstringPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);

        int matchCount = model.getFilteredPersonList().size();
        String searchTerm = predicate.getSearchTerm();
        if (matchCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, searchTerm));
        }
        return new CommandResult(
                String.format(MESSAGE_SUCCESS, Messages.formatContactCount(matchCount), searchTerm));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
