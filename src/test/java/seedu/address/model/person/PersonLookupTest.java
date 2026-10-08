package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonLookupTest {

    @Test
    public void findById_equalDetailsDifferentIds_returnsExactContact() {
        Person first = new PersonBuilder().build();
        Person second = new PersonBuilder(first).withId(ContactId.generate()).build();
        assertSame(second, PersonLookup.findById(List.of(first, second), second.getId()).orElseThrow());
    }

    @Test
    public void findById_absentContact_returnsEmpty() {
        assertTrue(PersonLookup.findById(List.of(new PersonBuilder().build()), ContactId.generate()).isEmpty());
        assertTrue(PersonLookup.findById(List.of(), ContactId.generate()).isEmpty());
    }

    @Test
    public void findById_nullArgument_rejectsInvalidLookup() {
        assertThrows(NullPointerException.class, () -> PersonLookup.findById(null, ContactId.generate()));
        assertThrows(NullPointerException.class, () -> PersonLookup.findById(List.of(), null));
    }
}
