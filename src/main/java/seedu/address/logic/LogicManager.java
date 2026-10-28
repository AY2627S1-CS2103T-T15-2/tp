package seedu.address.logic;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_SAVE_FAILURE = "Changes could not be saved. No changes were made.";
    public static final String MESSAGE_DATA_LOAD_FAILURE =
            "Data changes are disabled because the data file could not be loaded. "
                    + "Fix the file and restart TutorRoster.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final DeletionHistory deletionHistory = new DeletionHistory();

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser(deletionHistory);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        if (!command.isDataChanging()) {
            return command.execute(model);
        }
        if (storage.isDataFileLoadFailed()) {
            throw new CommandException(getDataLoadFailureMessage());
        }

        Model candidate = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        candidate.updateFilteredPersonList(model.getPersonFilter());
        CommandResult commandResult = command.execute(candidate);
        DeletionSnapshot deletionSnapshot = command instanceof DeleteCommand
                ? captureDeletion(candidate) : null;
        try {
            storage.saveAddressBook(candidate.getAddressBook());
        } catch (IOException exception) {
            logger.warning("Could not save contact changes: " + exception.getMessage());
            throw new CommandException(MESSAGE_SAVE_FAILURE, exception);
        }
        model.setAddressBook(candidate.getAddressBook());
        model.updateFilteredPersonList(candidate.getPersonFilter());
        if (deletionSnapshot != null) {
            deletionHistory.record(deletionSnapshot);
            logger.info("Recorded undo snapshot after saving deletion of "
                    + deletionSnapshot.getDeletedIds().size() + " contact(s).");
        } else {
            deletionHistory.getLatest().ifPresent(snapshot -> {
                if (command instanceof UndoCommand) {
                    logger.info("Undo saved successfully: restored " + snapshot.getDeletedIds().size()
                            + " contact(s). Deletion history consumed.");
                } else {
                    logger.info("Deletion undo history invalidated after successful "
                            + command.getClass().getSimpleName() + ".");
                }
            });
            deletionHistory.clear();
        }
        return commandResult;
    }

    /** Captures the original data while deriving removed identities from the executed candidate. */
    private DeletionSnapshot captureDeletion(Model candidate) {
        Set<ContactId> retainedIds = candidate.getAddressBook().getPersonList().stream()
                .map(Person::getId).collect(Collectors.toSet());
        List<ContactId> deletedIds = model.getAddressBook().getPersonList().stream()
                .map(Person::getId).filter(id -> !retainedIds.contains(id)).toList();
        return new DeletionSnapshot(new AddressBook(model.getAddressBook()), model.getPersonFilter(), deletedIds);
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return model.getAddressBook().getPersonList();
    }

    @Override
    public Optional<String> getStartupMessage() {
        return storage.isDataFileLoadFailed() ? Optional.of(getDataLoadFailureMessage()) : Optional.empty();
    }

    /**
     * Uses the same actionable explanation at startup and when a change is blocked.
     */
    private String getDataLoadFailureMessage() {
        return storage.getDataFileLoadFailureReason()
                .map(reason -> MESSAGE_DATA_LOAD_FAILURE + " Reason: " + reason)
                .orElse(MESSAGE_DATA_LOAD_FAILURE);
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
