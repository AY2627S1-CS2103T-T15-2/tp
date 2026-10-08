package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.UUID;

/**
 * Represents an immutable identifier for a contact.
 */
public final class ContactId {

    public static final String MESSAGE_CONSTRAINTS =
            "Contact ID must be a UUID in the format "
                    + "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.";

    private static final String VALIDATION_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}"
                    + "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final UUID value;

    /**
     * Constructs a contact ID from a valid UUID string.
     */
    public ContactId(String id) {
        requireNonNull(id);
        checkArgument(isValidId(id), MESSAGE_CONSTRAINTS);
        value = UUID.fromString(id);
    }

    /**
     * Generates a new contact ID.
     */
    public static ContactId generate() {
        return new ContactId(UUID.randomUUID().toString());
    }

    /**
     * Returns true if the string is a valid contact ID.
     */
    public static boolean isValidId(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ContactId otherId)) {
            return false;
        }

        return value.equals(otherId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
