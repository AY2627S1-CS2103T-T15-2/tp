package seedu.address.ui;

import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t15-2.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "For full instructions, examples and field rules, "
            + "copy the User Guide URL and open it in your browser.";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private static final List<CommandHelp> COMMAND_HELP = List.of(
            new CommandHelp("Add contact", AddCommand.COMMAND_FORMAT),
            new CommandHelp("Edit contact", EditCommand.COMMAND_FORMAT),
            new CommandHelp("Delete contacts", DeleteCommand.COMMAND_FORMAT),
            new CommandHelp("Undo latest deletion", UndoCommand.COMMAND_WORD),
            new CommandHelp("Check contact data", CheckCommand.COMMAND_FORMAT),
            new CommandHelp("View contact", ViewCommand.COMMAND_FORMAT),
            new CommandHelp("Find by name", FindCommand.COMMAND_FORMAT),
            new CommandHelp("Link or unlink guardian", LinkCommand.COMMAND_FORMAT),
            new CommandHelp("List contacts", ListCommand.COMMAND_FORMAT),
            new CommandHelp("Archive student", ArchiveCommand.COMMAND_FORMAT),
            new CommandHelp("Unarchive student", UnarchiveCommand.COMMAND_FORMAT),
            new CommandHelp("Clear contacts", ClearCommand.COMMAND_WORD),
            new CommandHelp("Show help", HelpCommand.COMMAND_WORD),
            new CommandHelp("Exit app", ExitCommand.COMMAND_WORD));

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    @FXML
    private TextField userGuideUrl;

    @FXML
    private GridPane commandReference;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        helpMessage.setText(HELP_MESSAGE);
        userGuideUrl.setText(USERGUIDE_URL);
        populateCommandReference();
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Returns the command formats displayed in the Help window, in display order.
     */
    static List<String> getCommandFormats() {
        return COMMAND_HELP.stream().map(CommandHelp::format).toList();
    }

    private void populateCommandReference() {
        for (int row = 0; row < COMMAND_HELP.size(); row++) {
            CommandHelp command = COMMAND_HELP.get(row);
            Label description = createWrappingLabel(command.description());
            Label format = createWrappingLabel(command.format());
            format.getStyleClass().add("command-format");
            commandReference.addRow(row, description, format);
        }
    }

    private Label createWrappingLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private record CommandHelp(String description, String format) {
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        getRoot().setWidth(Math.min(getRoot().getWidth(), screenBounds.getWidth()));
        getRoot().setHeight(Math.min(getRoot().getHeight(), screenBounds.getHeight()));
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
