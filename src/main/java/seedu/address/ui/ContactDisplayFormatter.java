package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * Formats contact details for display in the UI.
 */
public class ContactDisplayFormatter {

    public static final String MISSING_CONTACT_METHOD = "Not provided";

    private ContactDisplayFormatter() {
        // Prevents instantiation, as this class only has static methods.
    }

    /**
     * Returns {@code contactMethod}, or "Not provided" if {@code contactMethod} is blank.
     */
    public static String formatContactMethod(String contactMethod) {
        requireNonNull(contactMethod);
        return contactMethod.isBlank() ? MISSING_CONTACT_METHOD : contactMethod;
    }

    /**
     * Returns the text of {@code contactMethod}, or "Not provided" if {@code contactMethod} is empty.
     */
    public static String formatContactMethod(Optional<?> contactMethod) {
        requireNonNull(contactMethod);
        return contactMethod.map(Object::toString).orElse(MISSING_CONTACT_METHOD);
    }
}
