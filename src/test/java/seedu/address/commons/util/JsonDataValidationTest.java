package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.UserPrefs;

public class JsonDataValidationTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void readJsonFile_nullRoot_rejectsInvalidDataAndPreservesFile() throws Exception {
        Path filePath = temporaryFolder.resolve("null.json");
        Files.writeString(filePath, "null");
        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(filePath, UserPrefs.class));
        assertEquals("null", Files.readString(filePath));
    }
}
