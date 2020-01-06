package atea;

import java.util.Arrays;

public final class SplitString {
    private static final String defualtWordCharSet = "A-Z_";
    private static String wordCharPattern;
    private static String wordPattern;
    private static String delimiterPattern;

    private String text;
    private String[] words;
    private String[] delimiters;

    public SplitString(String text) {
        this.text = text;
        this.words = splitWords(text);
        this.delimiters = splitDelimiters(text);
        setRegexPatterns(defualtWordCharSet);
    }

    public SplitString(String text, String wordCharSet) {
        this.text = text;
        this.words = splitWords(text);
        this.delimiters = splitDelimiters(text);
        setRegexPatterns(wordCharSet);
    }

    private void setRegexPatterns(String wordCharSet) {
        wordCharPattern = "[" + wordCharSet + "]";
        wordPattern = wordCharPattern + "+";
        delimiterPattern = "[^" + wordCharSet + "]+";
    }

    public String getText() {
        return text;
    }

    public String[] getWords() {
        return words;
    }

    public String[] getDelimiters() {
        return delimiters;
    }

    /**
     * Gets the words in a string.
     * @param text  The string to get the words from.
     * @return      An ordered String[] of words found in text.
     */
    private String[] splitWords(String text) {
        String[] words = text.split(delimiterPattern);

        if(words.length > 0 && words[0].length() == 0) {
            // the string starts with a delimiter and the first element in words will be blank
            // remove that first blank element
            words = Arrays.copyOfRange(words, 1, words.length);
        }

        return words;
    }

    /**
     * Gets the delimiters (everything that isn't a word) in a string. Will always create a delimiter at the start and
     * end of the string, even if the delimiter is an empty string.
     * @param text  The string to get the delimiters from.
     * @return      An ordered String[] of delimiters found in text.
     */
    private String[] splitDelimiters(String text) {
        String[] delims = text.split(wordPattern);

        String lastCharacter = text.substring(text.length() - 1);
        if(lastCharacter.matches(wordCharPattern)) {
            // the string ends with a word and the final blank delimiter will be missing
            // add one more blank element to the end of delims
            delims = Arrays.copyOfRange(delims, 0, delims.length + 1);
            delims[delims.length - 1] = "";
        }

        return delims;
    }

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

    public String compose() {
        StringBuilder output = new StringBuilder();
        for(String item : getFullSplit()) {
            output.append(item);
        }
        return output.toString();
    }

//    public static Context getContext(String[] words, String[] delimiters, int word_index, int context_width) {
//        // Limit the start and end of the context around the abbr to not go out of bounds of text
//        int start_index = Math.max(0, word_index - context_width);
//        int end_index = Math.min(words.length - 1, word_index + context_width);
//        int word_context_index = word_index - start_index;
//        String[] context_words = Arrays.copyOfRange(words, start_index, end_index + 1);
//        String[] context_delims = Arrays.copyOfRange(delimiters, start_index, end_index + 2);
//
//        return new Context(context_words, context_delims, word_context_index);
//    }
}
