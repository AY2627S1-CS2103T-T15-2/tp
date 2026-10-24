package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

public class CheckCommandTest {
    private String report(Model model, CheckCommand.Section section) {
        return new CheckCommand(section).execute(model).getFeedbackToUser();
    }

    @Test
    public void execute_missingStudentDetails_reportsSuggestionsWithoutChangingModel() {
        Model model = new ModelManager();
        Person student = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
        model.addPerson(student);
        var originalFilter = model.getPersonFilter();

        String result = report(model, CheckCommand.Section.ALL);

        assertTrue(result.contains("1: Alex Tan [Student]: no guardian linked."));
        assertTrue(result.contains("consider adding phone or email, level, subject."));
        assertTrue(result.contains("Suggestions only"));
        assertEquals(List.of(student.getId()), model.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
        assertSame(originalFilter, model.getPersonFilter());
        assertFalse(new CheckCommand(CheckCommand.Section.ALL).isDataChanging());
    }

    @Test
    public void execute_hiddenGuardian_doesNotReportExistingLinkAsMissing() {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId())
                .withLevel("Sec 3").withSubject("Math").build();
        Model model = new ModelManager();
        model.addPerson(guardian);
        model.addPerson(student);
        model.updateFilteredPersonList(person -> person.getId().equals(student.getId()));

        String result = report(model, CheckCommand.Section.ALL);

        assertTrue(result.contains("1 displayed contact(s)"));
        assertFalse(result.contains("no guardian linked"));
        assertFalse(result.contains("consider adding"));
        assertFalse(result.contains("Mei Tan"));
    }

    @Test
    public void execute_duplicates_usesExistingPhoneEmailAndMissingFieldRules() {
        for (String mode : List.of("phone", "email", "missing")) {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alex Tan").withPhone("9123 4567")
                    .withEmail("first@example.com").build();
            Person second = new PersonBuilder().withName("alex tan").withPhone("91234567")
                    .withEmail("second@example.com").build();
            String expected = "phone matches ignoring spaces and hyphens";
            if (mode.equals("email")) {
                second = new PersonBuilder(second).withPhone("87654321").withEmail("FIRST@example.com").build();
                expected = "email matches ignoring letter case";
            } else if (mode.equals("missing")) {
                first = new PersonBuilder(first).withoutPhone().withoutEmail().build();
                second = new PersonBuilder(second).withoutPhone().withoutEmail().build();
                expected = "both lack a phone number or both lack an email address";
            }
            model.addPerson(first);
            model.addPerson(second);

            String result = report(model, CheckCommand.Section.DUPLICATES);

            assertTrue(result.contains("Possible duplicates (1)"));
            assertTrue(result.contains(expected));
            assertFalse(result.contains("Guardian links"));
        }
    }

    @Test
    public void execute_sameNameDifferentRoles_notReportedAsDuplicate() {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alex Tan").build());
        model.addPerson(new PersonBuilder().withName("Alex Tan").withRole("guardian").build());
        assertTrue(report(model, CheckCommand.Section.DUPLICATES).contains("Possible duplicates (0)"));
    }

    @Test
    public void execute_threeSameNameContacts_reportsEachPairOnce() {
        Model model = new ModelManager();
        for (int i = 0; i < 3; i++) {
            model.addPerson(new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build());
        }
        String result = report(model, CheckCommand.Section.DUPLICATES);
        assertTrue(result.contains("Possible duplicates (3)"));
        assertEquals(3, result.lines().filter(line -> line.contains(" and ") && line.contains(": same role")).count());
        assertTrue(result.contains("1: Alex Tan [Student] and 2: Alex Tan [Student]"));
        assertTrue(result.contains("1: Alex Tan [Student] and 3: Alex Tan [Student]"));
        assertTrue(result.contains("2: Alex Tan [Student] and 3: Alex Tan [Student]"));
    }

    @Test
    public void execute_hiddenDuplicate_isExcludedFromReport() {
        Model model = new ModelManager();
        Person visible = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
        model.addPerson(visible);
        model.addPerson(new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build());
        model.updateFilteredPersonList(person -> person.getId().equals(visible.getId()));
        assertTrue(report(model, CheckCommand.Section.DUPLICATES).contains("Possible duplicates (0)"));
        assertEquals(List.of(visible.getId()), model.getFilteredPersonList().stream().map(Person::getId).toList());
    }

    @Test
    public void execute_formerStudents_reportsStatusAndDisplayedIndex() {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zoe Tan").withStatus(StudentStatus.FORMER).build());
        assertTrue(report(model, CheckCommand.Section.ALL).startsWith("No displayed contacts to check."));
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_FORMER_STUDENTS);
        assertTrue(report(model, CheckCommand.Section.GUARDIANS).contains("1: Zoe Tan [Student] [Former]"));
    }

    @Test
    public void equals_sameSection_comparesCommandConfiguration() {
        CheckCommand command = new CheckCommand(CheckCommand.Section.ALL);
        assertEquals(command, new CheckCommand(CheckCommand.Section.ALL));
        assertFalse(command.equals(new CheckCommand(CheckCommand.Section.DETAILS)));
        assertFalse(command.equals(null));
    }
}
