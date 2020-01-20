package atea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running Strings")
class SplitStringTest {
    private SplitString ss_ww; // ww = string starts with word and ends with word
    private SplitString ss_wn; // wn = string starts with word and ends with non-word
    private SplitString ss_nw; // nw = string starts with non-word and ends with word
    private SplitString ss_nn; // nn = string starts with non-word and ends with non-word
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
        input_ww = "I love unit tests";
        ss_ww = new SplitString(input_ww);
        expected_delims_ww = new String[]{"", " ", " ", " ", ""};
        expected_full_split_ww = new String[]{"", "I", " ", "love", " ", "unit", " ", "tests", ""};

        input_wn = "I love unit tests!";
        ss_wn = new SplitString(input_wn);
        expected_delims_wn = new String[]{"", " ", " ", " ", "!"};
        expected_full_split_wn = new String[]{"", "I", " ", "love", " ", "unit", " ", "tests", "!"};

        input_nw = " I love unit tests";
        ss_nw = new SplitString(input_nw);
        expected_delims_nw = new String[]{" ", " ", " ", " ", ""};
        expected_full_split_nw = new String[]{" ", "I", " ", "love", " ", "unit", " ", "tests", ""};

        input_nn = " I love unit tests!";
        ss_nn = new SplitString(input_nn);
        expected_delims_nn = new String[]{" ", " ", " ", " ", "!"};
        expected_full_split_nn = new String[]{" ", "I", " ", "love", " ", "unit", " ", "tests", "!"};

        expected_words = new String[]{"I", "love", "unit", "tests"};
    }

    @Test
    void getWordsAsCSV() {
        String expected = ",I,love,unit,tests,";
        assertEquals(expected, ss_wn.getWordsAsCSV());
    }

    @Nested
    @DisplayName("getWords method should return all words")
    class getWordsTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_words, ss_ww.getWords());
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_words, ss_wn.getWords());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_words, ss_nw.getWords());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getWords_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_words, ss_nn.getWords());
        }
    }

    @Nested
    @DisplayName("getDelimiters method should return all delimiters")
    class getDelimitersTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_delims_ww, ss_ww.getDelimiters());
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_delims_wn, ss_wn.getDelimiters());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_delims_nw, ss_nw.getDelimiters());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getDelimiters_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_delims_nn, ss_nn.getDelimiters());
        }
    }

    @Nested
    @DisplayName("getFullSplit method should return all delimiters and words")
    class getFullSplitTest {
        @Test
        @DisplayName("when string begins with a word and ends with a word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithWord() {
            assertArrayEquals(expected_full_split_ww, ss_ww.getFullSplit());
        }

        @Test
        @DisplayName("when string begins with a word and ends with a non-word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithWordEndsWithNonWord() {
            assertArrayEquals(expected_full_split_wn, ss_wn.getFullSplit());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithWord() {
            assertArrayEquals(expected_full_split_nw, ss_nw.getFullSplit());
        }

        @Test
        @DisplayName("when string begins with a non-word and ends with a non-word")
        void getFullSplit_ShouldReturnAllWordsWhenStrStartsWithNonWordEndsWithNonWord() {
            assertArrayEquals(expected_full_split_nn, ss_nn.getFullSplit());
        }
    }


    @Nested
    @DisplayName("compose method should return a string of a combined a String[]")
    class composeTest {
        @Test
        @DisplayName("when two String[]'s are passed")
        void compose_ShouldReturnStringWhenTwoStringArraysArePassed() {
            assertEquals(input_wn, ss_wn.compose());
        }

        @Test
        @DisplayName("when one String[] is passed")
        void compose_ShouldReturnStringWhenOneStringArrayIsPassed() {
            assertEquals(input_wn, ss_wn.compose());
        }
    }

//    @DisplayName("getContext method should return a Context object")
//    @Test
//    void getContext() {
//        Context expected = new Context(expected_words, expected_delims_wn, 2);
//        assertEquals(expected, SplitString.getContext(expected_words, expected_delims_wn, 2, 3));
//    }
}