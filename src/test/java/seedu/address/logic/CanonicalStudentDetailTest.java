package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class CanonicalStudentDetailTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_composedFilters_matchDecomposedDetailsAfterReload() throws Exception {
        assertCanonicalFiltersAfterReload("Franc\u0327ais", "Fran\u00e7ais");
    }

    @Test
    public void execute_decomposedFilters_matchComposedDetailsAfterReload() throws Exception {
        assertCanonicalFiltersAfterReload("Fran\u00e7ais", "Franc\u0327ais");
    }

    @Test
    public void execute_greekSigmaFilters_matchAfterReload() throws Exception {
        assertCanonicalFiltersAfterReload("\u039f\u03a3", "\u03bf\u03c3");
        assertCanonicalFiltersAfterReload("\u03bf\u03c3", "\u03bf\u03c2");
    }

    private void assertCanonicalFiltersAfterReload(String storedValue, String queryValue) throws Exception {
        Path file = temporaryFolder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Logic original = createLogic(new ModelManager(), storage);
        original.execute("add r/student n/Alex Tan lv/" + storedValue + " sub/" + storedValue);
        original.execute("add r/student n/Bea Lim lv/Francais sub/Francais");
        Person student = original.getPersonList().getFirst();
        byte[] savedData = Files.readAllBytes(file);
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic logic = createLogic(reloaded, storage);

        for (String filter : List.of("lv/" + queryValue, "sub/" + queryValue,
                "lv/" + queryValue + " sub/" + queryValue)) {
            logic.execute("list " + filter);
            assertEquals(List.of(student.getId()), logic.getFilteredPersonList().stream().map(Person::getId).toList());
            Person match = logic.getFilteredPersonList().getFirst();
            assertEquals(storedValue, match.getLevel().orElseThrow().value);
            assertEquals(storedValue, match.getSubject().orElseThrow().value);
            assertArrayEquals(savedData, Files.readAllBytes(file));
        }
        logic.execute("list lv/Francais sub/Francais");
        assertEquals(List.of("Bea Lim"), logic.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
