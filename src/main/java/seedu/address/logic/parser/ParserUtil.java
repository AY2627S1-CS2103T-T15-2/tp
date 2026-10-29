package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
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
    // Level and Subject share this message; the parser alias is retained for existing callers.
    public static final String MESSAGE_RESERVED_REMOVE_VALUE = Level.MESSAGE_RESERVED_VALUE;
    public static final String MESSAGE_INVALID_NAME_QUOTES =
            "Invalid quoted name. Use n/NAME or n/\"NAME\", with quotation marks surrounding the entire name.";

    private static final String MESSAGE_MISSING_INDEX = "Missing contact index. Use: %1$s";
    private static final String MESSAGE_MULTIPLE_INDEXES = "Only one contact index is allowed. Use: %1$s";

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
     * Parses exactly one displayed contact index, showing {@code commandFormat} for missing or extra arguments.
     * Uses the same Java-whitespace boundaries and separators as {@link AddressBookParser}.
     *
     * @throws ParseException if the arguments are missing, contain extra tokens, or contain an invalid index.
     */
    public static Index parseSingleIndex(String args, String commandFormat) throws ParseException {
        String trimmedArgs = args.trim().strip();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_INDEX, commandFormat));
        }
        if (trimmedArgs.split("\\p{javaWhitespace}+").length > 1) {
            throw new ParseException(String.format(MESSAGE_MULTIPLE_INDEXES, commandFormat));
        }
        try {
            return parseIndex(trimmedArgs);
        } catch (ParseException exception) {
            throw new ParseException(Messages.MESSAGE_INVALID_CONTACT_INDEX, exception);
        }
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
     */
    private static String normalizeStudentDetail(String value) {
        return value.trim().replaceAll(" {2,}", " ");
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
