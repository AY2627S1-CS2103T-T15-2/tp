package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises student status changes through the command box and contact views.
 */
@EnabledOnOs(OS.WINDOWS)
public class StudentStatusUiTest {

    @TempDir
    public Path temporaryFolder;

    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan")
            .withGuardianId(guardian.getId()).build();

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
    }

    @Test
    public void command_archiveInDefaultView_clearsSelectionAndDetails() throws Exception {
        runOnJavaFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = createWindow(stage, StudentStatus.ACTIVE);
                Region root = (Region) stage.getScene().getRoot();
                Region details = (Region) root.lookup("#personDetailsPanelPlaceholder");
                window.getPersonListPanel().selectPerson(student.getId());
                assertEquals("Alex Tan", label(details, "name").getText());

                executeCommand(root, "archive 1");

                assertNull(window.getPersonListPanel().getSelectedPerson());
                assertEquals("Select a contact to view details.", label(details, "name").getText());
                assertEquals("", label(details, "role").getText());
                assertEquals("", label(details, "phone").getText());
                assertFalse(details.lookup("#guardianDetails").isVisible());
                assertFalse(hasCard(root, "Alex Tan", "Student", true));
                assertTrue(hasCard(root, "Mei Tan", "Guardian", false));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void command_unarchiveInFormerView_clearsSelectionAndDetails() throws Exception {
        runOnJavaFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = createWindow(stage, StudentStatus.FORMER);
                Region root = (Region) stage.getScene().getRoot();
                Region details = (Region) root.lookup("#personDetailsPanelPlaceholder");
                executeCommand(root, "list st/former");
                window.getPersonListPanel().selectPerson(student.getId());
                assertEquals("Role: Student [Former]", label(details, "role").getText());

                executeCommand(root, "unarchive 1");

                assertNull(window.getPersonListPanel().getSelectedPerson());
                assertEquals("Select a contact to view details.", label(details, "name").getText());
                assertEquals("", label(details, "role").getText());
                assertEquals("", label(details, "email").getText());
                assertFalse(details.lookup("#guardianDetails").isVisible());
                assertFalse(hasCard(root, "Alex Tan", "Student", false));
                assertFalse(hasCard(root, "Alex Tan", "Student", true));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void command_statusChangesInSearchResults_refreshLabelsAndKeepSelection() throws Exception {
        runOnJavaFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = createWindow(stage, StudentStatus.ACTIVE);
                Region root = (Region) stage.getScene().getRoot();
                Region details = (Region) root.lookup("#personDetailsPanelPlaceholder");
                executeCommand(root, "find Alex");
                window.getPersonListPanel().selectPerson(student.getId());
                assertTrue(hasCard(root, "Alex Tan", "Student", false));

                executeCommand(root, "archive 1");

                assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertTrue(window.getPersonListPanel().getSelectedPerson().isFormerStudent());
                assertEquals("Role: Student [Former]", label(details, "role").getText());
                assertEquals("Guardian: Mei Tan", label(details, "guardianName").getText());
                assertTrue(hasCard(root, "Alex Tan", "Student", true));
                assertFalse(hasCard(root, "Mei Tan", "Guardian", false));

                executeCommand(root, "unarchive 1");

                assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertFalse(window.getPersonListPanel().getSelectedPerson().isFormerStudent());
                assertEquals("Role: Student", label(details, "role").getText());
                assertTrue(hasCard(root, "Alex Tan", "Student", false));
                assertFalse(hasCard(root, "Alex Tan", "Student", true));
                assertFalse(hasCard(root, "Mei Tan", "Guardian", false));
            } finally {
                stage.close();
            }
        });
    }

    private MainWindow createWindow(Stage stage, StudentStatus status) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(guardian, student.withStatus(status)));
        ModelManager model = new ModelManager(addressBook, new UserPrefs());
        Path dataPath = temporaryFolder.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        MainWindow window = new MainWindow(stage, logic, dataPath);
        window.fillInnerParts();
        Region root = (Region) stage.getScene().getRoot();
        root.resize(960, 640);
        root.applyCss();
        root.layout();
        return window;
    }

    private void executeCommand(Region root, String text) {
        TextField command = (TextField) root.lookup("#commandTextField");
        command.setText(text);
        command.fireEvent(new ActionEvent());
        assertEquals("", command.getText(), ((TextArea) root.lookup("#resultDisplay")).getText());
        root.applyCss();
        root.layout();
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    /**
     * Returns true if the contact list shows a card for {@code name} with a {@code role} badge,
     * and shows the former badge on that card only if {@code isFormer} is true.
     */
    private static boolean hasCard(Region root, String name, String role, boolean isFormer) {
        Region contactList = (Region) root.lookup("#personListPanelPlaceholder");
        return contactList.lookupAll("#cardPane").stream()
                .anyMatch(card -> name.equals(((Label) card.lookup("#name")).getText())
                        && role.equals(((Label) card.lookup("#roleBadge")).getText())
                        && card.lookup("#formerBadge").isVisible() == isFormer);
    }
}
