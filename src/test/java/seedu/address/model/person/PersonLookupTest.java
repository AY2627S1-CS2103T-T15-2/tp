package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
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

    @Test
    public void findByExactName_caseAndSpacesDiffer_returnsOriginalContact() {
        Person contact = new PersonBuilder().withName("Alice  Tan").build();
        List<Person> matches = PersonLookup.findByExactName(List.of(contact), new Name("  aLiCe Tan  "));
        assertEquals(1, matches.size());
        assertSame(contact, matches.getFirst());
    }

    @Test
    public void findByExactName_canonicalSpellingsDiffer_returnsOriginalContact() {
        List<String> spellings = List.of("Jos\u00e9 Tan", "Jose\u0301 Tan");
        for (String storedName : spellings) {
            Person contact = new PersonBuilder().withName(storedName).build();
            for (String lookupName : spellings) {
                List<Person> matches = PersonLookup.findByExactName(List.of(contact), new Name(lookupName));
                assertEquals(1, matches.size());
                assertSame(contact, matches.getFirst());
                assertEquals(storedName, contact.getName().fullName);
            }
            assertTrue(PersonLookup.findByExactName(List.of(contact), new Name("Jose Tan")).isEmpty());
            assertTrue(PersonLookup.findByExactName(List.of(contact), new Name("Jos\u00e8 Tan")).isEmpty());
        }
    }

    @Test
    public void findByExactName_caseDependentComposition_returnsOriginalContact() {
        for (String storedName : List.of("\u01f0 Tan", "J\u030c Tan")) {
            Person contact = new PersonBuilder().withName(storedName).build();
            for (String lookupName : List.of("\u01f0 Tan", "J\u030c Tan")) {
                assertSame(contact, PersonLookup.findByExactName(List.of(contact), new Name(lookupName)).getFirst());
            }
        }
    }

    @Test
    public void findByExactName_apostropheVariants_returnsOriginalContact() {
        for (String storedName : List.of("O'Brien", "O\u2018Brien", "O\u2019Brien")) {
            Person contact = new PersonBuilder().withName(storedName).build();
            for (String lookupName : List.of("O'Brien", "O\u2018Brien", "O\u2019Brien")) {
                assertSame(contact, PersonLookup.findByExactName(List.of(contact), new Name(lookupName)).getFirst());
                assertEquals(storedName, contact.getName().fullName);
            }
        }
    }

    @Test
    public void findByExactName_multipleMatches_returnsAllInSuppliedOrder() {
        Person student = new PersonBuilder().withName("Alice Tan").build();
        Person guardian = new PersonBuilder().withName("ALICE TAN").withRole("guardian").build();
        List<Person> contacts = List.of(guardian, student);
        List<Person> matches = PersonLookup.findByExactName(contacts, new Name("Alice Tan"));
        assertEquals(2, matches.size());
        assertSame(guardian, matches.get(0));
        assertSame(student, matches.get(1));
    }

    @Test
    public void findByExactName_partialOrReorderedName_returnsEmpty() {
        Person contact = new PersonBuilder().withName("Alice Tan").build();
        for (String name : List.of("Alice", "Tan", "Tan Alice", "Alice Tan Lee")) {
            assertTrue(PersonLookup.findByExactName(List.of(contact), new Name(name)).isEmpty());
        }
        assertTrue(PersonLookup.findByExactName(List.of(), new Name("Alice Tan")).isEmpty());
    }

    @Test
    public void findByExactName_accentsAndPunctuationDiffer_returnsEmpty() {
        Person contact = new PersonBuilder().withName("Élise O’Brien").build();
        assertTrue(PersonLookup.findByExactName(List.of(contact), new Name("Elise O’Brien")).isEmpty());
        assertTrue(PersonLookup.findByExactName(List.of(contact), new Name("Élise O-Brien")).isEmpty());
    }

    @Test
    public void findByExactName_hiddenContact_isNotSearched() {
        Person hidden = new PersonBuilder().withName("Alice Tan").build();
        Person displayed = new PersonBuilder().withName("Bob Tan").build();
        Model model = new ModelManager();
        model.addPerson(hidden);
        model.addPerson(displayed);
        model.updateFilteredPersonList(person -> person.getId().equals(displayed.getId()));

        assertTrue(PersonLookup.findByExactName(model.getFilteredPersonList(), hidden.getName()).isEmpty());
        assertSame(displayed, PersonLookup.findByExactName(model.getFilteredPersonList(), displayed.getName())
                .getFirst());
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void findByExactName_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PersonLookup.findByExactName(null, new Name("Alice Tan")));
        assertThrows(NullPointerException.class, () -> PersonLookup.findByExactName(List.of(), null));
    }
}
