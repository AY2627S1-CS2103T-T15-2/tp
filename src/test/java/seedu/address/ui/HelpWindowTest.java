package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;

@EnabledOnOs(OS.WINDOWS)
public class HelpWindowTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    @Test
    public void show_commandReferenceAndCopyUrl_available() throws Exception {
        runOnJavaFxThread(() -> {
            HelpWindow help = new HelpWindow();
            try {
                help.show();
                Region root = (Region) help.getRoot().getScene().getRoot();
                root.applyCss();
                root.layout();
                TextField url = (TextField) root.lookup("#userGuideUrl");
                assertEquals(HelpWindow.USERGUIDE_URL, url.getText());
                assertFalse(url.isEditable());
                List<String> formats = root.lookupAll(".command-format").stream()
                        .map(node -> ((Label) node).getText()).toList();
                assertEquals(9, formats.size());
                assertTrue(formats.containsAll(List.of("add r/ROLE n/NAME [p/PHONE] [e/EMAIL]",
                        "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]", "delete INDEX", "find NAME",
                        "link s/STUDENT_INDEX g/GUARDIAN_INDEX", "list", "clear", "help", "exit")));
                assertCopyUrl(root);
                saveScreenshot(root);
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void resize_smallWindow_referenceRemainsScrollable() throws Exception {
        runOnJavaFxThread(() -> {
            HelpWindow help = new HelpWindow();
            try {
                help.show();
                help.getRoot().setWidth(360);
                help.getRoot().setHeight(300);
                ScrollPane scroll = (ScrollPane) help.getRoot().getScene().getRoot();
                scroll.resize(344, 260);
                scroll.applyCss();
                scroll.layout();
                Region content = (Region) scroll.getContent();
                assertTrue(content.getHeight() > scroll.getViewportBounds().getHeight());
                assertTrue(content.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
                scroll.setVvalue(1);
                assertEquals(1, scroll.getVvalue());
            } finally {
                help.hide();
            }
        });
    }

    private void assertCopyUrl(Region root) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent original = new ClipboardContent();
        clipboard.getContentTypes().forEach(type -> original.put(type, clipboard.getContent(type)));
        try {
            Button copyButton = (Button) root.lookup("#copyButton");
            copyButton.fire();
            assertEquals(HelpWindow.USERGUIDE_URL, clipboard.getString());
        } finally {
            clipboard.setContent(original);
        }
    }

    private void saveScreenshot(Region root) {
        WritableImage snapshot = root.snapshot(null, null);
        BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        try {
            Path destination = Path.of("build", "reports", "help-window.png");
            Files.createDirectories(destination.getParent());
            ImageIO.write(image, "png", destination.toFile());
        } catch (java.io.IOException exception) {
            throw new AssertionError("Could not save Help window screenshot", exception);
        }
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
