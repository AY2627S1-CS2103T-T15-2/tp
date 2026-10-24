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
    public void parse_status_returnsListCommandWithStatus() {
        assertParseSuccess(parser, " st/active", new ListCommand(ContactFilter.ACTIVE_CONTACTS));
        assertParseSuccess(parser, " st/ACTIVE", new ListCommand(ContactFilter.ACTIVE_CONTACTS));
        assertParseSuccess(parser, " st/former", new ListCommand(ContactFilter.FORMER_STUDENTS));
        assertParseSuccess(parser, "  st/FORMER  ", new ListCommand(ContactFilter.FORMER_STUDENTS));
        assertParseSuccess(parser, " st/all", new ListCommand(ContactFilter.ALL_CONTACTS));
        assertParseSuccess(parser, " st/ All ", new ListCommand(ContactFilter.ALL_CONTACTS));
    }

    @Test
    public void parse_role_returnsListCommandWithRole() {
        ListCommand listStudents = new ListCommand(filterOf(Role.STUDENT, StudentStatus.ACTIVE));
        ListCommand listGuardians = new ListCommand(filterOf(Role.GUARDIAN, StudentStatus.ACTIVE));

        assertParseSuccess(parser, " r/student", listStudents);
        assertParseSuccess(parser, " r/  STUDENT  ", listStudents);
        assertParseSuccess(parser, " r/guardian", listGuardians);
        assertParseSuccess(parser, " r/Guardian", listGuardians);
    }

    @Test
    public void parse_roleAndStatus_returnsListCommandWithBoth() {
        ListCommand listFormerStudents = new ListCommand(filterOf(Role.STUDENT, StudentStatus.FORMER));

        assertParseSuccess(parser, " r/student st/former", listFormerStudents);
        assertParseSuccess(parser, " st/former r/student", listFormerStudents);
        assertParseSuccess(parser, " r/student st/all", new ListCommand(filterOf(Role.STUDENT, null)));
        assertParseSuccess(parser, " r/guardian st/all", new ListCommand(filterOf(Role.GUARDIAN, null)));
        assertParseSuccess(parser, " r/guardian st/active",
                new ListCommand(filterOf(Role.GUARDIAN, StudentStatus.ACTIVE)));
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        assertParseFailure(parser, " r/teacher", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/student guardian", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", AddCommandParser.MESSAGE_EMPTY_ROLE);
        assertParseFailure(parser, " r/   ", AddCommandParser.MESSAGE_EMPTY_ROLE);
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, " st/formers", ListCommand.MESSAGE_INVALID_STATUS);
        assertParseFailure(parser, " st/archived", ListCommand.MESSAGE_INVALID_STATUS);
        assertParseFailure(parser, " st/former 1", ListCommand.MESSAGE_INVALID_STATUS);
        assertParseFailure(parser, " st/", ListCommand.MESSAGE_INVALID_STATUS);
        assertParseFailure(parser, " st/   ", ListCommand.MESSAGE_INVALID_STATUS);
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
    public void parse_roleOrStatusWithoutPrefix_suggestsPrefix() {
        assertParseFailure(parser, " former", "Invalid list command. Did you mean: list st/former?");
        assertParseFailure(parser, " ALL", "Invalid list command. Did you mean: list st/all?");
        assertParseFailure(parser, " active", "Invalid list command. Did you mean: list st/active?");
        assertParseFailure(parser, " student", "Invalid list command. Did you mean: list r/student?");
        assertParseFailure(parser, " Guardian", "Invalid list command. Did you mean: list r/guardian?");
    }

    @Test
    public void parse_extraText_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " formers", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " former students", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 3 r/student", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " former r/student", ListCommand.MESSAGE_INVALID_FORMAT);
    }

    /**
     * Returns a filter for {@code role} and {@code status}, where {@code null} matches any value.
     */
    private static ContactFilter filterOf(Role role, StudentStatus status) {
        return new ContactFilter(Optional.ofNullable(role), Optional.ofNullable(status));
    }
}
