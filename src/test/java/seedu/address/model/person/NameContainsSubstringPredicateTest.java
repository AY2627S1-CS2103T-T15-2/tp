package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsSubstringPredicateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NameContainsSubstringPredicate(null));
    }

    @Test
    public void equals() {
        NameContainsSubstringPredicate firstPredicate = new NameContainsSubstringPredicate("first");
        NameContainsSubstringPredicate secondPredicate = new NameContainsSubstringPredicate("second");

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsSubstringPredicate firstPredicateCopy = new NameContainsSubstringPredicate("first");
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different search term -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsSearchTerm_returnsTrue() {
        // full word
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("Alice");
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // partial word
        predicate = new NameContainsSubstringPredicate("Ali");
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // spans across words
        predicate = new NameContainsSubstringPredicate("ce Ta");
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // mixed case
        predicate = new NameContainsSubstringPredicate("aLIcE tAn");
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // consecutive spaces in name
        predicate = new NameContainsSubstringPredicate("alice tan");
        assertTrue(predicate.test(new PersonBuilder().withName("Alice   Tan").build()));
    }

    @Test
    public void test_nameDoesNotContainSearchTerm_returnsFalse() {
        // non-matching search term
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("Carol");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // words in a different order
        predicate = new NameContainsSubstringPredicate("Tan Alice");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // search term matches phone, email and address, but not name
        predicate = new NameContainsSubstringPredicate("12345");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void toStringMethod() {
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("keyword");

        String expected = NameContainsSubstringPredicate.class.getCanonicalName() + "{searchTerm=keyword}";
        assertEquals(expected, predicate.toString());
    }
}
