package seedu.address.model.util;

import java.util.Optional;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"),
                Optional.of(new Email("alexyeoh@example.com")),
                Role.STUDENT),
            new Person(new Name("Bernice Yu"), new Phone("99272758"),
                Optional.of(new Email("berniceyu@example.com")),
                Role.GUARDIAN),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"),
                Optional.of(new Email("charlotte@example.com")),
                Role.STUDENT),
            new Person(new Name("David Li"), new Phone("91031282"),
                Optional.of(new Email("lidavid@example.com")),
                Role.GUARDIAN),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"),
                Optional.of(new Email("irfan@example.com")),
                Role.STUDENT),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"),
                Optional.of(new Email("royb@example.com")),
                Role.GUARDIAN)
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

}
