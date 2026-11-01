package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;

public class GuiSettingsTest {

    /** Visible bounds of a 1280x720 screen at 150% scaling. */
    private static final Rectangle2D SMALL_SCREEN = new Rectangle2D(0, 0, 853, 480);

    @Test
    public void fitWithin_nullBounds_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GuiSettings().fitWithin(null));
    }

    @Test
    public void fitWithin_windowFits_unchanged() {
        GuiSettings guiSettings = new GuiSettings(740, 400, 50, 40);
        assertEquals(guiSettings, guiSettings.fitWithin(SMALL_SCREEN));
    }

    @Test
    public void fitWithin_savedWindowTallerThanScreen_shrunkAndMovedOntoScreen() {
        GuiSettings guiSettings = new GuiSettings(682, 600, 485, 167);
        assertEquals(new GuiSettings(682, 480, 171, 0), guiSettings.fitWithin(SMALL_SCREEN));
    }

    @Test
    public void fitWithin_windowOffScreen_movedOntoScreen() {
        GuiSettings guiSettings = new GuiSettings(740, 400, 2000, -100);
        assertEquals(new GuiSettings(740, 400, 113, 0), guiSettings.fitWithin(SMALL_SCREEN));
    }

    @Test
    public void fitWithin_screenNotAtOrigin_keptWithinScreen() {
        Rectangle2D secondScreen = new Rectangle2D(1920, 25, 853, 455);
        GuiSettings guiSettings = new GuiSettings(740, 600, 1800, 0);
        assertEquals(new GuiSettings(740, 455, 1920, 25), guiSettings.fitWithin(secondScreen));
    }

    @Test
    public void fitWithin_noPosition_shrunkWithoutPosition() {
        GuiSettings fittedSettings = new GuiSettings().fitWithin(SMALL_SCREEN);
        assertEquals(740, fittedSettings.getWindowWidth());
        assertEquals(480, fittedSettings.getWindowHeight());
        assertNull(fittedSettings.getWindowCoordinates());
    }

    @Test
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + "}";
        assertEquals(expected, guiSettings.toString());
    }
}
