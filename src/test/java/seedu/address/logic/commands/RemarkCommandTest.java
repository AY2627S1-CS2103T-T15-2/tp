package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_addRemark_success() {
        assertRemarkSuccess("Likes baseball");
    }

    @Test
    public void execute_replaceAndClearRemark_success() {
        assertRemarkSuccess("Original remark");
        assertRemarkSuccess("Updated remark");
        assertRemarkSuccess("");
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess("Remark for the displayed person");
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index invalidIndex = Index.fromZeroBased(model.getFilteredPersonList().size());
        assertCommandFailure(new RemarkCommand(invalidIndex, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_editOtherFields_preservesRemark() throws Exception {
        assertRemarkSuccess("Keep this remark");
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withPhone("91234567").build())
                .execute(model);
        assertEquals(new Remark("Keep this remark"), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertEquals(command, command);
        assertEquals(command, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("different"))));
    }

    private void assertRemarkSuccess(String value) {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withRemark(value).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = value.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(value)), model,
                String.format(message, Messages.format(edited)), expectedModel);
    }
}
