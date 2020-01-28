package atea;

import java.util.Arrays;

/**
 * Splits strings up into individual words, preserving the delimiters between the words for
 * re-composition.
 */
public final class SplitString {
    private static final String defaultWordCharSet = "A-z_";
    private static String wordCharPattern;
    private static String wordPattern;
    private static String delimiterPattern;

    private String text;
    private String[] words;
    private String[] delimiters;

    /**
     *
     * @param text          The text to be split
     */
    public SplitString(String text) {
        setRegexPatterns(defaultWordCharSet);

        this.text = text;
        splitWords();
        splitDelimiters();
    }

    /**
     *
     * @param text          The text to be split
     * @param wordCharSet   A regular expression character set to use to split the string into words
     */
    public SplitString(String text, String wordCharSet) {
        setRegexPatterns(wordCharSet);

        this.text = text;
        splitWords();
        splitDelimiters();
    }

    /**
     * Sets the regular expression patterns used for splitting strings into words and delimiters
     * @param wordCharSet   A regular expression character set to use to split the string into words
     */
    private void setRegexPatterns(String wordCharSet) {
        wordCharPattern = "[" + wordCharSet + "]";
        wordPattern = wordCharPattern + "+";
        delimiterPattern = "[^" + wordCharSet + "]+";
    }

    public String getText() { return text; }

    public String[] getWords() { return words; }

    public String[] getDelimiters() { return delimiters; }

    /**
     * Gets the words in a string.
     */
    private void splitWords() {
        words = text.split(delimiterPattern);

        if(words.length > 0 && words[0].length() == 0) {
            // the string starts with a delimiter and the first element in words will be blank
            // remove that first blank element
            words = Arrays.copyOfRange(words, 1, words.length);
        }
    }

    /**
     * Gets the delimiters (everything that isn't a word) in a string. Will always create a delimiter
     * at the start and end of the string, even if the delimiter is an empty string.
     */
    private void splitDelimiters() {
        delimiters = text.split(wordPattern);

        String lastCharacter = text.substring(text.length() - 1);
        if(lastCharacter.matches(wordCharPattern)) {
            // the string ends with a word and the final blank delimiter will be missing
            // add one more blank element to the end of delims
            delimiters = Arrays.copyOfRange(delimiters, 0, delimiters.length + 1);
            delimiters[delimiters.length - 1] = "";
        }
    }

    /**
     * Gets the text split into an array of alternating delimiters and words. The array will always
     * start and end with a delimiter.
     * @return  An array ofalternating delimiters and words
     */
    public String[] getFullSplit() {
        String[] mixed = new String[delimiters.length + words.length];
        int j=0;
        for(int i=0; i<words.length; i++) {
            mixed[j++] = delimiters[i];
            mixed[j++] = words[i];
        }
        mixed[j] = delimiters[delimiters.length - 1];

        return mixed;
    }

    /**
     * Gets the words as a string in Comma Separated Values format
     * @return  CSV formated string of the words
     */
    public String getWordsAsCSV() {
        StringBuilder csv = new StringBuilder(",");
        for(String word : words) {
            csv.append(word).append(",");
        }

        return csv.toString();
    }

    @Override
    public String toString() { return text; }
}
