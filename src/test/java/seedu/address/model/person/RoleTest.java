package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void isValidRole() {
        // invalid roles
        assertFalse(Role.isValidRole(null));
        assertFalse(Role.isValidRole(""));
        assertFalse(Role.isValidRole(" "));
        assertFalse(Role.isValidRole("teacher"));
        assertFalse(Role.isValidRole("student guardian"));

        // valid roles
        assertTrue(Role.isValidRole("student"));
        assertTrue(Role.isValidRole("STUDENT"));
        assertTrue(Role.isValidRole("guardian"));
        assertTrue(Role.isValidRole("GUARDIAN"));
        assertTrue(Role.isValidRole("  GuArDiAn  "));
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Role.fromString(null));
    }

    @Test
    public void fromString_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("teacher"));
    }

    @Test
    public void fromString_validRoles_returnsRole() {
        assertEquals(Role.STUDENT, Role.fromString("student"));
        assertEquals(Role.GUARDIAN, Role.fromString("  GuArDiAn  "));
    }

    @Test
    public void toStringMethod() {
        assertEquals("Student", Role.STUDENT.toString());
        assertEquals("Guardian", Role.GUARDIAN.toString());
    }
}
