package seedu.address.model.person;

import java.text.Normalizer;
import java.util.Comparator;

/**
 * Compares {@code Person}s by the order in which they are displayed in the contact list.
 * Contacts are ordered alphabetically by name, ignoring letter case and treating consecutive
 * spaces as a single space. Accented letters are ordered with their unaccented forms, for example
 * "Élise" is placed with "Elise" and "Søren" with "Soren", rather than after "Zoe". Names that
 * differ only in accents are ordered with the unaccented name first. Contacts with the same name are
 * ordered by role, with guardians before students.
 */
public class PersonDisplayOrderComparator implements Comparator<Person> {

    @Override
    public int compare(Person firstPerson, Person secondPerson) {
        int unaccentedNameComparison = toUnaccentedName(firstPerson)
                .compareToIgnoreCase(toUnaccentedName(secondPerson));
        if (unaccentedNameComparison != 0) {
            return unaccentedNameComparison;
        }
        int nameComparison = toNormalizedName(firstPerson).compareToIgnoreCase(toNormalizedName(secondPerson));
        if (nameComparison != 0) {
            return nameComparison;
        }
        return Integer.compare(toRoleRank(firstPerson), toRoleRank(secondPerson));
    }

    /**
     * Returns the name of {@code person} in canonical Unicode form, with consecutive spaces reduced to a
     * single space and curly apostrophes replaced by straight apostrophes. Case is ignored during comparison;
     * lowercasing whole words would treat the ordinary and final Greek sigma differently.
     */
    private static String toNormalizedName(Person person) {
        String name = person.getName().fullName.replaceAll("\\s+", " ");
        return Normalizer.normalize(Name.replaceCurlyApostrophes(name), Normalizer.Form.NFD);
    }

    /**
     * Returns the name of {@code person} as given by {@link #toNormalizedName(Person)}, with accents removed
     * as described in {@link Name#removeAccents(String)}. For example, "Élise  O’Brien" gives
     * "Elise O'Brien".
     */
    private static String toUnaccentedName(Person person) {
        return Name.removeAccents(toNormalizedName(person));
    }

    /**
     * Returns the rank of the role of {@code person}, where a guardian ranks before a student.
     */
    private static int toRoleRank(Person person) {
        return switch (person.getRole()) {
            case GUARDIAN -> 0;
            case STUDENT -> 1;
        };
    }
}
