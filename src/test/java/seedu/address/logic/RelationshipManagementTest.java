package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ChildrenCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GuardianChildrenPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.model.person.StudentStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests relationship lookup and removal through filtering, persistence and deletion history.
 */
public class RelationshipManagementTest {
    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail()
            .withGuardianId(guardian.getId()).withLevel("Sec 3").withSubject("Math").build();
    private final Person former = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail()
            .withGuardianId(guardian.getId()).withStatus(StudentStatus.FORMER)
            .withLevel("Sec 4").withSubject("Science").build();
    private final Person unrelated = new PersonBuilder().withName("Zoe Lim").build();

    private Model createModel() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(guardian, student, former, unrelated));
        return new ModelManager(book, new UserPrefs());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private int indexOf(Model model, Person person) {
        return model.getFilteredPersonList().indexOf(person) + 1;
    }

    private Person current(Model model, Person person) {
        return PersonLookup.findById(model.getAddressBook().getPersonList(), person.getId()).orElseThrow();
    }

    @Test
    public void execute_unlink_retainsSharedGuardianAndOtherStudent() throws Exception {
        Model model = createModel();
        var filter = model.getPersonFilter();
        CommandResult result = new UnlinkCommand(Index.fromOneBased(indexOf(model, student))).execute(model);

        assertEquals(student.withGuardianId(Optional.empty()), current(model, student));
        assertEquals(student.getId(), current(model, student).getId());
        assertEquals(former, current(model, former));
        assertEquals(guardian, current(model, guardian));
        assertEquals(4, model.getAddressBook().getPersonList().size());
        assertSame(filter, model.getPersonFilter());
        assertEquals(Optional.of(student.getId()), result.getSelectedContactId());
        assertEquals(String.format(UnlinkCommand.MESSAGE_SUCCESS, student.getName()), result.getFeedbackToUser());
        assertTrue(new UnlinkCommand(Index.fromOneBased(1)).isDataChanging());
    }

    @Test
    public void execute_unlinkFormer_preservesDetailsAndPersists() throws Exception {
        Model model = createModel();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = createLogic(model, storage);
        logic.execute("list st/former");
        logic.execute("unlink s/1");

        assertEquals(former.withGuardianId(Optional.empty()), current(model, former));
        assertEquals(student, current(model, student));
        assertEquals(List.of(current(model, former)), List.copyOf(model.getFilteredPersonList()));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(former.withGuardianId(Optional.empty()), current(reloaded, former));
        assertEquals(student, current(reloaded, student));
    }

    @Test
    public void execute_children_matchesIdentityAcrossAllStatuses() throws Exception {
        Model model = createModel();
        Person distinctGuardian = new PersonBuilder().withName("Mei Tan").withRole("guardian")
                .withPhone("87654321").withEmail("other@example.com").build();
        model.addPerson(distinctGuardian);
        model.setPerson(unrelated, unrelated.withGuardianId(Optional.of(distinctGuardian.getId())));
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        model.updateFilteredPersonList(person -> person.getId().equals(guardian.getId()));

        CommandResult result = new ChildrenCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(student, former), List.copyOf(model.getFilteredPersonList()));
        assertEquals(original, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals("Listed 2 students linked to Mei Tan, including former students.", result.getFeedbackToUser());
        assertFalse(new ChildrenCommand(Index.fromOneBased(1)).isDataChanging());
    }

    @Test
    public void execute_childrenNoMatches_showsRecoveryInstruction() throws Exception {
        Model model = createModel();
        model.setPerson(student, student.withGuardianId(Optional.empty()));
        model.setPerson(former, former.withGuardianId(Optional.empty()));

        CommandResult result = new ChildrenCommand(Index.fromOneBased(indexOf(model, guardian))).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(String.format(ChildrenCommand.MESSAGE_NO_CHILDREN, guardian.getName()),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_childrenSingleMatch_usesSingular() throws Exception {
        Model model = createModel();
        model.setPerson(former, former.withGuardianId(Optional.empty()));
        CommandResult result = new ChildrenCommand(Index.fromOneBased(indexOf(model, guardian))).execute(model);
        assertEquals("Listed 1 student linked to Mei Tan, including former students.", result.getFeedbackToUser());
    }

    @Test
    public void execute_unlinkFromChildrenView_updatesResults() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("children g/" + indexOf(model, guardian));
        logic.execute("unlink s/" + indexOf(model, former));

        assertEquals(List.of(student), List.copyOf(model.getFilteredPersonList()));
        assertEquals(former.withGuardianId(Optional.empty()), current(model, former));
        assertEquals(student, current(model, student));
    }

    @Test
    public void execute_childrenAfterDeletion_preservesUndoAndSavedFile() throws Exception {
        Model model = createModel();
        Path file = temporaryFolder.resolve("contacts.json");
        Logic logic = createLogic(model, new JsonAddressBookStorage(file));
        logic.execute("delete " + indexOf(model, unrelated));
        String saved = Files.readString(file);
        logic.execute("children g/" + indexOf(model, guardian));
        assertEquals(saved, Files.readString(file));
        logic.execute("undo");
        assertEquals(unrelated, current(model, unrelated));
    }

    @Test
    public void execute_successfulUnlink_invalidatesDeletionUndo() throws Exception {
        Model model = createModel();
        Logic logic = createLogic(model, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")));
        logic.execute("delete " + indexOf(model, unrelated));
        logic.execute("unlink s/" + indexOf(model, student));
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
    }

    @Test
    public void execute_failedUnlinkSave_preservesDataFilterFileAndUndo() throws Exception {
        Model model = createModel();
        Path file = temporaryFolder.resolve("contacts.json");
        boolean[] shouldFail = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (shouldFail[0]) {
                    throw new IOException("Simulated save failure");
                }
                super.saveAddressBook(book);
            }
        };
        Logic logic = createLogic(model, storage);
        logic.execute("delete " + indexOf(model, unrelated));
        String saved = Files.readString(file);
        var filter = model.getPersonFilter();
        shouldFail[0] = true;

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE,
                () -> logic.execute("unlink s/" + indexOf(model, student)));

        assertEquals(saved, Files.readString(file));
        assertEquals(student, current(model, student));
        assertEquals(former, current(model, former));
        assertSame(filter, model.getPersonFilter());
        shouldFail[0] = false;
        logic.execute("undo");
        assertEquals(unrelated, current(model, unrelated));
    }

    @Test
    public void execute_invalidTargets_preserveDataAndFilter() {
        Model model = createModel();
        var filter = model.getPersonFilter();
        List<Person> contacts = List.copyOf(model.getAddressBook().getPersonList());
        assertThrows(CommandException.class, ChildrenCommand.MESSAGE_INVALID_ROLE,
                () -> new ChildrenCommand(Index.fromOneBased(indexOf(model, student))).execute(model));
        assertThrows(CommandException.class, UnlinkCommand.MESSAGE_INVALID_ROLE,
                () -> new UnlinkCommand(Index.fromOneBased(indexOf(model, guardian))).execute(model));
        assertThrows(CommandException.class, String.format(ChildrenCommand.MESSAGE_NOT_FOUND, 99),
                () -> new ChildrenCommand(Index.fromOneBased(99)).execute(model));
        assertThrows(CommandException.class, String.format(UnlinkCommand.MESSAGE_NOT_FOUND, 99),
                () -> new UnlinkCommand(Index.fromOneBased(99)).execute(model));
        assertThrows(CommandException.class, String.format(UnlinkCommand.MESSAGE_NO_LINK, unrelated.getName()),
                () -> new UnlinkCommand(Index.fromOneBased(indexOf(model, unrelated))).execute(model));
        assertEquals(contacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertSame(filter, model.getPersonFilter());
    }

    @Test
    public void parse_validCommands_acceptWhitespaceAndLeadingZeros() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertEquals(new UnlinkCommand(Index.fromOneBased(2)), parser.parseCommand("unlink s/02"));
        assertEquals(new ChildrenCommand(Index.fromOneBased(2)), parser.parseCommand("children\t g/2"));
        assertEquals(new UnlinkCommand(Index.fromOneBased(1)), parser.parseCommand("unlink \n s/ 1 "));
    }

    @Test
    public void parse_malformedCommands_rejectMissingRepeatedAndExtraArguments() {
        AddressBookParser parser = new AddressBookParser();
        for (String command : List.of("unlink", "unlink 1", "unlink s/", "unlink s/0", "unlink s/-1",
                "unlink s/1.5", "unlink s/2147483648", "unlink s/1 s/2", "unlink s/1 g/2",
                "unlink s/1 extra", "unlink x/1", "unlink extra s/1", "children", "children g/0",
                "children g/1 g/1", "children g/1 s/2", "children g/1 extra", "children s/1",
                "children g/99999999999999999999", "children g/1.5", "children g/-1")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void constructorsAndExecution_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new UnlinkCommand(null));
        assertThrows(NullPointerException.class, () -> new ChildrenCommand(null));
        assertThrows(NullPointerException.class, () -> new GuardianChildrenPredicate(null));
        assertThrows(NullPointerException.class, () -> new UnlinkCommand(Index.fromOneBased(1)).execute(null));
        assertThrows(NullPointerException.class, () -> new ChildrenCommand(Index.fromOneBased(1)).execute(null));
    }

    @Test
    public void equalityAndDescriptions_dependOnTarget() {
        UnlinkCommand unlink = new UnlinkCommand(Index.fromOneBased(1));
        ChildrenCommand children = new ChildrenCommand(Index.fromOneBased(1));
        assertEquals(unlink, unlink);
        assertEquals(unlink, new UnlinkCommand(Index.fromOneBased(1)));
        assertNotEquals(unlink, new UnlinkCommand(Index.fromOneBased(2)));
        assertNotEquals(unlink, children);
        assertNotEquals(unlink, null);
        assertEquals(children, children);
        assertEquals(children, new ChildrenCommand(Index.fromOneBased(1)));
        assertNotEquals(children, new ChildrenCommand(Index.fromOneBased(2)));
        assertNotEquals(children, unlink);
        assertNotEquals(children, null);
        GuardianChildrenPredicate predicate = new GuardianChildrenPredicate(guardian.getId());
        assertEquals(predicate, predicate);
        assertEquals(predicate, new GuardianChildrenPredicate(guardian.getId()));
        assertNotEquals(predicate, new GuardianChildrenPredicate(unrelated.getId()));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, children);
        assertFalse(predicate.test(guardian));
        assertFalse(predicate.test(unrelated));
        assertTrue(predicate.test(former));
        assertTrue(unlink.toString().contains("studentIndex"));
        assertTrue(children.toString().contains("guardianIndex"));
        assertTrue(predicate.toString().contains(guardian.getId().toString()));
    }
}
