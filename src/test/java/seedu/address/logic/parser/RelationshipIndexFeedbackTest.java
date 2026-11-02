package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class RelationshipIndexFeedbackTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void uppercasePrefix_explainsLowercaseRequirement() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("children G/1"));
        assertEquals("Use: children g/GUARDIAN_INDEX. Use the lowercase prefix g/ exactly once.",
                error.getMessage());
    }

    @Test
    public void invalidIndexOrExtraArgument_suggestsBothPossibilities() {
        for (String command : new String[] {"children g/0", "children g/1 s/2"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertEquals("Invalid index or extra arguments. Use: children g/GUARDIAN_INDEX", error.getMessage());
        }
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("unlink s/"));
        assertEquals("Invalid index or extra arguments. Use: unlink s/STUDENT_INDEX", error.getMessage());
    }
}
