package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
    }

    @Test
    public void constructor_invalidSubject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Subject(""));
    }

    @Test
    public void isValidSubjectAndConstructor_reservedNone_rejected() {
        for (String value : List.of("NONE", "none", "None", "nOnE")) {
            assertFalse(Subject.isValidSubject(value));
            assertThrows(IllegalArgumentException.class, () -> new Subject(value));
            assertEquals(Subject.MESSAGE_RESERVED_VALUE, Subject.getValidationErrorMessage(value));
        }
        assertTrue(Subject.isValidSubject("None Studies"));
    }

    @Test
    public void isValidSubject_validValues_returnsTrue() {
        assertTrue(Subject.isValidSubject("A-Math"));
        assertTrue(Subject.isValidSubject("Math"));
        assertTrue(Subject.isValidSubject("IB (HL) Year 1"));
        assertTrue(Subject.isValidSubject("O-Level Pure Chem."));
        assertTrue(Subject.isValidSubject("Math+"));
        assertTrue(Subject.isValidSubject("Combined Science (Chem/Bio)"));
        assertTrue(Subject.isValidSubject("English Language & Literature"));
        assertTrue(Subject.isValidSubject("中三")); // non-English letters
        assertTrue(Subject.isValidSubject("a".repeat(Subject.MAX_LENGTH))); // maximum length
    }

    @Test
    public void constructor_combiningMarks_preservesTextAndValidationLimits() {
        for (String value : List.of("\u0ba4\u0bae\u0bbf\u0bb4\u0bcd", "\u0939\u093f\u0928\u094d\u0926\u0940",
                "Franc\u0327ais", "e\u0301".repeat(15))) {
            assertEquals(value, new Subject(value).value);
        }
        assertEquals(Subject.MESSAGE_MISSING_ALPHANUMERIC, Subject.getValidationErrorMessage("\u0301\u0327"));
        assertEquals(Subject.MESSAGE_TOO_LONG, Subject.getValidationErrorMessage("e\u0301".repeat(15) + "a"));
    }

    @Test
    public void isValidSubject_invalidValues_returnsFalse() {
        assertFalse(Subject.isValidSubject("")); // empty
        assertFalse(Subject.isValidSubject("   ")); // spaces only
        assertFalse(Subject.isValidSubject("-.+()")); // no letter or digit
        assertFalse(Subject.isValidSubject(" A-Math")); // leading space
        assertFalse(Subject.isValidSubject("A-Math ")); // trailing space
        assertFalse(Subject.isValidSubject("/&")); // separators alone are not a subject
        assertFalse(Subject.isValidSubject("Sec #3")); // other symbol
        assertFalse(Subject.isValidSubject("a".repeat(Subject.MAX_LENGTH + 1))); // too long
    }

    @Test
    public void getValidationErrorMessage_reportsSpecificConstraint() {
        assertThrows(NullPointerException.class, () -> Subject.getValidationErrorMessage(null));
        assertEquals(Subject.MESSAGE_TOO_LONG, Subject.getValidationErrorMessage("a".repeat(31)));
        assertEquals(Subject.MESSAGE_INVALID_CHARACTERS, Subject.getValidationErrorMessage("A#B"));
        assertEquals(Subject.MESSAGE_MISSING_ALPHANUMERIC, Subject.getValidationErrorMessage("-.+()"));
        assertEquals("Subject cannot start or end with spaces.", Subject.getValidationErrorMessage(" A "));
        assertNull(Subject.getValidationErrorMessage("中".repeat(30)));
        assertNull(Subject.getValidationErrorMessage("\uD801\uDC00".repeat(30)));
        assertEquals(Subject.MESSAGE_TOO_LONG, Subject.getValidationErrorMessage("\uD801\uDC00".repeat(31)));
    }

    @Test
    public void getValidationErrorMessage_multipleViolations_preservesConstraintPriority() {
        assertEquals(Subject.MESSAGE_TOO_LONG, Subject.getValidationErrorMessage("&".repeat(31)));
        assertEquals(Subject.MESSAGE_INVALID_CHARACTERS, Subject.getValidationErrorMessage(" # "));
        assertEquals(Subject.MESSAGE_MISSING_ALPHANUMERIC, Subject.getValidationErrorMessage(" --- "));
    }

    @Test
    public void isSameValue() {
        Subject subject = new Subject("A-Math");
        assertTrue(subject.isSameValue(new Subject("A-Math")));
        assertTrue(subject.isSameValue(new Subject("a - math")));
        assertFalse(subject.isSameValue(new Subject("Math")));
        assertFalse(subject.isSameValue(null));
    }

    @Test
    public void isSameValue_canonicallyEquivalentText_matchesBothDirections() {
        for (String[] pair : List.of(new String[] {"Fran\u00e7ais", "Franc\u0327ais", "composed and decomposed accent"},
                new String[] {"FRAN \u00c7AIS", "franc\u0327ais", "case and spacing"},
                new String[] {"FRANC \u0327AIS", "fran\u00e7ais", "space before combining mark"},
                new String[] {"s\u0323\u0307", "s\u0307\u0323", "combining mark order"},
                new String[] {"J\u030c", "j\u030c", "normalization after case conversion"},
                new String[] {"\uac00", "\u1100\u1161", "Hangul decomposition"})) {
            Subject first = new Subject(pair[0]);
            Subject second = new Subject(pair[1]);
            assertTrue(first.isSameValue(second), pair[2] + ": " + pair[0] + " vs " + pair[1]);
            assertTrue(second.isSameValue(first), pair[2] + ": " + pair[1] + " vs " + pair[0]);
        }
        assertFalse(new Subject("Fran\u00e7ais").isSameValue(new Subject("Francais")));
        assertFalse(new Subject("Fran\u00e7ais").isSameValue(new Subject("\uff26ran\u00e7ais")));
    }

    @Test
    public void isSameValue_greekSigmaVariants_matchesBothDirections() {
        for (String first : List.of("\u039f\u03a3", "\u03bf\u03c3", "\u03bf\u03c2")) {
            for (String second : List.of("\u039f\u03a3", "\u03bf\u03c3", "\u03bf\u03c2")) {
                assertTrue(new Subject(first).isSameValue(new Subject(second)), first + " vs " + second);
            }
        }
        assertFalse(new Subject("\u03bf\u03c3").isSameValue(new Subject("\u03cc\u03c3")));
    }

    @Test
    public void equals() {
        Subject subject = new Subject("A-Math");

        // same values -> returns true
        assertTrue(subject.equals(new Subject("A-Math")));

        // same object -> returns true
        assertTrue(subject.equals(subject));

        // null -> returns false
        assertFalse(subject.equals(null));

        // different types -> returns false
        assertFalse(subject.equals(5.0f));

        // different capitalisation -> returns false, as the saved text is kept as typed
        assertFalse(subject.equals(new Subject("a - math")));

        // different values -> returns false
        assertFalse(subject.equals(new Subject("Math")));
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("A-Math", new Subject("A-Math").toString());
    }
}
