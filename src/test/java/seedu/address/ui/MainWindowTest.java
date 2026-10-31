package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;
import static seedu.address.testutil.JavaFxTestUtil.waitForLayout;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;

/**
 * Checks window restoration and feedback layout on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
    }

    @Test
    public void constructor_savedWindowOnScreen_keptAtSavedPosition() throws Exception {
        runOnJavaFxThread(() -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            int savedX = (int) bounds.getMinX() + 10;
            int savedY = (int) bounds.getMinY() + 10;
            Stage stage = createMainWindowStage(new GuiSettings(600, 400, savedX, savedY));
            try {
                assertEquals(savedX, stage.getX());
                assertEquals(savedY, stage.getY());
                assertEquals(600, stage.getWidth());
                assertEquals(400, stage.getHeight());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void constructor_savedWindowOffScreenAndTooTall_movedOntoPrimaryScreen() throws Exception {
        runOnJavaFxThread(() -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Stage stage = createMainWindowStage(new GuiSettings(740, 3000, 100000, 100000));
            try {
                assertTrue(stage.getX() >= bounds.getMinX());
                assertTrue(stage.getY() >= bounds.getMinY());
                assertTrue(stage.getX() + stage.getWidth() <= bounds.getMaxX());
                assertTrue(stage.getY() + stage.getHeight() <= bounds.getMaxY());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void executeCommand_missingRoleAtMinimumWindowSize_showsWholeMessage() throws Exception {
        assertMissingAddFieldIsReadable("add", AddCommandParser.MESSAGE_MISSING_ROLE);
    }

    @Test
    public void executeCommand_missingNameAtMinimumWindowSize_showsWholeMessage() throws Exception {
        assertMissingAddFieldIsReadable("add r/student", AddCommandParser.MESSAGE_MISSING_NAME);
    }

    private void assertMissingAddFieldIsReadable(String commandText, String expectedMessage) throws Exception {
        FutureTask<Stage> createWindow = new FutureTask<>(() -> showMainWindow(1100, 500));
        runOnJavaFxThread(createWindow);
        Stage stage = createWindow.get();
        try {
            waitForLayout(stage.getScene().getRoot());
            runOnJavaFxThread(() -> {
                TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                command.setText(commandText);
                command.fireEvent(new ActionEvent());
            });
            waitForLayout(stage.getScene().getRoot());
            runOnJavaFxThread(() -> assertFeedbackIsReadable(stage, expectedMessage));
        } finally {
            runOnJavaFxThread(stage::close);
        }
    }

    @Test
    public void executeCommand_missingRoleAtDefaultWindowSize_growsWithinHeightCap() throws Exception {
        GuiSettings defaults = new GuiSettings();
        FutureTask<Stage> createWindow = new FutureTask<>(() ->
                showMainWindow(defaults.getWindowWidth(), defaults.getWindowHeight()));
        runOnJavaFxThread(createWindow);
        Stage stage = createWindow.get();
        try {
            waitForLayout(stage.getScene().getRoot());
            FutureTask<Double> initialHeight = new FutureTask<>(() -> {
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                return feedback.getHeight();
            });
            runOnJavaFxThread(initialHeight);
            double emptyHeight = initialHeight.get();
            runOnJavaFxThread(() -> {
                TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                command.setText("add");
                command.fireEvent(new ActionEvent());
            });
            waitForLayout(stage.getScene().getRoot());
            runOnJavaFxThread(() -> {
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals(AddCommandParser.MESSAGE_MISSING_ROLE, feedback.getText());
                assertTrue(feedback.getHeight() > emptyHeight);
                assertTrue(feedback.getHeight() <= stage.getScene().getHeight()
                        * ResultDisplay.MAXIMUM_SCENE_HEIGHT_RATIO + 2);
                assertMessageDoesNotWrap(feedback);
                assertNoVerticalScrollBar(feedback);
            });
        } finally {
            runOnJavaFxThread(stage::close);
        }
    }

    private Stage showMainWindow(double width, double height) {
        MainWindow window = createMainWindow(new GuiSettings(width, height, 0, 0));
        window.fillInnerParts();
        Stage stage = window.getPrimaryStage();
        stage.setOpacity(0);
        stage.show();
        stage.setWidth(width);
        stage.setHeight(height);
        return stage;
    }

    private void assertFeedbackIsReadable(Stage stage, String expectedMessage) {
        TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
        assertEquals(1100, stage.getWidth(), 1);
        assertEquals(500, stage.getHeight(), 1);
        assertEquals(expectedMessage, feedback.getText());
        assertTrue(feedback.getHeight() >= 40, "Feedback height: " + feedback.getHeight());
        assertTrue(feedback.lookup(".viewport").getBoundsInLocal().getHeight() > 0);
        assertTrue(feedback.isWrapText());
        assertMessageDoesNotWrap(feedback);
        assertNoVerticalScrollBar(feedback);
    }

    private void assertNoVerticalScrollBar(TextArea feedback) {
        assertFalse(feedback.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                .anyMatch(bar -> bar.getOrientation() == Orientation.VERTICAL && bar.isVisible()),
                "The full add error should fit without scrolling");
    }

    private void assertMessageDoesNotWrap(TextArea feedback) {
        Region content = (Region) feedback.lookup(".content");
        double availableWidth = feedback.lookup(".viewport").getBoundsInLocal().getWidth()
                - content.getInsets().getLeft() - content.getInsets().getRight();
        feedback.getText().lines().forEach(line -> {
            Text measurement = new Text(line);
            measurement.setFont(feedback.getFont());
            assertTrue(measurement.getLayoutBounds().getWidth() <= availableWidth,
                    "Line should fit without wrapping: " + line);
        });
    }

    /**
     * Returns the stage of a new {@code MainWindow} whose saved window settings are {@code guiSettings}.
     */
    private Stage createMainWindowStage(GuiSettings guiSettings) {
        return createMainWindow(guiSettings).getPrimaryStage();
    }

    private MainWindow createMainWindow(GuiSettings guiSettings) {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(guiSettings);
        Path dataPath = temporaryFolder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(new ModelManager(new AddressBook(), userPrefs), storage);
        Stage stage = new Stage();
        return new MainWindow(stage, logic, dataPath);
    }
}
