package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks how {@code MainWindow} restores its saved size and position, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
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

    /**
     * Returns the stage of a new {@code MainWindow} whose saved window settings are {@code guiSettings}.
     */
    private Stage createMainWindowStage(GuiSettings guiSettings) {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(guiSettings);
        Path dataPath = temporaryFolder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(new ModelManager(new AddressBook(), userPrefs), storage);
        Stage stage = new Stage();
        new MainWindow(stage, logic, dataPath);
        return stage;
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
