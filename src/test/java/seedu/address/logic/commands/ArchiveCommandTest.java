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
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code ArchiveCommand}.
 */
public class ArchiveCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_activeStudent_marksFormer() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format(ArchiveCommand.MESSAGE_SUCCESS, student.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, student.withStatus(StudentStatus.FORMER));

        assertCommandSuccess(new ArchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format(ArchiveCommand.MESSAGE_SUCCESS, student.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(student, student.withStatus(StudentStatus.FORMER));

        assertCommandSuccess(new ArchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_linkedStudent_keepsGuardianAndDetails() throws Exception {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(student, guardian));
        Model linkedModel = new ModelManager(addressBook, new UserPrefs());

        new ArchiveCommand(INDEX_FIRST_PERSON).execute(linkedModel);

        Person archived = linkedModel.getAddressBook().getPersonList().stream()
                .filter(person -> person.getId().equals(student.getId())).findFirst().orElseThrow();
        assertEquals(student.withStatus(StudentStatus.FORMER), archived);
        assertEquals(student.getGuardianId(), archived.getGuardianId());
    }

    @Test
    public void execute_guardian_throwsCommandException() {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(guardian));
        Model guardianModel = new ModelManager(addressBook, new UserPrefs());

        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PERSON), guardianModel,
                ArchiveCommand.MESSAGE_NOT_STUDENT);
    }

    @Test
    public void execute_alreadyFormer_throwsCommandException() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(student, student.withStatus(StudentStatus.FORMER));

        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PERSON), model,
                String.format(ArchiveCommand.MESSAGE_ALREADY_FORMER, student.getName()));
    }

    @Test
    public void execute_indexOutOfBounds_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ArchiveCommand(outOfBounds), model,
                String.format(ArchiveCommand.MESSAGE_CONTACT_NOT_FOUND, outOfBounds.getOneBased()));
    }

    @Test
    public void isDataChanging_returnsTrue() {
        assertTrue(new ArchiveCommand(INDEX_FIRST_PERSON).isDataChanging());
    }

    @Test
    public void equals() {
        ArchiveCommand archiveFirst = new ArchiveCommand(INDEX_FIRST_PERSON);

        assertTrue(archiveFirst.equals(archiveFirst));
        assertTrue(archiveFirst.equals(new ArchiveCommand(INDEX_FIRST_PERSON)));
        assertFalse(archiveFirst.equals(new ArchiveCommand(INDEX_SECOND_PERSON)));
        assertFalse(archiveFirst.equals(new UnarchiveCommand(INDEX_FIRST_PERSON)));
        assertFalse(archiveFirst.equals(1));
        assertFalse(archiveFirst.equals(null));
    }

    @Test
    public void toStringMethod() {
        ArchiveCommand archiveCommand = new ArchiveCommand(INDEX_FIRST_PERSON);
        String expected = ArchiveCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", targetStatus=" + StudentStatus.FORMER + "}";
        assertEquals(expected, archiveCommand.toString());
    }
}
