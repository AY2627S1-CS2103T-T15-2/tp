package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.layout.Region;

public class UiUtilTest {

    @Test
    public void setShown_true_showsNodeAndKeepsItsSpace() {
        Node node = new Region();
        UiUtil.setShown(node, true);
        assertShown(node);
    }

    @Test
    public void setShown_false_hidesNodeAndFreesItsSpace() {
        Node node = new Region();
        UiUtil.setShown(node, false);
        assertHidden(node);
    }

    @Test
    public void setShown_falseThenTrue_showsNodeAgain() {
        Node node = new Region();
        UiUtil.setShown(node, false);
        UiUtil.setShown(node, true);
        assertShown(node);
    }

    @Test
    public void setShown_trueThenFalse_hidesNode() {
        Node node = new Region();
        UiUtil.setShown(node, true);
        UiUtil.setShown(node, false);
        assertHidden(node);
    }

    @Test
    public void setShown_falseTwice_staysHidden() {
        Node node = new Region();
        UiUtil.setShown(node, false);
        UiUtil.setShown(node, false);
        assertHidden(node);
    }

    @Test
    public void setShown_nullNode_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> UiUtil.setShown(null, true));
    }

    private static void assertShown(Node node) {
        assertTrue(node.isVisible());
        assertTrue(node.isManaged());
    }

    private static void assertHidden(Node node) {
        assertFalse(node.isVisible());
        assertFalse(node.isManaged());
    }
}
