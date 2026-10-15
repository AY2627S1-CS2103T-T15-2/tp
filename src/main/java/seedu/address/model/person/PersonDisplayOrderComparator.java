package seedu.address.model.person;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Compares {@code Person}s by the order in which they are displayed in the contact list.
 * Contacts are ordered alphabetically by name, ignoring letter case and treating consecutive
 * spaces as a single space. Accented letters are ordered with their unaccented forms, for example
 * "Élise" is placed with "Elise" rather than after "Zoe". Names that differ only in accents are
 * ordered with the unaccented name first. Contacts with the same name are ordered by role, with
 * guardians before students.
 */
public class PersonDisplayOrderComparator implements Comparator<Person> {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    @Override
    public int compare(Person firstPerson, Person secondPerson) {
        int unaccentedNameComparison = toUnaccentedName(firstPerson).compareTo(toUnaccentedName(secondPerson));
        if (unaccentedNameComparison != 0) {
            return unaccentedNameComparison;
        }
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
     * Returns the name of {@code person} in lower case, with consecutive spaces reduced to a single space
     * and accents removed from its letters. For example, "Élise  Tan" gives "elise tan".
     */
    private static String toUnaccentedName(Person person) {
        String decomposedName = Normalizer.normalize(toNormalisedName(person), Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposedName).replaceAll("");
    }

    /**
     * Returns the rank of the role of {@code person}, where a guardian ranks before a student.
     */
    private static int toRoleRank(Person person) {
        return person.getRole() == Role.GUARDIAN ? 0 : 1;
    }
}
