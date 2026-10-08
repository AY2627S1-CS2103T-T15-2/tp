package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.NameContainsSubstringPredicate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class CommandTest {

    @Test
    public void isDataChanging_mutationsRequireSaving() {
        List<Command> mutations = List.of(new AddCommand(new PersonBuilder().build()), new ClearCommand(),
                new DeleteCommand(INDEX_FIRST_PERSON),
                new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withName("Alex Tan").build()),
                new LinkCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
        mutations.forEach(command -> assertTrue(command.isDataChanging()));
    }

    @Test
    public void isDataChanging_readOnlyCommandsDoNotRequireSaving() {
        List<Command> readOnlyCommands = List.of(new ListCommand(), new HelpCommand(), new ExitCommand(),
                new FindCommand(new NameContainsSubstringPredicate("Alex")));
        readOnlyCommands.forEach(command -> assertFalse(command.isDataChanging()));
    }
}
