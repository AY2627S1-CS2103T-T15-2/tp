package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises the linking UI on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class LinkUiTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException exception) {
            // Another UI test may have already initialized JavaFX.
        }
    }

    @Test
    public void details_guardianEditedOrRemoved_refreshesSelectedStudent() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student, guardian);
            SimpleObjectProperty<Person> selectedPerson = new SimpleObjectProperty<>(student);
            PersonDetailsPanel panel = new PersonDetailsPanel(contacts, selectedPerson);
            new Scene(panel.getRoot(), 320, 500);
            panel.getRoot().applyCss();
            assertEquals("Guardian: Mei Tan", label(panel.getRoot(), "guardianName").getText());
            contacts.set(1, new PersonBuilder(guardian).withName("Sarah Lim").withPhone("91234567").build());
            assertEquals("Guardian: Sarah Lim", label(panel.getRoot(), "guardianName").getText());
            assertEquals("Guardian phone: 91234567", label(panel.getRoot(), "guardianPhone").getText());
            contacts.remove(1);
            contacts.set(0, student.withGuardianId(Optional.empty()));
            assertEquals("Guardian: None", label(panel.getRoot(), "guardianName").getText());
        });
    }

    @Test
    public void command_link_selectsStudentAndDuplicateKeepsDetails() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            Person student = new PersonBuilder().withName("Alex Tan").build();
            AddressBook book = new AddressBook();
            book.setPersons(List.of(guardian, student));
            Path dataPath = temporaryFolder.resolve("contacts.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            LogicManager logic = new LogicManager(new ModelManager(book, new UserPrefs()), storage);
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, dataPath);
            window.fillInnerParts();
            Scene scene = stage.getScene();
            Region root = (Region) scene.getRoot();
            root.resize(960, 640);
            root.applyCss();
            root.layout();
            TextField command = (TextField) root.lookup("#commandTextField");
            assertNotNull(command);
            command.setText("link s/1 g/2");
            command.fireEvent(new ActionEvent());
            assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
            Region personDetailsRoot = (Region) root.lookup("#personDetailsPanelPlaceholder");
            assertEquals(Color.WHITE, label(personDetailsRoot, "name").getTextFill());
            assertEquals(20.0, label(personDetailsRoot, "name").getFont().getSize(), 0.01);
            assertEquals("", command.getText());
            command.setText("link s/1 g/2");
            command.fireEvent(new ActionEvent());
            assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
            assertEquals("link s/1 g/2", command.getText());
            command.clear();
            root.applyCss();
            root.layout();
            saveSnapshot(root);

            command.setText("edit 2 n/Sarah Tan p/91234567");
            command.fireEvent(new ActionEvent());
            assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Guardian: Sarah Tan", label(root, "guardianName").getText());
            assertEquals("Guardian phone: 91234567", label(root, "guardianPhone").getText());

            command.setText("delete 2");
            command.fireEvent(new ActionEvent());
            assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Guardian: None", label(root, "guardianName").getText());
            stage.close();
        });
    }

    @Test
    public void selection_filteredOrDeletedContact_clearsSelectionAndDetails() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withName("Alex Tan").build();
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student, guardian);
            PersonListPanel listPanel = new PersonListPanel(contacts);
            PersonDetailsPanel details = new PersonDetailsPanel(contacts, listPanel.selectedPersonProperty());
            new Scene(details.getRoot());
            details.getRoot().applyCss();

            listPanel.selectPerson(guardian.getId());
            assertEquals(guardian.getId(), listPanel.getSelectedPerson().getId());
            assertEquals("Mei Tan", label(details.getRoot(), "name").getText());
            assertFalse(details.getRoot().lookup("#guardianDetails").isVisible());

            listPanel.selectPerson(ContactId.generate());
            assertNull(listPanel.getSelectedPerson());
            assertEquals("Select a contact to view details.", label(details.getRoot(), "name").getText());

            listPanel.selectPerson(student.getId());
            contacts.remove(student);
            listPanel.selectPerson(student.getId());
            assertNull(listPanel.getSelectedPerson());
            assertEquals("Select a contact to view details.", label(details.getRoot(), "name").getText());

            contacts.clear();
            listPanel.selectPerson(guardian.getId());
            assertNull(listPanel.getSelectedPerson());
        });
    }

    @Test
    public void details_selectedContactRemoved_clearsStaleDetails() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student);
            SimpleObjectProperty<Person> selectedPerson = new SimpleObjectProperty<>(student);
            PersonDetailsPanel panel = new PersonDetailsPanel(contacts, selectedPerson);
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();

            assertTrue(panel.getRoot().lookup("#guardianDetails").isVisible());
            contacts.remove(student);
            assertEquals("Select a contact to view details.", label(panel.getRoot(), "name").getText());
            assertEquals("", label(panel.getRoot(), "phone").getText());
            assertFalse(panel.getRoot().lookup("#guardianDetails").isVisible());
        });
    }

    @Test
    public void details_contactMethods_formatsMissingAndProvidedValues() {
        assertEquals("Not provided", PersonDetailsPanel.formatContactMethod(""));
        assertEquals("Not provided", PersonDetailsPanel.formatContactMethod(" \t"));
        assertEquals("91234567", PersonDetailsPanel.formatContactMethod("91234567"));
        assertEquals("alex@example.com", PersonDetailsPanel.formatContactMethod("alex@example.com"));
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    private void runOnJavaFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private void saveSnapshot(Region root) {
        WritableImage image = root.snapshot(new SnapshotParameters(), null);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        try {
            Path path = Path.of("build", "reports", "linking-ui.png");
            Files.createDirectories(path.getParent());
            ImageIO.write(output, "png", path.toFile());
        } catch (java.io.IOException exception) {
            throw new AssertionError("Could not save UI preview", exception);
        }
    }
}
