package atea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running Strings")
class StringsTest {
    private Strings S;
    private String input_ww;
    private String input_wn;
    private String input_nw;
    private String input_nn;
    private String[] expected_delims_ww;
    private String[] expected_delims_wn;
    private String[] expected_delims_nw;
    private String[] expected_delims_nn;
    private String[] expected_full_split_ww;
    private String[] expected_full_split_wn;
    private String[] expected_full_split_nw;
    private String[] expected_full_split_nn;
    private String[] expected_words;

    @BeforeEach
    void init() {
        S = new Strings();
        input_ww = "I love unit tests";
        expected_delims_ww = new String[]{"", " ", " ", " ", ""};
        expected_full_split_ww = new String[]{"", "I", " ", "love", " ", "unit", " ", "tests", ""};

        input_wn = "I love unit tests!";
        expected_delims_wn = new String[]{"", " ", " ", " ", "!"};
        expected_full_split_wn = new String[]{"", "I", " ", "love", " ", "unit", " ", "tests", "!"};

        input_nw = " I love unit tests";
        expected_delims_nw = new String[]{" ", " ", " ", " ", ""};
        expected_full_split_nw = new String[]{" ", "I", " ", "love", " ", "unit", " ", "tests", ""};

        input_nn = " I love unit tests!";
        expected_delims_nn = new String[]{" ", " ", " ", " ", "!"};
        expected_full_split_nn = new String[]{" ", "I", " ", "love", " ", "unit", " ", "tests", "!"};

        expected_words = new String[]{"I", "love", "unit", "tests"};
    }

    @Nested
    @DisplayName("getWords method should return all words")
    class getWordsTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_words, S.getWords(input_ww));
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_words, S.getWords(input_wn));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_words, S.getWords(input_nw));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_words, S.getWords(input_nn));
        }
    }

    @Nested
    @DisplayName("getDelimiters method should return all delimiters")
    class getDelimitersTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_delims_ww, S.getDelimiters(input_ww));
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_delims_wn, S.getDelimiters(input_wn));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_delims_nw, S.getDelimiters(input_nw));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_delims_nn, S.getDelimiters(input_nn));
        }
    }

    @Nested
    @DisplayName("getFullSplit method should return all delimiters and words")
    class getFullSplitTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_full_split_ww, S.getFullSplit(input_ww));
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_full_split_wn, S.getFullSplit(input_wn));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_full_split_nw, S.getFullSplit(input_nw));
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_full_split_nn, S.getFullSplit(input_nn));
        }
    }
}