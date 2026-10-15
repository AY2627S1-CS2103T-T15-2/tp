package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 70;
    public static final String MESSAGE_CONSTRAINTS =
            "Names must contain 1 to 70 characters and cannot be blank. "
                    + "Names may contain letters, digits, spaces, apostrophes, hyphens, periods, and slashes.";

    public static final String VALIDATION_REGEX = "[\\p{L}\\p{M}\\p{N}\\p{Pd} './’]+";

    private static final char APOSTROPHE_STRAIGHT = '\'';
    private static final char APOSTROPHE_CURLY_LEFT = '\u2018';
    private static final char APOSTROPHE_CURLY_RIGHT = '\u2019';
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    /**
     * Replacements for letters that Unicode does not decompose into a base letter and an accent, such as
     * letters with a stroke (ø, ł), ligatures (æ, œ), ß and þ.
     */
    private static final Map<Character, String> STANDALONE_LETTER_REPLACEMENTS = Map.ofEntries(
            Map.entry('\u00f8', "o"), Map.entry('\u00d8', "O"), // ø, Ø
            Map.entry('\u0142', "l"), Map.entry('\u0141', "L"), // ł, Ł
            Map.entry('\u0111', "d"), Map.entry('\u0110', "D"), // đ, Đ
            Map.entry('\u00f0', "d"), Map.entry('\u00d0', "D"), // ð, Ð
            Map.entry('\u0127', "h"), Map.entry('\u0126', "H"), // ħ, Ħ
            Map.entry('\u0131', "i"), // ı
            Map.entry('\u00fe', "th"), Map.entry('\u00de', "Th"), // þ, Þ
            Map.entry('\u00e6', "ae"), Map.entry('\u00c6', "Ae"), // æ, Æ
            Map.entry('\u0153', "oe"), Map.entry('\u0152', "Oe"), // œ, Œ
            Map.entry('\u00df', "ss"), Map.entry('\u1e9e', "SS")); // ß, ẞ

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);

        return getValidationErrorMessage(test) == null;
    }

    /**
     * Returns a specific validation error for {@code test}, or {@code null} if it is valid.
     */
    public static String getValidationErrorMessage(String test) {
        requireNonNull(test);
        if (test.isBlank()) {
            return "Name cannot be blank.";
        }
        int characterCount = test.codePointCount(0, test.length());
        if (characterCount > MAX_LENGTH) {
            return "Name cannot exceed " + MAX_LENGTH + " characters.";
        }
        if (!test.matches(VALIDATION_REGEX)) {
            return "Name contains unsupported characters. Use letters, digits, spaces, apostrophes, "
                    + "hyphens, periods, and slashes.";
        }
        return null;
    }

    /**
     * Returns {@code text} with curly single quotation marks, such as the apostrophe in "O’Brien",
     * replaced by straight apostrophes. This allows names copied from other apps to be matched and
     * ordered in the same way as names typed with a straight apostrophe.
     */
    public static String replaceCurlyApostrophes(String text) {
        requireNonNull(text);
        return text.replace(APOSTROPHE_CURLY_LEFT, APOSTROPHE_STRAIGHT)
                .replace(APOSTROPHE_CURLY_RIGHT, APOSTROPHE_STRAIGHT);
    }

    /**
     * Returns {@code text} with accents removed from its letters, for example "Elise" for "Élise".
     * Each letter is decomposed into its base letter and combining marks, and the combining marks are
     * removed. Letters that cannot be decomposed in this way are replaced by their base letters, for
     * example "Soren" for "Søren", "Lukasz" for "Łukasz" and "Strauss" for "Strauß".
     */
    public static String removeAccents(String text) {
        requireNonNull(text);
        String decomposedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        String textWithoutMarks = COMBINING_MARKS.matcher(decomposedText).replaceAll("");
        StringBuilder result = new StringBuilder(textWithoutMarks.length());
        for (char letter : textWithoutMarks.toCharArray()) {
            result.append(STANDALONE_LETTER_REPLACEMENTS.getOrDefault(letter, String.valueOf(letter)));
        }
        return result.toString();
    }

    /**
     * Returns true if both names are the same, ignoring letter case.
     */
    public boolean isSameName(Name otherName) {
        return otherName != null && fullName.equalsIgnoreCase(otherName.fullName);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
