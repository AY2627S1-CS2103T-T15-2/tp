package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_CONTACTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsSubstringPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void getFilteredPersonList_personsAddedOutOfOrder_sortedByName() {
        modelManager.addPerson(CARL);
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        assertEquals(List.of(ALICE, BENSON, CARL), modelManager.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_filtered_remainsSorted() {
        modelManager.addPerson(CARL);
        modelManager.addPerson(BENSON);
        modelManager.addPerson(ALICE);
        modelManager.updateFilteredPersonList(person -> !person.equals(BENSON));
        assertEquals(List.of(ALICE, CARL), modelManager.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_personRenamed_resorted() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe Pauline").build();
        modelManager.setPerson(ALICE, renamedAlice);
        assertEquals(List.of(BENSON, renamedAlice), modelManager.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_sameNameDifferentRole_guardianBeforeStudent() {
        Person student = new PersonBuilder().withName("Alex Tan").withRole("student")
                .withPhone("91234567").build();
        Person guardian = new PersonBuilder().withName("Alex Tan").withRole("guardian")
                .withPhone("98765432").build();
        modelManager.addPerson(student);
        modelManager.addPerson(guardian);
        assertEquals(List.of(guardian, student), modelManager.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_sigmaVariants_guardianFirstAfterRenameAndFilter() {
        Person student = new PersonBuilder().withName("\u039f\u03a3").withRole("student").build();
        Person guardian = new PersonBuilder().withName("\u039f\u03c3").withRole("guardian").build();
        modelManager.addPerson(student);
        modelManager.addPerson(guardian);
        assertEquals(List.of(guardian.getId(), student.getId()), modelManager.getFilteredPersonList().stream()
                .map(Person::getId).toList());

        Person renamedStudent = new PersonBuilder(student).withName("\u03bf\u03c2").build();
        modelManager.setPerson(student, renamedStudent);
        modelManager.updateFilteredPersonList(new NameContainsSubstringPredicate("\u039f\u03a3"));
        assertEquals(List.of(guardian.getId(), student.getId()), modelManager.getFilteredPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void getFilteredPersonList_formerStudent_hiddenByDefault() {
        Person former = new PersonBuilder(BENSON).withStatus(StudentStatus.FORMER).build();
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(former).build(),
                new UserPrefs());
        assertEquals(PREDICATE_SHOW_ACTIVE_CONTACTS, modelManager.getPersonFilter());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void addPerson_formerStudentsShown_showsActiveContacts() {
        Person former = new PersonBuilder(BENSON).withStatus(StudentStatus.FORMER).build();
        modelManager.addPerson(former);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        modelManager.addPerson(ALICE);
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_formerStudentPredicate_showsFormerStudentsOnly() {
        Person former = new PersonBuilder(BENSON).withStatus(StudentStatus.FORMER).build();
        modelManager.addPerson(ALICE);
        modelManager.addPerson(former);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_FORMER_STUDENTS);
        assertEquals(List.of(former), modelManager.getFilteredPersonList());
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(ALICE, former), modelManager.getFilteredPersonList());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredPersonList(new NameContainsSubstringPredicate(ALICE.getName().fullName));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_CONTACTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
