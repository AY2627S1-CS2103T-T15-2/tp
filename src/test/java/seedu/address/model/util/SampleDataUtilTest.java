package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_returnsContactsWithEmails() {
        Person[] persons = SampleDataUtil.getSamplePersons();

        assertEquals(6, persons.length);
        assertTrue(persons[0].getEmail().isPresent());
        assertEquals("alexyeoh@example.com", persons[0].getEmail().orElseThrow().value);
        assertTrue(persons[1].getEmail().isPresent());
        assertEquals("royb@example.com", persons[5].getEmail().orElseThrow().value);
    }

    @Test
    public void getSampleAddressBook_returnsAllSampleContacts() {
        ReadOnlyAddressBook addressBook = SampleDataUtil.getSampleAddressBook();

        assertEquals(6, addressBook.getPersonList().size());
        assertEquals("Alex Yeoh", addressBook.getPersonList().get(0).getName().fullName);
    }

    @Test
    public void getTagSet_returnsTagsForAllValues() {
        Set<Tag> tags = SampleDataUtil.getTagSet("friends", "colleagues");

        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(tag -> tag.tagName.equals("friends")));
        assertTrue(tags.stream().anyMatch(tag -> tag.tagName.equals("colleagues")));
    }
}
