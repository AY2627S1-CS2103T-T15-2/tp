package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.person.Person;

/**
 * Classifies possible duplicates independently of command-specific feedback.
 */
enum DuplicateWarning {
    NONE, PHONE, EMAIL, GENERAL;

    /**
     * Returns the highest-priority warning across the existing contacts: phone, email, then general.
     * The caller must exclude the contact being edited from {@code otherPersons}.
     * Exact duplicate rejection is handled separately by the command.
     */
    static DuplicateWarning classify(Person candidate, List<Person> otherPersons) {
        requireNonNull(candidate);
        requireNonNull(otherPersons);
        List<Person> personsWithSameNameAndRole = otherPersons.stream()
                .filter(candidate::hasSameNameAndRole)
                .toList();
        if (personsWithSameNameAndRole.stream().anyMatch(candidate::hasSamePhoneIgnoringFormatting)) {
            return PHONE;
        }
        if (personsWithSameNameAndRole.stream().anyMatch(candidate::hasSameEmailAddress)) {
            return EMAIL;
        }
        if (personsWithSameNameAndRole.stream().anyMatch(person -> hasSharedEmptyContactField(candidate, person))) {
            return GENERAL;
        }
        return NONE;
    }

    private static boolean hasSharedEmptyContactField(Person person, Person otherPerson) {
        return person.getPhone().isEmpty() && otherPerson.getPhone().isEmpty()
                || person.getEmail().isEmpty() && otherPerson.getEmail().isEmpty();
    }
}
