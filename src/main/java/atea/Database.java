package atea;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

final class Database {
    private Connection conn;

    Database(Connection conn) {
        this.conn = conn;
    }

    /**
     * Determines if the provided string of characters is an existing abbreviation in the database
     * @param  chars A string to be checked.
     * @return       The id of the abbreviation if chars is found in the database, otherwise -1.
     */
    int abbreviationExists(String chars) {
        try ( Statement stmt = this.conn.createStatement() ) {
            String query = "SELECT id FROM abbreviations WHERE value='" + chars + "'";
            ResultSet rset = stmt.executeQuery(query);

            if(rset.first()) {
                return rset.getInt("id");
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    /**
     * Checks if the string of characters is a word in the database and if that word has ever been used in context for
     * the given expansion.
     * @param chars         A string to be checked.
     * @param expansion_id  The id of the expansion to be checked against.
     * @return              The id of the word if it is found and has been used in context foe the given expansion,
     *                      otherwise -1.
     */
    int wordExistsForExpansion(String chars, int expansion_id) {
        try ( Statement stmt = this.conn.createStatement() ) {
            String query = "SELECT word_id FROM context JOIN words ON context.word_id=words.id WHERE words.value='" + chars + "' AND expansion_id=" + expansion_id;
            ResultSet rset = stmt.executeQuery(query);

            if(rset.first()) {
                return rset.getInt("word_id");
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    /**
     * Gets all expansions and creates Expansion objects for a given abbreviation.
     * @param abbr_id   The id of the abbreviation to get expansions for.
     * @return          An ArrayList of Expansion objects.
     */
    ArrayList<Expansion> getExpansions(int abbr_id) {
        ArrayList<Expansion> expansions = new ArrayList<>();

        try ( Statement stmt = this.conn.createStatement() ) {

            String query = "SELECT id, value FROM expansions JOIN abbreviation_expansion ON expansions.id=abbreviation_expansion.expansion_id WHERE abbreviation_expansion.abbreviation_id =" + abbr_id;
            ResultSet rset = stmt.executeQuery(query);
            while(rset.next()) {
                expansions.add(
                        new Expansion(
                            rset.getInt("id"),
                            rset.getString("value")
                        )
                );
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return expansions;
    }

    /**
     * Counts the number of times a word is found at the given distance for an expansion.
     * @param expansion_id  The id of the expansion to be checked against.
     * @param distance      The distance of the word to be checked against.
     * @param word_id       The word to have its occurrences counted.
     * @return              The number of times a word is found at the given distance for an expansion.
     */
    int countWordOccurrencesAtDistance(int expansion_id, int distance, int word_id) {
        int occurrences = 0;

        try ( Statement stmt = this.conn.createStatement() ) {

            String query = "SELECT SUM(count) AS count FROM context WHERE expansion_id=" + expansion_id + " AND distance=" + distance + " AND word_id=" + word_id;
            ResultSet rset = stmt.executeQuery(query);
            if(rset.first()) {
                occurrences = rset.getInt("count");
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return occurrences;
    }

    /**
     * Counts the total number of times any word is found at the given distance for an expansion.
     * @param expansion_id  The id of the expansion to be checked against.
     * @param distance      The distance of the words to be checked against.
     * @return              The total number of times any word is found at the given distance for an expansion.
     */
    int countAllOccurrencesAtDistance(int expansion_id, int distance) {
        int occurrences = 0;

        try ( Statement stmt = this.conn.createStatement() ) {

            String query = "SELECT SUM(count) AS count FROM context JOIN words ON context.word_id = words.id WHERE expansion_id=" + expansion_id + " AND distance=" + distance;
            ResultSet rset = stmt.executeQuery(query);
            if(rset.first()) {
                occurrences = rset.getInt("count");
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return occurrences;
    }
}
