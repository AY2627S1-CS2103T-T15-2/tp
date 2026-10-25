package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.scene.Node;

/**
 * Contains utility methods shared by UI components.
 */
final class UiUtil {

    private UiUtil() {
        // Prevents instantiation, as this class only has static methods.
    }

    /**
     * Shows {@code node} and gives it space in the layout if {@code isShown} is true,
     * and hides it and frees its space otherwise.
     */
    static void setShown(Node node, boolean isShown) {
        requireNonNull(node);
        node.setVisible(isShown);
        node.setManaged(isShown);
    }
}
