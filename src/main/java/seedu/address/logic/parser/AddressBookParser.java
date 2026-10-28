package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.DeletionHistory;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.commands.ChildrenCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {
    /** Used for initial separation of command word and args. */
    private static final Pattern BASIC_COMMAND_FORMAT =
            Pattern.compile("(?<commandWord>[^\\p{javaWhitespace}]+)(?<arguments>.*)", Pattern.DOTALL);
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    private final DeletionHistory deletionHistory;

    /** Creates a standalone parser with fresh session history. */
    public AddressBookParser() {
        this(new DeletionHistory());
    }

    /** Creates a parser sharing the logic session's deletion history. */
    public AddressBookParser(DeletionHistory deletionHistory) {
        this.deletionHistory = requireNonNull(deletionHistory);
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        String normalizedInput = StringUtil.normalizeSpaceSeparators(userInput);
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(normalizedInput.trim().strip());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case CheckCommand.COMMAND_WORD -> new CheckCommandParser().parse(arguments);
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case LinkCommand.COMMAND_WORD -> new LinkCommandParser().parse(arguments);
            case UnlinkCommand.COMMAND_WORD -> new UnlinkCommandParser().parse(arguments);
            case ChildrenCommand.COMMAND_WORD -> new ChildrenCommandParser().parse(arguments);
            case ArchiveCommand.COMMAND_WORD -> new ArchiveCommandParser().parse(arguments);
            case UnarchiveCommand.COMMAND_WORD -> new UnarchiveCommandParser().parse(arguments);
            case UndoCommand.COMMAND_WORD -> {
                if (!arguments.isBlank()) {
                    throw new ParseException(UndoCommand.MESSAGE_USAGE);
                }
                yield new UndoCommand(deletionHistory);
            }
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
