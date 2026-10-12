package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_studentWithoutContactMethod_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person student = new PersonBuilder().withRole("student").withoutPhone().withoutEmail().build();
        AddCommand addCommand = new AddCommand(student);

        CommandResult result = addCommand.execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(student)),
                result.getFeedbackToUser());
        assertEquals(List.of(student), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicateWithSameRoleNamePhoneAndEmail_rejectedForBothRoles() {
        for (String role : List.of("student", "guardian")) {
            Person contact = new PersonBuilder().withRole(role).build();
            AddCommand addCommand = new AddCommand(contact);
            ModelStub modelStub = new ModelStubWithPerson(contact);

            assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                    addCommand.execute(modelStub));
        }
    }

    @Test
    public void execute_personWithEmailDifferingOnlyInCase_throwsCommandException() {
        Person existingPerson = new PersonBuilder().build();
        Person duplicatePerson = new PersonBuilder(existingPerson)
                .withEmail(existingPerson.getEmail().orElseThrow().value.toUpperCase(Locale.ROOT))
                .build();
        AddCommand addCommand = new AddCommand(duplicatePerson);
        ModelStub modelStub = new ModelStubWithPerson(existingPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_studentsWithSameNameAndRoleButNoContactMethods_warnsAndAddsBoth() throws Exception {
        Person firstStudent = new PersonBuilder().withRole("student").withoutPhone().withoutEmail().build();
        Person secondStudent = new PersonBuilder().withName(firstStudent.getName().fullName)
                .withRole("student").withoutPhone().withoutEmail().build();
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

        new AddCommand(firstStudent).execute(modelStub);
        CommandResult result = new AddCommand(secondStudent).execute(modelStub);

        assertTrue(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE));
        assertEquals(List.of(firstStudent, secondStudent), modelStub.personsAdded);
    }

    @Test
    public void execute_personWithEquivalentPhoneFormatting_warnsAndAdds() throws Exception {
        Person existingPerson = new PersonBuilder().withPhone("+65 9123 4567").build();
        Person duplicatePerson = new PersonBuilder(existingPerson).withPhone("+65-9123-4567").build();
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

        new AddCommand(existingPerson).execute(modelStub);
        CommandResult result = new AddCommand(duplicatePerson).execute(modelStub);

        assertTrue(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
        assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE));
        assertEquals(List.of(existingPerson, duplicatePerson), modelStub.personsAdded);
    }

    @Test
    public void execute_samePhoneAndDifferentEmail_warnsAboutPhoneForBothRoles() throws Exception {
        for (String role : List.of("student", "guardian")) {
            Person existingContact = new PersonBuilder().withRole(role).build();
            Person newContact = new PersonBuilder().withName(existingContact.getName().fullName).withRole(role)
                    .withPhone(existingContact.getPhone().orElseThrow().value).withEmail("new@example.com").build();
            ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

            new AddCommand(existingContact).execute(modelStub);
            CommandResult result = new AddCommand(newContact).execute(modelStub);

            assertTrue(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
            assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
            assertEquals(List.of(existingContact, newContact), modelStub.personsAdded);
        }
    }

    @Test
    public void execute_personWithPhoneDifferingOnlyInSurroundingWhitespace_throwsCommandException() {
        Person existingPerson = new PersonBuilder().withPhone("+65 9123 4567").build();
        Person duplicatePerson = new PersonBuilder(existingPerson).withPhone(" +65 9123 4567 ").build();
        AddCommand addCommand = new AddCommand(duplicatePerson);
        ModelStub modelStub = new ModelStubWithPerson(existingPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_differentPhoneAndSameEmail_warnsAboutEmailForBothRoles() throws Exception {
        for (String role : List.of("student", "guardian")) {
            Person existingContact = new PersonBuilder().withRole(role).build();
            Person newContact = new PersonBuilder().withName(existingContact.getName().fullName).withRole(role)
                    .withPhone("87654321")
                    .withEmail(existingContact.getEmail().orElseThrow().value.toUpperCase(Locale.ROOT)).build();
            ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

            new AddCommand(existingContact).execute(modelStub);
            CommandResult result = new AddCommand(newContact).execute(modelStub);

            assertTrue(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
            assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
            assertEquals(List.of(existingContact, newContact), modelStub.personsAdded);
        }
    }

    @Test
    public void execute_personWithNameDifferingOnlyInCase_throwsCommandException() {
        Person existingPerson = new PersonBuilder().build();
        Person duplicatePerson = new PersonBuilder(existingPerson)
                .withName(existingPerson.getName().fullName.toUpperCase(Locale.ROOT))
                .build();
        AddCommand addCommand = new AddCommand(duplicatePerson);
        ModelStub modelStub = new ModelStubWithPerson(existingPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_differentPhoneAndEmail_addsWithoutWarningForBothRoles() throws Exception {
        for (String role : List.of("student", "guardian")) {
            ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
            Person firstPerson = new PersonBuilder().withRole(role).build();
            Person secondPerson = new PersonBuilder().withName(firstPerson.getName().fullName).withRole(role)
                    .withPhone("98765432").withEmail("new@example.com").build();

            new AddCommand(firstPerson).execute(modelStub);
            CommandResult result = new AddCommand(secondPerson).execute(modelStub);

            assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE));
            assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
            assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
            assertEquals(List.of(firstPerson, secondPerson), modelStub.personsAdded);
        }
    }

    @Test
    public void execute_personWithDifferentPhonesAndEmailMissingFromBoth_warnsGenerallyAndAdds() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person firstPerson = new PersonBuilder().withPhone("91234567").withoutEmail().build();
        Person secondPerson = new PersonBuilder(firstPerson).withPhone("98765432").build();

        new AddCommand(firstPerson).execute(modelStub);
        CommandResult result = new AddCommand(secondPerson).execute(modelStub);

        assertTrue(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE));
        assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
        assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_EMAIL));
        assertEquals(List.of(firstPerson, secondPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_personWithSameNameAndDifferentRole_addsWithoutWarning() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person firstPerson = new PersonBuilder().build();
        Person secondPerson = new PersonBuilder(firstPerson).withRole("guardian").build();

        new AddCommand(firstPerson).execute(modelStub);
        CommandResult result = new AddCommand(secondPerson).execute(modelStub);

        assertFalse(result.getFeedbackToUser().contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE));
        assertEquals(List.of(firstPerson, secondPerson), modelStub.personsAdded);
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        AddCommand addCommand = new AddCommand(ALICE);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Predicate<Person> getPersonFilter() {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            AddressBook addressBook = new AddressBook();
            addressBook.addPerson(person);
            return addressBook;
        }
    }

    /**
     * A Model stub that always accepts the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::isSamePerson);
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            AddressBook addressBook = new AddressBook();
            addressBook.setPersons(personsAdded);
            return addressBook;
        }
    }

}
