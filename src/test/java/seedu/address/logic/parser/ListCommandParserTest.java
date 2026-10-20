package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_former_returnsListFormerCommand() {
        assertParseSuccess(parser, " st/former", new ListCommand(ContactFilter.FORMER_STUDENTS));
        assertParseSuccess(parser, "  st/FORMER  ", new ListCommand(ContactFilter.FORMER_STUDENTS));
    }

    @Test
    public void parse_role_returnsListCommandWithRole() {
        ListCommand listStudents = new ListCommand(new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.ACTIVE));
        ListCommand listGuardians = new ListCommand(
                new ContactFilter(Optional.of(Role.GUARDIAN), StudentStatus.ACTIVE));

        assertParseSuccess(parser, " r/student", listStudents);
        assertParseSuccess(parser, " r/  STUDENT  ", listStudents);
        assertParseSuccess(parser, " r/guardian", listGuardians);
        assertParseSuccess(parser, " r/Guardian", listGuardians);
    }

    @Test
    public void parse_roleAndFormer_returnsListCommandWithBoth() {
        ListCommand listFormerStudents = new ListCommand(
                new ContactFilter(Optional.of(Role.STUDENT), StudentStatus.FORMER));

        assertParseSuccess(parser, " r/student st/former", listFormerStudents);
        assertParseSuccess(parser, " st/former r/student", listFormerStudents);
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        assertParseFailure(parser, " r/teacher", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/student guardian", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", AddCommandParser.MESSAGE_EMPTY_ROLE);
        assertParseFailure(parser, " r/   ", AddCommandParser.MESSAGE_EMPTY_ROLE);
    }

    @Test
    public void parse_invalidRoleAndStatus_reportsRoleFirst() {
        assertParseFailure(parser, " st/formers r/teacher", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_guardianAndFormer_throwsParseException() {
        assertParseFailure(parser, " r/guardian st/former", ListCommand.MESSAGE_GUARDIAN_NOT_FORMER);
        assertParseFailure(parser, " st/FORMER r/GUARDIAN", ListCommand.MESSAGE_GUARDIAN_NOT_FORMER);
    }

    @Test
    public void parse_repeatedParameter_throwsParseException() {
        assertParseFailure(parser, " r/student r/guardian", ListCommand.MESSAGE_REPEATED_PARAMETER);
        assertParseFailure(parser, " st/former st/former", ListCommand.MESSAGE_REPEATED_PARAMETER);
    }

    @Test
    public void parse_unknownParameter_throwsParseException() {
        assertParseFailure(parser, " n/Alex", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " r/student n/Alex", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " s/former", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " R/student", ListCommand.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraText_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " all", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " former", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 3 r/student", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/former 1", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/formers", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " st/active", ListCommand.MESSAGE_INVALID_FORMAT);
    }
}
