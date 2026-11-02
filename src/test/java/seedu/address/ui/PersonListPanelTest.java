package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.JavaFxTestUtil.runOnJavaFxThread;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the height of contact cards in {@code PersonListPanel}, on platforms with a desktop JavaFX runtime.
 */
@EnabledOnOs(OS.WINDOWS)
public class PersonListPanelTest {

    /** Wide enough for every detail line of the contacts below to fit on one line. */
    private static final int WIDE_LIST_WIDTH = 600;
    private static final int LIST_HEIGHT = 700;

    /** Allows for sub-pixel rounding when comparing heights. */
    private static final double LAYOUT_TOLERANCE = 1;

    @BeforeAll
    public static void startJavaFx() {
        JavaFxTestUtil.startJavaFx();
    }

    @Test
    public void personListPanel_shortDetailsInWideList_cardsOnlyAsTallAsContent() throws Exception {
        runOnJavaFxThread(() -> {
            Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian")
                    .withPhone("98765432").withEmail("mei@example.com").build();
            // EP: former student with every detail line
            Person fullStudent = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
                    .withEmail("alex@example.com").withLevel("Sec 3").withSubject("A-Math")
                    .withStatus(StudentStatus.FORMER).withGuardianId(guardian.getId()).build();
            // EP: student without optional details
            Person bareStudent = new PersonBuilder().withName("Bea Lim").build();
            ObservableList<Person> contacts = FXCollections.observableArrayList(fullStudent, bareStudent, guardian);
            PersonListPanel panel = new PersonListPanel(contacts, contacts);
            Scene scene = new Scene(panel.getRoot(), WIDE_LIST_WIDTH, LIST_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            panel.getRoot().applyCss();
            panel.getRoot().layout();

            int checkedCardCount = 0;
            for (Node node : panel.getRoot().lookupAll(".list-cell")) {
                ListCell<?> cell = (ListCell<?>) node;
                if (cell.getItem() == null) {
                    continue;
                }
                // Without a width, a card is measured with each detail on one line, which fits this wide list
                Region card = (Region) cell.getGraphic();
                assertEquals(card.prefHeight(-1), card.getHeight(), LAYOUT_TOLERANCE);
                checkedCardCount++;
            }
            assertEquals(contacts.size(), checkedCardCount);
            stage.close();
        });
    }
}
