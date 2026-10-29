package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Scans argument characters outside the designated prefixes' quoted values.
 * Quotes in other fields, or after the first non-whitespace value character, remain ordinary text.
 */
final class ArgumentTokenScanner {
    private static final int NO_OPENING_QUOTE = -1;

    private final String arguments;
    private final List<Prefix> quotedValuePrefixes;
    private final boolean isCaseInsensitive;
    private int position;
    private int openingQuoteIndex = NO_OPENING_QUOTE;
    private int tokenStart;
    private boolean hasSlashInToken;

    ArgumentTokenScanner(String arguments, List<Prefix> quotedValuePrefixes) {
        this(arguments, quotedValuePrefixes, false);
    }

    ArgumentTokenScanner(String arguments, List<Prefix> quotedValuePrefixes, boolean isCaseInsensitive) {
        this.arguments = requireNonNull(arguments);
        this.quotedValuePrefixes = List.copyOf(quotedValuePrefixes);
        this.isCaseInsensitive = isCaseInsensitive;
    }

    boolean hasNext() {
        return position < arguments.length();
    }

    int getPosition() {
        return position;
    }

    int getTokenStart() {
        return tokenStart;
    }

    /**
     * Returns the token's first slash-terminated prefix, or null at other positions.
     * A leading slash alone does not form a prefix.
     */
    String getParameterPrefix() {
        return isParameterSlash() && !hasSlashInToken ? arguments.substring(tokenStart, position + 1) : null;
    }

    boolean isTokenStart() {
        return position == 0 || Character.isWhitespace(arguments.charAt(position - 1));
    }

    /**
     * Advances to the next character outside the quoted value.
     * Callers can inspect a character before advancing so earlier syntax errors retain priority.
     *
     * @throws ParseException if the designated field's quoted value is unclosed
     */
    void advance() throws ParseException {
        if (isTokenStart() && position != openingQuoteIndex) {
            rememberOpeningQuote();
        }
        updateTokenBoundary();
        position = position == openingQuoteIndex ? findClosingQuote() + 1 : position + 1;
    }

    private void updateTokenBoundary() {
        if (Character.isWhitespace(arguments.charAt(position))) {
            tokenStart = position + 1;
            hasSlashInToken = false;
        } else if (isParameterSlash()) {
            hasSlashInToken = true;
        }
    }

    private boolean isParameterSlash() {
        return arguments.charAt(position) == '/' && position > tokenStart;
    }

    private void rememberOpeningQuote() {
        Prefix quotedValuePrefix = quotedValuePrefixes.stream()
                .filter(prefix -> arguments.regionMatches(isCaseInsensitive, position,
                        prefix.getPrefix(), 0, prefix.getPrefix().length()))
                .findFirst().orElse(null);
        if (quotedValuePrefix == null) {
            return;
        }
        int valueStart = position + quotedValuePrefix.getPrefix().length();
        while (valueStart < arguments.length() && Character.isWhitespace(arguments.charAt(valueStart))) {
            valueStart++;
        }
        openingQuoteIndex = valueStart < arguments.length() && arguments.charAt(valueStart) == '"'
                ? valueStart : NO_OPENING_QUOTE;
    }

    private int findClosingQuote() throws ParseException {
        int quoteEnd = arguments.indexOf('"', openingQuoteIndex + 1);
        if (quoteEnd < 0) {
            throw new ParseException(ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTED_VALUE);
        }
        return quoteEnd;
    }
}
