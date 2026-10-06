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
