package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Checks how {@code ResultDisplay} shows successful results and errors, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class ResultDisplayTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    @Test
    public void setErrorToUser_thenFeedback_errorStyleAddedOnceThenRemoved() throws Exception {
        runOnJavaFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            TextArea text = (TextArea) resultDisplay.getRoot().lookup("#resultDisplay");

            resultDisplay.setErrorToUser("Unknown command.");
            resultDisplay.setErrorToUser("Unknown command.");
            assertEquals("Unknown command.", text.getText());
            assertEquals(1, text.getStyleClass().stream().filter(ResultDisplay.ERROR_STYLE_CLASS::equals).count());

            resultDisplay.setFeedbackToUser("Listed 2 contacts");
            assertEquals("Listed 2 contacts", text.getText());
            assertFalse(text.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void setFeedbackToUser_null_throwsNullPointerException() throws Exception {
        runOnJavaFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            assertThrows(NullPointerException.class, () -> resultDisplay.setFeedbackToUser(null));
        });
    }

    @Test
    public void setErrorToUser_null_throwsNullPointerException() throws Exception {
        runOnJavaFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            assertThrows(NullPointerException.class, () -> resultDisplay.setErrorToUser(null));
        });
    }

    @Test
    public void setFeedbackToUser_newDisplay_noErrorStyle() throws Exception {
        runOnJavaFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            resultDisplay.setFeedbackToUser("Added contact: Alex Tan [Student].");
            TextArea text = (TextArea) resultDisplay.getRoot().lookup("#resultDisplay");
            assertTrue(text.getStyleClass().contains("result-display"));
            assertFalse(text.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
        });
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
