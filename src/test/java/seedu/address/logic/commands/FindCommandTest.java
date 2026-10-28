package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsSubstringPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void equals() {
        NameContainsSubstringPredicate firstPredicate = new NameContainsSubstringPredicate("first");
        NameContainsSubstringPredicate secondPredicate = new NameContainsSubstringPredicate("second");

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different search term -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_noMatchingName_noContactFound() {
        String expectedMessage = String.format(FindCommand.MESSAGE_NO_MATCHES, "zzz");
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("zzz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_oneMatchingName_singularMessage() {
        String expectedMessage = "Found 1 contact matching: alice.";
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("alice");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_formerStudent_findsStudentHiddenFromDefaultList() {
        Person former = BENSON.withStatus(StudentStatus.FORMER);
        model.setPerson(BENSON, former);
        expectedModel.setPerson(BENSON, former);
        assertFalse(model.getFilteredPersonList().contains(former));
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("benson");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model, "Found 1 contact matching: benson.", expectedModel);

        assertEquals(List.of(former), model.getFilteredPersonList());
    }

    @Test
    public void execute_formerView_findsMatchingActiveStudent() {
        Person former = BENSON.withStatus(StudentStatus.FORMER);
        model.setPerson(BENSON, former);
        expectedModel.setPerson(BENSON, former);
        model.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertEquals(List.of(former), model.getFilteredPersonList());
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("alice");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model, "Found 1 contact matching: alice.", expectedModel);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_partialNameMatchingMultipleContacts_pluralMessage() {
        String expectedMessage = "Found 2 contacts matching: meier.";
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("meier");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleMatches_resultsInDisplayOrder() {
        Person zoe = new PersonBuilder().withName("Zoe Tan").withRole("student").build();
        Person accentedElise = new PersonBuilder().withName("Élise Tan").withRole("student").build();
        Person alexStudent = new PersonBuilder().withName("Alex Tan").withRole("student").build();
        Person alexGuardian = new PersonBuilder().withName("alex tan").withRole("guardian").build();
        Person bernice = new PersonBuilder().withName("Bernice Yu").withRole("student").build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(zoe, accentedElise, alexStudent, bernice, alexGuardian));
        Model unsortedModel = new ModelManager(addressBook, new UserPrefs());

        new FindCommand(new NameContainsSubstringPredicate("tan")).execute(unsortedModel);

        assertEquals(List.of(alexGuardian, alexStudent, accentedElise, zoe), unsortedModel.getFilteredPersonList());
    }

    @Test
    public void execute_straightApostropheSearchTerm_findsNameWithCurlyApostrophe() {
        Person obrien = new PersonBuilder().withName("Liam O’Brien").withRole("student").build();
        model.addPerson(obrien);
        expectedModel.addPerson(obrien);
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("o'brien");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model, "Found 1 contact matching: o'brien.", expectedModel);
        assertEquals(List.of(obrien), model.getFilteredPersonList());
    }

    @Test
    public void execute_sigmaSearchTerms_findGreekContactAndPreserveStoredName() {
        Person greekContact = new PersonBuilder().withName("\u039f\u03a3").build();
        model.addPerson(greekContact);

        for (String searchTerm : new String[] {"\u03a3", "\u03c3", "\u03c2", "\u03bf\u03c3"}) {
            CommandResult result = new FindCommand(new NameContainsSubstringPredicate(searchTerm)).execute(model);

            assertEquals("Found 1 contact matching: " + searchTerm + ".", result.getFeedbackToUser());
            assertEquals(List.of(greekContact), model.getFilteredPersonList());
            assertEquals("\u039f\u03a3", model.getFilteredPersonList().getFirst().getName().fullName);
        }
    }

    @Test
    public void execute_unaccentedSearchTerm_findsAccentedName() {
        Person elise = new PersonBuilder().withName("Élise Tan").withRole("student").build();
        model.addPerson(elise);
        expectedModel.addPerson(elise);
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("elise");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), model, "Found 1 contact matching: elise.", expectedModel);
        assertEquals(List.of(elise), model.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        NameContainsSubstringPredicate predicate = new NameContainsSubstringPredicate("keyword");
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }
}
