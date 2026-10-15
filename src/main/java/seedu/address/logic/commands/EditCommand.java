package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    /** Value, ignoring case, that removes an optional field when given in place of the field's value. */
    public static final String REMOVE_FIELD_VALUE = "NONE";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL]\n"
            + "Use " + PREFIX_PHONE + REMOVE_FIELD_VALUE + " or " + PREFIX_EMAIL + REMOVE_FIELD_VALUE
            + " to remove the phone or email.\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX [" + PREFIX_NAME + "NAME] ["
            + PREFIX_PHONE + "PHONE] [" + PREFIX_EMAIL + "EMAIL]";
    public static final String MESSAGE_INVALID_COMMAND = "Invalid edit command. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_REPEATED_PARAMETER = "Each of " + PREFIX_NAME + ", " + PREFIX_PHONE
            + ", and " + PREFIX_EMAIL + " may appear only once.";
    public static final String MESSAGE_INVALID_INDEX =
            "The contact index must be a positive integer shown in the list.";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No contact exists at index %1$d.";
    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Updated contact: %1$s [%2$s].";
    public static final String MESSAGE_NOT_EDITED = "At least one of " + PREFIX_NAME + ", " + PREFIX_PHONE
            + ", or " + PREFIX_EMAIL + " must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This edit would create a duplicate contact.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE = "Warning: Another contact with the same role and name "
            + "exists. This contact may be a duplicate, but it was updated.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_PHONE = "Warning: Another contact with the same role and "
            + "name, and a phone number matching after ignoring spaces and hyphens exists. "
            + "This contact may be a duplicate, but it was updated.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_EMAIL = "Warning: Another contact with the same role and "
            + "name, and the same email address exists. This contact may be a duplicate, but it was updated.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(String.format(MESSAGE_CONTACT_NOT_FOUND, index.getOneBased()));
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        if (personToEdit.getRole() == Role.GUARDIAN
                && getUpdatedPhone(personToEdit, editPersonDescriptor).isEmpty()
                && getUpdatedEmail(personToEdit, editPersonDescriptor).isEmpty()) {
            throw new CommandException(Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);
        }
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        List<Person> otherPersons = model.getAddressBook().getPersonList().stream()
                .filter(person -> !person.getId().equals(editedPerson.getId()))
                .toList();
        Optional<String> warning = getPossibleDuplicateWarning(editedPerson, otherPersons);

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String feedback = String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(), editedPerson.getRole());
        return new CommandResult(warning.map(message -> feedback + "\n" + message).orElse(feedback),
                editedPerson.getId());
    }

    /**
     * Returns a warning if {@code editedPerson} has the same role and name as any of {@code otherPersons},
     * using the same rules as {@code add}. A matching phone number is reported before a matching email address,
     * which is reported before a phone number or email address that both contacts lack.
     */
    private static Optional<String> getPossibleDuplicateWarning(Person editedPerson, List<Person> otherPersons) {
        List<Person> sameNameAndRole = otherPersons.stream()
                .filter(editedPerson::hasSameNameAndRole)
                .toList();
        if (sameNameAndRole.stream().anyMatch(editedPerson::hasSamePhoneIgnoringFormatting)) {
            return Optional.of(MESSAGE_POSSIBLE_DUPLICATE_PHONE);
        }
        if (sameNameAndRole.stream().anyMatch(editedPerson::hasSameEmailAddress)) {
            return Optional.of(MESSAGE_POSSIBLE_DUPLICATE_EMAIL);
        }
        if (sameNameAndRole.stream().anyMatch(person -> hasSharedEmptyContactField(editedPerson, person))) {
            return Optional.of(MESSAGE_POSSIBLE_DUPLICATE);
        }
        return Optional.empty();
    }

    private static boolean hasSharedEmptyContactField(Person person, Person otherPerson) {
        return person.getPhone().isEmpty() && otherPerson.getPhone().isEmpty()
                || person.getEmail().isEmpty() && otherPerson.getEmail().isEmpty();
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Optional<Phone> updatedPhone = getUpdatedPhone(personToEdit, editPersonDescriptor);
        Optional<Email> updatedEmail = getUpdatedEmail(personToEdit, editPersonDescriptor);
        Role role = personToEdit.getRole();

        return new Person(personToEdit.getId(), updatedName, updatedPhone,
                updatedEmail, role, personToEdit.getGuardianId());
    }

    private static Optional<Phone> getUpdatedPhone(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        return editPersonDescriptor.isPhoneRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getPhone().or(personToEdit::getPhone);
    }

    private static Optional<Email> getUpdatedEmail(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        return editPersonDescriptor.isEmailRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getEmail().or(personToEdit::getEmail);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private boolean isPhoneRemoved;
        private boolean isEmailRemoved;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            isPhoneRemoved = toCopy.isPhoneRemoved;
            isEmailRemoved = toCopy.isEmailRemoved;
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email) || isPhoneRemoved || isEmailRemoved;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
            isPhoneRemoved = false;
        }

        /**
         * Marks the phone to be removed from the edited person.
         */
        public void removePhone() {
            phone = null;
            isPhoneRemoved = true;
        }

        public boolean isPhoneRemoved() {
            return isPhoneRemoved;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
            isEmailRemoved = false;
        }

        /**
         * Marks the email to be removed from the edited person.
         */
        public void removeEmail() {
            email = null;
            isEmailRemoved = true;
        }

        public boolean isEmailRemoved() {
            return isEmailRemoved;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && isPhoneRemoved == otherEditPersonDescriptor.isPhoneRemoved
                    && isEmailRemoved == otherEditPersonDescriptor.isEmailRemoved;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("isPhoneRemoved", isPhoneRemoved)
                    .add("isEmailRemoved", isEmailRemoved)
                    .toString();
        }
    }
}
