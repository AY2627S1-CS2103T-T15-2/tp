package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name} contains the given search term, ignoring letter case.
 * Consecutive spaces in the name are treated as a single space when matching.
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
        String lowerCaseName = person.getName().fullName.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String lowerCaseSearchTerm = searchTerm.toLowerCase(Locale.ROOT);
        return lowerCaseName.contains(lowerCaseSearchTerm);
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
    public String toString() {
        return new ToStringBuilder(this).add("searchTerm", searchTerm).toString();
    }
}
