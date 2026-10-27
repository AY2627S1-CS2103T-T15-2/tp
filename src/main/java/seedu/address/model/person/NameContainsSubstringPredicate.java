package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name} contains the given search term, ignoring letter case.
 * Consecutive spaces in the name are treated as a single space when matching. Curly apostrophes
 * in the name or search term are treated as straight apostrophes, and accents are ignored.
 */
public class NameContainsSubstringPredicate implements Predicate<Person> {
    private final String searchTerm;

    /**
     * Creates a predicate that matches names containing {@code searchTerm}.
     */
    public NameContainsSubstringPredicate(String searchTerm) {
        requireNonNull(searchTerm);
        this.searchTerm = searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public boolean test(Person person) {
        String searchableName = toSearchableText(person.getName().fullName.replaceAll("\\s+", " "));
        return searchableName.contains(toSearchableText(searchTerm));
    }

    /**
     * Returns {@code text} in lower case, with curly apostrophes replaced by straight apostrophes,
     * both lowercase Greek sigma forms matched alike, and accents removed as described in
     * {@link Name#removeAccents(String)}.
     */
    private static String toSearchableText(String text) {
        // Lowercasing a capital sigma depends on its position within a word.
        String lowercaseText = Name.replaceCurlyApostrophes(text).toLowerCase(Locale.ROOT);
        return Name.removeAccents(lowercaseText.replace('\u03c2', '\u03c3'));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsSubstringPredicate otherNameContainsSubstringPredicate)) {
            return false;
        }

        return searchTerm.equals(otherNameContainsSubstringPredicate.searchTerm);
    }

    @Override
    public int hashCode() {
        return searchTerm.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("searchTerm", searchTerm).toString();
    }
}
