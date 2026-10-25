package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person} matches the role, status, level and subject chosen for the contact list.
 * A contact matches when it has every chosen value. An empty value matches any contact.
 * Guardians are always active and have no level or subject, so they never match a filter for former contacts
 * or for a level or subject.
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
    private final Optional<Level> level;
    private final Optional<Subject> subject;

    /**
     * Creates a filter that matches contacts with {@code role} and {@code status}, whatever their level and subject.
     * An empty {@code role} or {@code status} matches any value.
     */
    public ContactFilter(Optional<Role> role, Optional<StudentStatus> status) {
        this(role, status, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a filter that matches contacts with {@code role}, {@code status}, {@code level} and {@code subject}.
     * An empty value matches any contact. Levels and subjects match when {@link Level#isSameValue(Level)} and
     * {@link Subject#isSameValue(Subject)} hold, so letter case and spaces are ignored.
     */
    public ContactFilter(Optional<Role> role, Optional<StudentStatus> status, Optional<Level> level,
            Optional<Subject> subject) {
        requireAllNonNull(role, status, level, subject);
        this.role = role;
        this.status = status;
        this.level = level;
        this.subject = subject;
    }

    public Optional<Role> getRole() {
        return role;
    }

    public Optional<StudentStatus> getStatus() {
        return status;
    }

    /**
     * Returns true if this filter chooses a level or subject.
     */
    public boolean hasStudentDetail() {
        return level.isPresent() || subject.isPresent();
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
     * Returns a filter with the same role, level and subject as this filter that matches contacts of any status.
     */
    public ContactFilter withAnyStatus() {
        return new ContactFilter(role, Optional.empty(), level, subject);
    }

    @Override
    public boolean test(Person person) {
        boolean hasChosenRole = role.map(chosenRole -> person.getRole() == chosenRole).orElse(true);
        boolean hasChosenStatus = status.map(chosenStatus -> person.getStatus() == chosenStatus).orElse(true);
        boolean hasChosenLevel = level.map(chosenLevel -> person.getLevel()
                .map(chosenLevel::isSameValue).orElse(false)).orElse(true);
        boolean hasChosenSubject = subject.map(chosenSubject -> person.getSubject()
                .map(chosenSubject::isSameValue).orElse(false)).orElse(true);
        return hasChosenRole && hasChosenStatus && hasChosenLevel && hasChosenSubject;
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

        return role.equals(otherContactFilter.role) && status.equals(otherContactFilter.status)
                && level.equals(otherContactFilter.level) && subject.equals(otherContactFilter.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, status, level, subject);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("role", role)
                .add("status", status)
                .add("level", level)
                .add("subject", subject)
                .toString();
    }
}
