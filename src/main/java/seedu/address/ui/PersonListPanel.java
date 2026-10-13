package seedu.address.ui;

import java.util.Optional;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonLookup;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final ObservableList<Person> allPersons;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} that displays {@code displayedPersons}.
     * Linked guardians are looked up in {@code allPersons}, so that a student's guardian is shown
     * even when the guardian is not in the displayed list. The cards are redrawn whenever any contact
     * changes, so that a student's card shows the current name of its guardian.
     */
    public PersonListPanel(ObservableList<Person> displayedPersons, ObservableList<Person> allPersons) {
        super(FXML);
        this.allPersons = allPersons;
        personListView.setItems(displayedPersons);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        allPersons.addListener((ListChangeListener<Person>) change -> personListView.refresh());
    }

    /**
     * Returns the selected contact property for the detail panel.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    public Person getSelectedPerson() {
        return personListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Selects and scrolls to the displayed contact with the given ID.
     */
    public void selectPerson(ContactId id) {
        for (int index = 0; index < personListView.getItems().size(); index++) {
            if (personListView.getItems().get(index).getId().equals(id)) {
                personListView.getSelectionModel().select(index);
                personListView.scrollTo(index);
                return;
            }
        }
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Returns the guardian linked to {@code person}, or an empty result if there is none.
     */
    private Optional<Person> findLinkedGuardian(Person person) {
        return person.getGuardianId().flatMap(guardianId -> PersonLookup.findById(allPersons, guardianId));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        /**
         * Creates a cell that fits the width of the list, so that long names wrap instead of
         * widening the list.
         */
        PersonListViewCell() {
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, findLinkedGuardian(person)).getRoot());
            }
        }
    }

}
