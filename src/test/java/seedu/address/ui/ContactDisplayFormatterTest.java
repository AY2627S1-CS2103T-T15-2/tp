package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Email;

public class ContactDisplayFormatterTest {

    @Test
    public void formatContactMethod_nullString_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatContactMethod(
                (String) null));
    }

    @Test
    public void formatContactMethod_blankString_returnsNotProvided() {
        assertEquals("Not provided", ContactDisplayFormatter.formatContactMethod(""));
        assertEquals("Not provided", ContactDisplayFormatter.formatContactMethod(" \t"));
    }

    @Test
    public void formatContactMethod_nonBlankString_returnsValue() {
        assertEquals("91234567", ContactDisplayFormatter.formatContactMethod("91234567"));
        assertEquals("alex@example.com", ContactDisplayFormatter.formatContactMethod("alex@example.com"));
    }

    @Test
    public void formatContactMethod_nullOptional_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactDisplayFormatter.formatContactMethod(
                (Optional<Email>) null));
    }

    @Test
    public void formatContactMethod_emptyOptional_returnsNotProvided() {
        assertEquals("Not provided", ContactDisplayFormatter.formatContactMethod(Optional.empty()));
    }

    @Test
    public void formatContactMethod_presentOptional_returnsValueWithCapitalisationPreserved() {
        Optional<Email> email = Optional.of(new Email("Alex@Example.COM"));
        assertEquals("Alex@Example.COM", ContactDisplayFormatter.formatContactMethod(email));
    }
}
