package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the badges and sections of {@code PersonDetailsPanel}, on platforms with a desktop JavaFX runtime.
 * The selection is no contact, a student with, without or with a deleted guardian, a former student or a
 * guardian, and the tests also change the selection or the contacts while a contact is shown.
 */
@EnabledOnOs(OS.WINDOWS)
public class PersonDetailsPanelTest {

    /** Minimum width of the details panel in {@code MainWindow.fxml}. */
    private static final int NARROWEST_PANEL_WIDTH = 240;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan")
            .withGuardianId(guardian.getId()).build();
    private final Person studentWithoutGuardian = new PersonBuilder().withName("Chloe Ng").build();
    private final Person formerStudent = new PersonBuilder().withName("Bea Lim")
            .withStatus(StudentStatus.FORMER).build();

    private ObservableList<Person> contacts;
    private SimpleObjectProperty<Person> selectedPerson;
    private PersonDetailsPanel panel;

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
    }

    @Test
    public void refreshDetails_noContactSelected_onlyPromptShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(null, student, guardian);
            assertNoDetailsShown();
        });
    }

    @Test
    public void refreshDetails_studentWithGuardian_allSectionsShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(student, student, guardian);
            assertEquals("Alex Tan", label("name").getText());
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS);
            assertShown("formerBadge", false);
            assertShown("contactDetails", true);
            assertShown("studentDetails", true);
            assertShown("guardianDetails", true);
            assertEquals("Guardian: Mei Tan", label("guardianName").getText());
            assertShown("guardianPhone", true);
            assertShown("guardianEmail", true);
        });
    }

    @Test
    public void refreshDetails_studentWithoutGuardian_onlyGuardianNameShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(studentWithoutGuardian, studentWithoutGuardian);
            assertNoGuardianShown();
        });
    }

    @Test
    public void refreshDetails_studentWithDeletedGuardian_onlyGuardianNameShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(student, student);
            assertNoGuardianShown();
        });
    }

    @Test
    public void refreshDetails_formerStudent_formerBadgeShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(formerStudent, formerStudent);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS);
            assertShown("formerBadge", true);
            assertEquals("Role: Student [Former]", label("role").getText());
        });
    }

    @Test
    public void refreshDetails_guardian_onlyDetailsSectionShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(guardian, student, guardian);
            assertRoleBadge("Guardian", RoleBadges.GUARDIAN_STYLE_CLASS);
            assertShown("formerBadge", false);
            assertShown("contactDetails", true);
            assertShown("studentDetails", false);
            assertShown("guardianDetails", false);
        });
    }

    @Test
    public void refreshDetails_studentThenGuardian_guardianBadgeAndSectionsShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(student, student, guardian);
            selectedPerson.set(guardian);
            assertRoleBadge("Guardian", RoleBadges.GUARDIAN_STYLE_CLASS);
            assertShown("studentDetails", false);
            assertShown("guardianDetails", false);
        });
    }

    @Test
    public void refreshDetails_guardianThenStudent_studentBadgeAndSectionsShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(guardian, student, guardian);
            selectedPerson.set(student);
            assertRoleBadge("Student", RoleBadges.STUDENT_STYLE_CLASS);
            assertShown("studentDetails", true);
            assertShown("guardianDetails", true);
        });
    }

    @Test
    public void refreshDetails_contactThenNoContact_onlyPromptShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(formerStudent, formerStudent);
            selectedPerson.set(null);
            assertNoDetailsShown();
        });
    }

    @Test
    public void refreshDetails_shownContactDeleted_onlyPromptShown() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(student, student, guardian);
            contacts.remove(student);
            assertNoDetailsShown();
        });
    }

    @Test
    public void refreshDetails_guardianLinkedWhileShown_guardianLinesShownAgain() throws Exception {
        runOnJavaFxThread(() -> {
            createPanel(studentWithoutGuardian, studentWithoutGuardian, guardian);
            assertNoGuardianShown();

            contacts.set(0, studentWithoutGuardian.withGuardianId(Optional.of(guardian.getId())));

            assertEquals("Guardian: Mei Tan", label("guardianName").getText());
            assertShown("guardianPhone", true);
            assertShown("guardianEmail", true);
        });
    }

    @Test
    public void refreshDetails_longestNameInNarrowestPanel_nameAndBadgesShownInFull() throws Exception {
        runOnJavaFxThread(() -> {
            String longestName = "Aloysius Bartholomew Cornelius Wellington Fitzgerald Tan Wei Ming Leow";
            assertEquals(Name.MAX_LENGTH, longestName.length());
            Person longNamedStudent = new PersonBuilder().withName(longestName)
                    .withStatus(StudentStatus.FORMER).build();
            createPanel(longNamedStudent, longNamedStudent);
            Scene scene = panel.getRoot().getScene();
            scene.getStylesheets().add(MainApp.class.getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setWidth(NARROWEST_PANEL_WIDTH);
            stage.show();
            try {
                panel.getRoot().applyCss();
                panel.getRoot().layout();
                Region root = panel.getRoot();
                double panelRightEdge = root.localToScene(root.getBoundsInLocal()).getMaxX();
                for (String id : List.of("name", "roleBadge", "formerBadge")) {
                    Label shownLabel = label(id);
                    Text shownText = (Text) shownLabel.lookup(".text");
                    assertTrue(shownLabel.isVisible());
                    assertEquals(shownLabel.getText(), shownText.getText());
                    double labelRightEdge = shownLabel.localToScene(shownLabel.getBoundsInLocal()).getMaxX();
                    assertTrue(labelRightEdge <= panelRightEdge, id);
                }
            } finally {
                stage.close();
            }
        });
    }

    /**
     * Creates a panel that shows {@code selected} from {@code persons}, and places it in a scene so that the
     * contents of its scroll pane can be looked up.
     */
    private void createPanel(Person selected, Person... persons) {
        contacts = FXCollections.observableArrayList(persons);
        selectedPerson = new SimpleObjectProperty<>(selected);
        panel = new PersonDetailsPanel(contacts, selectedPerson);
        new Scene(panel.getRoot());
        panel.getRoot().applyCss();
    }

    private void assertNoDetailsShown() {
        assertEquals("Select a contact to view details.", label("name").getText());
        for (String id : List.of("roleBadge", "formerBadge", "contactDetails", "studentDetails",
                "guardianDetails")) {
            assertShown(id, false);
        }
    }

    private void assertNoGuardianShown() {
        assertShown("guardianDetails", true);
        assertEquals("Guardian: None", label("guardianName").getText());
        assertShown("guardianPhone", false);
        assertShown("guardianEmail", false);
    }

    private void assertRoleBadge(String text, String styleClass) {
        assertShown("roleBadge", true);
        assertEquals(text, label("roleBadge").getText());
        assertEquals(List.of("label", "badge", styleClass), label("roleBadge").getStyleClass());
    }

    private void assertShown(String id, boolean isShown) {
        assertEquals(isShown, node(id).isVisible(), id);
        assertEquals(isShown, node(id).isManaged(), id);
    }

    private Node node(String id) {
        return panel.getRoot().lookup("#" + id);
    }

    private Label label(String id) {
        return (Label) node(id);
    }
}
