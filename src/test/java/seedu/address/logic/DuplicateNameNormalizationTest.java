package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises equivalent names through commands, duplicate reports and JSON reloads. */
public class DuplicateNameNormalizationTest {
    private static final List<List<String>> NAME_PAIRS = List.of(
            List.of("\u00c9lise", "E\u0301lise"),
            List.of("D'Souza", "D\u2019Souza"),
            List.of("Alex  Tan", "Alex Tan"),
            List.of("J\u030can", "\u01f0an"));

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addEquivalentName_warnsAndPreservesBothSpellingsOnReload() throws Exception {
        for (List<String> pair : NAME_PAIRS) {
            // Seed saved text directly: command parsing already collapses legacy repeated spaces.
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName(pair.get(0)).withPhone("91234567")
                    .withEmail("alex@example.com").build());
            storage().saveAddressBook(model.getAddressBook());
            Logic logic = createLogic(new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs()));
            String result = logic.execute("add r/student n/" + pair.get(1)
                    + " p/91234567 e/alex@example.com").getFeedbackToUser();

            assertTrue(result.contains(AddCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE), pair.toString());
            // Exact identity remains narrower, so existing files containing both variants stay loadable.
            List<Person> saved = storage().readAddressBook().orElseThrow().getPersonList();
            assertEquals(List.of(pair.get(0), pair.get(1)), saved.stream()
                    .map(person -> person.getName().fullName).toList());
        }
    }

    @Test
    public void execute_editEquivalentName_warnsAndPreservesContactId() throws Exception {
        for (List<String> pair : NAME_PAIRS) {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName(pair.get(0)).withPhone("91234567")
                    .withEmail("alex@example.com").build());
            Logic logic = createLogic(model);
            logic.execute("add r/student n/Zoe p/91234567 e/alex@example.com");
            Person target = logic.getPersonList().getLast();
            int displayedIndex = logic.getFilteredPersonList().indexOf(target) + 1;

            String result = logic.execute("edit " + displayedIndex + " n/" + pair.get(1)).getFeedbackToUser();

            assertTrue(result.contains(EditCommand.MESSAGE_POSSIBLE_DUPLICATE_PHONE), pair.toString());
            Person edited = storage().readAddressBook().orElseThrow().getPersonList().stream()
                    .filter(person -> person.getId().equals(target.getId())).findFirst().orElseThrow();
            assertEquals(pair.get(1), edited.getName().fullName);
        }
    }

    @Test
    public void execute_checkReloadedVariants_reportsPairWithoutChangingSavedData() throws Exception {
        for (List<String> pair : NAME_PAIRS) {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName(pair.get(0)).withPhone("91234567").build());
            model.addPerson(new PersonBuilder().withName(pair.get(1)).withPhone("9123 4567").build());
            storage().saveAddressBook(model.getAddressBook());
            Logic logic = createLogic(new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs()));
            byte[] saved = Files.readAllBytes(dataFile());

            String report = logic.execute("check duplicates").getFeedbackToUser();

            assertTrue(report.contains("Possible duplicates (1)"), pair.toString());
            assertTrue(report.contains(pair.get(0)));
            assertTrue(report.contains(pair.get(1)));
            assertArrayEquals(saved, Files.readAllBytes(dataFile()));
        }
    }

    @Test
    public void execute_exactDuplicate_stillRejectedWithoutChangingSavedData() throws Exception {
        Logic logic = createLogic(new ModelManager());
        logic.execute("add r/student n/Alex p/91234567 e/alex@example.com");
        byte[] saved = Files.readAllBytes(dataFile());

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                logic.execute("add r/student n/ALEX p/91234567 e/ALEX@example.com"));

        assertEquals(1, logic.getPersonList().size());
        assertArrayEquals(saved, Files.readAllBytes(dataFile()));
    }

    private Path dataFile() {
        return temporaryFolder.resolve("contacts.json");
    }

    private JsonAddressBookStorage storage() {
        return new JsonAddressBookStorage(dataFile());
    }

    private Logic createLogic(Model model) {
        return new LogicManager(model, new StorageManager(storage(),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
