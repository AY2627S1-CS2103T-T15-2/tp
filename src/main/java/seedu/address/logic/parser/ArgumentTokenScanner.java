package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Scans argument characters outside the designated prefixes' quoted values.
 * Quotes in other fields, or after the first non-whitespace value character, remain ordinary text.
 */
final class ArgumentTokenScanner {
    private final String arguments;
    private final List<Prefix> quotedValuePrefixes;
    private int position;
    private int openingQuote = -1;
    private int tokenStart;
    private boolean hasSlashInToken;

    ArgumentTokenScanner(String arguments, List<Prefix> quotedValuePrefixes) {
        this.arguments = requireNonNull(arguments);
        this.quotedValuePrefixes = List.copyOf(quotedValuePrefixes);
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
        if (isTokenStart() && position != openingQuote) {
            rememberOpeningQuote();
        }
        updateTokenBoundary();
        position = position == openingQuote ? findClosingQuote() + 1 : position + 1;
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
                .filter(prefix -> arguments.startsWith(prefix.getPrefix(), position))
                .findFirst().orElse(null);
        if (quotedValuePrefix == null) {
            return;
        }
        int valueStart = position + quotedValuePrefix.getPrefix().length();
        while (valueStart < arguments.length() && Character.isWhitespace(arguments.charAt(valueStart))) {
            valueStart++;
        }
        openingQuote = valueStart < arguments.length() && arguments.charAt(valueStart) == '"' ? valueStart : -1;
    }

    private int findClosingQuote() throws ParseException {
        int quoteEnd = arguments.indexOf('"', openingQuote + 1);
        if (quoteEnd < 0) {
            throw new ParseException(ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTED_VALUE);
        }
        return quoteEnd;
    }
}
