package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests supported email punctuation through Add, Edit and saved-data loading.
 */
public class EmailFormatTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_addAndEditApostrophes_preservesEmailOnReload() throws Exception {
        assertEmailsSurviveAddEditAndReload("mei.o'brien@example.com", "mei.o'connor@example.com");
    }

    @Test
    public void execute_addAndEditLocalSymbols_preservesEmailOnReload() throws Exception {
        for (char symbol : "!#$%&'*+-/=?^_`{|}~".toCharArray()) {
            assertEmailsSurviveAddEditAndReload("alex" + symbol + "tan@example.com",
                    symbol + "mei" + symbol + "@example.com");
        }
    }

    @Test
    public void parse_emailSlashWithInternalWhitespace_rejected() {
        AddressBookParser parser = new AddressBookParser();
        for (String email : List.of("alex /tan@example.com", "alex/ tan@example.com", "alex / tan@example.com",
                "alex tan/example.com", "alex p/tan@example.com")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("add r/student n/Mei e/" + email));
            assertThrows(ParseException.class, () -> parser.parseCommand("edit 1 e/" + email));
        }
    }

    @Test
    public void execute_addAndEditRepeatedDomainHyphens_preservesEmailOnReload() throws Exception {
        assertEmailsSurviveAddEditAndReload("mei@school--mail.com", "mei@school---mail.com");
    }

    private void assertEmailsSurviveAddEditAndReload(String addedEmail, String editedEmail) throws Exception {
        Path contactsFile = Files.createTempDirectory(temporaryFolder, "contacts-").resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(contactsFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(new ModelManager(), storage);

        logic.execute("add r/student n/Mei e/" + addedEmail);
        ContactId originalId = logic.getPersonList().getFirst().getId();
        assertSavedEmail(contactsFile, addedEmail, originalId);
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                logic.execute("add r/student n/MEI e/" + addedEmail.toUpperCase(Locale.ROOT)));

        logic.execute("edit 1 e/" + editedEmail);
        assertEquals(editedEmail, logic.getPersonList().getFirst().getEmail().orElseThrow().value);
        assertSavedEmail(contactsFile, editedEmail, originalId);
    }

    private void assertSavedEmail(Path contactsFile, String expectedEmail, ContactId expectedId) throws Exception {
        List<Person> contacts = new JsonAddressBookStorage(contactsFile).readAddressBook()
                .orElseThrow().getPersonList();
        assertEquals(1, contacts.size());
        Person contact = contacts.getFirst();
        assertEquals(expectedEmail, contact.getEmail().orElseThrow().value);
        assertEquals(expectedId, contact.getId());
    }
}
