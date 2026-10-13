package seedu.address.ui;

import static seedu.address.ui.ContactDisplayFormatter.formatContactMethod;
import static seedu.address.ui.ContactDisplayFormatter.formatGuardianName;
import static seedu.address.ui.ContactDisplayFormatter.formatNameWithRole;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label guardian;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     * If the person is a student, the card also shows the name of {@code linkedGuardian},
     * or "None" if the student has no linked guardian.
     */
    public PersonCard(Person person, int displayedIndex, Optional<Person> linkedGuardian) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(formatNameWithRole(person));
        phone.setText("Phone: " + formatContactMethod(person.getPhone()));
        email.setText("Email: " + formatContactMethod(person.getEmail()));

        boolean isStudent = person.getRole() == Role.STUDENT;
        guardian.setVisible(isStudent);
        guardian.setManaged(isStudent);
        guardian.setText(isStudent ? "Guardian: " + formatGuardianName(linkedGuardian) : "");
    }
}
