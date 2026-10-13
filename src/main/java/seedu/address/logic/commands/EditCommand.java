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
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (editedPerson.isGuardianWithoutContactMethod()) {
            throw new CommandException(Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);
        }

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(),
                editedPerson.getRole()), editedPerson.getId());
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Optional<Phone> updatedPhone = editPersonDescriptor.isPhoneRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getPhone().or(personToEdit::getPhone);
        Optional<Email> updatedEmail = editPersonDescriptor.isEmailRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getEmail().or(personToEdit::getEmail);
        Role role = personToEdit.getRole();

        return new Person(personToEdit.getId(), updatedName, updatedPhone,
                updatedEmail, role, personToEdit.getGuardianId());
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
