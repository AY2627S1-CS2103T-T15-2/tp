package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_nullContact_rejectsInvalidData() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(Collections.singletonList(null));
        assertThrows(IllegalValueException.class, "Contacts must not contain null entries.", data::toModelType);
    }

    @Test
    public void deserialize_repeatedGuardianField_rejectsAmbiguousRelationship() {
        String json = "{\"persons\":[{\"guardianId\":\"first\",\"guardianId\":\"second\"}]}";
        assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class));
    }


    @Test
    public void toModelType_relationship_roundTripsBothContactIds() throws Exception {
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withGuardianId(guardian.getId()).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(student), new JsonAdaptedPerson(guardian)));
        String json = JsonUtil.toJsonString(data);
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(student.getId(), loaded.getPersonList().get(0).getId());
        assertEquals(student.getGuardianId(), loaded.getPersonList().get(0).getGuardianId());
        assertEquals(guardian.getId(), loaded.getPersonList().get(1).getId());
    }

    @Test
    public void toModelType_missingGuardian_rejectsOrphanLink() {
        Person student = new PersonBuilder().withGuardianId(ContactId.generate()).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(student)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_INVALID_RELATIONSHIP,
                data::toModelType);
    }

    @Test
    public void toModelType_guardianIsStudent_rejectsWrongRole() {
        Person otherStudent = new PersonBuilder().withName("Bea Tan").build();
        Person student = new PersonBuilder().withGuardianId(otherStudent.getId()).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(student), new JsonAdaptedPerson(otherStudent)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_INVALID_RELATIONSHIP,
                data::toModelType);
    }


    @Test
    public void toModelType_duplicateIds_rejectsDifferentContactsWithSameId() {
        JsonAdaptedPerson first = new JsonAdaptedPerson(TypicalPersons.ALICE);
        JsonAdaptedPerson second = new JsonAdaptedPerson(new PersonBuilder(TypicalPersons.BOB)
                .withId(TypicalPersons.ALICE.getId()).build());
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(first, second));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_ID, data::toModelType);
    }


    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }


    @Test
    public void toModelType_optionalEmail_preservesRolesAndRelationships() throws Exception {
        Person guardian = new PersonBuilder().withoutEmail().withRole("guardian").build();
        Person student = new PersonBuilder().withoutEmail().withGuardianId(guardian.getId()).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(student), new JsonAdaptedPerson(guardian)));
        AddressBook loaded = JsonUtil.fromJsonString(JsonUtil.toJsonString(data),
                JsonSerializableAddressBook.class).toModelType();
        assertEquals(List.of(student, guardian), loaded.getPersonList());
        assertEquals(guardian.getId(), loaded.getPersonList().get(1).getId());
    }

    @Test
    public void toModelType_caseVariantOrAbsentEmailDuplicates_rejectsDuplicates() {
        Person first = new PersonBuilder().withEmail("Alex@Example.COM").build();
        Person second = new PersonBuilder(first).withName("AMY BEE").withEmail("alex@example.com")
                .withId(ContactId.generate()).build();
        assertDuplicateContactsRejected(first, second);
        assertDuplicateContactsRejected(new PersonBuilder(first).withoutEmail().build(),
                new PersonBuilder(second).withoutEmail().build());
    }

    private void assertDuplicateContactsRejected(Person first, Person second) {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }
}
