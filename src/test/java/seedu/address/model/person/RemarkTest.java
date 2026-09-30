package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_unrestrictedText_preservesValue() {
        assertEquals("", new Remark("").value);
        assertEquals(" Likes baseball! ", new Remark(" Likes baseball! ").toString());
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("note");
        assertEquals(remark, remark);
        assertEquals(remark, new Remark("note"));
        assertEquals(remark.hashCode(), new Remark("note").hashCode());
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("note"));
        assertFalse(remark.equals(new Remark("other")));
    }
}
