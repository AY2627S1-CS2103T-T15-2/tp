package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "ResultDisplay.fxml";
    private static final int MINIMUM_ROW_COUNT = 3;
    private static final double MAXIMUM_SCENE_HEIGHT_RATIO = 0.25;
    private static final double DEFAULT_MAXIMUM_HEIGHT = 200;
    private static final double TEXT_HEIGHT_MARGIN = 2;

    private final ChangeListener<Number> sceneHeightListener = (observable, oldHeight, newHeight) -> requestResize();
    private boolean isResizePending;
    private boolean isScrollResetPending;

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a wrapping feedback display whose height adapts to its text and available window space.
     */
    public ResultDisplay() {
        super(FXML);
        observeLayoutChanges();
    }

    /**
     * Shows {@code feedbackToUser} as the result of a successful command.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.getStyleClass().remove(ERROR_STYLE_CLASS);
        showMessage(feedbackToUser);
    }

    /**
     * Shows {@code errorMessage} as an error, such as the reason a command failed.
     */
    public void setErrorToUser(String errorMessage) {
        requireNonNull(errorMessage);
        if (!resultDisplay.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        showMessage(errorMessage);
    }

    private void showMessage(String message) {
        resultDisplay.setText(message);
        isScrollResetPending = true;
        resetScrollPosition();
        requestResize();
    }

    private void observeLayoutChanges() {
        resultDisplay.widthProperty().addListener(observable -> requestResize());
        resultDisplay.fontProperty().addListener(observable -> requestResize());
        resultDisplay.insetsProperty().addListener(observable -> requestResize());
        resultDisplay.skinProperty().addListener(observable -> requestResize());
        resultDisplay.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.heightProperty().removeListener(sceneHeightListener);
            }
            if (newScene != null) {
                newScene.heightProperty().addListener(sceneHeightListener);
            }
            requestResize();
        });
    }

    private void requestResize() {
        if (isResizePending) {
            return;
        }
        isResizePending = true;
        Platform.runLater(() -> {
            isResizePending = false;
            resizeToFitMessage();
            if (isScrollResetPending) {
                resetScrollPosition();
                isScrollResetPending = false;
            }
        });
    }

    private void resizeToFitMessage() {
        Region content = (Region) resultDisplay.lookup(".content");
        ScrollPane scrollPane = (ScrollPane) resultDisplay.lookup(".scroll-pane");
        if (content == null || scrollPane == null || resultDisplay.getWidth() <= 0) {
            return;
        }
        Insets controlInsets = resultDisplay.getInsets();
        Insets scrollInsets = scrollPane.getInsets();
        Insets contentInsets = content.getInsets();
        double wrappingWidth = resultDisplay.getWidth() - controlInsets.getLeft() - controlInsets.getRight()
                - scrollInsets.getLeft() - scrollInsets.getRight() - contentInsets.getLeft() - contentInsets.getRight()
                - getVerticalScrollBarWidth();
        Text measurement = new Text(resultDisplay.getText());
        measurement.setFont(resultDisplay.getFont());
        measurement.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        measurement.setWrappingWidth(Math.max(1, wrappingWidth));
        double textHeight = Math.max(measurement.getLayoutBounds().getHeight(), getMinimumTextHeight());
        double preferredHeight = Math.ceil(textHeight + controlInsets.getTop() + controlInsets.getBottom()
                + scrollInsets.getTop() + scrollInsets.getBottom() + contentInsets.getTop() + contentInsets.getBottom()
                + TEXT_HEIGHT_MARGIN);
        resultDisplay.setPrefHeight(Math.min(preferredHeight, getMaximumHeight()));
    }

    private double getMinimumTextHeight() {
        Text row = new Text("Ag");
        row.setFont(resultDisplay.getFont());
        row.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return row.getLayoutBounds().getHeight() * MINIMUM_ROW_COUNT;
    }

    private double getMaximumHeight() {
        if (resultDisplay.getScene() == null || resultDisplay.getScene().getHeight() <= 0) {
            return DEFAULT_MAXIMUM_HEIGHT;
        }
        return resultDisplay.getScene().getHeight() * MAXIMUM_SCENE_HEIGHT_RATIO;
    }

    private double getVerticalScrollBarWidth() {
        return resultDisplay.lookupAll(".scroll-bar").stream()
                .map(node -> (ScrollBar) node)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                .mapToDouble(scrollBar -> scrollBar.prefWidth(-1))
                .max().orElse(0);
    }

    private void resetScrollPosition() {
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
        resultDisplay.setScrollLeft(0);
    }
}
