package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        String expectedMessage = "Listed " + getTypicalPersons().size() + " contacts";
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        String expectedMessage = "Listed " + getTypicalPersons().size() + " contacts";
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_oneContact_singularMessage() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model oneContactModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedOneContactModel = new ModelManager(addressBook, new UserPrefs());
        assertCommandSuccess(new ListCommand(), oneContactModel, "Listed 1 contact", expectedOneContactModel);
    }

    @Test
    public void execute_noContacts_noContactsFoundMessage() {
        Model emptyModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel, ListCommand.MESSAGE_NO_CONTACTS, new ModelManager());
    }

    @Test
    public void execute_formerStudentExists_hidesFormerStudent() {
        Person former = BENSON.withStatus(StudentStatus.FORMER);
        model.setPerson(BENSON, former);
        expectedModel.setPerson(BENSON, former);
        String expectedMessage = "Listed " + (getTypicalPersons().size() - 1) + " contacts";
        assertCommandSuccess(new ListCommand(), model, expectedMessage, expectedModel);
        assertFalse(model.getFilteredPersonList().contains(former));
    }

    @Test
    public void execute_former_showsFormerStudentsOnly() {
        Person former = BENSON.withStatus(StudentStatus.FORMER);
        model.setPerson(BENSON, former);
        expectedModel.setPerson(BENSON, former);
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertCommandSuccess(new ListCommand(true), model, "Listed 1 former student", expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().get(0).isFormerStudent());
    }

    @Test
    public void execute_formerWithSeveral_pluralMessage() {
        Person formerAlice = ALICE.withStatus(StudentStatus.FORMER);
        Person formerBenson = BENSON.withStatus(StudentStatus.FORMER);
        model.setPerson(ALICE, formerAlice);
        model.setPerson(BENSON, formerBenson);
        expectedModel.setPerson(ALICE, formerAlice);
        expectedModel.setPerson(BENSON, formerBenson);
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertCommandSuccess(new ListCommand(true), model, "Listed 2 former students", expectedModel);
    }

    @Test
    public void execute_formerWithNoFormerStudents_noFormerStudentsMessage() {
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertCommandSuccess(new ListCommand(true), model, ListCommand.MESSAGE_NO_FORMER_STUDENTS,
                expectedModel);
    }

    @Test
    public void execute_onlyFormerStudents_suggestsListFormer() {
        Person former = new PersonBuilder().withStatus(StudentStatus.FORMER).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(former);
        Model formerOnlyModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedFormerOnlyModel = new ModelManager(addressBook, new UserPrefs());
        assertCommandSuccess(new ListCommand(), formerOnlyModel, ListCommand.MESSAGE_ONLY_FORMER_STUDENTS,
                expectedFormerOnlyModel);
    }

    @Test
    public void equals() {
        ListCommand listCommand = new ListCommand();
        ListCommand listFormerCommand = new ListCommand(true);

        assertTrue(listCommand.equals(listCommand));
        assertTrue(listCommand.equals(new ListCommand(false)));
        assertTrue(listFormerCommand.equals(new ListCommand(true)));
        assertFalse(listCommand.equals(listFormerCommand));
        assertFalse(listCommand.equals(1));
        assertFalse(listCommand.equals(null));
    }

    @Test
    public void toStringMethod() {
        String expected = ListCommand.class.getCanonicalName() + "{isFormerList=true}";
        assertEquals(expected, new ListCommand(true).toString());
    }
}
