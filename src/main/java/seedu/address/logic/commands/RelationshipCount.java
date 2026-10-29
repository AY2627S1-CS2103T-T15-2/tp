package seedu.address.logic.commands;

import java.util.List;
import java.util.Set;

import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;

/** Shared relationship counts for deletion and restoration feedback. */
final class RelationshipCount {
    private RelationshipCount() {}

    /** Counts each linked student once when either endpoint belongs to {@code affectedIds}. */
    static long countAffected(List<Person> contacts, Set<ContactId> affectedIds) {
        return contacts.stream()
                .filter(student -> student.getGuardianId().isPresent())
                .filter(student -> affectedIds.contains(student.getId())
                        || affectedIds.contains(student.getGuardianId().orElseThrow()))
                .count();
    }
}
