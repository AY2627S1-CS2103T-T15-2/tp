package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

public class JsonAdaptedPersonTest {

    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_ROLE = "teacher";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().orElseThrow().toString();
    private static final String VALID_ROLE = BENSON.getRole().name();

    @Test
    public void toModelType_invalidGuardianId_throwsIllegalValueException() {
        JsonAdaptedPerson invalid = new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ROLE, "invalid-id");
        assertThrows(IllegalValueException.class, ContactId.MESSAGE_CONSTRAINTS, invalid::toModelType);
    }

    @Test
    public void toModelType_guardianWithGuardianId_rejectsInvalidRelationshipOwner() {
        JsonAdaptedPerson invalid = new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, VALID_EMAIL,
                "GUARDIAN", ContactId.generate().toString());
        assertThrows(IllegalValueException.class, Person.MESSAGE_INVALID_GUARDIAN_OWNER, invalid::toModelType);
    }

    @Test
    public void toModelType_savedId_preservesId() throws Exception {
        assertEquals(BENSON.getId(), new JsonAdaptedPerson(BENSON).toModelType().getId());
    }

    @Test
    public void toModelType_legacyContact_generatesId() throws Exception {
        JsonAdaptedPerson legacy = new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ROLE, null);
        assertTrue(ContactId.isValidId(legacy.toModelType().getId().toString()));
    }

    @Test
    public void toModelType_invalidId_throwsIllegalValueException() {
        JsonAdaptedPerson invalid = new JsonAdaptedPerson("1-1-1-1-1", VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ROLE, null);
        assertThrows(IllegalValueException.class, ContactId.MESSAGE_CONSTRAINTS, invalid::toModelType);
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ROLE,
                        null);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, null, VALID_PHONE, VALID_EMAIL,
                VALID_ROLE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ROLE,
                        null);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_NAME, null, VALID_EMAIL,
                VALID_ROLE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ROLE,
                        null);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_returnsPersonWithoutEmail() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, null,
                VALID_ROLE, null);
        assertTrue(person.toModelType().getEmail().isEmpty());
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ROLE,
                        null);
        assertThrows(IllegalValueException.class, Role.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullRole_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                        null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Role.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }


    @Test
    public void toModelType_emptyEmail_rejectsInvalidData() {
        for (String value : List.of("", " ", "alex@example", "alex@example..com")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_NAME, VALID_PHONE, value,
                    VALID_ADDRESS, VALID_ROLE, VALID_TAGS, null);
            assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }
}
