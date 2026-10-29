package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class DuplicateWarningTest {

    @Test
    public void classify_equivalentNames_appliesEachWarningRuleForBothRoles() {
        // All combinations: the three reported name variants x both roles x each warning reason.
        for (String[] pair : List.of(new String[] {"\u00c9lise", "E\u0301lise"},
                new String[] {"D'Souza", "D\u2019Souza"}, new String[] {"Alex Tan", "Alex  Tan"})) {
            for (String role : List.of("student", "guardian")) {
                Person first = new PersonBuilder().withName(pair[0]).withRole(role)
                        .withPhone("91234567").withEmail("alex@example.com").build();
                Person second = new PersonBuilder(first).withName(pair[1]).withPhone("9123 4567")
                        .withEmail("other@example.com").build();
                assertEquals(DuplicateWarning.PHONE, DuplicateWarning.classify(first, List.of(second)));
                second = new PersonBuilder(second).withPhone("87654321").withEmail("ALEX@example.com").build();
                assertEquals(DuplicateWarning.EMAIL, DuplicateWarning.classify(first, List.of(second)));
                // Guardians retain a valid email while both contacts lack a phone.
                first = new PersonBuilder(first).withoutPhone().build();
                second = new PersonBuilder(second).withoutPhone().withEmail("other@example.com").build();
                assertEquals(DuplicateWarning.GENERAL, DuplicateWarning.classify(first, List.of(second)));
            }
        }
    }

    @Test
    public void classify_phoneAndEmailMatchDifferentContacts_reportsPhoneForBothRoles() {
        for (String role : List.of("student", "guardian")) {
            Person candidate = new PersonBuilder().withRole(role).withPhone("91234567")
                    .withEmail("alex@example.com").build();
            Person emailMatch = new PersonBuilder(candidate).withPhone("87654321").build();
            Person phoneMatch = new PersonBuilder(candidate).withPhone("9123-4567")
                    .withEmail("other@example.com").build();

            assertEquals(DuplicateWarning.PHONE,
                    DuplicateWarning.classify(candidate, List.of(emailMatch, phoneMatch)));
            assertEquals(DuplicateWarning.PHONE,
                    DuplicateWarning.classify(candidate, List.of(phoneMatch, emailMatch)));
        }
    }

    @Test
    public void classify_phoneAndGeneralMatchDifferentContacts_reportsPhoneForBothRoles() {
        for (String role : List.of("student", "guardian")) {
            Person candidate = new PersonBuilder().withRole(role).withPhone("91234567").withoutEmail().build();
            Person generalMatch = new PersonBuilder(candidate).withPhone("87654321").build();
            Person phoneMatch = new PersonBuilder(candidate).withPhone("9123-4567")
                    .withEmail("other@example.com").build();

            assertEquals(DuplicateWarning.PHONE,
                    DuplicateWarning.classify(candidate, List.of(generalMatch, phoneMatch)));
            assertEquals(DuplicateWarning.PHONE,
                    DuplicateWarning.classify(candidate, List.of(phoneMatch, generalMatch)));
        }
    }

    @Test
    public void classify_emailAndGeneralMatchDifferentContacts_reportsEmailForBothRoles() {
        for (String role : List.of("student", "guardian")) {
            Person candidate = new PersonBuilder().withRole(role).withoutPhone()
                    .withEmail("alex@example.com").build();
            Person generalMatch = new PersonBuilder(candidate).withEmail("other@example.com").build();
            Person emailMatch = new PersonBuilder(candidate).withPhone("91234567")
                    .withEmail("ALEX@example.com").build();

            assertEquals(DuplicateWarning.EMAIL,
                    DuplicateWarning.classify(candidate, List.of(generalMatch, emailMatch)));
            assertEquals(DuplicateWarning.EMAIL,
                    DuplicateWarning.classify(candidate, List.of(emailMatch, generalMatch)));
        }
    }
}
