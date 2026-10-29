package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_INVALID_GUARDIAN_OWNER = "Only a student can have a linked guardian.";
    public static final String MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD =
            "A guardian must have at least one contact method: phone or email.";
    public static final String MESSAGE_INVALID_FORMER_STATUS = "Only a student can be marked as former.";
    public static final String MESSAGE_INVALID_STUDENT_DETAILS =
            "Only a student can have a level or subject.";

    // Identity fields
    private final ContactId id;
    private final Name name;
    private final Optional<Phone> phone;
    private final Optional<Email> email;

    // Data fields
    private final Role role;
    private final Optional<ContactId> guardianId;
    private final StudentStatus status;
    private final Optional<Level> level;
    private final Optional<Subject> subject;

    /**
     * Creates a new person with a generated contact ID.
     */
    public Person(Name name, Optional<Phone> phone, Optional<Email> email,
            Role role) {
        this(ContactId.generate(), name, phone, email, role);
    }

    /**
     * Creates a person with the specified contact ID and details.
     */
    public Person(ContactId id, Name name, Optional<Phone> phone, Optional<Email> email,
            Role role) {
        this(id, name, phone, email, role, Optional.empty());
    }

    /**
     * Creates a person with the specified ID, details and optional guardian relationship.
     */
    public Person(ContactId id, Name name, Optional<Phone> phone, Optional<Email> email,
            Role role, Optional<ContactId> guardianId) {
        this(id, name, phone, email, role, guardianId, StudentStatus.ACTIVE);
    }

    /**
     * Creates a person with the specified ID, details, optional guardian relationship and status.
     */
    public Person(ContactId id, Name name, Optional<Phone> phone, Optional<Email> email,
            Role role, Optional<ContactId> guardianId, StudentStatus status) {
        this(id, name, phone, email, role, guardianId, status, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a person with the specified ID, details, optional guardian relationship, status,
     * and optional level and subject. Only a student can have a level or subject.
     */
    public Person(ContactId id, Name name, Optional<Phone> phone, Optional<Email> email,
            Role role, Optional<ContactId> guardianId, StudentStatus status,
            Optional<Level> level, Optional<Subject> subject) {
        requireAllNonNull(id, name, phone, email, role, guardianId, status, level, subject);
        checkArgument(guardianId.isEmpty() || role == Role.STUDENT, MESSAGE_INVALID_GUARDIAN_OWNER);
        checkArgument(role != Role.GUARDIAN || phone.isPresent() || email.isPresent(),
                MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);
        checkArgument(status == StudentStatus.ACTIVE || role == Role.STUDENT, MESSAGE_INVALID_FORMER_STATUS);
        checkArgument(role == Role.STUDENT || level.isEmpty() && subject.isEmpty(),
                MESSAGE_INVALID_STUDENT_DETAILS);
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.guardianId = guardianId;
        this.status = status;
        this.level = level;
        this.subject = subject;
    }

    public ContactId getId() {
        return id;
    }

    public Optional<ContactId> getGuardianId() {
        return guardianId;
    }

    /**
     * Returns a copy of this contact with the specified guardian relationship.
     */
    public Person withGuardianId(Optional<ContactId> updatedGuardianId) {
        return new Person(id, name, phone, email, role, updatedGuardianId, status, level, subject);
    }

    public StudentStatus getStatus() {
        return status;
    }

    /**
     * Returns true if this contact is a former student.
     */
    public boolean isFormerStudent() {
        return status == StudentStatus.FORMER;
    }

    /**
     * Returns a copy of this contact with the specified status.
     * Only students can be marked as former.
     */
    public Person withStatus(StudentStatus updatedStatus) {
        assert role == Role.STUDENT || updatedStatus == StudentStatus.ACTIVE : "Only students can be former";
        return new Person(id, name, phone, email, role, guardianId, updatedStatus, level, subject);
    }

    public Optional<Level> getLevel() {
        return level;
    }

    public Optional<Subject> getSubject() {
        return subject;
    }

    public Name getName() {
        return name;
    }

    public Optional<Phone> getPhone() {
        return phone;
    }

    public Optional<Email> getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    /**
     * Returns true if this contact has a phone number or an email address.
     */
    public boolean hasContactMethod() {
        return phone.isPresent() || email.isPresent();
    }

    /**
     * Returns true if this contact is a guardian with neither a phone number nor an email address.
     */
    public boolean isGuardianWithoutContactMethod() {
        return role == Role.GUARDIAN && !hasContactMethod();
    }

    /**
     * Returns true if both persons have the same identity fields.
     * Identity consists of role, name, phone and email, ignoring name and email case. At least one contact method
     * must be present; role and name alone are insufficient to identify a duplicate.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && hasContactMethod()
                && otherPerson.hasContactMethod()
                && role == otherPerson.role
                && otherPerson.getName().isSameName(getName())
                && hasSamePhone(otherPerson)
                && hasSameEmail(otherPerson);
    }

    /**
     * Returns true if both persons have the same role and equivalent names for possible-duplicate warnings.
     */
    public boolean hasSameNameAndRole(Person otherPerson) {
        return otherPerson != null
                && role == otherPerson.role
                && name.isSameNameForDuplicateWarning(otherPerson.name);
    }

    /**
     * Returns true if both persons have phone numbers that match after removing Unicode space separators and hyphens.
     */
    public boolean hasSamePhoneIgnoringFormatting(Person otherPerson) {
        return otherPerson != null
                && phone.isPresent()
                && otherPerson.phone.isPresent()
                && normalizePhone(phone.get()).equals(normalizePhone(otherPerson.phone.get()));
    }

    /**
     * Returns true if both persons have the same email address, ignoring letter case.
     */
    public boolean hasSameEmailAddress(Person otherPerson) {
        return otherPerson != null
                && email.isPresent()
                && otherPerson.email.isPresent()
                && email.get().isSameEmail(otherPerson.email.get());
    }

    private boolean hasSamePhone(Person otherPerson) {
        if (phone.isEmpty() || otherPerson.phone.isEmpty()) {
            return phone.isEmpty() && otherPerson.phone.isEmpty();
        }
        return phone.get().value.trim().equals(otherPerson.phone.get().value.trim());
    }

    private String normalizePhone(Phone phoneNumber) {
        return StringUtil.normalizeSpaceSeparators(phoneNumber.value).trim().replace(" ", "").replace("-", "");
    }

    private boolean hasSameEmail(Person otherPerson) {
        if (email.isEmpty() || otherPerson.email.isEmpty()) {
            return email.isEmpty() && otherPerson.email.isEmpty();
        }
        return hasSameEmailAddress(otherPerson);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && role.equals(otherPerson.role)
                && guardianId.equals(otherPerson.guardianId)
                && status.equals(otherPerson.status)
                && level.equals(otherPerson.level)
                && subject.equals(otherPerson.subject);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, role, guardianId, status, level, subject);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("role", role)
                .add("status", status)
                .add("level", level)
                .add("subject", subject)
                .toString();
    }

}
