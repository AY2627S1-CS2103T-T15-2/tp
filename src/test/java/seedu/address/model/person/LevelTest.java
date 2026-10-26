package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class LevelTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Level(null));
    }

    @Test
    public void constructor_invalidLevel_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Level(""));
    }

    @Test
    public void isValidLevel_validValues_returnsTrue() {
        assertTrue(Level.isValidLevel("Sec 3"));
        assertTrue(Level.isValidLevel("JC1"));
        assertTrue(Level.isValidLevel("IB (HL) Year 1"));
        assertTrue(Level.isValidLevel("O-Level Pure Chem."));
        assertTrue(Level.isValidLevel("Math+"));
        assertTrue(Level.isValidLevel("中三")); // non-English letters
        assertTrue(Level.isValidLevel("a".repeat(Level.MAX_LENGTH))); // maximum length
    }

    @Test
    public void constructor_combiningMarks_preservesTextAndValidationLimits() {
        for (String value : List.of("\u0ba4\u0bae\u0bbf\u0bb4\u0bcd", "\u0939\u093f\u0928\u094d\u0926\u0940",
                "Franc\u0327ais", "e\u0301".repeat(15))) {
            assertEquals(value, new Level(value).value);
        }
        assertEquals(Level.MESSAGE_MISSING_ALPHANUMERIC, Level.getValidationErrorMessage("\u0301\u0327"));
        assertEquals(Level.MESSAGE_TOO_LONG, Level.getValidationErrorMessage("e\u0301".repeat(15) + "a"));
    }

    @Test
    public void isValidLevel_invalidValues_returnsFalse() {
        assertFalse(Level.isValidLevel("")); // empty
        assertFalse(Level.isValidLevel("   ")); // spaces only
        assertFalse(Level.isValidLevel("-.+()")); // no letter or digit
        assertFalse(Level.isValidLevel(" Sec 3")); // leading space
        assertFalse(Level.isValidLevel("Sec 3 ")); // trailing space
        assertFalse(Level.isValidLevel("Sec/3")); // slash
        assertFalse(Level.isValidLevel("Sec #3")); // other symbol
        assertFalse(Level.isValidLevel("a".repeat(Level.MAX_LENGTH + 1))); // too long
    }

    @Test
    public void getValidationErrorMessage_reportsSpecificConstraint() {
        assertThrows(NullPointerException.class, () -> Level.getValidationErrorMessage(null));
        assertEquals(Level.MESSAGE_TOO_LONG, Level.getValidationErrorMessage("a".repeat(31)));
        assertEquals(Level.MESSAGE_INVALID_CHARACTERS, Level.getValidationErrorMessage("A&B"));
        assertEquals(Level.MESSAGE_MISSING_ALPHANUMERIC, Level.getValidationErrorMessage("-.+()"));
        assertEquals("Level cannot start or end with spaces.", Level.getValidationErrorMessage(" A "));
        assertNull(Level.getValidationErrorMessage("中".repeat(30)));
        assertNull(Level.getValidationErrorMessage("\uD801\uDC00".repeat(30)));
        assertEquals(Level.MESSAGE_TOO_LONG, Level.getValidationErrorMessage("\uD801\uDC00".repeat(31)));
    }

    @Test
    public void getValidationErrorMessage_multipleViolations_preservesConstraintPriority() {
        assertEquals(Level.MESSAGE_TOO_LONG, Level.getValidationErrorMessage("&".repeat(31)));
        assertEquals(Level.MESSAGE_INVALID_CHARACTERS, Level.getValidationErrorMessage(" & "));
        assertEquals(Level.MESSAGE_MISSING_ALPHANUMERIC, Level.getValidationErrorMessage(" --- "));
    }

    @Test
    public void isSameValue() {
        Level level = new Level("Sec 3");
        assertTrue(level.isSameValue(new Level("Sec 3")));
        assertTrue(level.isSameValue(new Level("sec3")));
        assertFalse(level.isSameValue(new Level("JC1")));
        assertFalse(level.isSameValue(null));
    }

    @Test
    public void equals() {
        Level level = new Level("Sec 3");

        // same values -> returns true
        assertTrue(level.equals(new Level("Sec 3")));

        // same object -> returns true
        assertTrue(level.equals(level));

        // null -> returns false
        assertFalse(level.equals(null));

        // different types -> returns false
        assertFalse(level.equals(5.0f));

        // different capitalisation -> returns false, as the saved text is kept as typed
        assertFalse(level.equals(new Level("sec3")));

        // different values -> returns false
        assertFalse(level.equals(new Level("JC1")));
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Sec 3", new Level("Sec 3").toString());
    }
}
