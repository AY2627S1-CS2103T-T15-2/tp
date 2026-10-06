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
}
