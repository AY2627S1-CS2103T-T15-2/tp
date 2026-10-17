package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.NameContainsSubstringPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model, getExpectedResult(validPerson), expectedModel);
    }

    @Test
    public void execute_studentWithoutContactMethods_success() {
        Person student = new PersonBuilder().withRole("student").withoutPhone().withoutEmail().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(student);

        assertCommandSuccess(new AddCommand(student), model, getExpectedResult(student), expectedModel);
    }

    @Test
    public void execute_guardianWithPhoneOnly_success() {
        Person guardian = new PersonBuilder().withName("Phone Only Guardian").withRole("guardian")
                .withoutEmail().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(guardian);

        assertCommandSuccess(new AddCommand(guardian), model, getExpectedResult(guardian), expectedModel);
    }

    @Test
    public void execute_guardianWithEmailOnly_success() {
        Person guardian = new PersonBuilder().withName("Email Only Guardian").withRole("guardian")
                .withoutPhone().withEmail("guardian@example.com").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(guardian);

        assertCommandSuccess(new AddCommand(guardian), model, getExpectedResult(guardian), expectedModel);
    }

    @Test
    public void execute_findFilterActive_newContactShownAndSelected() throws Exception {
        model.updateFilteredPersonList(new NameContainsSubstringPredicate("alice"));
        Person student = new PersonBuilder().withName("Ben Lim").withRole("student")
                .withoutPhone().withoutEmail().build();

        CommandResult result = new AddCommand(student).execute(model);

        assertTrue(model.getFilteredPersonList().contains(student));
        assertEquals(model.getAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertEquals(Optional.of(student.getId()), result.getSelectedContactId());
    }

    @Test
    public void execute_formerStudentsShown_showsActiveContacts() throws Exception {
        Person former = model.getFilteredPersonList().get(0);
        model.setPerson(former, former.withStatus(StudentStatus.FORMER));
        model.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        Person student = new PersonBuilder().withName("Ben Lim").withRole("student")
                .withoutPhone().withoutEmail().build();

        new AddCommand(student).execute(model);

        assertTrue(model.getFilteredPersonList().contains(student));
        assertFalse(model.getFilteredPersonList().stream().anyMatch(Person::isFormerStudent));
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }


    @Test
    public void execute_optionalEmailAndRole_enforcesDuplicateIdentity() throws Exception {
        Person student = new PersonBuilder().withoutEmail().build();
        new AddCommand(student).execute(model);
        Person duplicate = new PersonBuilder(student).withName("AMY BEE").build();
        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        Person guardian = new PersonBuilder(student).withRole("guardian")
                .withId(ContactId.generate()).build();
        new AddCommand(guardian).execute(model);
        Person withEmail = new PersonBuilder(student).withEmail("Alex+Tuition@Example.COM")
                .withId(ContactId.generate()).build();
        new AddCommand(withEmail).execute(model);
        assertCommandFailure(new AddCommand(new PersonBuilder(withEmail)
                .withEmail("alex+tuition@example.com").build()), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    /**
     * Returns the result of successfully adding {@code person}, which selects the new contact.
     */
    private static CommandResult getExpectedResult(Person person) {
        return new CommandResult(String.format(AddCommand.MESSAGE_SUCCESS, person.getName(), person.getRole()),
                person.getId());
    }
}
