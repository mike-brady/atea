package atea;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running Atea")
class AteaTest {

    @Mock
    private Database db = Mockito.mock(Database.class);

    private Atea atea;
    private String input;
    private SplitString ss;
    private String[] input_array;
    private int[] abbr_indexes;
    private String expected_expand;
    private String expected_explain;

    @BeforeEach
    void init() throws SQLException {
        Mockito.when(db.getCommonWords()).thenReturn(new String[] {"a","form","of","word","and"});

        Mockito.when(db.abbreviationExists(Mockito.anyString())).thenReturn(-1);
        Mockito.when(db.abbreviationExists("abbr")).thenReturn(1);
        Mockito.when(db.abbreviationExists("DIY")).thenReturn(2);
        Mockito.when(db.abbreviationExists("misc")).thenReturn(3);

        Mockito.when(db.getExpansions(Mockito.anyInt())).thenReturn(new ArrayList<Expansion>());
        ArrayList<Expansion> expansions = new ArrayList<>();
        expansions.add(new Expansion(1, "abbreviation"));
        Mockito.when(db.getExpansions(1)).thenReturn(new ArrayList<Expansion>(expansions));
        expansions = new ArrayList<>();
        expansions.add(new Expansion(2, "do it yourself"));
        Mockito.when(db.getExpansions(2)).thenReturn(new ArrayList<Expansion>(expansions));
        expansions = new ArrayList<>();
        expansions.add(new Expansion(3, "miscellaneous"));
        Mockito.when(db.getExpansions(3)).thenReturn(new ArrayList<Expansion>(expansions));
        Mockito.when(db.getExpansionBaseProbability(Mockito.anyInt(), Mockito.anyInt())).thenReturn(-1f);

        atea = new Atea(db);
        input = "An abbr is a shortened form of a word. DIY and misc are examples of abbreviations.";
        ss = new SplitString(input);
        input_array = new String[]{"An", "abbr", "is", "a", "shortened", "form", "of", "a", "word.", "DIY", "and", "misc", "are", "examples", "of", "abbreviations."};
        abbr_indexes = new int[]{1, 9, 11};
        expected_expand = "An abbreviation is a shortened form of a word. do it yourself and miscellaneous are examples of abbreviations.";
        expected_explain = "An abbr (abbreviation) is a shortened form of a word. DIY (do it yourself) and misc (miscellaneous) are examples of abbreviations.";
    }
    
    @Test
    @DisplayName("findPotentialAbbreviations method should return an ArrayList of Abbreviation objects without expansions")
    void findPotentialAbbreviationsTest() throws SQLException {
        ArrayList<Abbreviation> expected = new ArrayList<>();
        expected.add(new Abbreviation(1, "abbr", ss, 1));
        expected.add(new Abbreviation(2, "DIY", ss, 9));
        expected.add(new Abbreviation(3, "misc", ss, 11));
        
        assertEquals(expected, atea.findPotentialAbbreviations(input));
    }

    @Test
    @DisplayName(("predictAbbreviations method should return an ArrayList of Abbreviation objects with expansions"))
    void predictAbbreviationsTest() throws SQLException {
        ArrayList<Abbreviation> expected = new ArrayList<>();

        ArrayList<Expansion> expansions = new ArrayList<>();
        expansions.add(new Expansion(1, "abbreviation"));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(1, "abbr", ss, 1, expansions));

        expansions = new ArrayList<>();
        expansions.add(new Expansion(2, "do it yourself"));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(2, "DIY", ss, 9, expansions));

        expansions = new ArrayList<>();
        expansions.add(new Expansion(3, "miscellaneous"));
        expansions.add(new Expansion(-1, ""));
        expected.add(new Abbreviation(3, "misc", ss, 11, expansions));

        assertEquals(expected, atea.predictAbbreviations(input));
    }

    @Test
    @DisplayName("expand method should return the input string with the abbreviations expanded")
    void expandTest() throws SQLException {
        assertEquals(expected_expand, atea.expand(input));
    }

    @Test
    @DisplayName("explain method should return the input string with the expansions in parenthesis after the abbreviations")
    void explainTest() throws SQLException {
        assertEquals(expected_explain, atea.explain(input));
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