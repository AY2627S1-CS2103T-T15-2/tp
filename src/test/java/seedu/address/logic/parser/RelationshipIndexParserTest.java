package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

public class RelationshipIndexParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_uppercasePrefix_explainsLowercaseRequirement() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("children G/1"));
        assertEquals("Invalid children command. Use: children g/GUARDIAN_INDEX, with lowercase g/ given once.",
                error.getMessage());
    }

    @Test
    public void parse_invalidIndex_reportsIndexProblem() {
        for (String command : new String[] {"children g/0", "children g/abc"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertEquals(Messages.MESSAGE_INVALID_CONTACT_INDEX, error.getMessage());
        }
    }

    @Test
    public void parse_extraArguments_reportsOnlyOneIndexAllowed() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("children g/1 s/2"));
        assertEquals("Only one contact index is allowed. Use: children g/GUARDIAN_INDEX", error.getMessage());
    }

    @Test
    public void parse_missingIndex_reportsMissingIndex() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("unlink s/"));
        assertEquals("Missing contact index. Use: unlink s/STUDENT_INDEX", error.getMessage());
    }
}
