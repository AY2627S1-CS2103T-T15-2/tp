package seedu.address.commons.core;

import static java.util.Objects.requireNonNull;

import java.awt.Point;
import java.io.Serializable;
import java.util.Objects;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.util.ToStringBuilder;

/**
 * A Serializable class that contains the GUI settings.
 * Guarantees: immutable.
 */
public class GuiSettings implements Serializable {

    private static final double DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_WIDTH = 1100;

    private final double windowWidth;
    private final double windowHeight;
    private final Point windowCoordinates;

    /**
     * Constructs a {@code GuiSettings} with the default height, width and position.
     */
    public GuiSettings() {
        windowWidth = DEFAULT_WIDTH;
        windowHeight = DEFAULT_HEIGHT;
        windowCoordinates = null; // null represent no coordinates
    }

    /**
     * Constructs a {@code GuiSettings} with the specified height, width and position.
     */
    public GuiSettings(double windowWidth, double windowHeight, int xPosition, int yPosition) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        windowCoordinates = new Point(xPosition, yPosition);
    }

    /**
     * Constructs a {@code GuiSettings} with the specified height, width and position.
     * A null {@code windowCoordinates} represents no position.
     */
    private GuiSettings(double windowWidth, double windowHeight, Point windowCoordinates) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.windowCoordinates = windowCoordinates;
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public Point getWindowCoordinates() {
        return windowCoordinates != null ? new Point(windowCoordinates) : null;
    }

    /**
     * Returns a copy of these settings in which the window is shrunk and moved so that it fits
     * within {@code bounds}. A window size that already fits is kept, and so is a window position
     * that already keeps the window within {@code bounds}. Settings without a position stay without one.
     */
    public GuiSettings fitWithin(Rectangle2D bounds) {
        requireNonNull(bounds);
        double fittedWidth = Math.min(windowWidth, bounds.getWidth());
        double fittedHeight = Math.min(windowHeight, bounds.getHeight());
        if (windowCoordinates == null) {
            return new GuiSettings(fittedWidth, fittedHeight, null);
        }

        double maxWindowX = bounds.getMaxX() - fittedWidth;
        double maxWindowY = bounds.getMaxY() - fittedHeight;
        int fittedX = (int) Math.clamp(windowCoordinates.getX(), bounds.getMinX(), maxWindowX);
        int fittedY = (int) Math.clamp(windowCoordinates.getY(), bounds.getMinY(), maxWindowY);
        return new GuiSettings(fittedWidth, fittedHeight, new Point(fittedX, fittedY));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof GuiSettings otherGuiSettings)) {
            return false;
        }

        return windowWidth == otherGuiSettings.windowWidth
                && windowHeight == otherGuiSettings.windowHeight
                && Objects.equals(windowCoordinates, otherGuiSettings.windowCoordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowWidth, windowHeight, windowCoordinates);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("windowWidth", windowWidth)
                .add("windowHeight", windowHeight)
                .add("windowCoordinates", windowCoordinates)
                .toString();
    }
}
