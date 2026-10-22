package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Looks up contacts within a supplied collection. */
public final class PersonLookup {

    private PersonLookup() {}

    /** Returns the contact with the given ID, or an empty result if it is absent. */
    public static Optional<Person> findById(Collection<Person> persons, ContactId id) {
        requireAllNonNull(persons, id);
        return persons.stream().filter(person -> person.getId().equals(id)).findFirst();
    }

    /**
     * Returns all contacts whose full names match {@code name}, ignoring letter case and repeated or
     * surrounding spaces. Only the supplied contacts are searched, in their original order.
     * Callers targeting displayed contacts must supply the current displayed list.
     * Accents, punctuation and word order remain significant; partial names do not match.
     */
    public static List<Person> findByExactName(Collection<Person> persons, Name name) {
        requireAllNonNull(persons, name);
        String normalizedName = normalizeSpaces(name.fullName);
        return persons.stream()
                .filter(person -> normalizeSpaces(person.getName().fullName).equalsIgnoreCase(normalizedName))
                .toList();
    }

    private static String normalizeSpaces(String name) {
        return name.strip().replaceAll(" +", " ");
    }
}
