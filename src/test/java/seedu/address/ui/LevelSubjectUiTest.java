package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks how a student's level and subject are shown on the contact card and details panel.
 */
@EnabledOnOs(OS.WINDOWS)
public class LevelSubjectUiTest {

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
    }

    @Test
    public void personCard_studentWithLevelAndSubject_showsBoth() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withLevel("Sec 3").withSubject("Math").build();
            PersonCard card = new PersonCard(student, 1, Optional.empty());
            new Scene(card.getRoot());
            Label studentDetails = label(card.getRoot(), "studentDetails");
            assertTrue(studentDetails.isVisible());
            assertEquals("Level: Sec 3 | Subject: Math", studentDetails.getText());
        });
    }

    @Test
    public void personCard_studentWithoutLevelOrSubject_hidesLine() throws Exception {
        runOnJavaFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1, Optional.empty());
            new Scene(card.getRoot());
            assertFalse(label(card.getRoot(), "studentDetails").isVisible());
        });
    }

    @Test
    public void personDetailsPanel_selectStudentThenGuardian_showsLevelAndSubjectOnlyForStudent() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withName("Alex Tan").withLevel("JC1").withSubject("H2 Math").build();
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student, guardian);
            PersonListPanel listPanel = new PersonListPanel(contacts, contacts);
            PersonDetailsPanel details = new PersonDetailsPanel(contacts, listPanel.selectedPersonProperty());
            new Scene(details.getRoot());
            details.getRoot().applyCss();

            listPanel.selectPerson(student.getId());
            assertTrue(details.getRoot().lookup("#studentDetails").isVisible());
            assertEquals("Level: JC1", label(details.getRoot(), "level").getText());
            assertEquals("Subject: H2 Math", label(details.getRoot(), "subject").getText());

            listPanel.selectPerson(guardian.getId());
            assertFalse(details.getRoot().lookup("#studentDetails").isVisible());
        });
    }

    @Test
    public void personDetailsPanel_selectedStudentDetailsChanged_refreshesChangedAndClearedValues() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withLevel("JC1").withSubject("H2 Math").build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student);
            SimpleObjectProperty<Person> selection = new SimpleObjectProperty<>(student);
            PersonDetailsPanel details = new PersonDetailsPanel(contacts, selection);
            new Scene(details.getRoot());
            details.getRoot().applyCss();
            assertEquals("Level: JC1", label(details.getRoot(), "level").getText());
            assertEquals("Subject: H2 Math", label(details.getRoot(), "subject").getText());

            Person updated = new PersonBuilder(student).withLevel("Sec 4").withSubject("Physics").build();
            contacts.set(0, updated);

            assertEquals("Level: Sec 4", label(details.getRoot(), "level").getText());
            assertEquals("Subject: Physics", label(details.getRoot(), "subject").getText());

            contacts.set(0, new PersonBuilder(updated).withoutLevel().withoutSubject().build());

            assertEquals("Level: Not set", label(details.getRoot(), "level").getText());
            assertEquals("Subject: Not set", label(details.getRoot(), "subject").getText());
        });
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }
}
