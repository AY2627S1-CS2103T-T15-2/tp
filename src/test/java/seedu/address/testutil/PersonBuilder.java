package seedu.address.testutil;

import java.util.Optional;

import seedu.address.model.person.ContactId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final Role DEFAULT_ROLE = Role.STUDENT;

    private ContactId id;
    private Optional<ContactId> guardianId = Optional.empty();
    private Name name;
    private Optional<Phone> phone;
    private Optional<Email> email;
    private Role role;
    private StudentStatus status = StudentStatus.ACTIVE;
    private Optional<Level> level = Optional.empty();
    private Optional<Subject> subject = Optional.empty();

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        id = ContactId.generate();
        name = new Name(DEFAULT_NAME);
        phone = Optional.of(new Phone(DEFAULT_PHONE));
        email = Optional.of(new Email(DEFAULT_EMAIL));
        role = DEFAULT_ROLE;
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        id = personToCopy.getId();
        guardianId = personToCopy.getGuardianId();
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        role = personToCopy.getRole();
        status = personToCopy.getStatus();
        level = personToCopy.getLevel();
        subject = personToCopy.getSubject();
    }

    /**
     * Sets the contact ID of the person being built.
     */
    public PersonBuilder withId(ContactId id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the guardian ID of the student being built.
     */
    public PersonBuilder withGuardianId(ContactId guardianId) {
        this.guardianId = Optional.of(guardianId);
        return this;
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = Optional.of(new Phone(phone));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = Optional.of(new Email(email));
        return this;
    }

    /**
     * Removes the phone from the person being built.
     */
    public PersonBuilder withoutPhone() {
        phone = Optional.empty();
        return this;
    }

    /**
     * Removes the email from the person being built.
     */
    public PersonBuilder withoutEmail() {
        email = Optional.empty();
        return this;
    }

    /**
     * Sets the {@code Role} of the {@code Person} that we are building.
     */
    public PersonBuilder withRole(String role) {
        this.role = Role.fromString(role);
        return this;
    }

    /**
     * Sets the {@code StudentStatus} of the student being built.
     */
    public PersonBuilder withStatus(StudentStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the {@code Level} of the student being built.
     */
    public PersonBuilder withLevel(String level) {
        this.level = Optional.of(new Level(level));
        return this;
    }

    /**
     * Sets the {@code Subject} of the student being built.
     */
    public PersonBuilder withSubject(String subject) {
        this.subject = Optional.of(new Subject(subject));
        return this;
    }

    public Person build() {
        return new Person(id, name, phone, email, role, guardianId, status, level, subject);
    }

}
