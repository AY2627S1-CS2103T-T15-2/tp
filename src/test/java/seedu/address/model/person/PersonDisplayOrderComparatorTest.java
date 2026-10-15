package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonDisplayOrderComparatorTest {

    private final PersonDisplayOrderComparator comparator = new PersonDisplayOrderComparator();

    @Test
    public void compare_differentNames_alphabeticalOrder() {
        Person alex = new PersonBuilder().withName("Alex Tan").build();
        Person bernice = new PersonBuilder().withName("Bernice Yu").build();

        assertTrue(comparator.compare(alex, bernice) < 0);
        assertTrue(comparator.compare(bernice, alex) > 0);
    }

    @Test
    public void compare_namesWithDifferentCase_caseIgnored() {
        Person lowerCaseAlex = new PersonBuilder().withName("alex tan").build();
        Person upperCaseBernice = new PersonBuilder().withName("BERNICE YU").build();
        Person mixedCaseAlex = new PersonBuilder().withName("AlEx TaN").build();

        // lower case name is not placed after upper case name
        assertTrue(comparator.compare(lowerCaseAlex, upperCaseBernice) < 0);

        // same name with different case -> equal
        assertEquals(0, comparator.compare(lowerCaseAlex, mixedCaseAlex));
    }

    @Test
    public void compare_namesWithConsecutiveSpaces_spacesTreatedAsOne() {
        Person alZed = new PersonBuilder().withName("Al  Zed").build();
        Person alBob = new PersonBuilder().withName("Al Bob").build();
        Person alZedSingleSpace = new PersonBuilder().withName("Al Zed").build();

        assertTrue(comparator.compare(alBob, alZed) < 0);
        assertEquals(0, comparator.compare(alZed, alZedSingleSpace));
    }

    @Test
    public void compare_accentedName_orderedWithUnaccentedLetter() {
        Person bernice = new PersonBuilder().withName("Bernice Yu").build();
        Person elise = new PersonBuilder().withName("Élise Tan").build();
        Person zoe = new PersonBuilder().withName("Zoe Lim").build();

        assertTrue(comparator.compare(bernice, elise) < 0);
        assertTrue(comparator.compare(elise, zoe) < 0);
        assertTrue(comparator.compare(zoe, elise) > 0);
    }

    @Test
    public void compare_namesDifferingOnlyInAccents_unaccentedNameFirst() {
        Person elise = new PersonBuilder().withName("Elise Tan").withRole("student").build();
        Person accentedElise = new PersonBuilder().withName("Élise Tan").withRole("guardian").build();

        // name takes precedence over role
        assertTrue(comparator.compare(elise, accentedElise) < 0);
        assertTrue(comparator.compare(accentedElise, elise) > 0);
    }

    @Test
    public void compare_letterWithStrokeOrLigature_orderedWithBaseLetter() {
        Person sophia = new PersonBuilder().withName("Sophia Lim").build();
        Person soren = new PersonBuilder().withName("S\u00f8ren Tan").build();
        Person sun = new PersonBuilder().withName("Sun Li").build();
        Person lukasz = new PersonBuilder().withName("\u0141ukasz Ng").build();
        Person lydia = new PersonBuilder().withName("Lydia Ong").build();
        Person zoe = new PersonBuilder().withName("Zoe Lim").build();

        assertTrue(comparator.compare(sophia, soren) < 0);
        assertTrue(comparator.compare(soren, sun) < 0);
        assertTrue(comparator.compare(lukasz, lydia) < 0);
        assertTrue(comparator.compare(lukasz, zoe) < 0);
    }

    @Test
    public void compare_nameWithStrokeAndPlainName_plainNameFirst() {
        Person soren = new PersonBuilder().withName("Soren Tan").withRole("student").build();
        Person strokedSoren = new PersonBuilder().withName("S\u00f8ren Tan").withRole("guardian").build();

        assertTrue(comparator.compare(soren, strokedSoren) < 0);
        assertTrue(comparator.compare(strokedSoren, soren) > 0);
    }

    @Test
    public void compare_nameIsPrefixOfLongerName_shorterNameFirst() {
        Person alexTan = new PersonBuilder().withName("Alex Tan").build();
        Person alexa = new PersonBuilder().withName("Alexa Lim").build();

        assertTrue(comparator.compare(alexTan, alexa) < 0);
    }

    @Test
    public void compare_sameNameDifferentRole_guardianBeforeStudent() {
        Person guardian = new PersonBuilder().withName("Alex Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withRole("student").build();

        assertTrue(comparator.compare(guardian, student) < 0);
        assertTrue(comparator.compare(student, guardian) > 0);
    }

    @Test
    public void compare_sameNameDifferentCaseAndRole_guardianBeforeStudent() {
        Person guardian = new PersonBuilder().withName("ALEX TAN").withRole("guardian").build();
        Person student = new PersonBuilder().withName("alex tan").withRole("student").build();

        assertTrue(comparator.compare(guardian, student) < 0);
    }

    @Test
    public void compare_differentNameAndRole_nameTakesPrecedence() {
        Person studentAlex = new PersonBuilder().withName("Alex Tan").withRole("student").build();
        Person guardianBernice = new PersonBuilder().withName("Bernice Yu").withRole("guardian").build();

        assertTrue(comparator.compare(studentAlex, guardianBernice) < 0);
    }

    @Test
    public void compare_sameNameAndRole_equal() {
        Person firstGuardian = new PersonBuilder().withName("Alex Tan").withRole("guardian")
                .withPhone("91234567").build();
        Person secondGuardian = new PersonBuilder().withName("Alex Tan").withRole("guardian")
                .withPhone("98765432").build();

        assertEquals(0, comparator.compare(firstGuardian, secondGuardian));
    }
}
