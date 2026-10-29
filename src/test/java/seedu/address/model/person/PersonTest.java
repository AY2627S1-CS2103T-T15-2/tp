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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void hasSameNameAndRole_equivalentSpellings_matchWithoutBroadeningIdentity() {
        // EP: canonical Unicode, apostrophe variants and legacy repeated spaces.
        for (String[] pair : List.of(new String[] {"\u00c9lise", "E\u0301LISE"},
                new String[] {"D'Souza", "D\u2019Souza"}, new String[] {"Alex Tan", "Alex  Tan"})) {
            for (String role : List.of("student", "guardian")) {
                Person first = new PersonBuilder().withRole(role).withName(pair[0]).build();
                Person second = new PersonBuilder(first).withName(pair[1]).build();
                assertTrue(first.hasSameNameAndRole(second));
                assertTrue(second.hasSameNameAndRole(first));
                assertFalse(first.isSamePerson(second));
                assertEquals(pair[0], first.getName().fullName);
                assertEquals(pair[1], second.getName().fullName);
            }
        }
    }

    @Test
    public void hasSameNameAndRole_distinctNamesOrRoles_doNotMatch() {
        Person student = new PersonBuilder().withName("\u00c9lise Tan").build();
        // EP: accents and word boundaries distinguish names; roles remain significant.
        for (String name : List.of("Elise Tan", "\u00c9liseTan", "\uff25lise Tan")) {
            assertFalse(student.hasSameNameAndRole(new PersonBuilder(student).withName(name).build()));
        }
        assertFalse(student.hasSameNameAndRole(new PersonBuilder(student).withRole("guardian").build()));
        assertFalse(student.hasSameNameAndRole(null));
    }

    @Test
    public void hasSamePhoneIgnoringFormatting_unicodeSpaces_matchesWithoutChangingStoredText() {
        Person compact = new PersonBuilder(ALICE).withPhone("93333333").build();
        for (String space : List.of("\u00a0", "\u2007", "\u202f", "\u2003", "\u3000")) {
            String savedPhone = "9333" + space + "3333";
            Person spaced = new PersonBuilder(ALICE).withPhone(savedPhone).build();
            assertTrue(compact.hasSamePhoneIgnoringFormatting(spaced), savedPhone);
            assertTrue(spaced.hasSamePhoneIgnoringFormatting(compact), savedPhone);
            assertEquals(savedPhone, spaced.getPhone().orElseThrow().value);
            assertFalse(spaced.hasSamePhoneIgnoringFormatting(
                    new PersonBuilder(ALICE).withPhone("93333334").build()));
        }
    }

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
    public void isSamePerson_optionalPhone_comparesMissingPhoneAsEmptyValue() {
        Person withoutPhone = new PersonBuilder(ALICE).withoutPhone().build();
        assertTrue(withoutPhone.isSamePerson(new PersonBuilder(withoutPhone).build()));
        assertFalse(withoutPhone.isSamePerson(ALICE));
        assertFalse(ALICE.isSamePerson(withoutPhone));
    }

    @Test
    public void isSamePerson_phoneFormattingDifferences_areNotExactDuplicates() {
        Person spacedPhone = new PersonBuilder(ALICE).withPhone("+65 9123 4567").build();
        Person hyphenatedPhone = new PersonBuilder(ALICE).withPhone("+65-9123-4567").build();
        Person differentPhone = new PersonBuilder(ALICE).withPhone("+65-9123-4568").build();

        assertFalse(spacedPhone.isSamePerson(hyphenatedPhone));
        assertTrue(spacedPhone.hasSamePhoneIgnoringFormatting(hyphenatedPhone));
        assertFalse(spacedPhone.isSamePerson(differentPhone));
    }

    @Test
    public void contactComparisonHelpers_handleNullAndMissingContactMethods() {
        Person contact = new PersonBuilder().withRole("student").build();
        Person withoutPhone = new PersonBuilder(contact).withoutPhone().build();
        Person withoutEmail = new PersonBuilder(contact).withoutEmail().build();

        assertFalse(contact.hasSameNameAndRole(null));
        assertFalse(contact.hasSamePhoneIgnoringFormatting(null));
        assertFalse(withoutPhone.hasSamePhoneIgnoringFormatting(contact));
        assertFalse(contact.hasSamePhoneIgnoringFormatting(withoutPhone));
        assertFalse(contact.hasSameEmailAddress(null));
        assertFalse(withoutEmail.hasSameEmailAddress(contact));
        assertFalse(contact.hasSameEmailAddress(withoutEmail));
        assertFalse(contact.hasSameNameAndRole(new PersonBuilder(contact).withRole("guardian").build()));
    }

    @Test
    public void isSamePerson_studentWithoutContactMethods_nameAndRoleAloneAreNotDuplicate() {
        Person studentWithoutContact = new PersonBuilder().withRole("student").withoutPhone().withoutEmail().build();
        Person anotherStudentWithoutContact = new PersonBuilder(studentWithoutContact)
                .withId(ContactId.generate()).build();

        assertFalse(studentWithoutContact.isSamePerson(anotherStudentWithoutContact));
    }

    @Test
    public void isGuardianWithoutContactMethod() {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        assertFalse(guardian.isGuardianWithoutContactMethod());
        assertFalse(new PersonBuilder(guardian).withoutPhone().build().isGuardianWithoutContactMethod());
        assertFalse(new PersonBuilder(guardian).withoutEmail().build().isGuardianWithoutContactMethod());

        Person studentWithoutContactMethod = new PersonBuilder().withRole("student").withoutPhone().withoutEmail()
                .build();
        assertFalse(studentWithoutContactMethod.hasContactMethod());
        assertFalse(studentWithoutContactMethod.isGuardianWithoutContactMethod());
    }

    @Test
    public void constructor_guardianWithoutContactMethod_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD, () ->
                new PersonBuilder().withRole("guardian").withoutPhone().withoutEmail().build());
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
                + ", status=" + ALICE.getStatus() + ", level=" + ALICE.getLevel()
                + ", subject=" + ALICE.getSubject() + "}";
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

    @Test
    public void constructor_noStatus_defaultsToActive() {
        for (Role role : Role.values()) {
            for (Person person : List.of(
                    new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), role),
                    new Person(ALICE.getId(), ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), role),
                    new Person(ALICE.getId(), ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), role,
                            Optional.empty()))) {
                assertEquals(StudentStatus.ACTIVE, person.getStatus());
                assertFalse(person.isFormerStudent());
            }
        }
    }

    @Test
    public void constructor_formerGuardian_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_INVALID_FORMER_STATUS, () ->
                new PersonBuilder().withRole("guardian").withStatus(StudentStatus.FORMER).build());
    }

    @Test
    public void withStatus_student_changesOnlyStatus() {
        Person former = ALICE.withStatus(StudentStatus.FORMER);
        assertTrue(former.isFormerStudent());
        assertEquals(ALICE.getId(), former.getId());
        assertEquals(ALICE.getName(), former.getName());
        assertEquals(ALICE, former.withStatus(StudentStatus.ACTIVE));
    }

    @Test
    public void withStatus_activeGuardian_returnsEqualGuardian() {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        assertEquals(guardian, guardian.withStatus(StudentStatus.ACTIVE));
    }

    @Test
    public void withGuardianId_formerStudent_preservesStatus() {
        Person former = ALICE.withStatus(StudentStatus.FORMER);
        assertTrue(former.withGuardianId(Optional.of(ContactId.generate())).isFormerStudent());
    }

    @Test
    public void equals_differentStatus_statusAffectsEqualityButNotIdentity() {
        Person former = ALICE.withStatus(StudentStatus.FORMER);
        assertFalse(ALICE.equals(former));
        assertTrue(ALICE.isSamePerson(former));
    }

    @Test
    public void constructor_guardianWithLevel_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_INVALID_STUDENT_DETAILS, () ->
                new Person(ContactId.generate(), ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                        Role.GUARDIAN, Optional.empty(), StudentStatus.ACTIVE,
                        Optional.of(new Level("Sec 3")), Optional.empty()));
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_INVALID_STUDENT_DETAILS, () ->
                new Person(ContactId.generate(), ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                        Role.GUARDIAN, Optional.empty(), StudentStatus.ACTIVE,
                        Optional.empty(), Optional.of(new Subject("Math"))));
    }

    @Test
    public void withStatusAndWithGuardianId_studentWithLevelAndSubject_preservesLevelAndSubject() {
        Person student = new PersonBuilder(ALICE).withLevel("Sec 3").withSubject("Math").build();
        Person former = student.withStatus(StudentStatus.FORMER);
        Person linked = student.withGuardianId(Optional.of(ContactId.generate()));
        assertEquals(student.getLevel(), former.getLevel());
        assertEquals(student.getSubject(), former.getSubject());
        assertEquals(student.getLevel(), linked.getLevel());
        assertEquals(student.getSubject(), linked.getSubject());
    }

    @Test
    public void equals_differentLevelOrSubject_affectsEqualityButNotIdentity() {
        Person withLevel = new PersonBuilder(ALICE).withLevel("Sec 3").build();
        Person withSubject = new PersonBuilder(ALICE).withSubject("Math").build();
        assertFalse(ALICE.equals(withLevel));
        assertFalse(ALICE.equals(withSubject));
        assertTrue(ALICE.isSamePerson(withLevel));
        assertTrue(ALICE.isSamePerson(withSubject));
    }
}
