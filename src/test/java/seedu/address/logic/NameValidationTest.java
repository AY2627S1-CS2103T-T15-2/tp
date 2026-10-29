package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests name rejection through the command, model and persistence boundaries. */
public class NameValidationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_punctuationOnlyNames_preservesContactsAndSavedFile() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(contactsFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);
        logic.execute("add r/student n/Alex Tan");
        List<Person> originalContacts = List.copyOf(logic.getPersonList());
        byte[] originalBytes = Files.readAllBytes(contactsFile);

        for (String name : List.of("...", "-", "---", "'", "\u0301")) {
            for (String command : List.of("add r/student n/" + name, "add r/student n/\"" + name + "\"",
                    "add r/guardian p/91234567 n/" + name, "edit 1 n/" + name, "edit 1 n/\"" + name + "\"")) {
                ParseException failure = assertThrows(ParseException.class, () -> logic.execute(command));
                assertEquals(Name.MESSAGE_MISSING_ALPHANUMERIC, failure.getMessage());
                assertEquals(originalContacts, logic.getPersonList());
                assertArrayEquals(originalBytes, Files.readAllBytes(contactsFile));
            }
        }
    }
}
