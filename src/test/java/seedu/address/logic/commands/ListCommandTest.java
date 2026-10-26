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
import seedu.address.model.person.Level;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private static final Person ACTIVE_STUDENT = new PersonBuilder().withName("Alex Tan").build();
    private static final Person FORMER_STUDENT = new PersonBuilder().withName("Bea Tan")
            .withStatus(StudentStatus.FORMER).build();
    private static final Person GUARDIAN = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private static final Person SEC3_MATH_STUDENT = new PersonBuilder().withName("Cal Lim")
            .withLevel("Sec 3").withSubject("Math").build();
    private static final Person SEC3_PHYSICS_STUDENT = new PersonBuilder().withName("Dan Lim")
            .withLevel("Sec 3").withSubject("Physics").build();
    private static final Person FORMER_SEC3_MATH_STUDENT = new PersonBuilder().withName("Eve Lim")
            .withLevel("Sec 3").withSubject("Math").withStatus(StudentStatus.FORMER).build();
    private static final Person SEC4_MATH_STUDENT = new PersonBuilder().withName("Fay Lim")
            .withLevel("Sec 4").withSubject("Math").build();

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
        String expectedMessage = "Listed " + (getTypicalPersons().size() - 1) + " contacts. "
                + "1 former student hidden. Add st/all to include them.";
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
    public void execute_roleStudent_showsActiveStudentsAndHiddenHint() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(filterOf(Role.STUDENT, StudentStatus.ACTIVE)).execute(mixedModel);

        assertEquals("Listed 1 contact. 1 former student hidden. Add st/all to include them.",
                result.getFeedbackToUser());
        assertEquals(List.of(ACTIVE_STUDENT), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_roleGuardian_showsGuardiansOnlyWithoutHint() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(filterOf(Role.GUARDIAN, StudentStatus.ACTIVE)).execute(mixedModel);

        assertEquals("Listed 1 contact", result.getFeedbackToUser());
        assertEquals(List.of(GUARDIAN), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_roleStudentAndFormer_showsFormerStudentsOnly() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(filterOf(Role.STUDENT, StudentStatus.FORMER)).execute(mixedModel);

        assertEquals("Listed 1 former student", result.getFeedbackToUser());
        assertEquals(List.of(FORMER_STUDENT), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_guardianRoleWithNoGuardians_noGuardiansMessage() {
        Model expectedStudentsOnlyModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        ContactFilter guardiansOnly = filterOf(Role.GUARDIAN, StudentStatus.ACTIVE);
        expectedStudentsOnlyModel.updateFilteredPersonList(guardiansOnly);

        assertCommandSuccess(new ListCommand(guardiansOnly), model, ListCommand.MESSAGE_NO_GUARDIANS,
                expectedStudentsOnlyModel);
    }

    @Test
    public void execute_studentRoleWithOnlyFormerStudents_suggestsListingFormerStudents() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(FORMER_STUDENT, GUARDIAN));
        Model formerStudentsModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListCommand(filterOf(Role.STUDENT, StudentStatus.ACTIVE))
                .execute(formerStudentsModel);

        assertEquals("No active students found. Use list r/student st/former to see former students.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_studentRoleWithNoStudents_noStudentsMessage() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(GUARDIAN));
        Model guardiansOnlyModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListCommand(filterOf(Role.STUDENT, StudentStatus.ACTIVE))
                .execute(guardiansOnlyModel);

        assertEquals(ListCommand.MESSAGE_NO_STUDENTS, result.getFeedbackToUser());
    }

    @Test
    public void execute_defaultWithFormerStudent_showsHiddenHint() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand().execute(mixedModel);

        assertEquals("Listed 2 contacts. 1 former student hidden. Add st/all to include them.",
                result.getFeedbackToUser());
        assertEquals(List.of(ACTIVE_STUDENT, GUARDIAN), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_activeStatusWithSeveralFormerStudents_pluralHint() {
        AddressBook addressBook = new AddressBook();
        Person otherFormerStudent = new PersonBuilder().withName("Cara Tan").withStatus(StudentStatus.FORMER).build();
        addressBook.setPersons(List.of(ACTIVE_STUDENT, FORMER_STUDENT, otherFormerStudent));
        Model severalFormerModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new ListCommand(filterOf(null, StudentStatus.ACTIVE)).execute(severalFormerModel);

        assertEquals("Listed 1 contact. 2 former students hidden. Add st/all to include them.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_allStatuses_showsEveryContactWithoutHint() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(ContactFilter.ALL_CONTACTS).execute(mixedModel);

        assertEquals("Listed 3 contacts", result.getFeedbackToUser());
        assertEquals(List.of(ACTIVE_STUDENT, FORMER_STUDENT, GUARDIAN),
                List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_studentRoleAllStatuses_showsActiveAndFormerStudents() {
        Model mixedModel = createMixedModel();
        CommandResult result = new ListCommand(filterOf(Role.STUDENT, null)).execute(mixedModel);

        assertEquals("Listed 2 contacts", result.getFeedbackToUser());
        assertEquals(List.of(ACTIVE_STUDENT, FORMER_STUDENT), List.copyOf(mixedModel.getFilteredPersonList()));
    }

    @Test
    public void execute_allStatusesWithNoContacts_noContactsFoundMessage() {
        CommandResult result = new ListCommand(ContactFilter.ALL_CONTACTS).execute(new ModelManager());

        assertEquals(ListCommand.MESSAGE_NO_CONTACTS, result.getFeedbackToUser());
    }

    @Test
    public void execute_level_showsMatchingStudentsAndHiddenHint() {
        Model studentDetailModel = createStudentDetailModel();
        CommandResult result = new ListCommand(detailFilterOf("Sec 3", null)).execute(studentDetailModel);

        assertEquals("Listed 2 contacts. 1 former student hidden. Add st/all to include them.",
                result.getFeedbackToUser());
        assertEquals(List.of(SEC3_MATH_STUDENT, SEC3_PHYSICS_STUDENT),
                List.copyOf(studentDetailModel.getFilteredPersonList()));
    }

    @Test
    public void execute_subject_showsMatchingStudents() {
        Model studentDetailModel = createStudentDetailModel();
        CommandResult result = new ListCommand(detailFilterOf(null, "Physics")).execute(studentDetailModel);

        assertEquals("Listed 1 contact", result.getFeedbackToUser());
        assertEquals(List.of(SEC3_PHYSICS_STUDENT), List.copyOf(studentDetailModel.getFilteredPersonList()));
    }

    @Test
    public void execute_levelAndSubject_showsStudentsMatchingBoth() {
        Model studentDetailModel = createStudentDetailModel();
        CommandResult result = new ListCommand(detailFilterOf("Sec 3", "Math")).execute(studentDetailModel);

        assertEquals("Listed 1 contact. 1 former student hidden. Add st/all to include them.",
                result.getFeedbackToUser());
        assertEquals(List.of(SEC3_MATH_STUDENT), List.copyOf(studentDetailModel.getFilteredPersonList()));
    }

    @Test
    public void execute_levelAndAllStatuses_showsActiveAndFormerStudents() {
        Model studentDetailModel = createStudentDetailModel();
        ContactFilter filter = new ContactFilter(Optional.empty(), Optional.empty(),
                Optional.of(new Level("Sec 3")), Optional.of(new Subject("Math")));
        CommandResult result = new ListCommand(filter).execute(studentDetailModel);

        assertEquals("Listed 2 contacts", result.getFeedbackToUser());
        assertEquals(List.of(SEC3_MATH_STUDENT, FORMER_SEC3_MATH_STUDENT),
                List.copyOf(studentDetailModel.getFilteredPersonList()));
    }

    @Test
    public void execute_levelAndFormer_showsFormerStudentsOnly() {
        Model studentDetailModel = createStudentDetailModel();
        ContactFilter filter = new ContactFilter(Optional.empty(), Optional.of(StudentStatus.FORMER),
                Optional.of(new Level("Sec 3")), Optional.empty());
        CommandResult result = new ListCommand(filter).execute(studentDetailModel);

        assertEquals("Listed 1 former student", result.getFeedbackToUser());
        assertEquals(List.of(FORMER_SEC3_MATH_STUDENT), List.copyOf(studentDetailModel.getFilteredPersonList()));
    }

    @Test
    public void execute_formerAndLevelWithNoMatches_noContactsFoundMessage() {
        // A former student exists, but at another level, so "No former students found." would be misleading
        Model studentDetailModel = createStudentDetailModel();
        ContactFilter filter = new ContactFilter(Optional.empty(), Optional.of(StudentStatus.FORMER),
                Optional.of(new Level("Sec 4")), Optional.empty());
        CommandResult result = new ListCommand(filter).execute(studentDetailModel);

        assertEquals(ListCommand.MESSAGE_NO_CONTACTS, result.getFeedbackToUser());
        assertTrue(studentDetailModel.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_levelWithNoMatches_noContactsFoundMessage() {
        Model studentDetailModel = createStudentDetailModel();
        CommandResult result = new ListCommand(detailFilterOf("JC1", null)).execute(studentDetailModel);

        assertEquals(ListCommand.MESSAGE_NO_CONTACTS, result.getFeedbackToUser());
        assertTrue(studentDetailModel.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_levelWithOnlyFormerMatches_noContactsFoundWithHiddenHint() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(FORMER_SEC3_MATH_STUDENT, GUARDIAN));
        Model formerOnlyModel = new ModelManager(addressBook, new UserPrefs());
        CommandResult result = new ListCommand(detailFilterOf("Sec 3", null)).execute(formerOnlyModel);

        assertEquals("No contacts found. 1 former student hidden. Add st/all to include them.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_studentRoleAndLevelWithNoMatches_noContactsFoundMessage() {
        Model studentDetailModel = createStudentDetailModel();
        ContactFilter filter = new ContactFilter(Optional.of(Role.STUDENT), Optional.of(StudentStatus.ACTIVE),
                Optional.of(new Level("JC1")), Optional.empty());
        CommandResult result = new ListCommand(filter).execute(studentDetailModel);

        assertEquals(ListCommand.MESSAGE_NO_CONTACTS, result.getFeedbackToUser());
    }

    @Test
    public void equals() {
        ListCommand listCommand = new ListCommand();
        ListCommand listFormerCommand = new ListCommand(ContactFilter.FORMER_STUDENTS);

        assertTrue(listCommand.equals(listCommand));
        assertTrue(listCommand.equals(new ListCommand(ContactFilter.ACTIVE_CONTACTS)));
        assertTrue(listFormerCommand.equals(new ListCommand(ContactFilter.FORMER_STUDENTS)));
        assertFalse(listCommand.equals(listFormerCommand));
        assertFalse(listCommand.equals(new ListCommand(filterOf(Role.STUDENT, StudentStatus.ACTIVE))));
        assertFalse(listCommand.equals(new ListCommand(ContactFilter.ALL_CONTACTS)));
        assertFalse(listCommand.equals(new ListCommand(detailFilterOf("Sec 3", null))));
        ListCommand listSec3 = new ListCommand(detailFilterOf("Sec 3", null));
        assertFalse(listSec3.equals(new ListCommand(detailFilterOf("Sec 3", "Math"))));
        assertFalse(listCommand.equals(1));
        assertFalse(listCommand.equals(null));
    }

    /**
     * Returns a filter for {@code role} and {@code status}, where {@code null} matches any value.
     */
    private static ContactFilter filterOf(Role role, StudentStatus status) {
        return new ContactFilter(Optional.ofNullable(role), Optional.ofNullable(status));
    }

    /**
     * Returns a filter for active contacts with {@code level} and {@code subject},
     * where {@code null} matches any value.
     */
    private static ContactFilter detailFilterOf(String level, String subject) {
        return new ContactFilter(Optional.empty(), Optional.of(StudentStatus.ACTIVE),
                Optional.ofNullable(level).map(Level::new), Optional.ofNullable(subject).map(Subject::new));
    }

    /**
     * Returns a model with students of different levels and subjects, one of them former, and a guardian.
     */
    private static Model createStudentDetailModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(SEC3_MATH_STUDENT, SEC3_PHYSICS_STUDENT, FORMER_SEC3_MATH_STUDENT,
                SEC4_MATH_STUDENT, GUARDIAN));
        return new ModelManager(addressBook, new UserPrefs());
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
