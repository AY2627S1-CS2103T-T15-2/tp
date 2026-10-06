package seedu.address.model.person;

import java.util.Comparator;
import java.util.Locale;

/**
 * Compares {@code Person}s by the order in which they are displayed in the contact list.
 * Contacts are ordered alphabetically by name, ignoring letter case and treating consecutive
 * spaces as a single space.
 */
public class PersonDisplayOrderComparator implements Comparator<Person> {

    @Override
    public int compare(Person firstPerson, Person secondPerson) {
        return toNormalisedName(firstPerson).compareTo(toNormalisedName(secondPerson));
    }

    /**
     * Returns the name of {@code person} in lower case, with consecutive spaces reduced to a single space.
     */
    private static String toNormalisedName(Person person) {
        return person.getName().fullName.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
