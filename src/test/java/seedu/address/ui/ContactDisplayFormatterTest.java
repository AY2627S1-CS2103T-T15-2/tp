package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

public class ContactDisplayFormatterTest {

    @Test
    public void formatContactMethod_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatContactMethod(null));
    }

    @Test
    public void formatContactMethod_emptyOptional_returnsNotProvided() {
        assertEquals("Not provided", ContactDisplayFormatter.formatContactMethod(Optional.empty()));
    }

    @Test
    public void formatContactMethod_presentPhone_returnsPhone() {
        Optional<Phone> phone = Optional.of(new Phone("91234567"));
        assertEquals("91234567", ContactDisplayFormatter.formatContactMethod(phone));
    }

    @Test
    public void formatContactMethod_presentEmail_returnsEmailWithCapitalizationPreserved() {
        Optional<Email> email = Optional.of(new Email("Alex@Example.COM"));
        assertEquals("Alex@Example.COM", ContactDisplayFormatter.formatContactMethod(email));
    }

    @Test
    public void formatNameWithRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatNameWithRole(null));
    }

    @Test
    public void formatNameWithRole_student_returnsNameWithStudentRole() {
        Person student = new PersonBuilder().withName("Alex Tan").withRole("student").build();
        assertEquals("Alex Tan [Student]", ContactDisplayFormatter.formatNameWithRole(student));
    }

    @Test
    public void formatNameWithRole_guardian_returnsNameWithGuardianRole() {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        assertEquals("Mei Tan [Guardian]", ContactDisplayFormatter.formatNameWithRole(guardian));
    }

    @Test
    public void formatNameWithRole_formerStudent_returnsNameWithFormerLabel() {
        Person student = new PersonBuilder().withName("Alex Tan").withRole("student")
                .withStatus(StudentStatus.FORMER).build();
        assertEquals("Alex Tan [Student] [Former]", ContactDisplayFormatter.formatNameWithRole(student));
    }

    @Test
    public void formatRoleWithStatus_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatRoleWithStatus(null));
    }

    @Test
    public void formatRoleWithStatus_activeStudent_returnsRole() {
        Person student = new PersonBuilder().withRole("student").build();
        assertEquals("Student", ContactDisplayFormatter.formatRoleWithStatus(student));
    }

    @Test
    public void formatRoleWithStatus_guardian_returnsRole() {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        assertEquals("Guardian", ContactDisplayFormatter.formatRoleWithStatus(guardian));
    }

    @Test
    public void formatRoleWithStatus_formerStudent_returnsRoleWithFormerLabel() {
        Person student = new PersonBuilder().withRole("student").withStatus(StudentStatus.FORMER).build();
        assertEquals("Student [Former]", ContactDisplayFormatter.formatRoleWithStatus(student));
    }

    @Test
    public void formatGuardianName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatGuardianName(null));
    }

    @Test
    public void formatGuardianName_noGuardian_returnsNone() {
        assertEquals("None", ContactDisplayFormatter.formatGuardianName(Optional.empty()));
    }

    @Test
    public void formatGuardianName_linkedGuardian_returnsGuardianName() {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        assertEquals("Mei Tan", ContactDisplayFormatter.formatGuardianName(Optional.of(guardian)));
    }
}
