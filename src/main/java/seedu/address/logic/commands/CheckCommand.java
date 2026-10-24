package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.Model;

/** Reports possible data-quality improvements without changing contacts or their displayed filter. */
public class CheckCommand extends Command {
    public static final String COMMAND_WORD = "check";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " [guardians|details|duplicates]";
    public static final String MESSAGE_USAGE = "Use check [guardians|details|duplicates].";

    /** Selects the checks to include in the report. */
    public enum Section {
        ALL, GUARDIANS, DETAILS, DUPLICATES
    }

    private final Section section;

    /** Creates a report command for the requested section. */
    public CheckCommand(Section section) {
        this.section = requireNonNull(section);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        ContactQualityReport report = new ContactQualityReport(List.copyOf(model.getFilteredPersonList()));
        return new CommandResult(report.format(section));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CheckCommand command && section == command.section;
    }
}
