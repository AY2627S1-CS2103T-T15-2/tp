package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.ContactFilter;
import seedu.address.model.person.Level;
import seedu.address.model.person.Role;
import seedu.address.model.person.StudentStatus;
import seedu.address.model.person.Subject;

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
    public void parse_levelAndSubject_returnsListCommandWithBoth() {
        ListCommand listSec3 = new ListCommand(filterOf(null, StudentStatus.ACTIVE, "Sec 3", null));
        ListCommand listMath = new ListCommand(filterOf(null, StudentStatus.ACTIVE, null, "A-Math"));
        ListCommand listSec3Math = new ListCommand(filterOf(null, StudentStatus.ACTIVE, "Sec 3", "A-Math"));

        assertParseSuccess(parser, " lv/Sec 3", listSec3);
        // EP: surrounding and repeated spaces are removed, as in add and edit
        assertParseSuccess(parser, " lv/  Sec   3  ", listSec3);
        assertParseSuccess(parser, " sub/A-Math", listMath);
        assertParseSuccess(parser, " lv/Sec 3 sub/A-Math", listSec3Math);
        assertParseSuccess(parser, " sub/A-Math lv/Sec 3", listSec3Math);
    }

    @Test
    public void parse_allParameters_returnsListCommandWithAll() {
        // EP: all four parameters together, in any order
        assertParseSuccess(parser, " r/student st/former lv/JC1 sub/Physics",
                new ListCommand(filterOf(Role.STUDENT, StudentStatus.FORMER, "JC1", "Physics")));
        assertParseSuccess(parser, " sub/Physics st/all lv/JC1 r/student",
                new ListCommand(filterOf(Role.STUDENT, null, "JC1", "Physics")));
    }

    @Test
    public void parse_levelLength_acceptsUpToMaxLength() {
        String longestLevel = "A".repeat(Level.MAX_LENGTH);
        // Boundary: shortest level
        assertParseSuccess(parser, " lv/A", new ListCommand(filterOf(null, StudentStatus.ACTIVE, "A", null)));
        // Boundary: longest level
        assertParseSuccess(parser, " lv/" + longestLevel,
                new ListCommand(filterOf(null, StudentStatus.ACTIVE, longestLevel, null)));
        // Boundary: one character too long
        assertParseFailure(parser, " lv/" + longestLevel + "A", Level.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_subjectLength_acceptsUpToMaxLength() {
        String longestSubject = "A".repeat(Subject.MAX_LENGTH);
        // Boundary: shortest subject
        assertParseSuccess(parser, " sub/A", new ListCommand(filterOf(null, StudentStatus.ACTIVE, null, "A")));
        // Boundary: longest subject
        assertParseSuccess(parser, " sub/" + longestSubject,
                new ListCommand(filterOf(null, StudentStatus.ACTIVE, null, longestSubject)));
        // Boundary: one character too long
        assertParseFailure(parser, " sub/" + longestSubject + "A", Subject.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_invalidLevel_throwsParseException() {
        // EP: empty value
        assertParseFailure(parser, " lv/", Level.MESSAGE_EMPTY);
        assertParseFailure(parser, " lv/   ", Level.MESSAGE_EMPTY);
        // EP: unsupported characters
        assertParseFailure(parser, " lv/Sec@3", Level.MESSAGE_INVALID_CHARACTERS);
        // EP: no letter or digit
        assertParseFailure(parser, " lv/--", Level.MESSAGE_MISSING_ALPHANUMERIC);
        // EP: value reserved for removing a field in edit
        assertParseFailure(parser, " lv/none", ParserUtil.MESSAGE_RESERVED_REMOVE_VALUE);
        // Heuristic: one invalid input per test case, so the other values are valid
        assertParseFailure(parser, " r/student st/all lv/Sec@3 sub/Math", Level.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_invalidSubject_throwsParseException() {
        // EP: empty value
        assertParseFailure(parser, " sub/", Subject.MESSAGE_EMPTY);
        // EP: unsupported characters
        assertParseFailure(parser, " sub/Math!", Subject.MESSAGE_INVALID_CHARACTERS);
        // EP: value reserved for removing a field in edit
        assertParseFailure(parser, " sub/NONE", ParserUtil.MESSAGE_RESERVED_REMOVE_VALUE);
        // Heuristic: one invalid input per test case, so the other values are valid
        assertParseFailure(parser, " r/student lv/Sec 3 sub/Math!", Subject.MESSAGE_INVALID_CHARACTERS);
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
    public void parse_invalidStatusAndLevel_reportsStatusFirst() {
        // Two invalid inputs on purpose: this checks which error is reported first
        assertParseFailure(parser, " lv/Sec@3 st/formers", ListCommand.MESSAGE_INVALID_STATUS);
    }

    @Test
    public void parse_invalidLevelAndSubject_reportsLevelFirst() {
        // Two invalid inputs on purpose: this checks which error is reported first
        assertParseFailure(parser, " sub/Math! lv/Sec@3", Level.MESSAGE_INVALID_CHARACTERS);
    }

    @Test
    public void parse_guardianWithLevelOrSubject_throwsParseException() {
        assertParseFailure(parser, " r/guardian lv/Sec 3", ListCommand.MESSAGE_GUARDIAN_NO_STUDENT_DETAIL);
        assertParseFailure(parser, " sub/Math r/Guardian", ListCommand.MESSAGE_GUARDIAN_NO_STUDENT_DETAIL);
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
        assertParseFailure(parser, " lv/Sec 3 lv/Sec 4", ListCommand.MESSAGE_REPEATED_PARAMETER);
        assertParseFailure(parser, " sub/Math sub/Math", ListCommand.MESSAGE_REPEATED_PARAMETER);
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
        assertParseFailure(parser, " former lv/Sec 3", ListCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " Sec 3", ListCommand.MESSAGE_INVALID_FORMAT);
    }

    /**
     * Returns a filter for {@code role} and {@code status}, where {@code null} matches any value.
     */
    private static ContactFilter filterOf(Role role, StudentStatus status) {
        return new ContactFilter(Optional.ofNullable(role), Optional.ofNullable(status));
    }

    /**
     * Returns a filter for {@code role}, {@code status}, {@code level} and {@code subject},
     * where {@code null} matches any value.
     */
    private static ContactFilter filterOf(Role role, StudentStatus status, String level, String subject) {
        return new ContactFilter(Optional.ofNullable(role), Optional.ofNullable(status),
                Optional.ofNullable(level).map(Level::new), Optional.ofNullable(subject).map(Subject::new));
    }
}
