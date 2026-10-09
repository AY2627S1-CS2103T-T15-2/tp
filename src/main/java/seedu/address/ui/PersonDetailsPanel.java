package seedu.address.ui;

import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.model.person.Role;

/**
 * Displays the selected contact and its linked guardian's current contact details.
 */
public class PersonDetailsPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailsPanel.fxml";

    private final ObservableList<Person> persons;
    private final ReadOnlyObjectProperty<Person> selectedPerson;

    @FXML
    private Label name;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private VBox guardianDetails;
    @FXML
    private Label guardianName;
    @FXML
    private Label guardianPhone;
    @FXML
    private Label guardianEmail;

    /**
     * Creates a detail panel that follows the selected contact and changes to all saved contacts.
     */
    public PersonDetailsPanel(ObservableList<Person> persons, ReadOnlyObjectProperty<Person> selectedPerson) {
        super(FXML);
        this.persons = persons;
        this.selectedPerson = selectedPerson;
        selectedPerson.addListener((observable, previous, current) -> refreshDetails());
        persons.addListener((ListChangeListener<Person>) change -> refreshDetails());
        refreshDetails();
    }

    private void refreshDetails() {
        Person contact = Optional.ofNullable(selectedPerson.get())
                .flatMap(person -> PersonLookup.findById(persons, person.getId())).orElse(null);
        if (contact == null) {
            name.setText("Select a contact to view details.");
            role.setText("");
            phone.setText("");
            email.setText("");
            guardianDetails.setVisible(false);
            guardianDetails.setManaged(false);
            return;
        }

        name.setText(contact.getName().fullName);
        role.setText("Role: " + contact.getRole());
        phone.setText("Phone: " + formatContactMethod(contact.getPhone().value));
        email.setText("Email: " + contact.getEmail().map(Object::toString).orElse("Not provided"));
        boolean isStudent = contact.getRole() == Role.STUDENT;
        guardianDetails.setVisible(isStudent);
        guardianDetails.setManaged(isStudent);
        Person guardian = contact.getGuardianId().flatMap(id -> PersonLookup.findById(persons, id)).orElse(null);
        guardianName.setText(guardian == null ? "Guardian: None" : "Guardian: " + guardian.getName());
        guardianPhone.setText(guardian == null ? ""
                : "Guardian phone: " + formatContactMethod(guardian.getPhone().value));
        guardianEmail.setText(guardian == null ? ""
                : "Guardian email: " + guardian.getEmail().map(Object::toString).orElse("Not provided"));
    }

    /** Formats the missing-contact-method text required by the MVP. */
    static String formatContactMethod(String value) {
        return value.isBlank() ? "Not provided" : value;
    }
}
