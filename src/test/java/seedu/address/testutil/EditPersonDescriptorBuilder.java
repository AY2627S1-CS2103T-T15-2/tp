package seedu.address.testutil;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

/**
 * A utility class to help with building EditPersonDescriptor objects.
 */
public class EditPersonDescriptorBuilder {

    private EditPersonDescriptor descriptor;

    public EditPersonDescriptorBuilder() {
        descriptor = new EditPersonDescriptor();
    }

    public EditPersonDescriptorBuilder(EditPersonDescriptor descriptor) {
        this.descriptor = new EditPersonDescriptor(descriptor);
    }

    /**
     * Returns an {@code EditPersonDescriptor} with fields containing {@code person}'s details
     */
    public EditPersonDescriptorBuilder(Person person) {
        descriptor = new EditPersonDescriptor();
        descriptor.setName(person.getName());
        person.getPhone().ifPresent(descriptor::setPhone);
        person.getEmail().ifPresent(descriptor::setEmail);
        person.getLevel().ifPresent(descriptor::setLevel);
        person.getSubject().ifPresent(descriptor::setSubject);
    }

    /**
     * Sets the {@code Name} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withName(String name) {
        descriptor.setName(new Name(name));
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withPhone(String phone) {
        descriptor.setPhone(new Phone(phone));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withEmail(String email) {
        descriptor.setEmail(new Email(email));
        return this;
    }

    /**
     * Marks the {@code Phone} of the {@code EditPersonDescriptor} that we are building to be removed.
     */
    public EditPersonDescriptorBuilder withoutPhone() {
        descriptor.removePhone();
        return this;
    }

    /**
     * Marks the {@code Email} of the {@code EditPersonDescriptor} that we are building to be removed.
     */
    public EditPersonDescriptorBuilder withoutEmail() {
        descriptor.removeEmail();
        return this;
    }

    /**
     * Sets the {@code Level} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withLevel(String level) {
        descriptor.setLevel(new Level(level));
        return this;
    }

    /**
     * Marks the {@code Level} of the {@code EditPersonDescriptor} that we are building to be removed.
     */
    public EditPersonDescriptorBuilder withoutLevel() {
        descriptor.removeLevel();
        return this;
    }

    /**
     * Sets the {@code Subject} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withSubject(String subject) {
        descriptor.setSubject(new Subject(subject));
        return this;
    }

    /**
     * Marks the {@code Subject} of the {@code EditPersonDescriptor} that we are building to be removed.
     */
    public EditPersonDescriptorBuilder withoutSubject() {
        descriptor.removeSubject();
        return this;
    }

    public EditPersonDescriptor build() {
        return descriptor;
    }
}
