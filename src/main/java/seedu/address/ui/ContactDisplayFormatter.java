package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Formats contact details for display in the UI.
 */
public class ContactDisplayFormatter {

    public static final String MISSING_CONTACT_METHOD = "Not provided";

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
}
