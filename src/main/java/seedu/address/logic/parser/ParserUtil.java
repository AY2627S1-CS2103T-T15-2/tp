package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Subject;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_RESERVED_REMOVE_VALUE =
            "NONE cannot be used as a level or subject. Use lv/NONE or sub/NONE in edit to remove it.";
    public static final String MESSAGE_INVALID_NAME_QUOTES =
            "Invalid quoted name. Use n/NAME or n/\"NAME\", with quotation marks surrounding the entire name.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespace is trimmed, including inside optional enclosing quotation marks.
     * The quotation marks are removed before the name is validated and stored.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim().strip();
        boolean isQuoted = trimmedName.startsWith("\"");

        if (isQuoted) {
            if (trimmedName.length() < 2 || !trimmedName.endsWith("\"")) {
                throw new ParseException(MESSAGE_INVALID_NAME_QUOTES);
            }
            trimmedName = trimmedName.substring(1, trimmedName.length() - 1).trim().strip();
            if (trimmedName.contains("\"")) {
                throw new ParseException(MESSAGE_INVALID_NAME_QUOTES);
            }
        } else {
            if (trimmedName.contains("\"")) {
                throw new ParseException(MESSAGE_INVALID_NAME_QUOTES);
            }
        }

        String normalizedName = trimmedName.replaceAll(" {2,}", " ");
        String nameValidationError = Name.getValidationErrorMessage(normalizedName);
        if (nameValidationError != null) {
            throw new ParseException(nameValidationError);
        }
        return new Name(normalizedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (trimmedPhone.isEmpty()) {
            throw new ParseException(Phone.MESSAGE_EMPTY);
        }
        if (Phone.hasInvalidCharacters(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_INVALID_CHARACTERS);
        }
        if (Phone.getDigitCount(trimmedPhone) < Phone.MINIMUM_DIGIT_COUNT) {
            throw new ParseException(Phone.MESSAGE_TOO_FEW_DIGITS);
        }
        if (trimmedPhone.codePointCount(0, trimmedPhone.length()) > Phone.MAX_LENGTH) {
            throw new ParseException(Phone.MESSAGE_TOO_LONG);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (trimmedEmail.isEmpty()) {
            throw new ParseException(Email.MESSAGE_EMPTY);
        }
        String emailValidationError = Email.getValidationErrorMessage(trimmedEmail);
        if (emailValidationError != null) {
            throw new ParseException(emailValidationError);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String level} into a {@code Level}.
     * Leading and trailing whitespaces will be trimmed, and repeated spaces will be collapsed into one.
     *
     * @throws ParseException if the given {@code level} is invalid or is the reserved value NONE.
     */
    public static Level parseLevel(String level) throws ParseException {
        requireNonNull(level);
        String normalizedLevel = normalizeStudentDetail(level);
        if (normalizedLevel.isEmpty()) {
            throw new ParseException(Level.MESSAGE_EMPTY);
        }
        String validationError = Level.getValidationErrorMessage(normalizedLevel);
        if (validationError != null) {
            throw new ParseException(validationError);
        }
        return new Level(normalizedLevel);
    }

    /**
     * Parses a {@code String subject} into a {@code Subject}.
     * Leading and trailing whitespaces will be trimmed, and repeated spaces will be collapsed into one.
     *
     * @throws ParseException if the given {@code subject} is invalid or is the reserved value NONE.
     */
    public static Subject parseSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String subjectValue = subject.trim();
        if (subjectValue.startsWith("\"")) {
            if (subjectValue.length() < 2 || !subjectValue.endsWith("\"")) {
                throw new ParseException("Invalid quoted subject. Surround the entire subject with quotation marks.");
            }
            subjectValue = subjectValue.substring(1, subjectValue.length() - 1);
        }
        String normalizedSubject = normalizeStudentDetail(subjectValue);
        if (normalizedSubject.isEmpty()) {
            throw new ParseException(Subject.MESSAGE_EMPTY);
        }
        String validationError = Subject.getValidationErrorMessage(normalizedSubject);
        if (validationError != null) {
            throw new ParseException(validationError);
        }
        return new Subject(normalizedSubject);
    }

    /** Returns text outside a leading quoted value after the tokenizer has checked its closing quote. */
    public static String textOutsideQuotedValue(String value) {
        String trimmedValue = value.trim();
        if (!trimmedValue.startsWith("\"")) {
            return value;
        }
        return " " + trimmedValue.substring(trimmedValue.indexOf('"', 1) + 1);
    }

    /**
     * Returns {@code value} trimmed with repeated spaces collapsed into one.
     *
     * @throws ParseException if {@code value} is NONE, ignoring case, which {@code edit} uses to remove
     *     a field.
     */
    private static String normalizeStudentDetail(String value) throws ParseException {
        String normalizedValue = value.trim().replaceAll(" {2,}", " ");
        if (normalizedValue.equalsIgnoreCase(EditCommand.REMOVE_FIELD_VALUE)) {
            throw new ParseException(MESSAGE_RESERVED_REMOVE_VALUE);
        }
        return normalizedValue;
    }

    /**
     * Parses a {@code String role} into a {@code Role}.
     * Leading and trailing whitespaces will be trimmed, and letter case is ignored.
     *
     * @throws ParseException if the given {@code role} is invalid.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        String trimmedRole = role.trim();
        if (!Role.isValidRole(trimmedRole)) {
            throw new ParseException(Role.MESSAGE_CONSTRAINTS);
        }
        return Role.fromString(trimmedRole);
    }
}
