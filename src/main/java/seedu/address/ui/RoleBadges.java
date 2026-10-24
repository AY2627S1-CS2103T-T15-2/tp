package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.ui.ContactDisplayFormatter.FORMER_LABEL;

import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Shows a contact's role, and whether a student is a former student, as badges.
 */
public final class RoleBadges {

    public static final String STUDENT_STYLE_CLASS = "badge-student";
    public static final String GUARDIAN_STYLE_CLASS = "badge-guardian";

    private RoleBadges() {
        // Prevents instantiation, as this class only has static methods.
    }

    /**
     * Shows the role of {@code person} on {@code roleBadge}, and shows {@code formerBadge} only if
     * {@code person} is a former student.
     * The badges may be reused for another person, so any role style from an earlier person is replaced.
     */
    public static void show(Label roleBadge, Label formerBadge, Person person) {
        requireNonNull(roleBadge);
        requireNonNull(formerBadge);
        requireNonNull(person);

        boolean isStudent = person.getRole() == Role.STUDENT;
        assert isStudent || !person.isFormerStudent() : "Only a student can be a former student.";
        roleBadge.setText(person.getRole().toString());
        roleBadge.getStyleClass().removeAll(STUDENT_STYLE_CLASS, GUARDIAN_STYLE_CLASS);
        roleBadge.getStyleClass().add(isStudent ? STUDENT_STYLE_CLASS : GUARDIAN_STYLE_CLASS);
        roleBadge.setVisible(true);
        roleBadge.setManaged(true);

        formerBadge.setText(FORMER_LABEL);
        formerBadge.setVisible(person.isFormerStudent());
        formerBadge.setManaged(person.isFormerStudent());
    }

    /**
     * Hides both badges, such as when no contact is selected.
     */
    public static void hide(Label roleBadge, Label formerBadge) {
        requireNonNull(roleBadge);
        requireNonNull(formerBadge);

        roleBadge.setVisible(false);
        roleBadge.setManaged(false);
        formerBadge.setVisible(false);
        formerBadge.setManaged(false);
    }
}
