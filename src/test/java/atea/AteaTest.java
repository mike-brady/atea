package atea;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.spi.AbstractResourceBundleProvider;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When running Atea")
class AteaTest {

    private Atea A;
    private String input;
    private String[] input_array;
    private int[] abbr_indexes;
    private String expected_expand;
    private String expected_explain;

    @BeforeEach
    void init() {
        try {
            String[] db_credentials = {
                    System.getProperty("db.host"),
                    System.getProperty("db.username"),
                    System.getProperty("db.password")
            };
            A = new Atea(db_credentials);
        } catch(SQLException ex) {
            ex.printStackTrace();
            fail("Could not connect to database;");
        }

        input = "An abbr is a shortened form of a word. DIY and misc are examples of abbreviations.";
        input_array = new String[]{"An", "abbr", "is", "a", "shortened", "form", "of", "a", "word.", "DIY", "and", "misc", "are", "examples", "of", "abbreviations."};
        abbr_indexes = new int[]{1, 9, 11};
        expected_expand = "An abbreviation is a shortened form of a word. do it yourself and miscellaneous are examples of abbreviations.";
        expected_explain = "An abbr (abbreviation) is a shortened form of a word. DIY (do it yourself) and misc (miscellaneous) are examples of abbreviations.";
    }

    @Test
    @DisplayName("getAbbreviations method should return an ArrayList of Abbreviation objects")
    void getAbbreviationsTest() {
        ArrayList<Abbreviation> expected = new ArrayList<Abbreviation>();
        ArrayList<Expansion> expansions = new ArrayList<Expansion>();
        expansions.add(new Expansion(1, "abbreviation", 1));
        expected.add(new Abbreviation("abbr", abbr_indexes[0], expansions));

        expansions = new ArrayList<Expansion>();
        expansions.add(new Expansion(2, "do it yourself", 1));
        expected.add(new Abbreviation("DIY", abbr_indexes[1], expansions));

        expansions = new ArrayList<Expansion>();
        expansions.add(new Expansion(3, "miscellaneous", 1));
        expected.add(new Abbreviation("misc", abbr_indexes[2], expansions));

        assertEquals(expected, A.getAbbreviations(input));
    }

    @Test
    @DisplayName("expand method should return the input string with the abbreviations expanded")
    void expandTest() {
        assertEquals(expected_expand, A.expand(input));
    }

    @Test
    @DisplayName("explain method should return the input string with the expansions in parenthesis after the abbreviations")
    void explainTest() {
        assertEquals(expected_explain, A.explain(input));
    }

    @Test
    @DisplayName("predictExpansions method should return an ArrayList of Expansion objects")
    void predictExpansionsTest() {
        ArrayList<Expansion> expected = new ArrayList<Expansion>();
        expected.add(new Expansion(1, "abbreviation", 1));
        Strings S = new Strings();
        assertEquals(expected, A.predictExpansions(input_array, 1));
    }

    @Nested
    @DisplayName("addAbbreviationExample method should add to the context table in the database")
    class addAbbreviationExampleTest {
        @Test
        void addAbbreviationExample () {
            assertEquals(A.addAbbreviationExample(input_array, abbr_indexes[0], 1), true);
        }

        @Test
        @DisplayName("and should create a new expansion entry in the database")
        void testAddAbbreviationExample () {
            assertEquals(A.addAbbreviationExample(input_array, abbr_indexes[0], "abbreviation"), true);
        }
    }
}