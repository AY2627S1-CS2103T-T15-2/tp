package seedu.address.logic;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ContactId;
import seedu.address.model.person.Person;

/** Contact data and display state captured before a successful deletion. */
public final class DeletionSnapshot {
    private final AddressBook addressBook;
    private final Predicate<Person> filter;
    private final List<ContactId> deletedIds;

    /**
     * Copies the pre-deletion data and records the deleted IDs in the order supplied.
     * The filter must be stable for the lifetime of this snapshot, as with the model's existing filters.
     * Callers must publish this snapshot to history only after the deletion saves successfully.
     */
    public DeletionSnapshot(ReadOnlyAddressBook addressBook, Predicate<Person> filter, List<ContactId> deletedIds) {
        requireAllNonNull(addressBook, filter, deletedIds);
        this.deletedIds = List.copyOf(deletedIds);
        if (this.deletedIds.isEmpty() || this.deletedIds.stream().distinct().count() != this.deletedIds.size()) {
            throw new IllegalArgumentException("Deleted contact IDs must be non-empty and unique");
        }
        if (!this.deletedIds.stream().allMatch(id -> addressBook.getPersonList().stream()
                .anyMatch(person -> person.getId().equals(id)))) {
            throw new IllegalArgumentException("Deleted contact IDs must exist in the pre-deletion data");
        }
        this.addressBook = new AddressBook(addressBook);
        this.filter = filter;
    }

    /** Returns a fresh data copy so callers cannot mutate the stored snapshot. */
    public AddressBook copyAddressBook() {
        return new AddressBook(addressBook);
    }

    /** Returns the display filter captured before deletion. */
    public Predicate<Person> getFilter() {
        return filter;
    }

    /** Returns the immutable list of original deleted contact IDs. */
    public List<ContactId> getDeletedIds() {
        return deletedIds;
    }
}
