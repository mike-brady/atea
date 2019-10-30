package atea;

import java.util.Arrays;

final class Strings {
    private final String wordCharacterPattern;
    private final String wordPattern;
    private final String delimiterPattern;

    public Strings() {
        String wordCharacterSet = "A-z_";
        wordCharacterPattern = "[" + wordCharacterSet + "]";
        wordPattern = wordCharacterPattern + "+";
        delimiterPattern = "[^" + wordCharacterSet + "]+";
    }

    public String[] getWords(String text) {
        String[] words = text.split(delimiterPattern);

        if(words[0].length() == 0) {
            // the string starts with a delimiter and the first element in words will be blank
            // remove that first blank element
            words = Arrays.copyOfRange(words, 1, words.length);
        }

        return words;
    }

    public String[] getDelimiters(String text) {
        String[] delims = text.split(wordPattern);

        String lastCharacter = text.substring(text.length() - 1, text.length());
        if(lastCharacter.matches(wordCharacterPattern)) {
            // the string ends with a word and the final blank delimiter will be missing
            // add one more blank element to the end of delims
            delims = Arrays.copyOfRange(delims, 0, delims.length + 1);
            delims[delims.length - 1] = "";
        }

        return delims;
    }

    public String[] getFullSplit(String text) {
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
