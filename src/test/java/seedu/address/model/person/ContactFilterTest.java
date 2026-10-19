package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ContactFilterTest {

    private final Person activeStudent = new PersonBuilder().withName("Alex Tan").build();
    private final Person formerStudent = new PersonBuilder().withName("Bea Tan")
            .withStatus(StudentStatus.FORMER).build();
    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactFilter(null, StudentStatus.ACTIVE));
        assertThrows(NullPointerException.class, () -> new ContactFilter(Optional.empty(), null));
    }

    @Test
    public void test_activeContacts_matchesGuardiansAndActiveStudents() {
        assertTrue(ContactFilter.ACTIVE_CONTACTS.test(activeStudent));
        assertTrue(ContactFilter.ACTIVE_CONTACTS.test(guardian));
        assertFalse(ContactFilter.ACTIVE_CONTACTS.test(formerStudent));
    }

    @Test
    public void test_formerStudents_matchesFormerStudentsOnly() {
        assertTrue(ContactFilter.FORMER_STUDENTS.test(formerStudent));
        assertFalse(ContactFilter.FORMER_STUDENTS.test(activeStudent));
        assertFalse(ContactFilter.FORMER_STUDENTS.test(guardian));
    }

    @Test
    public void test_studentRole_matchesActiveStudentsOnly() {
        ContactFilter filter = new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE);
        assertTrue(filter.test(activeStudent));
        assertFalse(filter.test(formerStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_guardianRole_matchesGuardiansOnly() {
        ContactFilter filter = new ContactFilter(Optional.of(Role.GUARDIAN), StudentStatus.ACTIVE);
        assertTrue(filter.test(guardian));
        assertFalse(filter.test(activeStudent));
        assertFalse(filter.test(formerStudent));
    }

    @Test
    public void test_studentRoleAndFormer_matchesFormerStudentsOnly() {
        ContactFilter filter = new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.FORMER);
        assertTrue(filter.test(formerStudent));
        assertFalse(filter.test(activeStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void isFormerOnly() {
        assertTrue(ContactFilter.FORMER_STUDENTS.isFormerOnly());
        assertFalse(ContactFilter.ACTIVE_CONTACTS.isFormerOnly());
    }

    @Test
    public void equals() {
        ContactFilter studentFilter = new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE);

        // same object -> returns true
        assertTrue(studentFilter.equals(studentFilter));

        // same values -> returns true
        ContactFilter studentFilterCopy = new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE);
        assertTrue(studentFilter.equals(studentFilterCopy));
        assertEquals(studentFilter.hashCode(), studentFilterCopy.hashCode());

        // different types -> returns false
        assertFalse(studentFilter.equals(1));

        // null -> returns false
        assertFalse(studentFilter.equals(null));

        // different role -> returns false
        assertFalse(studentFilter.equals(new ContactFilter(Optional.of(Role.GUARDIAN), StudentStatus.ACTIVE)));
        assertFalse(studentFilter.equals(ContactFilter.ACTIVE_CONTACTS));

        // different status -> returns false
        assertFalse(studentFilter.equals(new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.FORMER)));
    }

    @Test
    public void toStringMethod() {
        ContactFilter filter = new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.FORMER);
        String expected = ContactFilter.class.getCanonicalName() + "{role=" + Optional.of(Role.STUDENT)
                + ", status=" + StudentStatus.FORMER + "}";
        assertEquals(expected, filter.toString());
    }
}
