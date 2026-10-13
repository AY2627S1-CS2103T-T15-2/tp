package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Formats contact details for display in the UI.
 */
public class ContactDisplayFormatter {

    public static final String MISSING_CONTACT_METHOD = "Not provided";
    public static final String MISSING_GUARDIAN = "None";

    private ContactDisplayFormatter() {
        // Prevents instantiation, as this class only has static methods.
    }

    /**
     * Returns the text of {@code contactMethod}, or "Not provided" if {@code contactMethod} is empty.
     */
    public static String formatContactMethod(Optional<?> contactMethod) {
        requireNonNull(contactMethod);
        return contactMethod.map(Object::toString).orElse(MISSING_CONTACT_METHOD);
    }

    /**
     * Returns the name of {@code person} followed by its role in square brackets,
     * for example "Alex Tan [Student]".
     */
    public static String formatNameWithRole(Person person) {
        requireNonNull(person);
        return person.getName().fullName + " [" + person.getRole() + "]";
    }

    /**
     * Returns the name of {@code guardian}, or "None" if {@code guardian} is empty.
     */
    public static String formatGuardianName(Optional<Person> guardian) {
        requireNonNull(guardian);
        return guardian.map(person -> person.getName().fullName).orElse(MISSING_GUARDIAN);
    }
}
