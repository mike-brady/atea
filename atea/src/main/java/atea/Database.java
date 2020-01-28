package atea;

import java.sql.*;
import java.util.ArrayList;

final class Database {
    private String host;
    private String username;
    private String password;

    Database(String host, String username, String password) throws SQLException {
        this.host = host;
        this.username = username;
        this.password = password;

        // Verify the credentials
        connect().close();
    }

    private Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/atea", username, password);
        conn.setAutoCommit(false);

        return conn;
    }

    private void close(Connection conn, boolean commit) throws SQLException {
        if(commit) {
            try {
                conn.commit();
            } catch(SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } else {
            conn.rollback();
        }
        conn.close();
    }

    private int insert(PreparedStatement stmt, Connection conn) throws SQLException {
        boolean ourConn = (conn == null);
        if (ourConn) {
            conn = connect();
        }

        stmt.executeUpdate();
        ResultSet rset = stmt.getGeneratedKeys();

        // if not successful
        if (!rset.first()) {
            if (ourConn) {
                close(conn, false);
            }
            return -1;
        }

        int result = rset.getInt(1);

        if(ourConn) { close(conn, true); }

        return result;
    }


    /**
     * Determines if the provided string of characters is an existing abbreviation in the database
     * @param  chars A string to be checked.
     * @return       The id of the abbreviation if chars is found in the database, otherwise -1.
     */
    int abbreviationExists(String chars) throws SQLException {
        int id = -1;
        Connection conn = connect();

        String query = "SELECT id FROM abbreviations WHERE value=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, chars);
        ResultSet rset = stmt.executeQuery();
        if(rset.first()) { id = rset.getInt("id"); }

        conn.close();

        return id;
    }

    private boolean abbreviationExists(int id, Connection conn) throws SQLException {
        boolean ourConn = (conn == null);
        if(ourConn) { conn = connect(); }

        String query = "SELECT id FROM abbreviations WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        boolean result = stmt.execute();

        if(ourConn) { close(conn, true); }

        return result;
    }

    int expansionExists(String chars) throws SQLException {
        int id = -1;
        Connection conn = connect();

        String query = "SELECT id FROM expansions WHERE value=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, chars);
        ResultSet rset = stmt.executeQuery();
        if(rset.first()) { id = rset.getInt("id"); }

        conn.close();

        return id;
    }

    private boolean expansionExists(int id, Connection conn) throws SQLException {
        boolean ourConn = (conn == null);
        if(ourConn) { conn = connect(); }

        String query = "SELECT id FROM expansions WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        boolean result = stmt.execute();

        if(ourConn) { close(conn, true); }

        return result;
    }

    private boolean isExpansionFor(int expansion_id, int abbr_id, Connection conn) throws SQLException {
        boolean ourConn = (conn == null);
        if(ourConn) { conn = connect(); }

        String query = "SELECT abbreviation_id FROM abbreviation_expansion WHERE abbreviation_id=? AND expansion_id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, expansion_id);
        boolean result = stmt.execute();

        if(ourConn) { close(conn, true); }

        return result;
    }

    boolean isCommonWord(String word) throws  SQLException {
        Connection conn = connect();

        String query = "SELECT id FROM words WHERE value=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, word);
        boolean result = stmt.execute();

        conn.close();

        return result;
    }

    String[] getCommonWords() throws SQLException {
        ArrayList<String> words = new ArrayList<>();

        Connection conn = connect();

        String query = "SELECT value FROM common_words";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rset = stmt.executeQuery();
        while(rset.next()) {
            words.add(
                    rset.getString("value")
            );
        }
        conn.close();

        return words.toArray( new String[words.size()] );
    }

    /**
     * Gets all expansions and creates Expansion objects for a given abbreviation.
     * @param abbr_id   The id of the abbreviation to get expansions for.
     * @return          An ArrayList of Expansion objects.
     */
    ArrayList<Expansion> getExpansions(int abbr_id) throws SQLException {
        ArrayList<Expansion> expansions = new ArrayList<>();

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

        return expansions;
    }

    int insertAbbreviation(String chars) throws SQLException {
        return insertAbbreviation(chars, null);
    }

    private int insertAbbreviation(String chars, Connection conn) throws SQLException {
        int id = -1;
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
     * @return          The id of the expansion or -1 on failure.
     */
    private int insertExpansion(String expansion, Connection conn) throws SQLException {
        Boolean ourConn = (conn == null);
        if (ourConn) {
            conn = connect();
        }

        int expansion_id = expansionExists(expansion);
        if (expansion_id != -1) {
            if (ourConn) {
                close(conn, false);
            }
            return expansion_id;
        }

        String query = "INSERT INTO expansions SET value=?";
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, expansion);

        expansion_id = insert(stmt, conn);

        if (ourConn) {
            close(conn, false);
        }

        return expansion_id;
    }

    private void insertAbbreviationExpansion(int abbr_id, int expansion_id, Connection conn) throws SQLException {
        Boolean ourConn = (conn == null);
        if (ourConn) {
            conn = connect();
        }

        if(!abbreviationExists(abbr_id, conn)) {
            throw new SQLException("Abbreviation does not exist.");
        }

        if(!expansionExists(expansion_id, conn)) {
            throw new SQLException("Expansion does not exist.");
        }

        String query = "SELECT expansion_id FROM abbreviation_expansion WHERE abbreviation_id=? AND expansion_id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, expansion_id);
        ResultSet rset = stmt.executeQuery();

        // if the expansion is not already attached to the abbreviation
        if(!rset.first()) {
            query = "INSERT INTO abbrevatiation_expansion SET abbreviation_id=?, expansion_id=?";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, abbr_id);
            stmt.setInt(2, expansion_id);
            if(!stmt.execute()) {
                if(ourConn) { close(conn, false); }
            } else {
                if(ourConn) { close(conn, true); }
            }
        }
    }


    public float getExpansionBaseProbability(int abbr_id, int expansion_id) throws SQLException {
        String query = "SELECT COUNT(id) / (SELECT COUNT(id) FROM examples WHERE abbreviation_id=?) AS probability FROM examples WHERE abbreviation_id=? AND expansion_id=?";
        Connection conn = connect();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, abbr_id);
        stmt.setInt(3, expansion_id);
        ResultSet rset = stmt.executeQuery();
        float probability = 0;
        if(rset.first()) { probability = rset.getFloat("probability"); }

        close(conn, true);

        return probability;
    }

    public float getExpansionKeywordScore(int abbr_id, int expansion_id, String keyword) throws SQLException {
        String query = "SELECT COUNT(id) / (SELECT COUNT(id) FROM examples WHERE abbreviation_id=? AND expansion_id=?) AS keyword_score FROM examples WHERE abbreviation_id=? AND expansion_id=? AND words LIKE ?";
        Connection conn = connect();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, expansion_id);
        stmt.setInt(3, abbr_id);
        stmt.setInt(4, expansion_id);
        stmt.setString(5, "%,"+keyword+",%");
        ResultSet rset = stmt.executeQuery();
        float keywordScore = 0;
        if(rset.first()) { keywordScore = rset.getFloat("keyword_score"); }

        close(conn, true);

        return keywordScore;
    }

    public boolean insertExample(Abbreviation abbr, String expansion) throws SQLException {
        Connection conn = connect();

        // TODO - Remove the commented line below
        // String abbreviation = abbr.getText().getWords()[abbr.getIndex()];
        String abbreviation = abbr.getValue();
        int abbr_id = abbreviationExists(abbreviation);
        if(abbr_id == -1) {
            abbr_id = insertAbbreviation(abbreviation, conn);
        }

        int expansion_id = expansionExists(expansion);
        if(expansion_id == -1) {
            expansion_id = insertExpansion(expansion, conn);
        }

        insertAbbreviationExpansion(abbr_id, expansion_id, conn);

        String query = "INSERT INTO examples SET abbreviation_id=?, expansion_id=?, words=?, abbr_index=?";
        PreparedStatement stmt = connect().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, expansion_id);
        stmt.setString(3, abbr.getText().getWordsAsCSV());
        stmt.setInt(4, abbr.getIndex());

        int id = insert(stmt, conn);
        close(conn, true);

        return true;
    }
}
