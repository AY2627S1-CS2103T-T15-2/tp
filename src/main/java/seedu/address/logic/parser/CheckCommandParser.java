package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a single optional data-quality report section. */
public class CheckCommandParser implements Parser<CheckCommand> {
    @Override
    public CheckCommand parse(String args) throws ParseException {
        requireNonNull(args);
        CheckCommand.Section section = switch (args.strip().toLowerCase(Locale.ROOT)) {
            case "" -> CheckCommand.Section.ALL;
            case "guardians" -> CheckCommand.Section.GUARDIANS;
            case "details" -> CheckCommand.Section.DETAILS;
            case "duplicates" -> CheckCommand.Section.DUPLICATES;
            default -> throw new ParseException(CheckCommand.MESSAGE_USAGE);
        };
        return new CheckCommand(section);
    }
}
