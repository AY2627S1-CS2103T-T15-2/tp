package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void isValidRole() {
        assertFalse(Role.isValidRole(null));
        assertFalse(Role.isValidRole(""));
        assertFalse(Role.isValidRole("teacher"));

        assertTrue(Role.isValidRole("STUDENT"));
        assertTrue(Role.isValidRole("student"));
        assertTrue(Role.isValidRole(" Guardian "));
    }

    @Test
    public void toStringMethod() {
        assertEquals("Student", Role.STUDENT.toString());
        assertEquals("Guardian", Role.GUARDIAN.toString());
    }
}
