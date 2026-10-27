package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.parser.AddCommandParser;

/**
 * Checks how {@code ResultDisplay} shows successful results and errors, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class ResultDisplayTest {

    // Nested layout panes can snap their dimensions separately on scaled displays.
    private static final double LAYOUT_TOLERANCE = 2;

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

    @Test
    public void setErrorToUser_longError_growsAndWrapsWithinWindow() throws Exception {
        FeedbackFixture fixture = createFixture(900, 800);
        try {
            double initialHeight = callOnJavaFxThread(() -> fixture.text().getHeight());
            runOnJavaFxThread(() -> fixture.display().setErrorToUser(AddCommandParser.MESSAGE_MISSING_ROLE));
            settleLayout(fixture);
            runOnJavaFxThread(() -> {
                assertTrue(fixture.text().getHeight() > initialHeight);
                assertTrue(fixture.text().getHeight()
                        <= fixture.stage().getScene().getHeight() * 0.25 + LAYOUT_TOLERANCE);
                assertTrue(fixture.text().isWrapText());
                assertFalse(hasVisibleScrollBar(fixture.text(), Orientation.HORIZONTAL));
                assertEquals(0, fixture.text().getCaretPosition());
                assertEquals(0, fixture.text().getScrollTop());
                assertTrue(fixture.contacts().getHeight() > fixture.text().getHeight());
            });
        } finally {
            runOnJavaFxThread(() -> fixture.stage().close());
        }
    }

    @Test
    public void resize_narrowerWindow_growsForWrappedLines() throws Exception {
        FeedbackFixture fixture = createFixture(1000, 900);
        try {
            runOnJavaFxThread(() -> fixture.display().setErrorToUser(
                    "Please check the supplied contact details. ".repeat(6)));
            settleLayout(fixture);
            double wideHeight = callOnJavaFxThread(() -> fixture.text().getHeight());
            runOnJavaFxThread(() -> fixture.stage().setWidth(500));
            settleLayout(fixture);
            runOnJavaFxThread(() -> {
                assertTrue(fixture.text().getHeight() > wideHeight);
                assertFalse(hasVisibleScrollBar(fixture.text(), Orientation.HORIZONTAL));
                assertFalse(hasVisibleScrollBar(fixture.text(), Orientation.VERTICAL), describeLayout(fixture));
            });
        } finally {
            runOnJavaFxThread(() -> fixture.stage().close());
        }
    }

    @Test
    public void setFeedbackToUser_afterScrolledError_shrinksAndStartsAtTop() throws Exception {
        FeedbackFixture fixture = createFixture(900, 800);
        try {
            double initialHeight = callOnJavaFxThread(() -> fixture.text().getHeight());
            runOnJavaFxThread(() -> fixture.display().setErrorToUser("Invalid contact details.\n".repeat(100)));
            settleLayout(fixture);
            runOnJavaFxThread(() -> {
                assertTrue(hasVisibleScrollBar(fixture.text(), Orientation.VERTICAL));
                fixture.text().positionCaret(fixture.text().getLength());
                fixture.text().setScrollTop(500);
                fixture.display().setFeedbackToUser("Listed 2 contacts");
            });
            settleLayout(fixture);
            runOnJavaFxThread(() -> {
                assertEquals(initialHeight, fixture.text().getHeight(), 1);
                assertEquals(0, fixture.text().getCaretPosition());
                assertEquals(0, fixture.text().getScrollTop());
                assertEquals(0, fixture.text().getScrollLeft());
                assertFalse(hasVisibleScrollBar(fixture.text(), Orientation.VERTICAL));
            });
        } finally {
            runOnJavaFxThread(() -> fixture.stage().close());
        }
    }

    @Test
    public void resize_shorterWindow_reducesFeedbackHeightCap() throws Exception {
        FeedbackFixture fixture = createFixture(900, 800);
        try {
            runOnJavaFxThread(() -> fixture.display().setErrorToUser("Invalid contact details.\n".repeat(100)));
            settleLayout(fixture);
            double tallHeight = callOnJavaFxThread(() -> fixture.text().getHeight());
            runOnJavaFxThread(() -> fixture.stage().setHeight(400));
            settleLayout(fixture);
            runOnJavaFxThread(() -> {
                assertTrue(fixture.text().getHeight() < tallHeight);
                assertTrue(fixture.text().getHeight()
                        <= fixture.stage().getScene().getHeight() * 0.25 + LAYOUT_TOLERANCE,
                        describeLayout(fixture));
                assertTrue(hasVisibleScrollBar(fixture.text(), Orientation.VERTICAL));
                assertFalse(hasVisibleScrollBar(fixture.text(), Orientation.HORIZONTAL));
            });
        } finally {
            runOnJavaFxThread(() -> fixture.stage().close());
        }
    }

    private FeedbackFixture createFixture(double width, double height) throws Exception {
        FeedbackFixture fixture = callOnJavaFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            Region contacts = new Region();
            VBox.setVgrow(contacts, Priority.ALWAYS);
            VBox layout = new VBox(display.getRoot(), contacts);
            Scene scene = new Scene(layout, width, height);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setOpacity(0);
            stage.setScene(scene);
            stage.show();
            return new FeedbackFixture(display, stage, layout, contacts);
        });
        settleLayout(fixture);
        return fixture;
    }

    private void settleLayout(FeedbackFixture fixture) throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        runOnJavaFxThread(() -> new AnimationTimer() {
            private int pulses;

            @Override
            public void handle(long now) {
                fixture.layout().applyCss();
                fixture.layout().layout();
                if (++pulses == 3) {
                    stop();
                    finished.complete(null);
                }
            }
        }.start());
        finished.get(5, TimeUnit.SECONDS);
    }

    private boolean hasVisibleScrollBar(TextArea text, Orientation orientation) {
        return text.lookupAll(".scroll-bar").stream().map(node -> (ScrollBar) node)
                .anyMatch(scrollBar -> scrollBar.getOrientation() == orientation && scrollBar.isVisible());
    }

    private String describeLayout(FeedbackFixture fixture) {
        return "Height=" + fixture.text().getHeight() + ", preferred=" + fixture.text().getPrefHeight()
                + ", scene=" + fixture.stage().getScene().getHeight() + ", width=" + fixture.text().getWidth()
                + ", content=" + fixture.text().lookup(".content").getLayoutBounds()
                + ", text=" + fixture.text().lookup(".text").getLayoutBounds();
    }

    private record FeedbackFixture(ResultDisplay display, Stage stage, VBox layout, Region contacts) {
        TextArea text() {
            return (TextArea) display.getRoot().lookup("#resultDisplay");
        }
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        callOnJavaFxThread(() -> {
            action.run();
            return null;
        });
    }

    private <T> T callOnJavaFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
