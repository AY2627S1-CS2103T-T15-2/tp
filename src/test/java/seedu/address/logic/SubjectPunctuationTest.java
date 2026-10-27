package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Subject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class SubjectPunctuationTest {

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();

    private Logic createLogic(JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_addEditFilterAndReload_preservesSubjectPunctuation() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = createLogic(storage);
        logic.execute("add r/student n/\"Ravi s/o Kumar\" sub/\"Combined Science (Chem/Bio)\"");
        var studentId = model.getFilteredPersonList().getFirst().getId();
        assertEquals(new Subject("Combined Science (Chem/Bio)"),
                model.getFilteredPersonList().getFirst().getSubject().orElseThrow());
        logic.execute("list sub/\"Combined Science (Chem/Bio)\"");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("edit 1 sub/English Language & Literature");
        logic.execute("list sub/English Language & Literature");
        assertEquals(1, model.getFilteredPersonList().size());

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(studentId, reloaded.getFilteredPersonList().getFirst().getId());
        assertEquals(new Subject("English Language & Literature"),
                reloaded.getFilteredPersonList().getFirst().getSubject().orElseThrow());
    }

    @Test
    public void execute_prefixTextInsideQuotedSubject_isLiteral() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Logic logic = createLogic(storage);
        logic.execute("add r/student n/Alex sub/\"Math p/Science\"");
        logic.execute("edit 1 sub/\"Math r/student\"");
        logic.execute("list sub/\"Math r/student\"");

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new Subject("Math r/student"),
                model.getFilteredPersonList().getFirst().getSubject().orElseThrow());
    }

    @Test
    public void execute_invalidSubjectsAndParameters_preserveSavedData() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Logic logic = createLogic(new JsonAddressBookStorage(file));
        logic.execute("add r/student n/Alex sub/Math");
        String saved = Files.readString(file);
        var originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        for (String command : List.of(
                "add r/student n/Bea sub/\"Chem/Bio\" x/value",
                "edit 1 sub/\"Chem/Bio\" x/value",
                "list sub/\"Chem/Bio\" x/value",
                "edit 1 sub/\"Chem/Bio\" sub/Math",
                "edit 1 sub/\"Chem/Bio",
                "edit 1 sub/\"\"",
                "edit 1 sub/\"NONE\"",
                "edit 1 sub/\"/&\"",
                "edit 1 sub/\"Math#Science\"",
                "edit 1 sub/\"" + "a".repeat(31) + "\"")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(saved, Files.readString(file));
            assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        }
    }
}
