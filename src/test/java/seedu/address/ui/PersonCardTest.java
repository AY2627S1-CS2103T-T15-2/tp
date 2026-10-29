package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.sun.javafx.font.CharToGlyphMapper;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.scene.text.FontHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the role and former badges on {@code PersonCard}, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class PersonCardTest {

    /** Minimum width of the contact list in {@code MainWindow.fxml}. */
    private static final int NARROWEST_LIST_WIDTH = 240;
    private static final int GLYPH_TEST_SCENE_WIDTH = 500;
    private static final int GLYPH_TEST_SCENE_HEIGHT = 450;

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
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

    @Test
    public void personListPanel_formerStudentWithLongNameInNarrowList_nameAndBadgesShownInFull() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder().withName("Aloysius Bartholomew Cornelius Tan Wei Ming")
                    .withStatus(StudentStatus.FORMER).build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(student);
            PersonListPanel panel = new PersonListPanel(contacts, contacts);
            Scene scene = new Scene(panel.getRoot(), NARROWEST_LIST_WIDTH, 400);
            scene.getStylesheets().add(MainApp.class.getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            try {
                panel.getRoot().applyCss();
                panel.getRoot().layout();
                Region card = (Region) panel.getRoot().lookup("#cardPane");
                double cardRightEdge = card.localToScene(card.getBoundsInLocal()).getMaxX();
                for (String id : List.of("name", "roleBadge", "formerBadge")) {
                    Label shownLabel = (Label) card.lookup("#" + id);
                    Text shownText = (Text) shownLabel.lookup(".text");
                    assertTrue(shownLabel.isVisible());
                    assertEquals(shownLabel.getText(), shownText.getText());
                    assertTrue(shownLabel.localToScene(shownLabel.getBoundsInLocal()).getMaxX() <= cardRightEdge);
                }
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void personListPanel_namesInDifferentScripts_haveRenderableGlyphs() throws Exception {
        runOnJavaFxThread(() -> {
            List<String> names = List.of("\u0915\u093f\u0930\u0923", "\u674e\u5c0f\u660e", "\u00c9lise Tan");
            ObservableList<Person> contacts = FXCollections.observableArrayList();
            names.forEach(name -> contacts.add(new PersonBuilder().withName(name).build()));
            PersonListPanel panel = new PersonListPanel(contacts, contacts);
            Scene scene = new Scene(panel.getRoot(), GLYPH_TEST_SCENE_WIDTH, GLYPH_TEST_SCENE_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setOpacity(0);
            stage.show();
            try {
                panel.getRoot().applyCss();
                panel.getRoot().layout();
                Set<String> displayedNames = new HashSet<>();
                for (Node node : panel.getRoot().lookupAll(".list-cell #name")) {
                    Label name = (Label) node;
                    displayedNames.add(name.getText());
                    // Text equality alone also passes when JavaFX renders missing-glyph boxes.
                    PGFont font = (PGFont) FontHelper.getNativeFont(name.getFont());
                    CharToGlyphMapper mapper = font.getFontResource().getGlyphMapper();
                    for (int codePoint : name.getText().codePoints().toArray()) {
                        assertNotEquals(mapper.getMissingGlyphCode(), mapper.charToGlyph(codePoint),
                                String.format("Missing U+%04X in %s", codePoint, name.getFont()));
                    }
                }
                assertEquals(Set.copyOf(names), displayedNames);
            } finally {
                stage.close();
            }
        });
    }

    private static Label label(PersonCard card, String id) {
        return (Label) card.getRoot().lookup("#" + id);
    }
}
