package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class CheckReportIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Logic createLogic(Model model, Path file) {
        return new LogicManager(model, new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_checkReadOnly_preservesFileFilterAndUndo() throws Exception {
        Model model = new ModelManager();
        Person first = new PersonBuilder().withName("Alex Tan").build();
        Person second = new PersonBuilder().withName("Bea Lim").build();
        model.addPerson(first);
        model.addPerson(second);
        Path file = temporaryFolder.resolve("contacts.json");
        Logic logic = createLogic(model, file);
        logic.execute("delete 1");
        logic.execute("find Bea");
        var filter = model.getPersonFilter();
        String originalFile = Files.readString(file);

        assertTrue(logic.execute("check").getFeedbackToUser().contains("1: Bea Lim"));

        assertSame(filter, model.getPersonFilter());
        assertEquals(originalFile, Files.readString(file));
        logic.execute("undo");
        assertEquals(List.of(first.getId(), second.getId()), model.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void execute_emptyReport_doesNotCreateFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        assertTrue(createLogic(new ModelManager(), file).execute("check details").getFeedbackToUser()
                .startsWith("No displayed contacts to check."));
        assertFalse(Files.exists(file));
    }

    @Test
    public void execute_invalidDataFile_allowsReadOnlyCheckWithoutOverwritingFile() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Files.writeString(file, "{invalid json}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertTrue(logic.execute("check").getFeedbackToUser().startsWith("No displayed contacts to check."));
        assertEquals("{invalid json}", Files.readString(file));
    }
}
