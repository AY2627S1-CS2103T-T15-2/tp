package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;

/**
 * Shared JavaFX startup and thread helpers for UI tests.
 */
public final class JavaFxTestUtil {

    private static final int FX_TASK_TIMEOUT_SECONDS = 20;
    // Allow the layout change, deferred resize and subsequent relayout to settle.
    private static final int LAYOUT_PULSE_COUNT = 3;
    // A few layout pulses should finish well before the timeout for a general JavaFX task.
    private static final int LAYOUT_TIMEOUT_SECONDS = 5;

    private JavaFxTestUtil() {
    }

    /**
     * Starts JavaFX if necessary and keeps it running between UI test classes.
     */
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    /**
     * Runs {@code action} on the JavaFX thread, waiting up to 20 seconds for completion.
     * Exceptions and assertion failures from the action are propagated through the task.
     * Must be called from a non-JavaFX thread after {@link #startJavaFx()}.
     */
    public static void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(FX_TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits for three layout pulses, including deferred changes requested by layout listeners.
     * Must be called from a non-JavaFX thread while the root belongs to a showing window.
     */
    public static void waitForLayout(Parent root) throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        runOnJavaFxThread(() -> new AnimationTimer() {
            private int pulses;

            @Override
            public void handle(long now) {
                root.applyCss();
                root.layout();
                if (++pulses == LAYOUT_PULSE_COUNT) {
                    stop();
                    finished.complete(null);
                }
            }
        }.start());
        finished.get(LAYOUT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
