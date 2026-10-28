package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_CONTACTS;
import static seedu.address.model.Model.PREDICATE_SHOW_FORMER_STUDENTS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Subject;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    /** Value, ignoring case, that removes an optional field when given in place of the field's value. */
    public static final String REMOVE_FIELD_VALUE = "NONE";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the contact identified "
            + "by the index number used in the displayed contact list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_LEVEL + "LEVEL] "
            + "[" + PREFIX_SUBJECT + "SUBJECT]\n"
            + "Use " + REMOVE_FIELD_VALUE + " as the value, such as " + PREFIX_PHONE + REMOVE_FIELD_VALUE
            + ", to remove a phone, email, level or subject. Only students can have a level and subject.\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com "
            + PREFIX_LEVEL + "Sec 4";

    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX [" + PREFIX_NAME + "NAME] ["
            + PREFIX_PHONE + "PHONE] [" + PREFIX_EMAIL + "EMAIL] [" + PREFIX_LEVEL + "LEVEL] ["
            + PREFIX_SUBJECT + "SUBJECT]";
    public static final String MESSAGE_INVALID_COMMAND = "Invalid edit command. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter. Use: " + COMMAND_FORMAT;
    public static final String MESSAGE_REPEATED_PARAMETER = "Each of " + PREFIX_NAME + ", " + PREFIX_PHONE
            + ", " + PREFIX_EMAIL + ", " + PREFIX_LEVEL + ", and " + PREFIX_SUBJECT
            + " may appear only once.";
    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Updated contact: %1$s [%2$s].";
    public static final String MESSAGE_NOT_EDITED = "At least one of " + PREFIX_NAME + ", " + PREFIX_PHONE
            + ", " + PREFIX_EMAIL + ", " + PREFIX_LEVEL + ", or " + PREFIX_SUBJECT + " must be provided.";
    public static final String MESSAGE_GUARDIAN_STUDENT_DETAILS =
            "Only students can have a level or subject. Remove lv/ and sub/ when editing a guardian.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This edit would create a duplicate contact.";
    public static final String MESSAGE_NO_CHANGE =
            "No changes made: the new values are the same as the current ones.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE = "Warning: Another contact with the same role and name "
            + "exists. This contact may be a duplicate, but it was updated.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_PHONE = "Warning: Another contact with the same role and "
            + "name, and a phone number matching after ignoring spaces and hyphens exists. "
            + "This contact may be a duplicate, but it was updated.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE_EMAIL = "Warning: Another contact with the same role and "
            + "name, and the same email address exists. This contact may be a duplicate, but it was updated.";

    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

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
        Person personToEdit = getPersonToEdit(model);
        verifyEditableFields(personToEdit);
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);
        if (editedPerson.equals(personToEdit)) {
            throw new CommandException(MESSAGE_NO_CHANGE);
        }
        verifyNoDuplicate(model, personToEdit, editedPerson);
        CommandResult result = createSuccessResult(model, editedPerson);

        applyEdit(model, personToEdit, editedPerson);
        return result;
    }

    /** Returns the contact at the displayed index before validating any requested changes. */
    private Person getPersonToEdit(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(String.format(Messages.MESSAGE_CONTACT_NOT_FOUND, index.getOneBased()));
        }
        return lastShownList.get(index.getZeroBased());
    }

    /** Checks role restrictions before constructing a replacement contact. */
    private void verifyEditableFields(Person personToEdit) throws CommandException {
        if (personToEdit.getRole() != Role.STUDENT && editPersonDescriptor.isStudentDetailEdited()) {
            throw new CommandException(MESSAGE_GUARDIAN_STUDENT_DETAILS);
        }
        if (personToEdit.getRole() == Role.GUARDIAN
                && getUpdatedPhone(personToEdit, editPersonDescriptor).isEmpty()
                && getUpdatedEmail(personToEdit, editPersonDescriptor).isEmpty()) {
            throw new CommandException(Person.MESSAGE_GUARDIAN_WITHOUT_CONTACT_METHOD);
        }
    }

    /** Allows changes that keep the contact's identity, including capitalization-only edits. */
    private static void verifyNoDuplicate(Model model, Person personToEdit, Person editedPerson)
            throws CommandException {
        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
    }

    /** Prepares feedback from the original contacts before publishing any changes to the candidate model. */
    private static CommandResult createSuccessResult(Model model, Person editedPerson) {
        List<Person> otherPersons = model.getAddressBook().getPersonList().stream()
                .filter(person -> !person.getId().equals(editedPerson.getId()))
                .toList();
        Optional<String> warning = getPossibleDuplicateWarning(editedPerson, otherPersons);

        String feedback = String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedPerson.getName(), editedPerson.getRole());
        return new CommandResult(warning.map(message -> feedback + "\n" + message).orElse(feedback),
                editedPerson.getId());
    }

    /** Replaces the contact and shows its active or former view before the candidate model is saved. */
    private static void applyEdit(Model model, Person personToEdit, Person editedPerson) {
        model.setPerson(personToEdit, editedPerson);
        // Show the list that the edited contact belongs to, so that it stays visible.
        model.updateFilteredPersonList(editedPerson.isFormerStudent()
                ? PREDICATE_SHOW_FORMER_STUDENTS
                : PREDICATE_SHOW_ACTIVE_CONTACTS);
        logger.fine("Prepared edit of contact " + editedPerson.getId() + ", pending save.");
    }

    /**
     * Returns a warning if {@code editedPerson} has the same role and name as any of {@code otherPersons},
     * using the same rules as {@code add}. A matching phone number is reported before a matching email address,
     * which is reported before a phone number or email address that both contacts lack.
     */
    private static Optional<String> getPossibleDuplicateWarning(Person editedPerson, List<Person> otherPersons) {
        return switch (DuplicateWarning.classify(editedPerson, otherPersons)) {
            case PHONE -> Optional.of(MESSAGE_POSSIBLE_DUPLICATE_PHONE);
            case EMAIL -> Optional.of(MESSAGE_POSSIBLE_DUPLICATE_EMAIL);
            case GENERAL -> Optional.of(MESSAGE_POSSIBLE_DUPLICATE);
            case NONE -> Optional.empty();
        };
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
        Optional<Level> updatedLevel = editPersonDescriptor.isLevelRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getLevel().or(personToEdit::getLevel);
        Optional<Subject> updatedSubject = editPersonDescriptor.isSubjectRemoved()
                ? Optional.empty()
                : editPersonDescriptor.getSubject().or(personToEdit::getSubject);

        return new Person(personToEdit.getId(), updatedName, updatedPhone, updatedEmail, role,
                personToEdit.getGuardianId(), personToEdit.getStatus(), updatedLevel, updatedSubject);
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
        private Level level;
        private Subject subject;
        private boolean isLevelRemoved;
        private boolean isSubjectRemoved;

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
            setLevel(toCopy.level);
            setSubject(toCopy.subject);
            isLevelRemoved = toCopy.isLevelRemoved;
            isSubjectRemoved = toCopy.isSubjectRemoved;
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email) || isPhoneRemoved || isEmailRemoved
                    || isStudentDetailEdited();
        }

        /**
         * Returns true if the level or subject is set or removed.
         */
        public boolean isStudentDetailEdited() {
            return CollectionUtil.isAnyNonNull(level, subject) || isLevelRemoved || isSubjectRemoved;
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

        public void setLevel(Level level) {
            this.level = level;
            isLevelRemoved = false;
        }

        /**
         * Marks the level to be removed from the edited person.
         */
        public void removeLevel() {
            level = null;
            isLevelRemoved = true;
        }

        public boolean isLevelRemoved() {
            return isLevelRemoved;
        }

        public Optional<Level> getLevel() {
            return Optional.ofNullable(level);
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
            isSubjectRemoved = false;
        }

        /**
         * Marks the subject to be removed from the edited person.
         */
        public void removeSubject() {
            subject = null;
            isSubjectRemoved = true;
        }

        public boolean isSubjectRemoved() {
            return isSubjectRemoved;
        }

        public Optional<Subject> getSubject() {
            return Optional.ofNullable(subject);
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
                    && isEmailRemoved == otherEditPersonDescriptor.isEmailRemoved
                    && Objects.equals(level, otherEditPersonDescriptor.level)
                    && Objects.equals(subject, otherEditPersonDescriptor.subject)
                    && isLevelRemoved == otherEditPersonDescriptor.isLevelRemoved
                    && isSubjectRemoved == otherEditPersonDescriptor.isSubjectRemoved;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("isPhoneRemoved", isPhoneRemoved)
                    .add("isEmailRemoved", isEmailRemoved)
                    .add("level", level)
                    .add("subject", subject)
                    .add("isLevelRemoved", isLevelRemoved)
                    .add("isSubjectRemoved", isSubjectRemoved)
                    .toString();
        }
    }
}
