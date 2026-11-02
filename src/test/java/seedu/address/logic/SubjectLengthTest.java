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

public class SubjectLengthTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_fullSubjectName_canBeAddedEditedFilteredAndReloaded() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan sub/English Language and Linguistics");
        var studentId = model.getFilteredPersonList().getFirst().getId();
        assertEquals("English Language and Linguistics",
                model.getFilteredPersonList().getFirst().getSubject().orElseThrow().value);

        logic.execute("edit 1 sub/H2 English Language and Linguistics");
        logic.execute("list sub/H2 English Language and Linguistics");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("H2 English Language and Linguistics",
                model.getFilteredPersonList().getFirst().getSubject().orElseThrow().value);

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(studentId, reloaded.getFilteredPersonList().getFirst().getId());
        assertEquals("H2 English Language and Linguistics",
                reloaded.getFilteredPersonList().getFirst().getSubject().orElseThrow().value);
    }

    @Test
    public void execute_subjectLengthBoundary_normalizesSpacesAndRejectsLongerValues() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Model model = new ModelManager();
        Logic logic = createLogic(model, new JsonAddressBookStorage(file));
        String subject = "a".repeat(98) + " b";
        logic.execute("add r/student n/Alex Tan sub/" + "a".repeat(98) + "    b");
        assertEquals(subject, model.getFilteredPersonList().getFirst().getSubject().orElseThrow().value);
        logic.execute("list sub/" + subject);
        assertEquals(1, model.getFilteredPersonList().size());

        String saved = Files.readString(file);
        var originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        for (String command : List.of("add r/student n/Bea sub/", "edit 1 sub/", "list sub/")) {
            assertThrows(ParseException.class, Subject.MESSAGE_TOO_LONG, ()
                    -> logic.execute(command + "a".repeat(101)));
            assertEquals(saved, Files.readString(file));
            assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
        }
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
