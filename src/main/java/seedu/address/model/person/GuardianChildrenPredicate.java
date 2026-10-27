package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches students by their guardian's persistent identity, independently of their status or name.
 */
public class GuardianChildrenPredicate implements Predicate<Person> {
    private final ContactId guardianId;

    /**
     * Creates a predicate for students linked to {@code guardianId}.
     */
    public GuardianChildrenPredicate(ContactId guardianId) {
        this.guardianId = requireNonNull(guardianId);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return person.getRole() == Role.STUDENT
                && person.getGuardianId().filter(guardianId::equals).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianChildrenPredicate otherPredicate
                && guardianId.equals(otherPredicate.guardianId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("guardianId", guardianId).toString();
    }
}
