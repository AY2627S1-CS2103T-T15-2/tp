package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.testutil.PersonBuilder;

public class LinkCommandTest {

    private final Person student = new PersonBuilder().withName("Alex Tan").build();
    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person otherGuardian = new PersonBuilder().withName("Sarah Lim").withRole("guardian").build();
    private final Model model = createModel();

    private Model createModel() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(otherGuardian, guardian, student));
        return new ModelManager(book, new UserPrefs());
    }

    private LinkCommand createLinkCommand(int studentIndex, int guardianIndex) {
        return new LinkCommand(Index.fromOneBased(studentIndex), Index.fromOneBased(guardianIndex));
    }

    private Person getCurrentStudent() {
        return PersonLookup.findById(model.getAddressBook().getPersonList(), student.getId()).orElseThrow();
    }

    @Test
    public void execute_firstLink_usesSortedDisplayIndexesAndSelectsStudent() throws Exception {
        CommandResult result = createLinkCommand(1, 2).execute(model);
        assertEquals(Optional.of(guardian.getId()), getCurrentStudent().getGuardianId());
        assertEquals(student.getId(), getCurrentStudent().getId());
        assertEquals(Optional.of(student.getId()), result.getSelectedContactId());
        assertEquals("Linked student Alex Tan to guardian Mei Tan.", result.getFeedbackToUser());
        assertEquals(List.of(getCurrentStudent(), guardian, otherGuardian), List.copyOf(model.getFilteredPersonList()));
    }

    @Test
    public void execute_filteredList_usesIndexesInCurrentResults() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().fullName.endsWith("Tan"));
        createLinkCommand(1, 2).execute(model);
        assertEquals(Optional.of(guardian.getId()), getCurrentStudent().getGuardianId());
        assertEquals(2, model.getFilteredPersonList().size());
        assertUnchangedFailure(createLinkCommand(1, 3), "No contact exists at guardian index 3.");
    }

    @Test
    public void execute_duplicateLink_leavesExistingRelationshipUnchanged() throws Exception {
        createLinkCommand(1, 2).execute(model);
        assertUnchangedFailure(createLinkCommand(1, 2), "Alex Tan is already linked to Mei Tan.");
    }

    @Test
    public void execute_differentGuardian_replacesRelationship() throws Exception {
        createLinkCommand(1, 2).execute(model);
        CommandResult result = createLinkCommand(1, 3).execute(model);
        assertEquals(Optional.of(otherGuardian.getId()), getCurrentStudent().getGuardianId());
        assertEquals("Changed Alex Tan’s guardian from Mei Tan to Sarah Lim.", result.getFeedbackToUser());
    }

    @Test
    public void execute_multipleStudents_canShareGuardian() throws Exception {
        Person secondStudent = new PersonBuilder().withName("Bea Tan").build();
        model.addPerson(secondStudent);
        createLinkCommand(1, 3).execute(model);
        createLinkCommand(2, 3).execute(model);
        assertEquals(Optional.of(guardian.getId()), getCurrentStudent().getGuardianId());
        assertEquals(Optional.of(guardian.getId()), model.getFilteredPersonList().get(1).getGuardianId());
    }

    @Test
    public void execute_invalidIndexesOrRoles_doNotChangeContacts() {
        assertUnchangedFailure(createLinkCommand(4, 2), "No contact exists at student index 4.");
        assertUnchangedFailure(createLinkCommand(1, 4), "No contact exists at guardian index 4.");
        assertUnchangedFailure(createLinkCommand(2, 3), LinkCommand.MESSAGE_NOT_STUDENT);
        assertUnchangedFailure(createLinkCommand(1, 1), LinkCommand.MESSAGE_NOT_GUARDIAN);
    }

    @Test
    public void execute_missingOldGuardian_reportsErrorWithoutChangingLink() {
        model.setPerson(student, student.withGuardianId(Optional.of(ContactId.generate())));
        assertUnchangedFailure(createLinkCommand(1, 2), LinkCommand.MESSAGE_MISSING_OLD_GUARDIAN);
    }

    @Test
    public void equals_comparesBothIndexes() {
        LinkCommand first = createLinkCommand(1, 2);
        assertEquals(first, createLinkCommand(1, 2));
        assertEquals(first, first);
        assertNotEquals(first, createLinkCommand(1, 3));
        assertNotEquals(first, createLinkCommand(2, 2));
        assertFalse(first.equals(null));
        assertFalse(first.equals("link"));
        assertTrue(first.toString().contains("studentIndex"));
    }

    private void assertUnchangedFailure(LinkCommand linkCommand, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        assertThrows(CommandException.class, message, () -> linkCommand.execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, List.copyOf(model.getFilteredPersonList()));
    }
}
