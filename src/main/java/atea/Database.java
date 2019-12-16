package atea;

import com.mysql.cj.xdevapi.PreparableStatement;

import javax.swing.plaf.nimbus.State;
import java.awt.image.ShortLookupTable;
import java.sql.*;
import java.util.ArrayList;

final class Database {
    private String host;
    private String username;
    private String password;

    /**
     * @param db_credentials    {"host", "username", "password"}
     */
    Database(String[] db_credentials) throws SQLException {
        this.host = db_credentials[0];
        this.username = db_credentials[1];
        this.password = db_credentials[2];

        // Verify the credentials
        connect().close();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + host + ":3306/atea", username, password);
    }

    private void close(Connection conn, boolean commit) throws SQLException {
        if(commit) {
            conn.commit();
        } else {
            conn.rollback();
        }
        conn.close();
    }


    /**
     * Determines if the provided string of characters is an existing abbreviation in the database
     * @param  chars A string to be checked.
     * @return       The id of the abbreviation if chars is found in the database, otherwise -1.
     */
    int abbreviationExists(String chars) {
        int id = -1;
        try {
            Connection conn = connect();

            String query = "SELECT id FROM abbreviations WHERE value=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, chars);
            ResultSet rset = stmt.executeQuery();
            if(rset.first()) { id = rset.getInt("id"); }

            conn.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return id;
    }

    private boolean abbreviationExists(int id, Connection conn) {
        boolean exists = false;

        try {
            boolean ourConn = (conn == null);
            if(ourConn) { conn = connect(); }

            String query = "SELECT id FROM abbreviations WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            exists = stmt.execute();

            if(ourConn) { conn.close(); }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    boolean isAlwaysAbbreviation(int id) {
        boolean always = false;

        try {
            Connection conn = connect();

            String query = "SELECT id FROM abbreviations WHERE id=? AND is_always_abbreviation=1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            always = stmt.execute();

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return always;
    }

    private boolean expansionExists(int id, Connection conn) {
        boolean exists = false;

        try {
            boolean ourConn = (conn == null);
            if(ourConn) { conn = connect(); }

            String query = "SELECT id FROM expansions WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            exists = stmt.execute();

            if(ourConn) { conn.close(); }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return exists;
    }

    private boolean isExpansionFor(int expansion_id, int abbr_id, Connection conn) {
        boolean result = false;

        try {
            boolean ourConn = (conn == null);
            if(ourConn) { conn = connect(); }

            String query = "SELECT abbreviation_id FROM abbreviation_expansion WHERE abbreviation_id=? AND expansion_id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, abbr_id);
            stmt.setInt(2, expansion_id);
            result = stmt.execute();

            if(ourConn) { conn.close(); }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
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
        int id = -1;
        try {
            Connection conn = connect();

            String query = "SELECT word_id FROM context JOIN words ON context.word_id=words.id WHERE words.value=? AND expansion_id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, chars);
            stmt.setInt(2, expansion_id);
            ResultSet rset = stmt.executeQuery();
            if(rset.first()) { id =rset.getInt("word_id"); }

            conn.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return id;
    }

    /**
     * Gets all expansions and creates Expansion objects for a given abbreviation.
     * @param abbr_id   The id of the abbreviation to get expansions for.
     * @return          An ArrayList of Expansion objects.
     */
    ArrayList<Expansion> getExpansions(int abbr_id) {
        ArrayList<Expansion> expansions = new ArrayList<>();

        try {
            Connection conn = connect();

            String query = "SELECT id, value FROM expansions JOIN abbreviation_expansion ON expansions.id=abbreviation_expansion.expansion_id WHERE abbreviation_expansion.abbreviation_id =?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, abbr_id);
            ResultSet rset = stmt.executeQuery();
            while(rset.next()) {
                expansions.add(
                        new Expansion(
                            rset.getInt("id"),
                            rset.getString("value")
                        )
                );
            }

            conn.close();
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

        try {
            Connection conn = connect();

            String query = "SELECT SUM(count) AS count FROM context WHERE expansion_id=? AND distance=? AND word_id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, expansion_id);
            stmt.setInt(2, distance);
            stmt.setInt(3, word_id);
            ResultSet rset = stmt.executeQuery();
            if(rset.first()) {
                occurrences = rset.getInt("count");
            }

            conn.close();
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

        try {
            Connection conn = connect();

            String query = "SELECT SUM(count) AS count FROM context JOIN words ON context.word_id = words.id WHERE expansion_id=? AND distance=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, expansion_id);
            stmt.setInt(2, distance);
            ResultSet rset = stmt.executeQuery();
            if(rset.first()) {
                occurrences = rset.getInt("count");
            }

            conn.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return occurrences;
    }

    private int insertAbbreviation(String chars, Connection conn) {
        int id = -1;
        try {
            boolean ourConn = (conn == null);
            if(ourConn) { conn = connect(); }

            String query = "SELECT id FROM abbreviations WHERE value=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, chars);
            ResultSet rset = stmt.executeQuery();

            if(rset.first()) {
                id = rset.getInt("id");
            } else {
                query = "INSERT INTO abbreviations SET value=?";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, chars);
                stmt.executeUpdate();
                rset = stmt.getGeneratedKeys();
                if(rset.first()) {
                    id = rset.getInt(1);
                }
            }

            if(ourConn) { conn.close(); }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return id;
    }

    /**
     * Inserts a word into the database if it does not already exist.
     * @param word  The word to be inserted.
     * @return      The id of the word that was inserted, or the id of the word if it already exists.
     *              Returns -1 on failure.
     */
    private int insertWord(String word, Connection conn) {
        int word_id = -1;
        try {
            boolean ourConn = (conn == null);
            if(ourConn) { conn = connect(); }

            String query = "SELECT id FROM words WHERE value=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, word);
            ResultSet rset = stmt.executeQuery();

            if(rset.first()) {
                word_id = rset.getInt("id");
            } else {
                query = "INSERT INTO words SET value=?";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, word);
                stmt.executeUpdate();
                rset = stmt.getGeneratedKeys();
                if(rset.first()) {
                    word_id = rset.getInt(1);
                }
            }

            if(ourConn) { conn.close(); }
        } catch(SQLException ex) {
            ex.printStackTrace();
            return -1;
        }

        return word_id;
    }

    /**
     * Inserts an expansion into the database if it does not already exist and links it to an abbreviation.
     * @param expansion The expansion to be inserted.
     * @param abbr_id   The id of the abbreviation to link the expansion to.
     * @return          The id of the expansion or -1 on failure.
     */
    private int insertExpansion(String expansion, int abbr_id, Connection conn) {
        int expansion_id = -1;
        try {
            Boolean ourConn = (conn == null);
            if(ourConn) {
                conn = connect();
                conn.setAutoCommit(false);
            }

            if(!abbreviationExists(abbr_id, null)) {
                if(ourConn) { close(conn, false); }
                return -1;
            }

            String query = "SELECT id FROM expansions WHERE value=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, expansion);
            ResultSet rset = stmt.executeQuery();

            // if the expansion exists
            if(rset.first()) {
                expansion_id = rset.getInt("id");
            } else {
                query = "INSERT INTO expansions SET value=?";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, expansion);
                stmt.executeUpdate();
                rset = stmt.getGeneratedKeys();

                // if not successful
                if(!rset.first()) {
                    if(ourConn) { close(conn, false); }
                    return -1;
                }

                expansion_id = rset.getInt("id");
            }

            query = "SELECT expansion_id FROM abbreviation_expansion WHERE abbreviation_id=? AND expansion_id=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, abbr_id);
            stmt.setInt(2, expansion_id);
            rset = stmt.executeQuery();

            // if the expansion is not already attached to the abbreviation
            if(!rset.first()) {
                query = "INSERT INTO abbrevatiation_expansion SET abbreviation_id=?, expansion_id=?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, abbr_id);
                stmt.setInt(2, expansion_id);
                if(!stmt.execute()) {
                    if(ourConn) { close(conn, false); }
                    return -1;
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            return -1;
        }

        return expansion_id;
    }

    private boolean insertAbbreviationExample(String[] context, int abbr_index, int abbr_id, int expansion_id, Connection conn) {
        try {
            boolean ourConn = (conn == null);
            if(ourConn) {
                conn = connect();
                conn.setAutoCommit(false);
            }

            if(
                !abbreviationExists(abbr_id, null) ||
                !expansionExists(abbr_id, null) ||
                !isExpansionFor(expansion_id, abbr_id, null)
            ) {
                if(ourConn) { close(conn, false); }
                return false;
            }

            String query_insert = "INSERT INTO context SET expansion_id=?, word_id=?, distance=?, count=?";
            String query_update = "UPDATE context SET count=? WHERE expansion_id=? AND word_id=? AND distance=?";
            for(int i=0; i<context.length; i++) {
                if(i == abbr_index) { continue; }

                int distance = i - abbr_index;
                int word_id = insertWord(context[i], conn);
                if(word_id == -1) {
                    if(ourConn) { close(conn, false); }
                    return false;
                }

                int count = countWordOccurrencesAtDistance(expansion_id, distance, word_id) + 1;

                PreparedStatement stmt;
                if(count == 1) {
                    stmt = conn.prepareStatement(query_insert);
                    stmt.setInt(1, expansion_id);
                    stmt.setInt(2, word_id);
                    stmt.setInt(3, distance);
                    stmt.setInt(4, count);
                } else {
                    stmt = conn.prepareStatement(query_update);
                    stmt.setInt(1, count);
                    stmt.setInt(2, expansion_id);
                    stmt.setInt(3, word_id);
                    stmt.setInt(4, distance);
                }

                if(stmt.executeUpdate() == 0) {
                    if(ourConn) { close(conn, false); }
                    return false;
                }
            }

            if(ourConn) { close(conn, true); }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    boolean insertAbbreviationExample(String[] context, int abbr_index, int expansion_id) {
        try {
            Connection conn = connect();
            conn.setAutoCommit(false);

            int abbr_id = insertAbbreviation(context[abbr_index], conn);
            if(abbr_id == -1) {
                close(conn, false);
            } else if(!insertAbbreviationExample(context, abbr_index, abbr_id, expansion_id, conn)) {
                close(conn, false);
            } else {
                close(conn, true);
                return true;
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    boolean insertAbbreviationExample(String[] context, int abbr_index, String new_expansion) {
        try {
            Connection conn = connect();
            conn.setAutoCommit(false);

            int abbr_id = insertAbbreviation(context[abbr_index], conn);
            if(abbr_id == -1) {
                close(conn, false);
            } else {
                int expansion_id = insertExpansion(new_expansion, abbr_id, conn);
                if(expansion_id == -1) {
                    close(conn, false);
                } else if(!insertAbbreviationExample(context, abbr_index, abbr_id, expansion_id, conn)) {
                    close(conn, false);
                } else {
                    close(conn, true);
                    return true;
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return false;

    }
}
