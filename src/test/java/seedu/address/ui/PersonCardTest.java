package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
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
 * Checks the role and former badges on {@code PersonCard}, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class PersonCardTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    @Test
    public void constructor_activeStudent_showsStudentBadgeOnly() throws Exception {
        runOnJavaFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withName("Alex Tan").build(), 1, Optional.empty());
            assertEquals("Alex Tan", label(card, "name").getText());
            assertEquals("Student", label(card, "roleBadge").getText());
            assertTrue(label(card, "roleBadge").getStyleClass().contains("badge-student"));
            assertFalse(label(card, "formerBadge").isVisible());
            assertFalse(label(card, "formerBadge").isManaged());
        });
    }

    @Test
    public void constructor_formerStudent_showsStudentAndFormerBadges() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withName("Bea Lim").withStatus(StudentStatus.FORMER).build();
            PersonCard card = new PersonCard(student, 1, Optional.empty());
            assertEquals("Bea Lim", label(card, "name").getText());
            assertEquals("Student", label(card, "roleBadge").getText());
            assertTrue(label(card, "formerBadge").isVisible());
            assertEquals(ContactDisplayFormatter.FORMER_LABEL, label(card, "formerBadge").getText());
        });
    }

    @Test
    public void constructor_guardian_showsGuardianBadgeOnly() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            PersonCard card = new PersonCard(guardian, 2, Optional.empty());
            assertEquals("Guardian", label(card, "roleBadge").getText());
            assertTrue(label(card, "roleBadge").getStyleClass().contains("badge-guardian"));
            assertFalse(label(card, "roleBadge").getStyleClass().contains("badge-student"));
            assertFalse(label(card, "formerBadge").isVisible());
        });
    }

    private static Label label(PersonCard card, String id) {
        return (Label) card.getRoot().lookup("#" + id);
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
