package seedu.address;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Runs packaged application sessions in separate processes, using real command fields on Windows.
 */
public final class ConcurrentAppTestLauncher {
    private static MainApp app;
    private static Stage stage;

    private ConcurrentAppTestLauncher() {
    }

    /**
     * Uses filesystem signals to keep both sessions open before either is allowed to save.
     */
    public static void main(String[] args) throws Exception {
        Path control = Path.of(args[0]);
        String session = args[1];
        if (args[2].equals("lock")) {
            holdWriteLock(control, session);
            return;
        }
        boolean showsGui = System.getProperty("os.name").startsWith("Windows");
        if (showsGui) {
            Platform.startup(() -> { });
            onFx(() -> {
                app = new MainApp();
                try {
                    app.init();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
                stage = new Stage();
                app.start(stage);
            });
        } else {
            app = new MainApp();
            app.init();
        }
        try {
            Files.writeString(control.resolve(session + ".ready"), showsGui ? "GUI" : "HEADLESS");
            awaitSignal(control.resolve(session + ".go"));
            String feedback;
            if (showsGui) {
                onFx(() -> {
                    TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                    command.setText("add r/student n/" + args[2]);
                    command.fireEvent(new ActionEvent());
                });
                CompletableFuture<String> result = new CompletableFuture<>();
                Platform.runLater(() -> result.complete(
                        ((TextArea) stage.getScene().lookup("#resultDisplay")).getText()));
                feedback = result.get(15, TimeUnit.SECONDS);
            } else {
                try {
                    feedback = app.logic.execute("add r/student n/" + args[2]).getFeedbackToUser();
                } catch (seedu.address.logic.commands.exceptions.CommandException exception) {
                    feedback = exception.getMessage();
                }
            }
            Files.writeString(control.resolve(session + ".result"), feedback);
            awaitSignal(control.resolve(session + ".stop"));
        } finally {
            if (showsGui) {
                onFx(() -> {
                    app.stop();
                    stage.hide();
                });
                Platform.exit();
            } else {
                app.stop();
            }
        }
    }

    private static void holdWriteLock(Path control, String session) throws Exception {
        Path data = Files.createDirectories(control.resolve("data"));
        try (FileChannel channel = FileChannel.open(data.resolve("addressbook.json.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            Files.writeString(control.resolve(session + ".ready"), "LOCKED");
            awaitSignal(control.resolve(session + ".stop"));
        }
    }

    private static void onFx(Runnable action) throws Exception {
        CompletableFuture<Void> complete = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                complete.complete(null);
            } catch (Throwable exception) {
                complete.completeExceptionally(exception);
            }
        });
        complete.get(15, TimeUnit.SECONDS);
    }

    private static void awaitSignal(Path signal) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(45);
        while (!Files.exists(signal)) {
            if (System.nanoTime() >= deadline) {
                throw new IllegalStateException("Timed out waiting for " + signal);
            }
            Thread.sleep(25);
        }
    }
}
