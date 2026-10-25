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
    private final Person sec3MathStudent = new PersonBuilder().withName("Cal Lim")
            .withLevel("Sec 3").withSubject("A-Math").build();
    private final Person sec4MathStudent = new PersonBuilder().withName("Dan Lim")
            .withLevel("Sec 4").withSubject("A-Math").build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactFilter(null, Optional.of(StudentStatus.ACTIVE)));
        assertThrows(NullPointerException.class, () -> new ContactFilter(Optional.empty(), null));
        assertThrows(NullPointerException.class, () -> new ContactFilter(Optional.empty(), Optional.empty(),
                null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new ContactFilter(Optional.empty(), Optional.empty(),
                Optional.empty(), null));
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
    public void test_allContacts_matchesEveryone() {
        assertTrue(ContactFilter.ALL_CONTACTS.test(activeStudent));
        assertTrue(ContactFilter.ALL_CONTACTS.test(formerStudent));
        assertTrue(ContactFilter.ALL_CONTACTS.test(guardian));
    }

    @Test
    public void test_studentRole_matchesActiveStudentsOnly() {
        ContactFilter filter = filterOf(Role.STUDENT, StudentStatus.ACTIVE);
        assertTrue(filter.test(activeStudent));
        assertFalse(filter.test(formerStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_guardianRole_matchesGuardiansOnly() {
        ContactFilter filter = filterOf(Role.GUARDIAN, StudentStatus.ACTIVE);
        assertTrue(filter.test(guardian));
        assertFalse(filter.test(activeStudent));
        assertFalse(filter.test(formerStudent));
    }

    @Test
    public void test_studentRoleAndFormer_matchesFormerStudentsOnly() {
        ContactFilter filter = filterOf(Role.STUDENT, StudentStatus.FORMER);
        assertTrue(filter.test(formerStudent));
        assertFalse(filter.test(activeStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_studentRoleAnyStatus_matchesAllStudents() {
        ContactFilter filter = filterOf(Role.STUDENT, null);
        assertTrue(filter.test(activeStudent));
        assertTrue(filter.test(formerStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_level_matchesStudentsWithSameLevel() {
        ContactFilter filter = detailFilterOf(new Level("Sec 3"), null);
        assertTrue(filter.test(sec3MathStudent));
        // EP: same level written with different letter case and spaces
        assertTrue(detailFilterOf(new Level("sec3"), null).test(sec3MathStudent));
        // EP: different level
        assertFalse(filter.test(sec4MathStudent));
        // EP: student without a level
        assertFalse(filter.test(activeStudent));
        // EP: guardians never have a level
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_subject_matchesStudentsWithSameSubject() {
        ContactFilter filter = detailFilterOf(null, new Subject("A-Math"));
        assertTrue(filter.test(sec3MathStudent));
        assertTrue(filter.test(sec4MathStudent));
        // EP: same subject written with different letter case
        assertTrue(detailFilterOf(null, new Subject("a-math")).test(sec3MathStudent));
        // EP: a subject that contains the chosen subject is still a different subject
        assertFalse(detailFilterOf(null, new Subject("Math")).test(sec3MathStudent));
        // EP: student without a subject
        assertFalse(filter.test(activeStudent));
        assertFalse(filter.test(guardian));
    }

    @Test
    public void test_levelAndSubject_matchesStudentsWithBoth() {
        ContactFilter filter = detailFilterOf(new Level("Sec 3"), new Subject("A-Math"));
        assertTrue(filter.test(sec3MathStudent));
        // EP: same subject, different level
        assertFalse(filter.test(sec4MathStudent));
        // EP: same level, different subject
        assertFalse(detailFilterOf(new Level("Sec 3"), new Subject("Physics")).test(sec3MathStudent));
    }

    @Test
    public void test_levelAndStatus_matchesBoth() {
        Person formerSec3Student = sec3MathStudent.withStatus(StudentStatus.FORMER);
        ContactFilter filter = new ContactFilter(Optional.empty(), Optional.of(StudentStatus.ACTIVE),
                Optional.of(new Level("Sec 3")), Optional.empty());
        assertTrue(filter.test(sec3MathStudent));
        assertFalse(filter.test(formerSec3Student));
    }

    @Test
    public void hasStudentDetail() {
        assertTrue(detailFilterOf(new Level("Sec 3"), null).hasStudentDetail());
        assertTrue(detailFilterOf(null, new Subject("Math")).hasStudentDetail());
        assertFalse(ContactFilter.ACTIVE_CONTACTS.hasStudentDetail());
        assertFalse(filterOf(Role.STUDENT, null).hasStudentDetail());
    }

    @Test
    public void isFormerOnly() {
        assertTrue(ContactFilter.FORMER_STUDENTS.isFormerOnly());
        assertTrue(filterOf(Role.STUDENT, StudentStatus.FORMER).isFormerOnly());
        assertFalse(ContactFilter.ACTIVE_CONTACTS.isFormerOnly());
        assertFalse(ContactFilter.ALL_CONTACTS.isFormerOnly());
    }

    @Test
    public void hidesFormerStudents() {
        assertTrue(ContactFilter.ACTIVE_CONTACTS.hidesFormerStudents());
        assertTrue(filterOf(Role.STUDENT, StudentStatus.ACTIVE).hidesFormerStudents());
        assertFalse(ContactFilter.FORMER_STUDENTS.hidesFormerStudents());
        assertFalse(ContactFilter.ALL_CONTACTS.hidesFormerStudents());
    }

    @Test
    public void withAnyStatus_keepsRoleAndDropsStatus() {
        assertEquals(filterOf(Role.STUDENT, null), filterOf(Role.STUDENT, StudentStatus.ACTIVE).withAnyStatus());
        assertEquals(ContactFilter.ALL_CONTACTS, ContactFilter.FORMER_STUDENTS.withAnyStatus());
    }

    @Test
    public void withAnyStatus_keepsLevelAndSubject() {
        Optional<Level> level = Optional.of(new Level("Sec 3"));
        Optional<Subject> subject = Optional.of(new Subject("Math"));
        ContactFilter filter = new ContactFilter(Optional.of(Role.STUDENT), Optional.of(StudentStatus.ACTIVE),
                level, subject);
        assertEquals(new ContactFilter(Optional.of(Role.STUDENT), Optional.empty(), level, subject),
                filter.withAnyStatus());
    }

    @Test
    public void equals() {
        ContactFilter studentFilter = filterOf(Role.STUDENT, StudentStatus.ACTIVE);

        // same object -> returns true
        assertTrue(studentFilter.equals(studentFilter));

        // same values -> returns true
        ContactFilter studentFilterCopy = filterOf(Role.STUDENT, StudentStatus.ACTIVE);
        assertTrue(studentFilter.equals(studentFilterCopy));
        assertEquals(studentFilter.hashCode(), studentFilterCopy.hashCode());

        // different types -> returns false
        assertFalse(studentFilter.equals(1));

        // null -> returns false
        assertFalse(studentFilter.equals(null));

        // different role -> returns false
        assertFalse(studentFilter.equals(filterOf(Role.GUARDIAN, StudentStatus.ACTIVE)));
        assertFalse(studentFilter.equals(ContactFilter.ACTIVE_CONTACTS));

        // different status -> returns false
        assertFalse(studentFilter.equals(filterOf(Role.STUDENT, StudentStatus.FORMER)));
        assertFalse(studentFilter.equals(filterOf(Role.STUDENT, null)));

        // different level -> returns false
        ContactFilter sec3Filter = detailFilterOf(new Level("Sec 3"), null);
        assertTrue(sec3Filter.equals(detailFilterOf(new Level("Sec 3"), null)));
        assertFalse(sec3Filter.equals(detailFilterOf(new Level("Sec 4"), null)));

        // different subject -> returns false
        ContactFilter mathFilter = detailFilterOf(null, new Subject("Math"));
        assertTrue(mathFilter.equals(detailFilterOf(null, new Subject("Math"))));
        assertFalse(mathFilter.equals(detailFilterOf(null, new Subject("Physics"))));
    }

    @Test
    public void toStringMethod() {
        ContactFilter filter = filterOf(Role.STUDENT, StudentStatus.FORMER);
        String expected = ContactFilter.class.getCanonicalName() + "{role=" + Optional.of(Role.STUDENT)
                + ", status=" + Optional.of(StudentStatus.FORMER) + ", level=" + Optional.empty()
                + ", subject=" + Optional.empty() + "}";
        assertEquals(expected, filter.toString());
    }

    /**
     * Returns a filter for {@code role} and {@code status}, where {@code null} matches any value.
     */
    private static ContactFilter filterOf(Role role, StudentStatus status) {
        return new ContactFilter(Optional.ofNullable(role), Optional.ofNullable(status));
    }

    /**
     * Returns a filter for {@code level} and {@code subject} with any role and status,
     * where {@code null} matches any value.
     */
    private static ContactFilter detailFilterOf(Level level, Subject subject) {
        return new ContactFilter(Optional.empty(), Optional.empty(), Optional.ofNullable(level),
                Optional.ofNullable(subject));
    }
}
