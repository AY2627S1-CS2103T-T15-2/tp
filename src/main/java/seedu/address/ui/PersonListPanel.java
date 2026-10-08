package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
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
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
