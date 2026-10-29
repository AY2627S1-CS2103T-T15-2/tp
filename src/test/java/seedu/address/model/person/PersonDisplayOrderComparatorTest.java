package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonDisplayOrderComparatorTest {

    private final PersonDisplayOrderComparator comparator = new PersonDisplayOrderComparator();

    @Test
    public void compare_differentNames_alphabeticalOrder() {
        Person alex = new PersonBuilder().withName("Alex Tan").build();
        Person bernice = new PersonBuilder().withName("Bernice Yu").build();

        assertTrue(comparator.compare(alex, bernice) < 0);
        assertTrue(comparator.compare(bernice, alex) > 0);
    }

    @Test
    public void compare_namesWithDifferentCase_caseIgnored() {
        Person lowerCaseAlex = new PersonBuilder().withName("alex tan").build();
        Person upperCaseBernice = new PersonBuilder().withName("BERNICE YU").build();
        Person mixedCaseAlex = new PersonBuilder().withName("AlEx TaN").build();

        // lower case name is not placed after upper case name
        assertTrue(comparator.compare(lowerCaseAlex, upperCaseBernice) < 0);

        // same name with different case -> equal
        assertEquals(0, comparator.compare(lowerCaseAlex, mixedCaseAlex));
    }

    @Test
    public void compare_namesWithConsecutiveSpaces_spacesTreatedAsOne() {
        Person alZed = new PersonBuilder().withName("Al  Zed").build();
        Person alBob = new PersonBuilder().withName("Al Bob").build();
        Person alZedSingleSpace = new PersonBuilder().withName("Al Zed").build();

        assertTrue(comparator.compare(alBob, alZed) < 0);
        assertEquals(0, comparator.compare(alZed, alZedSingleSpace));
    }

    @Test
    public void compare_accentedName_orderedWithUnaccentedLetter() {
        Person bernice = new PersonBuilder().withName("Bernice Yu").build();
        Person elise = new PersonBuilder().withName("Élise Tan").build();
        Person zoe = new PersonBuilder().withName("Zoe Lim").build();

        assertTrue(comparator.compare(bernice, elise) < 0);
        assertTrue(comparator.compare(elise, zoe) < 0);
        assertTrue(comparator.compare(zoe, elise) > 0);
    }

    @Test
    public void compare_namesDifferingOnlyInAccents_unaccentedNameFirst() {
        Person elise = new PersonBuilder().withName("Elise Tan").withRole("student").build();
        Person accentedElise = new PersonBuilder().withName("Élise Tan").withRole("guardian").build();

        // name takes precedence over role
        assertTrue(comparator.compare(elise, accentedElise) < 0);
        assertTrue(comparator.compare(accentedElise, elise) > 0);
    }

    @Test
    public void compare_letterWithStrokeOrLigature_orderedWithBaseLetter() {
        Person sophia = new PersonBuilder().withName("Sophia Lim").build();
        Person soren = new PersonBuilder().withName("S\u00f8ren Tan").build();
        Person sun = new PersonBuilder().withName("Sun Li").build();
        Person lukasz = new PersonBuilder().withName("\u0141ukasz Ng").build();
        Person lydia = new PersonBuilder().withName("Lydia Ong").build();
        Person zoe = new PersonBuilder().withName("Zoe Lim").build();

        assertTrue(comparator.compare(sophia, soren) < 0);
        assertTrue(comparator.compare(soren, sun) < 0);
        assertTrue(comparator.compare(lukasz, lydia) < 0);
        assertTrue(comparator.compare(lukasz, zoe) < 0);
    }

    @Test
    public void compare_nameWithStrokeAndPlainName_plainNameFirst() {
        Person soren = new PersonBuilder().withName("Soren Tan").withRole("student").build();
        Person strokedSoren = new PersonBuilder().withName("S\u00f8ren Tan").withRole("guardian").build();

        assertTrue(comparator.compare(soren, strokedSoren) < 0);
        assertTrue(comparator.compare(strokedSoren, soren) > 0);
    }

    @Test
    public void compare_nameIsPrefixOfLongerName_shorterNameFirst() {
        Person alexTan = new PersonBuilder().withName("Alex Tan").build();
        Person alexa = new PersonBuilder().withName("Alexa Lim").build();

        assertTrue(comparator.compare(alexTan, alexa) < 0);
    }

    @Test
    public void compare_curlyApostrophe_orderedAsStraightApostrophe() {
        Person curlyObrien = new PersonBuilder().withName("O’Brien Tan").build();
        Person straightObrien = new PersonBuilder().withName("O'Brien Tan").build();
        Person oliver = new PersonBuilder().withName("Oliver Lim").build();
        Person zoe = new PersonBuilder().withName("Zoe Lim").build();

        assertTrue(comparator.compare(curlyObrien, oliver) < 0);
        assertTrue(comparator.compare(curlyObrien, zoe) < 0);
        assertEquals(0, comparator.compare(curlyObrien, straightObrien));
    }

    @Test
    public void compare_sameNameDifferentRole_guardianBeforeStudent() {
        Person guardian = new PersonBuilder().withName("Alex Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withRole("student").build();

        assertTrue(comparator.compare(guardian, student) < 0);
        assertTrue(comparator.compare(student, guardian) > 0);
    }

    @Test
    public void compare_sameNameDifferentCaseAndRole_guardianBeforeStudent() {
        Person guardian = new PersonBuilder().withName("ALEX TAN").withRole("guardian").build();
        Person student = new PersonBuilder().withName("alex tan").withRole("student").build();

        assertTrue(comparator.compare(guardian, student) < 0);
    }

    @Test
    public void compare_sigmaCaseVariants_sameRoleComparesEqually() {
        // EP: capital, ordinary lowercase and final lowercase sigma are case variants of one letter.
        String[] names = {"\u039f\u03a3", "\u039f\u03c3", "\u03bf\u03c2"};
        for (String first : names) {
            for (String second : names) {
                Person firstStudent = new PersonBuilder().withName(first).withRole("student").build();
                Person secondStudent = new PersonBuilder().withName(second).withRole("student").build();
                assertEquals(0, comparator.compare(firstStudent, secondStudent), first + " / " + second);
            }
        }
    }

    @Test
    public void compare_sigmaCaseVariants_guardianBeforeStudentInBothDirections() {
        String[] names = {"\u039f\u03a3", "\u039f\u03c3", "\u03bf\u03c2"};
        for (String guardianName : names) {
            for (String studentName : names) {
                Person guardian = new PersonBuilder().withName(guardianName).withRole("guardian").build();
                Person student = new PersonBuilder().withName(studentName).withRole("student").build();
                assertTrue(comparator.compare(guardian, student) < 0, guardianName + " / " + studentName);
                assertTrue(comparator.compare(student, guardian) > 0, studentName + " / " + guardianName);
            }
        }
    }

    @Test
    public void compare_otherUnicodeCaseVariants_guardianBeforeStudent() {
        // EP: long-s needs case folding, and supplementary-plane case pairs must also compare consistently.
        String[][] names = {{"\u017fam", "Sam"}, {"\ud801\udc00", "\ud801\udc28"}};
        for (String[] pair : names) {
            Person guardian = new PersonBuilder().withName(pair[0]).withRole("guardian").build();
            Person student = new PersonBuilder().withName(pair[1]).withRole("student").build();
            assertTrue(comparator.compare(guardian, student) < 0);
            assertTrue(comparator.compare(student, guardian) > 0);
        }
    }

    @Test
    public void compare_canonicalAccentSpellings_guardianBeforeStudentAndPreservesNames() {
        String[] names = {"E\u0301lise", "\u00c9lise", "\u00e9lise"};
        for (String guardianName : names) {
            for (String studentName : names) {
                Person guardian = new PersonBuilder().withName(guardianName).withRole("guardian").build();
                Person student = new PersonBuilder().withName(studentName).withRole("student").build();
                assertTrue(comparator.compare(guardian, student) < 0);
                assertTrue(comparator.compare(student, guardian) > 0);
                assertEquals(guardianName, guardian.getName().fullName);
                assertEquals(studentName, student.getName().fullName);
            }
        }
    }

    @Test
    public void compare_caseDependentAccentComposition_guardianBeforeStudent() {
        // Only lowercase j with caron has a precomposed character; case comparison must still agree.
        String[] names = {"J\u030can", "j\u030can", "\u01f0an"};
        for (String guardianName : names) {
            for (String studentName : names) {
                Person guardian = new PersonBuilder().withName(guardianName).withRole("guardian").build();
                Person student = new PersonBuilder().withName(studentName).withRole("student").build();
                assertTrue(comparator.compare(guardian, student) < 0);
                assertTrue(comparator.compare(student, guardian) > 0);
            }
        }
    }

    @Test
    public void compare_differentNameAndRole_nameTakesPrecedence() {
        Person studentAlex = new PersonBuilder().withName("Alex Tan").withRole("student").build();
        Person guardianBernice = new PersonBuilder().withName("Bernice Yu").withRole("guardian").build();

        assertTrue(comparator.compare(studentAlex, guardianBernice) < 0);
    }

    @Test
    public void compare_sameNameAndRole_equal() {
        Person firstGuardian = new PersonBuilder().withName("Alex Tan").withRole("guardian")
                .withPhone("91234567").build();
        Person secondGuardian = new PersonBuilder().withName("Alex Tan").withRole("guardian")
                .withPhone("98765432").build();

        assertEquals(0, comparator.compare(firstGuardian, secondGuardian));
    }
}
