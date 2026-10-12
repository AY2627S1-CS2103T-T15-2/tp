package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
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

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
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
}
