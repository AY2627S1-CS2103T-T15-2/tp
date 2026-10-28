package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UnchangedEditTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_identicalValues_preservesUndoAndCurrentView() throws Exception {
        for (String editArguments : List.of("n/Alex Tan", "p/92222222", "e/alex@example.com", "lv/Sec 3", "sub/Math",
                "n/Alex Tan p/92222222 e/alex@example.com lv/Sec 3 sub/Math")) {
            assertUnchangedEditPreservesUndo("p/92222222 e/alex@example.com lv/Sec 3 sub/Math", editArguments);
        }
    }

    @Test
    public void execute_removeAbsentValues_preservesUndoAndCurrentView() throws Exception {
        for (String editArguments : List.of("p/NONE", "e/NONE", "lv/NONE", "sub/NONE",
                "p/NONE e/NONE lv/NONE sub/NONE")) {
            assertUnchangedEditPreservesUndo("", editArguments);
        }
    }

    private void assertUnchangedEditPreservesUndo(String studentArguments, String editArguments) throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan " + studentArguments);
        logic.execute("add r/student n/Bea Lim");
        AddressBook original = new AddressBook(model.getAddressBook());
        List<ContactId> originalIds = original.getPersonList().stream().map(Person::getId).toList();
        logic.execute("delete 2");
        logic.execute("find Alex");
        Predicate<Person> filter = model.getPersonFilter();
        List<Person> contacts = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        byte[] savedData = Files.readAllBytes(file);

        assertThrows(CommandException.class, EditCommand.MESSAGE_NO_CHANGE, () ->
                logic.execute("edit 1 " + editArguments));

        assertEquals(contacts, List.copyOf(model.getAddressBook().getPersonList()));
        assertEquals(originalIds.getFirst(), model.getAddressBook().getPersonList().getFirst().getId());
        assertSame(filter, model.getPersonFilter());
        assertEquals(displayed, List.copyOf(model.getFilteredPersonList()));
        assertArrayEquals(savedData, Files.readAllBytes(file));
        logic.execute("undo");
        assertEquals(original, model.getAddressBook());
        assertEquals(originalIds, model.getAddressBook().getPersonList().stream().map(Person::getId).toList());
        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_unchangedEditWithUnavailableStorage_doesNotAttemptSave() throws Exception {
        boolean[] isSaveFailing = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (isSaveFailing[0]) {
                    throw new IOException("Storage unavailable after deletion");
                }
                super.saveAddressBook(book);
            }
        };
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan");
        logic.execute("add r/student n/Bea Lim");
        logic.execute("delete 2");
        isSaveFailing[0] = true;

        assertThrows(CommandException.class, EditCommand.MESSAGE_NO_CHANGE, () ->
                logic.execute("edit 1 n/Alex Tan"));

        isSaveFailing[0] = false;
        logic.execute("undo");
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_capitalizationOnlyEdit_savesChangesAndInvalidatesUndo() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        Model model = new ModelManager();
        Logic logic = createLogic(model, storage);
        logic.execute("add r/student n/Alex Tan e/alex@example.com");
        ContactId studentId = model.getAddressBook().getPersonList().getFirst().getId();
        logic.execute("add r/student n/Bea Lim");
        logic.execute("delete 2");

        logic.execute("edit 1 n/ALEX TAN e/ALEX@example.com");

        Person edited = model.getAddressBook().getPersonList().getFirst();
        assertEquals("ALEX TAN", edited.getName().fullName);
        assertEquals("ALEX@example.com", edited.getEmail().orElseThrow().value);
        assertEquals(studentId, edited.getId());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertThrows(CommandException.class, UndoCommand.MESSAGE_UNAVAILABLE, () -> logic.execute("undo"));
    }
}
