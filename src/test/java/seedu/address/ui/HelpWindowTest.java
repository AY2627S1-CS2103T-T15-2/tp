package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
                assertEquals(HelpWindow.getCommandFormats().size(), formats.size());
                assertTrue(formats.containsAll(HelpWindow.getCommandFormats()));
                assertCopyUrl(root);
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

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
