package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void isSamePerson_optionalEmailAndRole_comparesAllIdentityFields() {
        Person student = new PersonBuilder(ALICE).withoutEmail().build();
        Person sameStudent = new PersonBuilder(student).withName("ALICE PAULINE").build();
        Person guardian = new PersonBuilder(student).withRole("guardian").build();
        assertTrue(student.isSamePerson(sameStudent));
        assertTrue(sameStudent.isSamePerson(student));
        assertFalse(student.isSamePerson(guardian));
        assertFalse(student.isSamePerson(ALICE));
        assertFalse(ALICE.isSamePerson(student));
        assertFalse(student.isSamePerson(new PersonBuilder(student).withPhone(VALID_PHONE_BOB).build()));
        assertTrue(student.withGuardianId(Optional.of(ContactId.generate())).getEmail().isEmpty());
        assertFalse(student.equals(sameStudent));
        assertEquals(student, new PersonBuilder(student).build());
    }


    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, other identity fields different -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withRole(VALID_ROLE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same name and phone, different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same identity fields, data fields different -> returns true
        editedAlice = new PersonBuilder(ALICE).withGuardianId(ContactId.generate()).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same identity fields with email differing only in letter case -> returns true
        editedAlice = new PersonBuilder(ALICE)
                .withEmail(ALICE.getEmail().orElseThrow().value.toUpperCase(Locale.ROOT))
                .build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs only in case, all other attributes same -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // email differs only in letter case -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withEmail(ALICE.getEmail().orElseThrow().value.toUpperCase(Locale.ROOT))
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different role -> returns false
        editedAlice = new PersonBuilder(ALICE).withRole(VALID_ROLE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", role=" + ALICE.getRole()
                + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void withGuardianId_guardianContact_rejectsRelationship() {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        assertThrows(IllegalArgumentException.class,
                "Only a student can have a linked guardian.", () -> guardian.withGuardianId(
                        Optional.of(ContactId.generate())));
        assertTrue(guardian.getGuardianId().isEmpty());
    }

    @Test
    public void equals_differentGuardian_relationshipAffectsEquality() {
        Person student = new PersonBuilder().build();
        ContactId guardianId = ContactId.generate();
        Person linkedStudent = student.withGuardianId(Optional.of(guardianId));
        Person sameLink = student.withGuardianId(Optional.of(guardianId));
        Person differentLink = student.withGuardianId(Optional.of(ContactId.generate()));

        assertFalse(student.equals(linkedStudent));
        assertFalse(linkedStudent.equals(differentLink));
        assertEquals(linkedStudent, sameLink);
        assertEquals(linkedStudent.hashCode(), sameLink.hashCode());
        assertEquals(student.getId(), linkedStudent.getId());
        assertTrue(student.getGuardianId().isEmpty());
    }
}
