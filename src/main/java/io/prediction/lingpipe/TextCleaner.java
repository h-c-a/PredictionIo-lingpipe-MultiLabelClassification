package io.prediction.lingpipe;

import org.apache.commons.lang3.StringUtils;

public class TextCleaner {

    private static final String RegReplaceSlashes = "\\b\\d+\\b";
    private static final String RegReplaceNumbers = "(?<!\\d)/";
    private static final String RegReplaceCharachtersWithSpace = "[\\p{Z}\\(\\)\\+]+";
    private static final String RegReplaceTrailingSymbols = "(\\w)[\\-\\.,:!\\?]+(\\s+|$)";
    private static final String RegReplaceLeadingSymbols = "(\\s+|^)[\\-\\.,:!\\?]+(\\w|\\s+)";
    private static final String RegReplaceSequenceSymbols = "(\\s+|^)[\\-\\.,:!\\?\\s]+(\\s+|$)";
    private static final String RegReplaceDoubleQuotes = "(?<!\\d)\"";
    private static final String RegNormalizeOutstandingQuotes = "([\\d\\.]+)\\s+(\"\"|\'\')(\\s|$)";
    public static final String SPACE = " ";

    public static String cleanUpSpecialCharachters(String result) {
        if (StringUtils.isBlank(result)) {
            return StringUtils.EMPTY;
        }

        result = result.replaceAll(RegReplaceCharachtersWithSpace, SPACE);
        result = result.replaceAll(RegReplaceTrailingSymbols, "$1 ");
        result = result.replaceAll(RegReplaceLeadingSymbols, " $2");
        result = result.replaceAll(RegReplaceSequenceSymbols, " ");
        result = result.replaceAll(RegNormalizeOutstandingQuotes, "$1\" ");
        result = result.replaceAll(RegReplaceSlashes, SPACE);
        result = result.replaceAll(RegReplaceDoubleQuotes, SPACE);
        result = result.replaceAll(RegReplaceNumbers, SPACE);

        result = result.trim();

        return result;
    }
}
