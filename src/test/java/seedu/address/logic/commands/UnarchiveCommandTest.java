package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code UnarchiveCommand}.
 */
public class UnarchiveCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_formerStudent_marksActive() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person former = student.withStatus(StudentStatus.FORMER);
        model.setPerson(student, former);
        model.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_SUCCESS, student.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        expectedModel.setPerson(former, student);

        assertCommandSuccess(new UnarchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_guardian_throwsCommandException() {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(guardian));
        Model guardianModel = new ModelManager(addressBook, new UserPrefs());

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PERSON), guardianModel,
                UnarchiveCommand.MESSAGE_NOT_STUDENT);
    }

    @Test
    public void execute_alreadyActive_throwsCommandException() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PERSON), model,
                String.format(UnarchiveCommand.MESSAGE_ALREADY_ACTIVE, student.getName()));
    }

    @Test
    public void execute_indexOutOfBounds_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new UnarchiveCommand(outOfBounds), model,
                String.format(UnarchiveCommand.MESSAGE_CONTACT_NOT_FOUND, outOfBounds.getOneBased()));
    }

    @Test
    public void equals() {
        UnarchiveCommand unarchiveFirst = new UnarchiveCommand(INDEX_FIRST_PERSON);

        assertTrue(unarchiveFirst.equals(unarchiveFirst));
        assertTrue(unarchiveFirst.equals(new UnarchiveCommand(INDEX_FIRST_PERSON)));
        assertFalse(unarchiveFirst.equals(new UnarchiveCommand(INDEX_SECOND_PERSON)));
        assertFalse(unarchiveFirst.equals(new ArchiveCommand(INDEX_FIRST_PERSON)));
        assertFalse(unarchiveFirst.equals(null));
    }

    @Test
    public void toStringMethod() {
        String expected = UnarchiveCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", targetStatus=" + StudentStatus.ACTIVE + "}";
        assertEquals(expected, new UnarchiveCommand(INDEX_FIRST_PERSON).toString());
    }
}
