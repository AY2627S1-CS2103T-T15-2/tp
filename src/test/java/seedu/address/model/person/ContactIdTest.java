package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ContactIdTest {

    private static final String VALID_ID = "12345678-1234-5678-abcd-123456789abc";

    @Test
    public void constructor_invalidId_rejectsMalformedAndShortenedUuids() {
        assertThrows(NullPointerException.class, () -> new ContactId(null));
        for (String invalid : new String[]{"", " ", "not-an-id", "1-1-1-1-1", VALID_ID + "0", " " + VALID_ID}) {
            assertFalse(ContactId.isValidId(invalid));
            assertThrows(IllegalArgumentException.class, () -> new ContactId(invalid));
        }
        assertFalse(ContactId.isValidId(null));
    }

    @Test
    public void constructor_uppercaseId_normalizesAndComparesByValue() {
        ContactId id = new ContactId(VALID_ID);
        ContactId uppercaseId = new ContactId(VALID_ID.toUpperCase(java.util.Locale.ROOT));
        assertEquals(id, uppercaseId);
        assertEquals(id.hashCode(), uppercaseId.hashCode());
        assertEquals(VALID_ID, uppercaseId.toString());
        assertNotEquals(id, null);
        assertNotEquals(id, VALID_ID);
    }

    @Test
    public void generate_returnsDistinctValidIds() {
        ContactId first = ContactId.generate();
        ContactId second = ContactId.generate();
        assertTrue(ContactId.isValidId(first.toString()));
        assertTrue(ContactId.isValidId(second.toString()));
        assertNotEquals(first, second);
        assertEquals(first, new ContactId(first.toString()));
    }
}
