package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_INVALID_CONTACT_INDEX =
            "The contact index must be a positive integer shown in the list.";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No contact exists at index %1$d.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns {@code count} followed by "contact" if {@code count} is 1, or "contacts" otherwise.
     * For example, 1 gives "1 contact" and 5 gives "5 contacts".
     */
    public static String formatContactCount(int count) {
        return count + (count == 1 ? " contact" : " contacts");
    }

    /**
     * Returns {@code count} followed by "former student" if {@code count} is 1,
     * or "former students" otherwise.
     * For example, 1 gives "1 former student" and 5 gives "5 former students".
     */
    public static String formatFormerStudentCount(int count) {
        return count + (count == 1 ? " former student" : " former students");
    }

}
