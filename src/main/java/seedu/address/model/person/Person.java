package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_INVALID_GUARDIAN_OWNER = "Only a student can have a linked guardian.";
    public static final String MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD =
            "A guardian must have at least one contact method: phone or email.";

    // Identity fields
    private final ContactId id;
    private final Name name;
    private final Optional<Phone> phone;
    private final Optional<Email> email;

    // Data fields
    private final Role role;
    private final Optional<ContactId> guardianId;

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
        requireAllNonNull(id, name, phone, email, role, guardianId);
        checkArgument(guardianId.isEmpty() || role == Role.STUDENT, MESSAGE_INVALID_GUARDIAN_OWNER);
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.guardianId = guardianId;
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
        return new Person(id, name, phone, email, role, updatedGuardianId);
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
     * Identity consists of role, name, optional phone and optional email, ignoring name and email case.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && role == otherPerson.role
                && otherPerson.getName().isSameName(getName())
                && hasSamePhone(otherPerson)
                && hasSameEmail(otherPerson);
    }

    /**
     * Returns true if both persons have the same role and name, ignoring name case.
     */
    public boolean hasSameNameAndRole(Person otherPerson) {
        return otherPerson != null
                && role == otherPerson.role
                && name.isSameName(otherPerson.name);
    }

    private boolean hasSamePhone(Person otherPerson) {
        if (phone.isEmpty() || otherPerson.phone.isEmpty()) {
            return phone.isEmpty() && otherPerson.phone.isEmpty();
        }
        return normalizePhone(phone.get()).equals(normalizePhone(otherPerson.phone.get()));
    }

    private String normalizePhone(Phone phoneNumber) {
        return phoneNumber.value.replace(" ", "").replace("-", "");
    }

    private boolean hasSameEmail(Person otherPerson) {
        if (email.isEmpty() || otherPerson.email.isEmpty()) {
            return email.isEmpty() && otherPerson.email.isEmpty();
        }
        return email.get().isSameEmail(otherPerson.email.get());
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
                && guardianId.equals(otherPerson.guardianId);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, role, guardianId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("role", role)
                .toString();
    }

}
