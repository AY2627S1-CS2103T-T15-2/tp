package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import seedu.address.testutil.JavaFxTestUtil;

@EnabledOnOs(OS.WINDOWS)
public class HelpWindowTest {

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
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
    public void show_newWindowAfterResizingPreviousWindow_usesDefaultSize() throws Exception {
        runOnJavaFxThread(() -> {
            HelpWindow previous = new HelpWindow();
            HelpWindow fresh = null;
            try {
                previous.show();
                previous.getRoot().setWidth(500);
                previous.getRoot().setHeight(350);
                previous.hide();

                fresh = new HelpWindow();
                fresh.show();

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                assertEquals(Math.min(760, screenBounds.getWidth()), fresh.getRoot().getWidth(), 1);
                assertEquals(Math.min(640, screenBounds.getHeight()), fresh.getRoot().getHeight(), 1);
            } finally {
                previous.hide();
                if (fresh != null) {
                    fresh.hide();
                }
            }
        });
    }

    @Test
    public void resize_smallWindow_referenceRemainsScrollable() throws Exception {
        runOnJavaFxThread(() -> {
            HelpWindow help = new HelpWindow();
            try {
                help.show();
                assertEquals(400, help.getRoot().getMinWidth());
                assertEquals(300, help.getRoot().getMinHeight());
                help.getRoot().setWidth(400);
                help.getRoot().setHeight(300);
                ScrollPane scroll = (ScrollPane) help.getRoot().getScene().getRoot();
                scroll.resize(384, 260);
                scroll.applyCss();
                scroll.layout();
                Region content = (Region) scroll.getContent();
                assertTrue(content.getHeight() > scroll.getViewportBounds().getHeight());
                assertTrue(content.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
                Button copyButton = (Button) content.lookup("#copyButton");
                assertTrue(copyButton.getWidth() >= copyButton.prefWidth(-1) - 1);
                assertTrue(copyButton.localToScene(copyButton.getBoundsInLocal()).getMaxX()
                        <= scroll.localToScene(scroll.getBoundsInLocal()).getMaxX());
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
}
