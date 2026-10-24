package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks how {@code RoleBadges} shows and hides badges, on platforms with a desktop JavaFX runtime.
 * The person shown is an active student, a former student or a guardian, and the badges are either new
 * or reused from an earlier person, as they are in the details panel.
 */
@EnabledOnOs(OS.WINDOWS)
public class RoleBadgesTest {

    private final Person student = new PersonBuilder().withName("Alex Tan").build();
    private final Person formerStudent = new PersonBuilder().withName("Bea Lim")
            .withStatus(StudentStatus.FORMER).build();
    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();

    private Label roleBadge;
    private Label formerBadge;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    @Test
    public void show_activeStudent_studentBadgeOnly() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, student);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS, RoleBadges.GUARDIAN_STYLE_CLASS);
            assertShown(formerBadge, false);
        });
    }

    @Test
    public void show_formerStudent_studentAndFormerBadges() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, formerStudent);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS, RoleBadges.GUARDIAN_STYLE_CLASS);
            assertShown(formerBadge, true);
            assertEquals(ContactDisplayFormatter.FORMER_LABEL, formerBadge.getText());
        });
    }

    @Test
    public void show_guardian_guardianBadgeOnly() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, guardian);
            assertRoleBadge("Guardian", RoleBadges.GUARDIAN_STYLE_CLASS, RoleBadges.STUDENT_STYLE_CLASS);
            assertShown(formerBadge, false);
        });
    }

    @Test
    public void show_studentThenGuardian_onlyGuardianStyleKept() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, student);
            RoleBadges.show(roleBadge, formerBadge, guardian);
            assertRoleBadge("Guardian", RoleBadges.GUARDIAN_STYLE_CLASS, RoleBadges.STUDENT_STYLE_CLASS);
        });
    }

    @Test
    public void show_guardianThenStudent_onlyStudentStyleKept() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, guardian);
            RoleBadges.show(roleBadge, formerBadge, student);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS, RoleBadges.GUARDIAN_STYLE_CLASS);
        });
    }

    @Test
    public void show_sameStudentTwice_styleAddedOnce() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, student);
            RoleBadges.show(roleBadge, formerBadge, student);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS, RoleBadges.GUARDIAN_STYLE_CLASS);
        });
    }

    @Test
    public void show_formerStudentThenActiveStudent_formerBadgeHidden() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, formerStudent);
            RoleBadges.show(roleBadge, formerBadge, student);
            assertShown(formerBadge, false);
        });
    }

    @Test
    public void hide_afterShow_bothBadgesHidden() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.show(roleBadge, formerBadge, formerStudent);
            RoleBadges.hide(roleBadge, formerBadge);
            assertShown(roleBadge, false);
            assertShown(formerBadge, false);
        });
    }

    @Test
    public void show_afterHide_badgesShownAgain() throws Exception {
        runOnJavaFxThread(() -> {
            createBadges();
            RoleBadges.hide(roleBadge, formerBadge);
            RoleBadges.show(roleBadge, formerBadge, formerStudent);
            assertShown(roleBadge, true);
            assertShown(formerBadge, true);
        });
    }

    @Test
    public void showAndHide_nullArgument_throwsNullPointerException() throws Exception {
        runOnJavaFxThread(() -> {
            Label badge = new Label();
            assertThrows(NullPointerException.class, () -> RoleBadges.show(null, badge, student));
            assertThrows(NullPointerException.class, () -> RoleBadges.show(badge, null, student));
            assertThrows(NullPointerException.class, () -> RoleBadges.show(badge, badge, null));
            assertThrows(NullPointerException.class, () -> RoleBadges.hide(null, badge));
            assertThrows(NullPointerException.class, () -> RoleBadges.hide(badge, null));
        });
    }

    private void createBadges() {
        roleBadge = new Label();
        formerBadge = new Label();
    }

    /**
     * Asserts that the role badge is shown with {@code text} and {@code expectedStyleClass} exactly once,
     * and without {@code unexpectedStyleClass}.
     */
    private void assertRoleBadge(String text, String expectedStyleClass, String unexpectedStyleClass) {
        assertShown(roleBadge, true);
        assertEquals(text, roleBadge.getText());
        assertEquals(1, roleBadge.getStyleClass().stream().filter(expectedStyleClass::equals).count());
        assertFalse(roleBadge.getStyleClass().contains(unexpectedStyleClass));
    }

    private static void assertShown(Label badge, boolean isShown) {
        assertEquals(isShown, badge.isVisible());
        assertEquals(isShown, badge.isManaged());
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
