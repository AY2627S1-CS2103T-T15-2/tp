package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalizes levels and subjects for filtering without changing their stored text.
 */
final class StudentDetailNormalizer {
    private StudentDetailNormalizer() {
        // Prevents instantiation, as this class only has static methods.
    }

    /** Matches canonical Unicode spellings while ignoring case and spaces, preserving distinct accents. */
    static String normalizeForComparison(String value) {
        requireNonNull(value);
        String canonicalValue = Normalizer.normalize(value.replace(" ", ""), Normalizer.Form.NFC);
        // Lowercasing a capital sigma depends on its position within a word.
        String lowercaseValue = canonicalValue.toLowerCase(Locale.ROOT).replace('\u03c2', '\u03c3');
        // Lowercasing can introduce decomposed characters, so normalize its result too.
        return Normalizer.normalize(lowercaseValue, Normalizer.Form.NFC);
    }
}
