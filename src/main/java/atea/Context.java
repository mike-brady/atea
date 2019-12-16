package atea;

import java.util.Arrays;

public class Context {
    private String[] words;
    private String[] delimiters;
    private int word_index;

    Context(String[] words, String[] delimiters, int word_index) {
        this.words = words;
        this.delimiters = delimiters;
        this.word_index = word_index;
    }

    public String[] getWords() {
        return words;
    }

    public int getWord_index() {
        return word_index;
    }

    public String toString(boolean highlight_word) {
        String[] cur_words = words;
        cur_words[word_index] = "\033[1m" + cur_words[word_index] + "\033[0m";
        return StringSplitter.compose(cur_words, delimiters);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if (!(obj instanceof Context)) {
            return false;
        }

        Context a = (Context) obj;

        return Arrays.equals(a.words,words) && Arrays.equals(a.delimiters,delimiters) && a.getWord_index() == word_index;
    }
}
