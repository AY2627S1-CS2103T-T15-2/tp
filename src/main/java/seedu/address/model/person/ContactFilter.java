package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person} matches the role and status chosen for the contact list.
 * A contact matches when it has the chosen status and, if a role is chosen, that role.
 * Guardians are always active, so they never match a filter for former contacts.
 */
public class ContactFilter implements Predicate<Person> {

    /** Shows guardians and active students. This is the default contact list. */
    public static final ContactFilter ACTIVE_CONTACTS = new ContactFilter(Optional.empty(), StudentStatus.ACTIVE);

    /** Shows former students only. */
    public static final ContactFilter FORMER_STUDENTS = new ContactFilter(Optional.empty(), StudentStatus.FORMER);

    private final Optional<Role> role;
    private final StudentStatus status;

    /**
     * Creates a filter that matches contacts with {@code status} and, if present, {@code role}.
     */
    public ContactFilter(Optional<Role> role, StudentStatus status) {
        requireAllNonNull(role, status);
        this.role = role;
        this.status = status;
    }

    public Optional<Role> getRole() {
        return role;
    }

    public StudentStatus getStatus() {
        return status;
    }

    /**
     * Returns true if this filter shows former students only.
     */
    public boolean isFormerOnly() {
        return status == StudentStatus.FORMER;
    }

    @Override
    public boolean test(Person person) {
        boolean hasChosenRole = role.map(chosenRole -> person.getRole() == chosenRole).orElse(true);
        return hasChosenRole && person.getStatus() == status;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ContactFilter otherContactFilter)) {
            return false;
        }

        return role.equals(otherContactFilter.role) && status == otherContactFilter.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("role", role)
                .add("status", status)
                .toString();
    }
}
