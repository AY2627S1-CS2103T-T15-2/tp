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
        assertEquals(firstPredicate.hashCode(), firstPredicateCopy.hashCode());

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
    public void test_searchTermLength_matchesUpToWholeName() {
        // Boundary: the shortest search term, a single letter
        assertTrue(new NameContainsSubstringPredicate("a").test(new PersonBuilder().withName("Alice Tan").build()));
        // Boundary: the search term is the whole name
        assertTrue(new NameContainsSubstringPredicate("Alice Tan")
                .test(new PersonBuilder().withName("Alice Tan").build()));
        // Boundary: one character longer than the whole name
        assertFalse(new NameContainsSubstringPredicate("Alice Tann")
                .test(new PersonBuilder().withName("Alice Tan").build()));
    }

    @Test
    public void test_accentedAndUnaccentedLetters_treatedAsSame() {
        // unaccented search term, accented name
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("elise");
        assertTrue(predicate.test(new PersonBuilder().withName("Élise Tan").build()));

        // accented search term, unaccented name
        predicate = new NameContainsSubstringPredicate("élise");
        assertTrue(predicate.test(new PersonBuilder().withName("Elise Tan").build()));

        // name with a combining mark
        predicate = new NameContainsSubstringPredicate("elise");
        assertTrue(predicate.test(new PersonBuilder().withName("E\u0301lise Tan").build()));

        // partial name with accent in the middle
        predicate = new NameContainsSubstringPredicate("zoe n");
        assertTrue(predicate.test(new PersonBuilder().withName("Zoë Núñez").build()));

        // letters that cannot be decomposed, matched by their base letters
        predicate = new NameContainsSubstringPredicate("soren");
        assertTrue(predicate.test(new PersonBuilder().withName("S\u00f8ren Tan").build()));
        predicate = new NameContainsSubstringPredicate("lukasz");
        assertTrue(predicate.test(new PersonBuilder().withName("\u0141ukasz Ng").build()));
        predicate = new NameContainsSubstringPredicate("strauss");
        assertTrue(predicate.test(new PersonBuilder().withName("Anna Strau\u00df").build()));

        // accents ignored, but other letters must still match
        predicate = new NameContainsSubstringPredicate("elsie");
        assertFalse(predicate.test(new PersonBuilder().withName("Élise Tan").build()));
    }

    @Test
    public void test_sigmaSearchTerm_matchesEverySigmaFormInName() {
        // EP: uppercase, ordinary lowercase and word-final lowercase sigma must match each other.
        for (String name : new String[] {"\u039f\u03a3", "\u03bf\u03c3", "\u03bf\u03c2"}) {
            Person person = new PersonBuilder().withName(name).build();
            for (String searchTerm : new String[] {"\u03a3", "\u03c3", "\u03c2"}) {
                assertTrue(new NameContainsSubstringPredicate(searchTerm).test(person),
                        "Expected " + searchTerm + " to match " + name);
            }
        }
    }

    @Test
    public void test_fullGreekSearchTerm_ignoresSigmaFormButRequiresOtherLettersToMatch() {
        for (String name : new String[] {"\u039f\u03a3", "\u03bf\u03c3", "\u03bf\u03c2"}) {
            Person person = new PersonBuilder().withName(name).build();
            for (String searchTerm : new String[] {"\u039f\u03a3", "\u03bf\u03c3", "\u03bf\u03c2"}) {
                assertTrue(new NameContainsSubstringPredicate(searchTerm).test(person),
                        "Expected " + searchTerm + " to match " + name);
            }
            assertFalse(new NameContainsSubstringPredicate("\u03b1\u03c3").test(person));
        }
    }

    @Test
    public void test_curlyAndStraightApostrophes_treatedAsSame() {
        // straight apostrophe in search term, curly apostrophe in name
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("o'brien");
        assertTrue(predicate.test(new PersonBuilder().withName("Liam O’Brien").build()));

        // curly apostrophe in search term, straight apostrophe in name
        predicate = new NameContainsSubstringPredicate("O’Brien");
        assertTrue(predicate.test(new PersonBuilder().withName("Liam O'Brien").build()));

        // left single quotation mark in search term
        predicate = new NameContainsSubstringPredicate("o‘brien");
        assertTrue(predicate.test(new PersonBuilder().withName("Liam O'Brien").build()));

        // apostrophe in search term, none in name
        predicate = new NameContainsSubstringPredicate("o'brien");
        assertFalse(predicate.test(new PersonBuilder().withName("Liam OBrien").build()));
    }

    @Test
    public void test_nameDoesNotContainSearchTerm_returnsFalse() {
        // non-matching search term
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("Carol");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // words in a different order
        predicate = new NameContainsSubstringPredicate("Tan Alice");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // search term matches phone, but not name
        predicate = new NameContainsSubstringPredicate("12345");
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").build()));
    }

    @Test
    public void toStringMethod() {
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("keyword");

        String expected = NameContainsSubstringPredicate.class.getCanonicalName() + "{searchTerm=keyword}";
        assertEquals(expected, predicate.toString());
    }
}
