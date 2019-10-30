package atea;

import java.util.Arrays;

final class Strings {
    private final String wordCharacterPattern;
    private final String wordPattern;
    private final String delimiterPattern;

    Strings() {
        String wordCharacterSet = "A-z_";
        wordCharacterPattern = "[" + wordCharacterSet + "]";
        wordPattern = wordCharacterPattern + "+";
        delimiterPattern = "[^" + wordCharacterSet + "]+";
    }

    /**
     * Gets the words in a string.
     * @param text  The string to get the words from.
     * @return      An ordered String[] of words found in text.
     */
    String[] getWords(String text) {
        String[] words = text.split(delimiterPattern);

        if(words[0].length() == 0) {
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
    String[] getDelimiters(String text) {
        String[] delims = text.split(wordPattern);

        String lastCharacter = text.substring(text.length() - 1);
        if(lastCharacter.matches(wordCharacterPattern)) {
            // the string ends with a word and the final blank delimiter will be missing
            // add one more blank element to the end of delims
            delims = Arrays.copyOfRange(delims, 0, delims.length + 1);
            delims[delims.length - 1] = "";
        }

        return delims;
    }

    /**
     * Gets the delimiters (everything that isn't a word) and words in a string. Will always create a delimiter at the
     * start and end of the string, even if the delimiter is an empty string.
     * @param text  The string to get the delimiters and words from.
     * @return      An ordered String[] of delimiters and words found in text.
     */
    String[] getFullSplit(String text) {
        String[] words = getWords(text);
        String[] delims = getDelimiters(text);

        String[] mixed = new String[delims.length + words.length];
        int j=0;
        for(int i=0; i<words.length; i++) {
            mixed[j++] = delims[i];
            mixed[j++] = words[i];
        }
        mixed[j] = delims[delims.length - 1];

        return mixed;
    }
}
