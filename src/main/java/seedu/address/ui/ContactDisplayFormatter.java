package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Formats contact details for display in the UI.
 */
public class ContactDisplayFormatter {

    public static final String MISSING_CONTACT_METHOD = "Not provided";
    public static final String MISSING_GUARDIAN = "None";
    public static final String FORMER_LABEL = "Former";
    public static final String MISSING_STUDENT_DETAIL = "Not set";

    private ContactDisplayFormatter() {
        // Prevents instantiation, as this class only has static methods.
    }

    /**
     * Returns the text of {@code contactMethod}, or "Not provided" if {@code contactMethod} is empty.
     */
    public static String formatContactMethod(Optional<?> contactMethod) {
        requireNonNull(contactMethod);
        return contactMethod.map(Object::toString).orElse(MISSING_CONTACT_METHOD);
    }

    /**
     * Returns the role of {@code person}, followed by its status in square brackets
     * if it is a former student, for example "Student" or "Student [Former]".
     */
    public static String formatRoleWithStatus(Person person) {
        requireNonNull(person);
        String role = person.getRole().toString();
        return person.isFormerStudent() ? role + " [" + FORMER_LABEL + "]" : role;
    }

    /**
     * Returns the text of {@code detail}, such as a level or subject,
     * or "Not set" if {@code detail} is empty.
     */
    public static String formatStudentDetail(Optional<?> detail) {
        requireNonNull(detail);
        return detail.map(Object::toString).orElse(MISSING_STUDENT_DETAIL);
    }

    /**
     * Returns the level and subject of {@code person} on one line,
     * for example "Level: Sec 3 | Subject: Math", or an empty {@code Optional} if {@code person} has neither.
     */
    public static Optional<String> formatLevelAndSubject(Person person) {
        requireNonNull(person);
        if (person.getLevel().isEmpty() && person.getSubject().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of("Level: " + formatStudentDetail(person.getLevel())
                + " | Subject: " + formatStudentDetail(person.getSubject()));
    }

    /**
     * Returns the name of {@code guardian}, or "None" if {@code guardian} is empty.
     */
    public static String formatGuardianName(Optional<Person> guardian) {
        requireNonNull(guardian);
        return guardian.map(person -> person.getName().fullName).orElse(MISSING_GUARDIAN);
    }
}
