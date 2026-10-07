package seedu.address.model.person;

import java.util.Comparator;
import java.util.Locale;

/**
 * Compares {@code Person}s by the order in which they are displayed in the contact list.
 * Contacts are ordered alphabetically by name, ignoring letter case and treating consecutive
 * spaces as a single space. Contacts with the same name are ordered by role, with guardians
 * before students.
 */
public class PersonDisplayOrderComparator implements Comparator<Person> {

    @Override
    public int compare(Person firstPerson, Person secondPerson) {
        int nameComparison = toNormalisedName(firstPerson).compareTo(toNormalisedName(secondPerson));
        if (nameComparison != 0) {
            return nameComparison;
        }
        return Integer.compare(toRoleRank(firstPerson), toRoleRank(secondPerson));
    }

    /**
     * Returns the name of {@code person} in lower case, with consecutive spaces reduced to a single space.
     */
    private static String toNormalisedName(Person person) {
        return person.getName().fullName.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the rank of the role of {@code person}, where a guardian ranks before a student.
     */
    private static int toRoleRank(Person person) {
        return person.getRole() == Role.GUARDIAN ? 0 : 1;
    }
}
