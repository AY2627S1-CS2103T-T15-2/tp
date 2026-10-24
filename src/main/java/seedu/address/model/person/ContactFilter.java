package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person} matches the role and status chosen for the contact list.
 * A contact matches when it has the chosen role and status. An empty role or status matches any value.
 * Guardians are always active, so they never match a filter for former contacts.
 */
public class ContactFilter implements Predicate<Person> {

    /** Shows guardians and active students. This is the default contact list. */
    public static final ContactFilter ACTIVE_CONTACTS =
            new ContactFilter(Optional.empty(), Optional.of(StudentStatus.ACTIVE));

    /** Shows former students only. */
    public static final ContactFilter FORMER_STUDENTS =
            new ContactFilter(Optional.empty(), Optional.of(StudentStatus.FORMER));

    /** Shows every contact, whatever its role or status. */
    public static final ContactFilter ALL_CONTACTS = new ContactFilter(Optional.empty(), Optional.empty());

    private final Optional<Role> role;
    private final Optional<StudentStatus> status;

    /**
     * Creates a filter that matches contacts with {@code role} and {@code status}.
     * An empty {@code role} or {@code status} matches any value.
     */
    public ContactFilter(Optional<Role> role, Optional<StudentStatus> status) {
        requireAllNonNull(role, status);
        this.role = role;
        this.status = status;
    }

    public Optional<Role> getRole() {
        return role;
    }

    public Optional<StudentStatus> getStatus() {
        return status;
    }

    /**
     * Returns true if this filter shows former students only.
     */
    public boolean isFormerOnly() {
        return status.equals(Optional.of(StudentStatus.FORMER));
    }

    /**
     * Returns true if this filter hides former students, which happens when it shows active contacts only.
     */
    public boolean hidesFormerStudents() {
        return status.equals(Optional.of(StudentStatus.ACTIVE));
    }

    /**
     * Returns a filter with the same role as this filter that matches contacts of any status.
     */
    public ContactFilter withAnyStatus() {
        return new ContactFilter(role, Optional.empty());
    }

    @Override
    public boolean test(Person person) {
        boolean hasChosenRole = role.map(chosenRole -> person.getRole() == chosenRole).orElse(true);
        boolean hasChosenStatus = status.map(chosenStatus -> person.getStatus() == chosenStatus).orElse(true);
        return hasChosenRole && hasChosenStatus;
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

        return role.equals(otherContactFilter.role) && status.equals(otherContactFilter.status);
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
