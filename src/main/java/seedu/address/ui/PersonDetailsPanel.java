package seedu.address.ui;

import static seedu.address.ui.ContactDisplayFormatter.formatContactMethod;
import static seedu.address.ui.ContactDisplayFormatter.formatRoleWithStatus;
import static seedu.address.ui.ContactDisplayFormatter.formatStudentDetail;

import java.util.Optional;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;
import seedu.address.model.person.Role;

/**
 * Displays the selected contact and its linked guardian's current contact details.
 */
public class PersonDetailsPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailsPanel.fxml";
    private static final Logger logger = LogsCenter.getLogger(PersonDetailsPanel.class);

    private final ObservableList<Person> persons;
    private final ReadOnlyObjectProperty<Person> selectedPerson;

    @FXML
    private Label name;
    @FXML
    private Label roleBadge;
    @FXML
    private Label formerBadge;
    @FXML
    private VBox contactDetails;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private VBox studentDetails;
    @FXML
    private Label level;
    @FXML
    private Label subject;
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
            logger.fine("No contact selected, so the details panel shows no details.");
            name.setText("Select a contact to view details.");
            RoleBadges.hide(roleBadge, formerBadge);
            role.setText("");
            phone.setText("");
            email.setText("");
            UiUtil.setShown(contactDetails, false);
            UiUtil.setShown(studentDetails, false);
            UiUtil.setShown(guardianDetails, false);
            return;
        }

        name.setText(contact.getName().fullName);
        RoleBadges.show(roleBadge, formerBadge, contact);
        UiUtil.setShown(contactDetails, true);
        role.setText("Role: " + formatRoleWithStatus(contact));
        phone.setText("Phone: " + formatContactMethod(contact.getPhone()));
        email.setText("Email: " + formatContactMethod(contact.getEmail()));
        boolean isStudent = contact.getRole() == Role.STUDENT;
        UiUtil.setShown(studentDetails, isStudent);
        level.setText("Level: " + formatStudentDetail(contact.getLevel()));
        subject.setText("Subject: " + formatStudentDetail(contact.getSubject()));
        UiUtil.setShown(guardianDetails, isStudent);
        Person guardian = contact.getGuardianId().flatMap(id -> PersonLookup.findById(persons, id)).orElse(null);
        guardianName.setText(guardian == null ? "Guardian: None" : "Guardian: " + guardian.getName());
        guardianPhone.setText(guardian == null ? ""
                : "Guardian phone: " + formatContactMethod(guardian.getPhone()));
        guardianEmail.setText(guardian == null ? ""
                : "Guardian email: " + formatContactMethod(guardian.getEmail()));
        UiUtil.setShown(guardianPhone, guardian != null);
        UiUtil.setShown(guardianEmail, guardian != null);
        logger.fine("Showing details of contact " + contact.getId()
                + (guardian == null ? " without a guardian" : " with guardian " + guardian.getId()));
    }
}
