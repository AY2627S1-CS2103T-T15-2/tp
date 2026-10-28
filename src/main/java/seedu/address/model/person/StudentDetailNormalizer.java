package seedu.address.model.person;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalizes levels and subjects for filtering without changing their stored text.
 */
final class StudentDetailNormalizer {
    private StudentDetailNormalizer() {
    }

    /** Matches canonical Unicode spellings while ignoring case and spaces, preserving distinct accents. */
    static String normalizeForComparison(String value) {
        String canonicalValue = Normalizer.normalize(value.replace(" ", ""), Normalizer.Form.NFC);
        // Lowercasing can introduce decomposed characters, so normalize its result too.
        return Normalizer.normalize(canonicalValue.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);
    }
}
