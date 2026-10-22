package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentStatus;
import seedu.address.testutil.PersonBuilder;

public class DeletionHistoryTest {
    private final Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
    private final Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId())
            .withStatus(StudentStatus.FORMER).withLevel("Sec 3").withSubject("Math").build();

    private AddressBook createBook() {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(guardian, student));
        return book;
    }

    private DeletionSnapshot snapshot() {
        return new DeletionSnapshot(createBook(), Model.PREDICATE_SHOW_FORMER_STUDENTS, List.of(guardian.getId()));
    }

    @Test
    public void snapshot_sourceAndReturnedCopyMutated_preservesOriginalDataAndLinks() {
        AddressBook original = createBook();
        List<ContactId> deletedIds = new ArrayList<>(List.of(guardian.getId(), student.getId()));
        DeletionSnapshot snapshot = new DeletionSnapshot(original, Model.PREDICATE_SHOW_FORMER_STUDENTS, deletedIds);
        original.removePerson(guardian);
        deletedIds.clear();
        AddressBook returnedCopy = snapshot.copyAddressBook();
        returnedCopy.removePerson(student);

        AddressBook restored = snapshot.copyAddressBook();
        assertEquals(List.of(guardian.getId(), student.getId()), restored.getPersonList().stream()
                .map(Person::getId).toList());
        assertEquals(List.of(guardian, student), List.copyOf(restored.getPersonList()));
        assertEquals(student.getGuardianId(), restored.getPersonList().get(1).getGuardianId());
        assertEquals(List.of(guardian.getId(), student.getId()), snapshot.getDeletedIds());
        assertSame(Model.PREDICATE_SHOW_FORMER_STUDENTS, snapshot.getFilter());
        assertTrue(snapshot.getFilter().test(restored.getPersonList().get(1)));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getDeletedIds().clear());
    }

    @Test
    public void history_recordReadReplaceAndClear_keepsOnlyLatestSnapshot() {
        DeletionHistory history = new DeletionHistory();
        assertTrue(history.getLatest().isEmpty());
        DeletionSnapshot first = snapshot();
        history.record(first);
        assertSame(first, history.getLatest().orElseThrow());
        assertSame(first, history.getLatest().orElseThrow());
        DeletionSnapshot second = snapshot();
        history.record(second);
        assertSame(second, history.getLatest().orElseThrow());
        assertThrows(NullPointerException.class, () -> history.record(null));
        assertSame(second, history.getLatest().orElseThrow());
        history.clear();
        history.clear();
        assertTrue(history.getLatest().isEmpty());
        assertTrue(new DeletionHistory().getLatest().isEmpty());
    }

    @Test
    public void snapshot_invalidInputs_rejected() {
        assertThrows(NullPointerException.class, () -> new DeletionSnapshot(null,
                Model.PREDICATE_SHOW_ALL_PERSONS, List.of(student.getId())));
        assertThrows(NullPointerException.class, () -> new DeletionSnapshot(createBook(), null,
                List.of(student.getId())));
        assertThrows(NullPointerException.class, () -> new DeletionSnapshot(createBook(),
                Model.PREDICATE_SHOW_ALL_PERSONS, null));
        assertThrows(IllegalArgumentException.class, () -> new DeletionSnapshot(createBook(),
                Model.PREDICATE_SHOW_ALL_PERSONS, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new DeletionSnapshot(createBook(),
                Model.PREDICATE_SHOW_ALL_PERSONS, List.of(student.getId(), student.getId())));
        assertThrows(IllegalArgumentException.class, () -> new DeletionSnapshot(createBook(),
                Model.PREDICATE_SHOW_ALL_PERSONS, List.of(ContactId.generate())));
    }
}
