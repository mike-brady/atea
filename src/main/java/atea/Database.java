package atea;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

final class Database {
    Connection conn;

    Database(Connection conn) {
        this.conn = conn;
    }

    /**
     * Determines if the provided string of characters is an existing abbreviation in the database
     * @param  chars A string to be checked
     * @return       the id of the abbreviation if chars is found in the database, otherwise -1
     */
    public int abbreviationExists(String chars) {
        try (Statement stmt = this.conn.createStatement(); ) {
            String strSelect = "SELECT id FROM abbreviations WHERE value='" + chars + "'";
            ResultSet rset = stmt.executeQuery(strSelect);

            if(rset.first()) {
                return rset.getInt("id");
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    public int wordExistsForExpansion(String chars, int expansion_id) {
        try (Statement stmt = this.conn.createStatement(); ) {
            String strSelect = "SELECT word_id FROM context JOIN words ON context.word_id=words.id WHERE words.value='" + chars + "' AND expansion_id=" + expansion_id;
            ResultSet rset = stmt.executeQuery(strSelect);

            if(rset.first()) {
                return rset.getInt("word_id");
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    public ArrayList<Expansion> getExpansions(int abbr_id) {
        ArrayList<Expansion> expansions = new ArrayList<Expansion>();

        try ( Statement stmt = this.conn.createStatement(); ) {

            String strSelect = "SELECT id, value FROM expansions JOIN abbreviation_expansion ON expansions.id=abbreviation_expansion.expansion_id WHERE abbreviation_expansion.abbreviation_id =" + abbr_id;
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()) {
                expansions.add(
                        new Expansion(
                            rset.getInt("id"),
                            rset.getString("value")
                        )
                );
            };

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return expansions;
    }

    public int countWordOccurrencesAtDistance(int expansion_id, int distance, int word_id) {
        int occurrences = 0;

        try ( Statement stmt = this.conn.createStatement(); ) {

            String strSelect = "SELECT SUM(count) AS count FROM context WHERE expansion_id=" + expansion_id + " AND distance=" + distance + " AND word_id=" + word_id;
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.first()) {
                occurrences = rset.getInt("count");
            };

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return occurrences;
    }

    public int countAllOccurrencesAtDistance(int expansion_id, int distance) {
        int occurrences = 0;

        try ( Statement stmt = this.conn.createStatement(); ) {

            String strSelect = "SELECT SUM(count) AS count FROM context JOIN words ON context.word_id = words.id WHERE expansion_id=" + expansion_id + " AND distance=" + distance;
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.first()) {
                occurrences = rset.getInt("count");
            };

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return occurrences;
    }
}
