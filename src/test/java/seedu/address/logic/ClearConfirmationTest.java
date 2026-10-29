package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Verifies confirmation at the command boundary, including saving and deletion-history behaviour. */
public class ClearConfirmationTest {
    @TempDir
    public Path temporaryFolder;

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_unconfirmedClear_preservesContactsFileFilterAndUndo() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        Model model = new ModelManager();
        Logic logic = createLogic(model, new JsonAddressBookStorage(file));
        logic.execute("add r/student n/Alice");
        logic.execute("add r/student n/Bob");
        logic.execute("delete n/Bob");
        logic.execute("find Alice");
        List<?> originalContacts = List.copyOf(model.getAddressBook().getPersonList());
        String originalFile = Files.readString(file);
        var originalFilter = model.getPersonFilter();

        for (String command : List.of("clear", "clear 1", "clear confirm extra")) {
            assertThrows(ParseException.class, ClearCommand.MESSAGE_USAGE, () -> logic.execute(command));
            assertEquals(originalContacts, List.copyOf(model.getAddressBook().getPersonList()));
            assertEquals(originalFile, Files.readString(file));
            assertEquals(originalFilter, model.getPersonFilter());
        }
        logic.execute("undo");
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_confirmedClear_removesHiddenContactsAndLinksAndPersists() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/guardian n/Mei p/91234567");
        logic.execute("add r/student n/Alex");
        logic.execute("link s/1 g/2");
        logic.execute("archive 1");
        logic.execute("find Mei");

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear confirm").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
    }

    @Test
    public void execute_confirmedClearSaveFailure_preservesFileAndUndoAndAllowsRetry() throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        boolean[] failSave = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave[0]) {
                    throw new IOException("Simulated disk failure");
                }
                super.saveAddressBook(book);
            }
        };
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alice");
        logic.execute("add r/student n/Bob");
        logic.execute("delete n/Bob");
        String originalFile = Files.readString(file);
        failSave[0] = true;

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute("clear confirm"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(originalFile, Files.readString(file));
        failSave[0] = false;
        logic.execute("undo");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        logic.execute("clear confirm");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }
}
