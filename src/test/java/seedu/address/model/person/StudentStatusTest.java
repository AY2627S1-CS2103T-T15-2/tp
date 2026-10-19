package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StudentStatusTest {

    @Test
    public void isValidStatus() {
        // invalid statuses
        assertFalse(StudentStatus.isValidStatus(null));
        assertFalse(StudentStatus.isValidStatus(""));
        assertFalse(StudentStatus.isValidStatus(" "));
        assertFalse(StudentStatus.isValidStatus("archived"));
        assertFalse(StudentStatus.isValidStatus("active former"));

        // valid statuses
        assertTrue(StudentStatus.isValidStatus("active"));
        assertTrue(StudentStatus.isValidStatus("ACTIVE"));
        assertTrue(StudentStatus.isValidStatus("former"));
        assertTrue(StudentStatus.isValidStatus("  FoRmEr  "));
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StudentStatus.fromString(null));
    }

    @Test
    public void fromString_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StudentStatus.fromString("archived"));
    }

    @Test
    public void fromString_validStatus_returnsStatus() {
        assertEquals(StudentStatus.ACTIVE, StudentStatus.fromString(" active "));
        assertEquals(StudentStatus.FORMER, StudentStatus.fromString("FORMER"));
    }

    @Test
    public void toStringMethod() {
        assertEquals("Active", StudentStatus.ACTIVE.toString());
        assertEquals("Former", StudentStatus.FORMER.toString());
    }
}
