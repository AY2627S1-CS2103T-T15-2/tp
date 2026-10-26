package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
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
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code ViewCommand}.
 */
public class ViewCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_selectsContact() {
        Person personToView = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        CommandResult expectedResult = new CommandResult(String.format(ViewCommand.MESSAGE_SUCCESS,
                personToView.getName(), personToView.getRole()), personToView.getId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ViewCommand(INDEX_SECOND_PERSON), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_selectsContactAndKeepsFilter() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person personToView = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CommandResult expectedResult = new CommandResult(String.format(ViewCommand.MESSAGE_SUCCESS,
                personToView.getName(), personToView.getRole()), personToView.getId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_formerStudentInFormerView_selectsStudentAndKeepsFormerView() throws Exception {
        Person activeStudent = new PersonBuilder().withName("Alex Tan").build();
        Person formerStudent = new PersonBuilder().withName("Bea Tan").withStatus(StudentStatus.FORMER).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(activeStudent, formerStudent));
        Model statusModel = new ModelManager(addressBook, new UserPrefs());
        statusModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);

        CommandResult result = new ViewCommand(INDEX_FIRST_PERSON).execute(statusModel);

        assertEquals("Viewing contact: Bea Tan [Student].", result.getFeedbackToUser());
        assertEquals(formerStudent.getId(), result.getSelectedContactId().orElseThrow());
        assertEquals(List.of(formerStudent), List.copyOf(statusModel.getFilteredPersonList()));
    }

    @Test
    public void execute_lastIndexUnfilteredList_selectsContact() {
        // Boundary: the last displayed index
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person personToView = model.getFilteredPersonList().get(lastIndex.getZeroBased());
        CommandResult expectedResult = new CommandResult(String.format(ViewCommand.MESSAGE_SUCCESS,
                personToView.getName(), personToView.getRole()), personToView.getId());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ViewCommand(lastIndex), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // Boundary: the first index past the displayed list
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model,
                String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, outOfBoundIndex.getOneBased()));
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model,
                String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, outOfBoundIndex.getOneBased()));
    }

    @Test
    public void isDataChanging_returnsFalse() {
        assertFalse(new ViewCommand(INDEX_FIRST_PERSON).isDataChanging());
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(viewFirstCommand.equals(viewFirstCommand));

        // same values -> returns true
        assertTrue(viewFirstCommand.equals(new ViewCommand(INDEX_FIRST_PERSON)));

        // different types -> returns false
        assertFalse(viewFirstCommand.equals(1));

        // null -> returns false
        assertFalse(viewFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(viewFirstCommand.equals(viewSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        ViewCommand viewCommand = new ViewCommand(targetIndex);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, viewCommand.toString());
    }
}
