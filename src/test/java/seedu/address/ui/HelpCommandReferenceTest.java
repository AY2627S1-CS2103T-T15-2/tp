package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CheckCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.AddressBookParser;

public class HelpCommandReferenceTest {

    @Test
    public void getCommandFormats_currentCommands_listsEachCommandOnce() {
        List<String> formats = HelpWindow.getCommandFormats();
        Set<String> commandWords = formats.stream().map(format -> format.split(" ", 2)[0])
                .collect(Collectors.toSet());

        assertEquals(Set.of("add", "edit", "delete", "undo", "view", "find", "link", "list", "archive",
                "unarchive", "clear", "check", "help", "exit"), commandWords);
        assertEquals(commandWords.size(), formats.size());
    }

    @Test
    public void getCommandFormats_deleteAndUndo_describesSupportedSyntax() throws Exception {
        List<String> formats = HelpWindow.getCommandFormats();
        assertTrue(formats.contains("delete INDEX [INDEX...] or delete n/NAME"));
        assertTrue(formats.contains("undo"));

        AddressBookParser parser = new AddressBookParser();
        for (String example : List.of("delete 1", "delete 1 3 5", "delete n/Alex Tan")) {
            assertTrue(parser.parseCommand(example) instanceof DeleteCommand);
        }
        assertTrue(parser.parseCommand("undo") instanceof UndoCommand);
    }

    @Test
    public void getCommandFormats_check_describesSupportedSections() throws Exception {
        assertTrue(HelpWindow.getCommandFormats().contains(CheckCommand.COMMAND_FORMAT));
        AddressBookParser parser = new AddressBookParser();
        for (String example : List.of("check", "check guardians", "check details", "check duplicates")) {
            assertTrue(parser.parseCommand(example) instanceof CheckCommand);
        }
    }
}
