package atea;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running Atea")
class AteaTest {

    private Atea atea;
    private String input;
    private SplitString ss;
    private String[] input_array;
    private int[] abbr_indexes;
    private String expected_expand;
    private String expected_explain;

    @BeforeEach
    void init() {
        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("test.properties"));
            String host = properties.getProperty("host");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            atea = new Atea(host, username, password);
        } catch(SQLException | IOException ex) {
            ex.printStackTrace();
            fail("Could not connect to database;");
        }

        input = "An abbr is a shortened form of a word. DIY and misc are examples of abbreviations.";
        ss = new SplitString(input);
        input_array = new String[]{"An", "abbr", "is", "a", "shortened", "form", "of", "a", "word.", "DIY", "and", "misc", "are", "examples", "of", "abbreviations."};
        abbr_indexes = new int[]{1, 9, 11};
        expected_expand = "An abbreviation is a shortened form of a word. do it yourself and miscellaneous are examples of abbreviations.";
        expected_explain = "An abbr (abbreviation) is a shortened form of a word. DIY (do it yourself) and misc (miscellaneous) are examples of abbreviations.";
    }
    
    @Test
    @DisplayName("findPotentialAbbreviations method should return an ArrayList of Abbreviation objects without expansions")
    void findPotentialAbbreviationsTest() {
        ArrayList<Abbreviation> expected = new ArrayList<>();
        expected.add(new Abbreviation(1, "abbr", ss, 1));
        expected.add(new Abbreviation(7, "DIY", ss, 9));
        expected.add(new Abbreviation(17, "misc", ss, 11));
        
        assertEquals(expected, atea.findPotentialAbbreviations(input));
    }

    @Test
    @DisplayName(("predictAbbreviations method should return an ArrayList of Abbreviation objects with expansions"))
    void predictAbbreviationsTest() {
        ArrayList<Abbreviation> expected = new ArrayList<>();

        ArrayList<Expansion> expansions = new ArrayList<>();
        expansions.add(new Expansion(1, "abbreviation", 1));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(1, "abbr", ss, 1, expansions));

        expansions = new ArrayList<>();
        expansions.add(new Expansion(7, "do it yourself", 1));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(7, "DIY", ss, 9, expansions));

        expansions = new ArrayList<>();
        expansions.add(new Expansion(17, "miscellaneous", 1));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(17, "misc", ss, 11, expansions));

        assertEquals(expected, atea.predictAbbreviations(input));
    }

    @Test
    @DisplayName("expand method should return the input string with the abbreviations expanded")
    void expandTest() {
        assertEquals(expected_expand, atea.expand(input));
    }

    @Test
    @DisplayName("explain method should return the input string with the expansions in parenthesis after the abbreviations")
    void explainTest() {
        assertEquals(expected_explain, atea.explain(input));
    }

    @Test
    @DisplayName("predictExpansions method should return an ArrayList of Expansion objects")
    void predictExpansionsTest() {
        ArrayList<Expansion> expected = new ArrayList<Expansion>();
        expected.add(new Expansion(1, "abbreviation", 1));
        expected.add(new Expansion(-1, ""));
        Abbreviation abbr = new Abbreviation(1, "abbr", ss, 1);
        assertEquals(expected, atea.predictExpansions(abbr));
    }

    @Test
    @DisplayName("weightScores method should return an array of weighted scores")
    void weightScores() {
        double[] scores = {.21, .64, .15};
        double[] weights = {360, 61, 72};
        double[] expected = {.6026785844699303, .31122449653050366, .08609694063856145};
        assertArrayEquals(expected, atea.weightScores(scores, weights));
    }
}