package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_linkedStudent_reportsRemovedLinkAndRetainsGuardian() throws Exception {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(student, guardian));
        Model linkedModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new DeleteCommand(INDEX_FIRST_PERSON).execute(linkedModel);
        assertEquals("Deleted contact: Alex Tan [Student]. Removed 1 student-guardian relationship.",
                result.getFeedbackToUser());
        assertEquals(List.of(guardian), List.copyOf(linkedModel.getAddressBook().getPersonList()));
    }

    @Test
    public void execute_linkedGuardian_reportsRemovedLinkToHiddenStudent() throws Exception {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(student, guardian));
        Model linkedModel = new ModelManager(addressBook, new UserPrefs());
        linkedModel.updateFilteredPersonList(person -> person.getId().equals(guardian.getId()));

        CommandResult result = new DeleteCommand(INDEX_FIRST_PERSON).execute(linkedModel);
        assertEquals("Deleted contact: Mei Tan [Guardian]. Removed 1 student-guardian relationship.",
                result.getFeedbackToUser());
        assertEquals(student.getId(), linkedModel.getAddressBook().getPersonList().getFirst().getId());
        assertTrue(linkedModel.getAddressBook().getPersonList().getFirst().getGuardianId().isEmpty());
    }

    @Test
    public void execute_guardianOfMultipleStudents_reportsCountAndClearsAllLinks() throws Exception {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        Person otherStudent = new PersonBuilder().withName("Bea Tan").withGuardianId(guardian.getId()).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(student, otherStudent, guardian));
        Model linkedModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new DeleteCommand(Index.fromOneBased(3)).execute(linkedModel);
        assertEquals("Deleted contact: Mei Tan [Guardian]. Removed 2 student-guardian relationships.",
                result.getFeedbackToUser());
        assertEquals(2, linkedModel.getAddressBook().getPersonList().size());
        assertTrue(linkedModel.getAddressBook().getPersonList().stream()
                .allMatch(person -> person.getGuardianId().isEmpty()));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CONTACT_SUCCESS,
                personToDelete.getName(), personToDelete.getRole());

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CONTACT_SUCCESS,
                personToDelete.getName(), personToDelete.getRole());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
