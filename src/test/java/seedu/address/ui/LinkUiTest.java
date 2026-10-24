package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
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

    /** Minimum width of the contact list in {@code MainWindow.fxml}. */
    private static final int NARROWEST_LIST_WIDTH = 240;

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
    public void personCard_displaysContactDetailsWithoutAddressOrTags() throws Exception {
        runOnJavaFxThread(() -> {
            Person person = new PersonBuilder().withoutEmail().build();
            PersonCard card = new PersonCard(person, 1, Optional.empty());
            new Scene(card.getRoot());
            assertNull(card.getRoot().lookup("#address"));
            assertEquals(person.getName().fullName, label(card.getRoot(), "name").getText());
            assertEquals(person.getRole().toString(), label(card.getRoot(), "roleBadge").getText());
            assertFalse(label(card.getRoot(), "formerBadge").isVisible());
            assertEquals("Phone: " + person.getPhone().get(), label(card.getRoot(), "phone").getText());
            assertEquals("Email: Not provided", label(card.getRoot(), "email").getText());
            assertNull(card.getRoot().lookup("#role"));
            assertEquals("Guardian: None", label(card.getRoot(), "guardian").getText());
            assertNull(card.getRoot().lookup("#tags"));
        });
    }

    @Test
    public void personCard_contactDetailLabels_wrapText() throws Exception {
        runOnJavaFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1, Optional.empty());
            new Scene(card.getRoot());
            for (String id : List.of("name", "phone", "email", "guardian")) {
                assertTrue(label(card.getRoot(), id).isWrapText());
            }
        });
    }

    @Test
    public void personListPanel_longDetailsInNarrowList_shownInFullAndCardGrows() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withRole("guardian")
                    .withName("Maximilian Alexander Christopher Wellington-Fitzgerald Tan Wei Ming").build();
            Person longStudent = new PersonBuilder().withName("Aloysius Bartholomew Cornelius Tan")
                    .withPhone("(HP) +65 1234 5678 (Office) +65 8765 4321 (Home) +65 1111 2222 (Mum) +65 3333 4444")
                    .withEmail("averyveryveryverylongemailaddressfortesting@example-school-domain.com")
                    .withGuardianId(guardian.getId()).build();
            Person shortStudent = new PersonBuilder().withName("Al").withPhone("123").withEmail("a@b.co").build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(longStudent, shortStudent, guardian);
            PersonListPanel panel = new PersonListPanel(contacts, contacts);
            Scene scene = new Scene(panel.getRoot(), NARROWEST_LIST_WIDTH, 700);
            scene.getStylesheets().add(MainApp.class.getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            panel.getRoot().applyCss();
            panel.getRoot().layout();

            ListCell<?> longCell = cell(panel.getRoot(), longStudent);
            ListCell<?> shortCell = cell(panel.getRoot(), shortStudent);
            for (String id : List.of("name", "phone", "email", "guardian")) {
                Label detail = label(longCell, id);
                Text displayedText = (Text) detail.lookup(".text");
                assertEquals(detail.getText(), displayedText.getText());
                assertTrue(detail.localToScene(detail.getBoundsInLocal()).getMaxX()
                        <= longCell.localToScene(longCell.getBoundsInLocal()).getMaxX());
            }
            assertTrue(longCell.getHeight() > shortCell.getHeight());
            assertTrue(panel.getRoot().lookupAll(".scroll-bar").stream()
                    .noneMatch(bar -> bar.isVisible() && ((ScrollBar) bar).getOrientation() == Orientation.HORIZONTAL));
            stage.close();
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
            root.applyCss();
            root.layout();
            saveSnapshot(root);
            command.setText("link s/1 g/2");
            command.fireEvent(new ActionEvent());
            assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
            assertEquals("link s/1 g/2", command.getText());
            command.clear();
            root.applyCss();
            root.layout();

            command.setText("edit 2 n/Sarah Tan p/91234567");
            command.fireEvent(new ActionEvent());
            assertEquals(guardian.getId(), window.getPersonListPanel().getSelectedPerson().getId());
            assertEquals("Sarah Tan", label(personDetailsRoot, "name").getText());

            window.getPersonListPanel().selectPerson(student.getId());
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
            PersonListPanel listPanel = new PersonListPanel(contacts, contacts);
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
    public void personList_guardianRenamed_refreshesStudentCard() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            Person student = new PersonBuilder().withName("Alex Tan")
                    .withGuardianId(guardian.getId()).build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student, guardian);
            PersonListPanel listPanel = new PersonListPanel(contacts, contacts);
            new Scene(listPanel.getRoot(), 400, 600);
            listPanel.getRoot().applyCss();
            listPanel.getRoot().layout();
            assertTrue(hasLabelWithText(listPanel.getRoot(), "Guardian: Mei Tan"));

            contacts.set(1, new PersonBuilder(guardian).withName("Mei Lim").build());
            listPanel.getRoot().layout();
            assertTrue(hasLabelWithText(listPanel.getRoot(), "Guardian: Mei Lim"));
        });
    }

    private static boolean hasLabelWithText(Region root, String text) {
        return root.lookupAll(".label").stream()
                .anyMatch(node -> node instanceof Label textLabel && text.equals(textLabel.getText()));
    }

    private static ListCell<?> cell(Region root, Person person) {
        return root.lookupAll(".list-cell").stream()
                .map(node -> (ListCell<?>) node)
                .filter(listCell -> person.equals(listCell.getItem()))
                .findFirst()
                .orElseThrow();
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

    @Test
    public void details_optionalEmail_displaysAbsenceAndRefreshesCapitalization() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").withoutEmail().build();
            Person student = new PersonBuilder().withName("Alex Tan").withoutEmail()
                    .withGuardianId(guardian.getId()).build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student, guardian);
            PersonDetailsPanel panel = new PersonDetailsPanel(contacts, new SimpleObjectProperty<>(student));
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();
            assertEquals("Email: Not provided", label(panel.getRoot(), "email").getText());
            assertEquals("Guardian email: Not provided", label(panel.getRoot(), "guardianEmail").getText());
            PersonCard card = new PersonCard(student, 1, Optional.of(guardian));
            new Scene(card.getRoot());
            card.getRoot().applyCss();
            assertEquals("Email: Not provided", label(card.getRoot(), "email").getText());
            assertEquals("Guardian: Mei Tan", label(card.getRoot(), "guardian").getText());
            PersonCard guardianCard = new PersonCard(guardian, 2, Optional.empty());
            new Scene(guardianCard.getRoot());
            assertFalse(guardianCard.getRoot().lookup("#guardian").isVisible());

            contacts.set(1, new PersonBuilder(guardian).withEmail("Mei+Tuition@Example.COM").build());
            assertEquals("Guardian email: Mei+Tuition@Example.COM", label(panel.getRoot(), "guardianEmail").getText());
            contacts.set(0, new PersonBuilder(student).withEmail("Alex@Example.COM").build());
            assertEquals("Email: Alex@Example.COM", label(panel.getRoot(), "email").getText());
        });
    }

    @Test
    public void command_linkReplacementSaveFailure_preservesSelectionDetailsFileAndFocus() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").withoutEmail().build();
            Person newGuardian = new PersonBuilder().withName("Sarah Tan").withRole("guardian")
                    .withoutPhone().withEmail("sarah@example.com").build();
            AddressBook book = new AddressBook();
            book.setPersons(List.of(student, guardian, newGuardian,
                    new PersonBuilder().withName("Zoe Lee").build()));
            ModelManager model = new ModelManager(book, new UserPrefs());
            Path dataPath = temporaryFolder.resolve("contacts.json");
            AtomicBoolean failSave = new AtomicBoolean();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataPath) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    if (failSave.get()) {
                        throw new IOException("Simulated disk failure");
                    }
                    super.saveAddressBook(addressBook);
                }
            };
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataPath);
                window.fillInnerParts();
                Scene scene = stage.getScene();
                Region root = (Region) scene.getRoot();
                root.resize(960, 640);
                root.applyCss();
                root.layout();
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                command.setText("find Tan");
                command.fireEvent(new ActionEvent());
                command.setText("link s/1 g/2");
                command.fireEvent(new ActionEvent());
                assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
                assertEquals(command, scene.getFocusOwner());
                String originalFile = readFile(dataPath);

                failSave.set(true);
                command.setText("link s/1 g/3");
                command.fireEvent(new ActionEvent());
                assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
                assertEquals("Guardian phone: " + guardian.getPhone().orElseThrow(),
                        label(root, "guardianPhone").getText());
                assertEquals("Guardian email: Not provided", label(root, "guardianEmail").getText());
                assertEquals(Optional.of(guardian.getId()), model.getFilteredPersonList().getFirst().getGuardianId());
                assertEquals(3, model.getFilteredPersonList().size());
                assertEquals(originalFile, readFile(dataPath));
                assertEquals(LogicManager.MESSAGE_SAVE_FAILURE, feedback.getText());
                assertEquals("link s/1 g/3", command.getText());
                assertEquals(command, scene.getFocusOwner());

                failSave.set(false);
                command.setText("link g/3 s/1");
                command.fireEvent(new ActionEvent());
                assertEquals(student.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertEquals("Guardian: Sarah Tan", label(root, "guardianName").getText());
                assertEquals("Guardian phone: Not provided", label(root, "guardianPhone").getText());
                assertEquals("Guardian email: sarah@example.com", label(root, "guardianEmail").getText());
                assertEquals("", command.getText());
                assertEquals(command, scene.getFocusOwner());
                assertEquals(Optional.of(newGuardian.getId()),
                        model.getFilteredPersonList().getFirst().getGuardianId());

                command.setText("find Alex");
                command.fireEvent(new ActionEvent());
                assertEquals(1, model.getFilteredPersonList().size());
                assertEquals("Guardian: Sarah Tan", label(root, "guardianName").getText());
                assertEquals("Guardian email: sarah@example.com", label(root, "guardianEmail").getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void command_sameNameStudents_linksAndSelectsSecondRecord() throws Exception {
        runOnJavaFxThread(() -> {
            Person first = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
            Person second = new PersonBuilder().withName("Alex Tan").withoutPhone().withoutEmail().build();
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
            AddressBook book = new AddressBook();
            book.setPersons(List.of(first, second, guardian));
            ModelManager model = new ModelManager(book, new UserPrefs());
            Path dataPath = temporaryFolder.resolve("contacts.json");
            LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataPath);
                window.fillInnerParts();
                Region root = (Region) stage.getScene().getRoot();
                root.resize(960, 640);
                root.applyCss();
                root.layout();
                TextField command = (TextField) root.lookup("#commandTextField");
                command.setText("link s/2 g/3");
                command.fireEvent(new ActionEvent());

                assertEquals(second.getId(), window.getPersonListPanel().getSelectedPerson().getId());
                assertEquals(Optional.of(guardian.getId()),
                        window.getPersonListPanel().getSelectedPerson().getGuardianId());
                assertTrue(model.getFilteredPersonList().getFirst().getGuardianId().isEmpty());
                assertEquals("Guardian: Mei Tan", label(root, "guardianName").getText());
                assertEquals(command, stage.getScene().getFocusOwner());

                window.getPersonListPanel().selectPerson(first.getId());
                assertEquals("Guardian: None", label(root, "guardianName").getText());
            } finally {
                stage.close();
            }
        });
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new AssertionError("Could not read saved contacts", exception);
        }
    }
}
