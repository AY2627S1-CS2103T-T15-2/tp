package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests pasted spaces through the application's command, model and storage boundaries. */
public class PastedSpaceTest {
    private static final List<String> PASTED_SPACES = List.of("\u00a0", "\u2007", "\u202f", "\u2003", "\u3000");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addPastedSpaces_normalizesFieldsAndRetainsQuotedText() throws Exception {
        for (String space : PASTED_SPACES) {
            Logic logic = createLogic(new ModelManager());
            logic.execute(space + "add" + space + "r/student" + space + "n/\"Alex" + space + "Tan\""
                    + space + "p/9333" + space + "3333" + space + "e/alex@example.com" + space
                    + "lv/Sec" + space + "3" + space + "sub/\"Art" + space + "n/Design\"" + space);

            Person student = logic.getPersonList().getFirst();
            assertEquals("Alex Tan", student.getName().fullName);
            assertEquals("9333 3333", student.getPhone().orElseThrow().value);
            assertEquals("alex@example.com", student.getEmail().orElseThrow().value);
            assertEquals("Sec 3", student.getLevel().orElseThrow().value);
            assertEquals("Art n/Design", student.getSubject().orElseThrow().value);
            Person saved = new JsonAddressBookStorage(getContactsFile()).readAddressBook().orElseThrow()
                    .getPersonList().getFirst();
            assertEquals(student, saved);
            logic.execute("add r/guardian n/Mei Tan e/mei@example.com" + space);
            assertEquals("mei@example.com", logic.getPersonList().getLast().getEmail().orElseThrow().value);
        }
    }

    @Test
    public void execute_editPastedSpaces_normalizesValuesAndRemovalSentinels() throws Exception {
        for (String space : PASTED_SPACES) {
            Logic logic = createLogic(new ModelManager());
            logic.execute("add r/student n/Alex p/92222222 e/old@example.com lv/Sec 2 sub/Math");
            logic.execute("edit" + space + "1" + space + "n/Bea" + space + "Lim p/9333" + space
                    + "3333 e/new@example.com" + space + "lv/Sec" + space + "3 sub/\"Art" + space + "n/Design\"");
            Person student = logic.getPersonList().getFirst();
            assertEquals("Bea Lim", student.getName().fullName);
            assertEquals("9333 3333", student.getPhone().orElseThrow().value);
            assertEquals("new@example.com", student.getEmail().orElseThrow().value);
            assertEquals("Sec 3", student.getLevel().orElseThrow().value);
            assertEquals("Art n/Design", student.getSubject().orElseThrow().value);

            logic.execute("edit 1 p/" + space + "NONE" + space + "e/" + space + "NONE" + space
                    + "lv/" + space + "NONE" + space + "sub/" + space + "NONE" + space);
            Person cleared = logic.getPersonList().getFirst();
            assertTrue(cleared.getPhone().isEmpty());
            assertTrue(cleared.getEmail().isEmpty());
            assertTrue(cleared.getLevel().isEmpty());
            assertTrue(cleared.getSubject().isEmpty());
        }
    }

    @Test
    public void execute_findAndListPastedSpaces_matchWithoutChangingStoredData() throws Exception {
        Logic logic = createLogic(new ModelManager());
        logic.execute("add r/student n/Bea Lim lv/Sec 3 sub/\"Art n/Design\"");
        Person student = logic.getPersonList().getFirst();
        byte[] saved = Files.readAllBytes(getContactsFile());
        for (String space : PASTED_SPACES) {
            for (String command : List.of("find" + space + "Bea" + space + "Lim",
                    "list lv/Sec" + space + "3 sub/\"Art" + space + "n/Design\"")) {
                logic.execute(command);
                assertEquals(List.of(student), List.copyOf(logic.getFilteredPersonList()), command);
                assertArrayEquals(saved, Files.readAllBytes(getContactsFile()));
            }
        }
    }

