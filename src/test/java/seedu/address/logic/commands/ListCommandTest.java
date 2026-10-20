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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private static final Person ACTIVE_STUDENT = new PersonBuilder().withName("Alex Tan").build();
    private static final Person FORMER_STUDENT = new PersonBuilder().withName("Bea Tan")
            .withStatus(StudentStatus.FORMER).build();
    private static final Person GUARDIAN = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();

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
        assertCommandSuccess(new ListCommand(ContactFilter.FORMER_STUDENTS), model, "Listed 1 former student",
                expectedModel);
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
        assertCommandSuccess(new ListCommand(ContactFilter.FORMER_STUDENTS), model, "Listed 2 former students",
                expectedModel);
    }

    @Test
    public void execute_formerWithNoFormerStudents_noFormerStudentsMessage() {
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertCommandSuccess(new ListCommand(ContactFilter.FORMER_STUDENTS), model,
                ListCommand.MESSAGE_NO_FORMER_STUDENTS, expectedModel);
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
    public void execute_roleStudent_showsActiveStudentsOnly() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE)).execute(mixedModel);

        assertEquals("Listed 1 contact", result.getFeedbackToUser());
        assertEquals(List.of(ACTIVE_STUDENT), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_roleGuardian_showsGuardiansOnly() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(
                new ContactFilter(Optional.of(Role.GUARDIAN), StudentStatus.ACTIVE)).execute(mixedModel);

        assertEquals("Listed 1 contact", result.getFeedbackToUser());
        assertEquals(List.of(GUARDIAN), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_roleStudentAndFormer_showsFormerStudentsOnly() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.FORMER)).execute(mixedModel);

        assertEquals("Listed 1 former student", result.getFeedbackToUser());
        assertEquals(List.of(FORMER_STUDENT), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_guardianRoleWithNoGuardians_noGuardiansMessage() {
        Model expectedStudentsOnlyModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        ContactFilter guardiansOnly = new ContactFilter(Optional.of(Role.GUARDIAN), StudentStatus.ACTIVE);
        expectedStudentsOnlyModel.updateFilteredPersonList(guardiansOnly);

        assertCommandSuccess(new ListCommand(guardiansOnly), model, ListCommand.MESSAGE_NO_GUARDIANS,
                expectedStudentsOnlyModel);
    }

    @Test
    public void execute_studentRoleWithOnlyFormerStudents_suggestsListingFormerStudents() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(FORMER_STUDENT, GUARDIAN));
        Model formerStudentsModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE)).execute(formerStudentsModel);

        assertEquals("No active students found. Use list r/student st/former to see former students.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_studentRoleWithNoStudents_noStudentsMessage() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(GUARDIAN));
        Model guardiansOnlyModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE)).execute(guardiansOnlyModel);

        assertEquals(ListCommand.MESSAGE_NO_STUDENTS, result.getFeedbackToUser());
    }

    @Test
    public void equals() {
        ListCommand listCommand = new ListCommand();
        ListCommand listFormerCommand = new ListCommand(ContactFilter.FORMER_STUDENTS);

        assertTrue(listCommand.equals(listCommand));
        assertTrue(listCommand.equals(new ListCommand(ContactFilter.ACTIVE_CONTACTS)));
        assertTrue(listFormerCommand.equals(new ListCommand(ContactFilter.FORMER_STUDENTS)));
        assertFalse(listCommand.equals(listFormerCommand));
        assertFalse(listCommand.equals(new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE))));
        assertFalse(listCommand.equals(1));
        assertFalse(listCommand.equals(null));
    }

    /**
     * Returns a model with an active student, a former student and a guardian.
     */
    private static Model createMixedModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(ACTIVE_STUDENT, FORMER_STUDENT, GUARDIAN));
        return new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void toStringMethod() {
        String expected = ListCommand.class.getCanonicalName() + "{filter=" + ContactFilter.FORMER_STUDENTS + "}";
        assertEquals(expected, new ListCommand(ContactFilter.FORMER_STUDENTS).toString());
    }
}
