package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.Optional;

/** Looks up contacts using their permanent IDs. */
public final class PersonLookup {

    private PersonLookup() {}

    /** Returns the contact with the given ID, or an empty result if it is absent. */
    public static Optional<Person> findById(Collection<Person> persons, ContactId id) {
        requireAllNonNull(persons, id);
        return persons.stream().filter(person -> person.getId().equals(id)).findFirst();
    }
}
