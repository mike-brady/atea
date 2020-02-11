package atea;

import java.sql.*;
import java.util.ArrayList;

class Database {
    private final String host;
    private final String username;
    private final String password;
    private boolean autoCommit = false;

    Database(String host, String username, String password) throws SQLException {
        this.host = host;
        this.username = username;
        this.password = password;

        // Verify the credentials
        connect().close();
    }

    private Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/atea", username, password);
        conn.setAutoCommit(autoCommit);

        return conn;
    }

    private boolean validTable(String table) throws SQLException {
        Boolean found = false;

        Connection conn = connect();
        String query = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'atea' AND table_name = ?;";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, table);

        ResultSet rset = stmt.executeQuery();
        float probability = -1;
        if(rset.first()) {
            if(rset.getInt(1) > 0)
                found = true;
        }

        conn.close();

        return found;
    }

    private ResultSet select(Connection conn, String table) throws SQLException {
        return select(conn, table, new String[0], new String[0], new String[0]);
    }

    private ResultSet select(Connection conn, String table, String[] columns) throws SQLException {
        return select(conn, table, columns, new String[0], new String[0]);
    }

    private ResultSet select(Connection conn, String table, String[] whereColumns, String[] whereValues) throws SQLException {
        return select(conn, table, new String[0], whereColumns, whereValues);
    }

    private ResultSet select(Connection conn, String table, String[] columns, String[] whereColumns, String[] whereValues) throws SQLException {
        if(whereColumns.length != whereValues.length) {
            throw new ArrayIndexOutOfBoundsException("whereColumns and whereValues length do not match");
        }

        if(!validTable(table)) {
            throw new SQLException("Table '" + table + "' does not exist.");
        }

        String query = "SELECT";

        // columns
        if(columns.length == 0) {
            query += " *";
        } else {
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) {
                    query += ", ";
                }
                query += " ?";
            }
        }

        // FROM table
        query += " FROM " + table;

        // WHERE column=value AND column=value ...
        for(int i=0; i<whereColumns.length; i++) {
            if(i>0) {
                query += " AND";
            }
            query += " WHERE ?=?";
        }

        PreparedStatement stmt = conn.prepareStatement(query);
        int nextParameterIndex = 1;

        // columns
        for(int i=0; i<columns.length; i++) {
            stmt.setString(nextParameterIndex++, columns[i]);
        }

        // where clauses
        for(int i=0; i<whereColumns.length; i++) {
            stmt.setString(nextParameterIndex++, whereColumns[i]);
            stmt.setString(nextParameterIndex++, whereValues[i]);
        }

        ResultSet rset = stmt.executeQuery();

        return rset;
    }

    private ResultSet insert(Connection conn, String table, String[] columns, String[] values) throws SQLException {
        if(columns.length != values.length) {
            throw new ArrayIndexOutOfBoundsException("columns and values length do not match");
        }

        boolean ourConn = (conn == null);
        if(ourConn) { conn = connect(); }

        String query = "INSERT INTO ? (";

        // columns
        for(int i=0; i<columns.length; i++) {
            if(i>0) {
                query += ", ";
            }
            query += " ?";
        }

        query += ") VALUES (";

        // values
        for(int i=0; i<values.length; i++) {
            if(i>0) {
                query += ", ";
            }
            query += " ?";
        }

        PreparedStatement stmt = conn.prepareStatement(query);
        int nextParameterIndex = 1;

        stmt.setString(nextParameterIndex++, table);

        // columns
        for(int i=0; i<columns.length; i++) {
            stmt.setString(nextParameterIndex++, columns[i]);
        }

        // columns
        for(int i=0; i<values.length; i++) {
            stmt.setString(nextParameterIndex++, values[i]);
        }

        stmt.executeUpdate();

        ResultSet rset = stmt.getGeneratedKeys();

        if(ourConn) {
            conn.commit();
            conn.close();
        }
        return rset;
    }

    private int getRowId(String table, String column, String value) throws SQLException {
        Connection conn = connect();
        int id = -1;
        ResultSet rset = select(conn, table, new String[] {column}, new String[] {value});
        if(rset.first()) {
            id = rset.getInt("id");
        }

        conn.close();

        return id;
    }

    private boolean rowExists(String table, String column, String value) throws SQLException {
        Connection conn = connect();
        Boolean exists = select(conn, table, new String[] {column}, new String[] {value}).first();
        conn.close();

        return exists;
    }

    /**
     * Determines if the provided string of characters is an existing abbreviation in the database
     * @param  chars A string to be checked.
     * @return       The id of the abbreviation if chars is found in the database, otherwise -1.
     */
    int abbreviationExists(String chars) throws SQLException {
        return getRowId("abbreviations", "value", chars);
    }

    private boolean abbreviationExists(int id) throws SQLException {
        return rowExists("abbreviations", "id", Integer.toString(id));
    }

    int expansionExists(String chars) throws SQLException {
        return getRowId("expansions", "value", chars);
    }

    private boolean expansionExists(int id) throws SQLException {
        return rowExists("expansions", "id", Integer.toString(id));
    }

    boolean isCommonWord(String word) throws SQLException {
        return rowExists("words", "value", word);
    }

    private boolean isExpansionFor(int expansion_id, int abbr_id) throws SQLException {
        Connection conn = connect();
        ResultSet rset = select(
                conn,
                "abbreviation_expansion",
                new String[] {"abbreviation_id"},
                new String[] {"abbreviation_id", "expansion_id"},
                new String[] {Integer.toString(abbr_id), Integer.toString(expansion_id)}
                );

        boolean result = rset.first();
        conn.close();

        return result;
    }

    String[] getCommonWords() throws SQLException {
        ArrayList<String> words = new ArrayList<>();

        Connection conn = connect();
        ResultSet rset = select(conn, "common_words");
        while (rset.next()) {
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

        try {
            String query = "SELECT id, value FROM expansions JOIN abbreviation_expansion ON expansions.id=abbreviation_expansion.expansion_id WHERE abbreviation_expansion.abbreviation_id =?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, abbr_id);
            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                expansions.add(
                        new Expansion(
                                rset.getInt("id"),
                                rset.getString("value")
                        )
                );
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            expansions = new ArrayList<>();
        }

        conn.close();

        return expansions;
    }

    int insertAbbreviation(String chars) throws SQLException {
        return insertAbbreviation(chars, null);
    }

    private int insertAbbreviation(String chars, Connection conn) throws SQLException {
        int id = abbreviationExists(chars);
        // if the abbreviation is already in the DB, return its id
        if(id != -1) {
            return id;
        }

        ResultSet rset = insert(
                conn,
                "abbreviations",
                new String[] {"value"},
                new String[] {chars}
                );
        if(rset.first()) {
            id = rset.getInt(1);
        }

        return id;
    }

    /**
     * Inserts an expansion into the database if it does not already exist and links it to an abbreviation.
     * @param expansion The expansion to be inserted.
     * @return          The id of the expansion or -1 on failure.
     */
    private int insertExpansion(String expansion, Connection conn) throws SQLException {
        int id = expansionExists(expansion);
        if (id != -1) {
            return id;
        }

        ResultSet rset = insert(
                conn,
                "expansions",
                new String[] {"value"},
                new String[] {expansion}
                );
        if(rset.first()) {
            id = rset.getInt(1);
        }

        return id;
    }

    private void insertAbbreviationExpansion(int abbr_id, int expansion_id, Connection conn) throws SQLException {
        if(!abbreviationExists(abbr_id)) {
            throw new SQLException("Abbreviation does not exist.");
        }

        if(!expansionExists(expansion_id)) {
            throw new SQLException("Expansion does not exist.");
        }

        // if the expansion is not already attached to the abbreviation
        if(!isExpansionFor(expansion_id, abbr_id)) {
            insert(
                    conn,
                    "abbreviation_expansion",
                    new String[] {"abbreviation_id", "expansion_id"},
                    new String[] { Integer.toString(abbr_id), Integer.toString(expansion_id)}
            );
        }
    }



    public float getExpansionBaseProbability(int abbr_id, int expansion_id) throws SQLException {
        Connection conn = connect();

        String query = "SELECT COUNT(id) / (SELECT COUNT(id) FROM examples WHERE abbreviation_id=?) AS probability FROM examples WHERE abbreviation_id=? AND expansion_id=?";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, abbr_id);
        stmt.setInt(3, expansion_id);

        ResultSet rset = stmt.executeQuery();
        float probability = -1;
        if (rset.first()) {
            probability = rset.getFloat("probability");
        }

        conn.close();

        return probability;
    }

    public float getExpansionKeywordScore(int abbr_id, int expansion_id, String keyword) throws SQLException {
        Connection conn = connect();

        String query = "SELECT COUNT(id) / (SELECT COUNT(id) FROM examples WHERE abbreviation_id=? AND expansion_id=?) AS keyword_score FROM examples WHERE abbreviation_id=? AND expansion_id=? AND words LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, abbr_id);
        stmt.setInt(2, expansion_id);
        stmt.setInt(3, abbr_id);
        stmt.setInt(4, expansion_id);
        stmt.setString(5, "%,"+keyword+",%");

        ResultSet rset = stmt.executeQuery();
        float keywordScore = 0;
        if(rset.first()) {
            keywordScore = rset.getFloat("keyword_score");
        }

        conn.close();

        return keywordScore;
    }


    public void insertExample(Abbreviation abbr, String expansion) throws SQLException {
        Connection conn = connect();

        String abbreviation = abbr.getValue();
        try {
            int abbr_id = abbreviationExists(abbreviation);
            if (abbr_id == -1) {
                abbr_id = insertAbbreviation(abbreviation, conn);
            }

            int expansion_id = expansionExists(expansion);
            if (expansion_id == -1) {
                expansion_id = insertExpansion(expansion, conn);
            }

            insertAbbreviationExpansion(abbr_id, expansion_id, conn);

            insert(
                    conn,
                    "examples",
                    new String[]{
                            "abbreviation_id",
                            "expansion_id",
                            "words",
                            "abbr_index"},
                    new String[]{
                            Integer.toString(abbr_id),
                            Integer.toString(expansion_id),
                            abbr.getText().getWordsAsCSV(),
                            Integer.toString(abbr.getIndex())
                    }
            );
        }
        catch(SQLException ex) {
            conn.rollback();
            conn.close();

            throw ex;
        }

        conn.commit();
        conn.close();
    }
}