    @Test
    public void execute_deleteNameWithPastedSpaces_deletesMatchingContact() throws Exception {
        for (String space : PASTED_SPACES) {
            Logic logic = createLogic(new ModelManager());
            logic.execute("add r/student n/Bea Lim");
            logic.execute("delete" + space + "n/Bea" + space + "Lim");
            assertTrue(logic.getPersonList().isEmpty());
            assertTrue(new JsonAddressBookStorage(getContactsFile()).readAddressBook().orElseThrow()
                    .getPersonList().isEmpty());
        }
    }

    @Test
    public void execute_pastedPhoneSpace_warnsAndReportsDuplicatePairs() throws Exception {
        for (String space : PASTED_SPACES) {
            Logic logic = createLogic(new ModelManager());
            logic.execute("add r/student n/Bea Lim p/93333333");
            String feedback = logic.execute("add r/student n/Bea Lim p/9333" + space
                    + "3333 e/b@x.com").getFeedbackToUser();
            assertTrue(feedback.contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE), feedback);
            assertEquals(2, logic.getPersonList().size());
            assertTrue(logic.execute("check duplicates").getFeedbackToUser().contains("Possible duplicates (1):"));
            logic.execute("add r/student n/Bea Lim p/9333 3333 e/c@x.com");
            assertTrue(logic.execute("check duplicates").getFeedbackToUser().contains("Possible duplicates (3):"));
        }
    }

    @Test
    public void execute_reloadedPhoneSpaces_warnAndReportWithoutRewritingOriginalPhone() throws Exception {
        Person original = new PersonBuilder().withName("Bea Lim").withPhone("9333\u00a03333")
                .withEmail("a@x.com").build();
        Model model = new ModelManager();
        model.addPerson(original);
        model.addPerson(new PersonBuilder(original).withId(ContactId.generate())
                .withPhone("93333333").withEmail("b@x.com").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(getContactsFile());
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs()));
        byte[] saved = Files.readAllBytes(getContactsFile());

        assertTrue(logic.execute("check duplicates").getFeedbackToUser().contains("Possible duplicates (1):"));
        assertArrayEquals(saved, Files.readAllBytes(getContactsFile()));
        assertTrue(logic.execute("add r/student n/Bea Lim p/9333-3333 e/c@x.com").getFeedbackToUser()
                .contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
        Person added = logic.getFilteredPersonList().stream()
                .filter(person -> person.getEmail().orElseThrow().value.equals("c@x.com")).findFirst().orElseThrow();
        int addedIndex = logic.getFilteredPersonList().indexOf(added) + 1;
        assertTrue(logic.execute("edit " + addedIndex + " e/d@x.com").getFeedbackToUser()
                .contains(EditCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE));
        assertEquals("9333\u00a03333", storage.readAddressBook().orElseThrow().getPersonList().stream()
                .filter(person -> person.getId().equals(original.getId())).findFirst().orElseThrow()
                .getPhone().orElseThrow().value);
    }

    @Test
    public void execute_invalidInternalWhitespace_rejectsWithoutChangingData() throws Exception {
        Logic logic = createLogic(new ModelManager());
        logic.execute("add r/student n/Bea Lim");
        List<Person> original = List.copyOf(logic.getPersonList());
        byte[] saved = Files.readAllBytes(getContactsFile());
        for (String command : List.of("add r/student n/Alex\tTan", "add r/student n/Alex\nTan",
                "add r/student n/Alex p/9333\t3333", "add r/student n/Alex lv/Sec\t3",
                "edit 1 sub/Art\nDesign", "edit 1 e/mei\u00a0@example.com")) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertEquals(original, List.copyOf(logic.getPersonList()));
            assertArrayEquals(saved, Files.readAllBytes(getContactsFile()));
        }
        ParseException emailFailure = assertThrows(ParseException.class, () ->
                logic.execute("add r/guardian n/Mei Tan e/mei\u00a0@example.com"));
        assertEquals("Email address cannot contain whitespace.", emailFailure.getMessage());
    }

    private Path getContactsFile() {
        return temporaryFolder.resolve("contacts.json");
    }

    private Logic createLogic(Model model) {
        return new LogicManager(model, new StorageManager(new JsonAddressBookStorage(getContactsFile()),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
