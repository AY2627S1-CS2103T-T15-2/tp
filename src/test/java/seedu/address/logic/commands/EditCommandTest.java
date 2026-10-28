package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_CONTACTS;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    /**
     * Returns the expected result of a successful edit of the person at {@code index}, which selects that person.
     */
    private CommandResult createExpectedResult(String message, Index index) {
        return new CommandResult(message, model.getFilteredPersonList().get(index.getZeroBased()).getId());
    }

    @Test
    public void execute_editFormerStudent_preservesStatus() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(original, original.withStatus(StudentStatus.FORMER));
        model.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        Person edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(edited.isFormerStudent());
        assertEquals(original.getId(), edited.getId());
    }

    @Test
    public void execute_editFormerStudentFromFind_showsFormerStudents() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(original, original.withStatus(StudentStatus.FORMER));
        model.updateFilteredPersonList(person -> person.getId().equals(original.getId()));
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        assertEquals(PREDICATE_SHOW_FORMER_STUDENTS, model.getPersonFilter());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(VALID_NAME_BOB, model.getFilteredPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_editActiveContact_showsActiveContacts() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person former = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(former, former.withStatus(StudentStatus.FORMER));
        model.updateFilteredPersonList(person -> person.getId().equals(original.getId()));
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        assertEquals(PREDICATE_SHOW_ACTIVE_CONTACTS, model.getPersonFilter());
        assertFalse(model.getFilteredPersonList().stream().anyMatch(Person::isFormerStudent));
    }

    @Test
    public void execute_editPhone_preservesContactId() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        Person edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(original.getId(), edited.getId());
        assertEquals(VALID_PHONE_BOB, edited.getPhone().get().value);
    }

    @Test
    public void execute_editPhone_preservesExistingLevelAndSubject() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(original).withLevel("Sec 3").withSubject("Math").build();
        model.setPerson(original, student);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(VALID_PHONE_BOB, edited.getPhone().orElseThrow().value);
        assertEquals(student.getLevel(), edited.getLevel());
        assertEquals(student.getSubject(), edited.getSubject());
        assertEquals(student.getId(), edited.getId());
    }

    @Test
    public void execute_validEdit_selectsEditedContact() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
        assertEquals(Optional.of(original.getId()), result.getSelectedContactId());
    }

    @Test
    public void execute_removeEmail_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withoutEmail().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutEmail().build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_removePhone_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withoutPhone().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutPhone().build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_removeGuardianLastContactMethod_failure() {
        Person guardian = new PersonBuilder().withRole("guardian").withoutEmail().build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(guardian);
        Model guardianModel = new ModelManager(addressBook, new UserPrefs());

        EditCommand removePhone = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withoutPhone().build());
        assertCommandFailure(removePhone, guardianModel, Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);

        EditCommand removeBoth = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withoutPhone().withoutEmail().build());
        assertCommandFailure(removeBoth, guardianModel, Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);
    }

    @Test
    public void execute_removeGuardianPhoneKeepingEmail_success() throws Exception {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(guardian);
        Model guardianModel = new ModelManager(addressBook, new UserPrefs());

        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withoutPhone().build())
                .execute(guardianModel);

        Person edited = guardianModel.getFilteredPersonList().get(0);
        assertTrue(edited.getPhone().isEmpty());
        assertEquals(guardian.getEmail(), edited.getEmail());
    }

    @Test
    public void execute_removeStudentPhoneAndEmail_success() throws Exception {
        Person student = new PersonBuilder().withRole("student").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model studentModel = new ModelManager(addressBook, new UserPrefs());

        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withoutPhone().withoutEmail().build())
                .execute(studentModel);

        assertFalse(studentModel.getFilteredPersonList().get(0).hasContactMethod());
    }

    /**
     * Returns a model holding {@code existing} and {@code secondContact}, where {@code secondContact} is listed second.
     */
    private Model createModelWithSecondContact(Person existing, Person secondContact) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(existing);
        addressBook.addPerson(secondContact);
        return new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_renameToSameNameWithEquivalentPhone_warnsAboutPhone() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("+65 9123 4567").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withPhone("+65-9123-4567")
                .withEmail("zoe@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("alex tan").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
        assertEquals(2, warningModel.getFilteredPersonList().size());
    }

    @Test
    public void execute_renameToSameNameWithSameEmail_warnsAboutEmail() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withPhone("87654321")
                .withEmail("ALEX@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
    }

    @Test
    public void execute_renameToSameNameBothWithoutEmail_warns() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567").withoutEmail().build();
        Person other = new PersonBuilder().withName("Zoe Ng").withPhone("87654321").withoutEmail().build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE));
    }

    @Test
    public void execute_renameToSameNameBothWithoutPhone_warns() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withoutPhone().withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withoutPhone().withEmail("zoe@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE));
    }

    @Test
    public void execute_editPhoneToMatchSameNameContact_warnsAboutPhone() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan").withPhone("87654321")
                .withEmail("other@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);
        Index otherIndex = Index.fromZeroBased(warningModel.getFilteredPersonList().indexOf(other));

        CommandResult result = new EditCommand(otherIndex,
                new EditPersonDescriptorBuilder().withPhone("9123-4567").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
    }

    @Test
    public void execute_editEmailToMatchSameNameContact_warnsAboutEmail() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Alex Tan").withPhone("87654321")
                .withEmail("other@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);
        Index otherIndex = Index.fromZeroBased(warningModel.getFilteredPersonList().indexOf(other));

        CommandResult result = new EditCommand(otherIndex,
                new EditPersonDescriptorBuilder().withEmail("ALEX@example.com").build()).execute(warningModel);

        assertTrue(result.getFeedbackToUser().endsWith("\n" + EditCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
    }

    @Test
    public void execute_renameContactlessStudentToNameOfStudentWithContactDetails_noWarning() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withoutPhone().withoutEmail().build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertFalse(result.getFeedbackToUser().contains("Warning"));
    }

    @Test
    public void execute_renameToSameNameWithDifferentContactDetails_noWarning() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                .withEmail("alex@example.com").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withPhone("87654321")
                .withEmail("zoe@example.com").build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, "Alex Tan", other.getRole()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_sameNameDifferentRole_noWarning() throws Exception {
        Person existing = new PersonBuilder().withName("Alex Tan").withRole("guardian").build();
        Person other = new PersonBuilder().withName("Zoe Ng").withRole("student").build();
        Model warningModel = createModelWithSecondContact(existing, other);

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Alex Tan").build()).execute(warningModel);

        assertFalse(result.getFeedbackToUser().contains("Warning"));
    }

    @Test
    public void execute_editOnlyContactWithoutEmail_noWarning() throws Exception {
        Person student = new PersonBuilder().withName("Alex Tan").withoutEmail().build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model studentModel = new ModelManager(addressBook, new UserPrefs());

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("87654321").build()).execute(studentModel);

        assertFalse(result.getFeedbackToUser().contains("Warning"));
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, indexLastPerson), expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_failure() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_NO_CHANGE);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model,
                String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, outOfBoundIndex.getOneBased()));
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, 2));
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }


    @Test
    public void execute_contactWithoutEmail_preservesAbsenceThenAddsEmail() throws Exception {
        Person student = new PersonBuilder().withoutEmail().build();
        Model localModel = new ModelManager();
        localModel.addPerson(student);
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withName("Alex Tan").build())
                .execute(localModel);
        Person renamed = localModel.getFilteredPersonList().get(0);
        assertTrue(renamed.getEmail().isEmpty());
        assertEquals(student.getId(), renamed.getId());

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("Alex+Tuition@Example.COM").build()).execute(localModel);
        Person updated = localModel.getFilteredPersonList().get(0);
        assertEquals("Alex+Tuition@Example.COM", updated.getEmail().orElseThrow().value);
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("alex+tuition@example.com").build()).execute(localModel);
        assertEquals("alex+tuition@example.com",
                localModel.getFilteredPersonList().get(0).getEmail().orElseThrow().value);
        assertEquals(student.getId(), updated.getId());
    }

    @Test
    public void execute_setLevelAndSubject_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withLevel("Sec 4").withSubject("Physics")
                .build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withLevel("Sec 4")
                .withSubject("Physics").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, createExpectedResult(expectedMessage, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_removeLevelKeepingSubject_success() throws Exception {
        Person student = new PersonBuilder().withRole("student").withLevel("Sec 3").withSubject("Math")
                .build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model studentModel = new ModelManager(addressBook, new UserPrefs());

        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withoutLevel().build())
                .execute(studentModel);

        Person edited = studentModel.getFilteredPersonList().get(0);
        assertTrue(edited.getLevel().isEmpty());
        assertEquals(student.getSubject(), edited.getSubject());
    }

    @Test
    public void execute_editName_preservesLevelAndSubject() throws Exception {
        Person student = new PersonBuilder().withRole("student").withLevel("JC1").withSubject("H2 Math")
                .build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model studentModel = new ModelManager(addressBook, new UserPrefs());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(studentModel);

        Person edited = studentModel.getFilteredPersonList().get(0);
        assertEquals(VALID_NAME_BOB, edited.getName().fullName);
        assertEquals(student.getId(), edited.getId());
        assertEquals(student.getLevel(), edited.getLevel());
        assertEquals(student.getSubject(), edited.getSubject());
    }

    @Test
    public void execute_removeSubjectKeepingLevel_success() throws Exception {
        Person student = new PersonBuilder().withLevel("Sec 3").withSubject("Math").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model studentModel = new ModelManager(addressBook, new UserPrefs());

        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withoutSubject().build())
                .execute(studentModel);

        Person expected = new PersonBuilder(student).withoutSubject().build();
        assertEquals(expected, studentModel.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_guardianLevelOrSubject_failure() {
        Person guardian = new PersonBuilder().withRole("guardian").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(guardian);
        Model guardianModel = new ModelManager(addressBook, new UserPrefs());

        EditCommand setLevel = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withLevel("Sec 3").build());
        assertCommandFailure(setLevel, guardianModel, EditCommand.MESSAGE_GUARDIAN_STUDENT_DETAILS);

        EditCommand removeSubject = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withoutSubject().build());
        assertCommandFailure(removeSubject, guardianModel, EditCommand.MESSAGE_GUARDIAN_STUDENT_DETAILS);
    }
}
