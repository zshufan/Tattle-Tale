package edu.policy.model.data;

import edu.policy.dbms.MySQLConnectionManager;
import edu.policy.dbms.MySQLConnectionManagerDBCP;

import java.sql.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class User {

    private UUID userId;

    private String uName;

    private boolean isPagination;

    private int tuple_start;

    private int tuple_end;

    private static final Logger logger = LogManager.getLogger(User.class);

    private static Connection connection = MySQLConnectionManagerDBCP.getInstance().getConnection();

    public User() {
    }

    public User(String uName) {
        this.uName = uName;
    }

    public User(UUID userId, String uName) {
        this.userId = userId;
        this.uName = uName;
    }

    public User(UUID userId, String uName, int tuple_start, int tuple_end, boolean isPagination) {
        this.userId = userId;
        this.uName = uName;
        this.tuple_start = tuple_start;
        this.tuple_end = tuple_end;
        this.isPagination = isPagination;
    }

    public Hashtable<String, Double> retrieveNumericalMin (List<String> numericalSchema, String databaseName, String relationName) {

        Hashtable<String, Double> numericalAttrDomMin = new Hashtable<>();

        PreparedStatement queryStm = null;

        try {

            for (String attr: numericalSchema) {

                if (isPagination)
                    queryStm = connection.prepareStatement(String.format("SELECT MIN( %s ) AS MinDom FROM ( SELECT %s FROM %s.%s LIMIT %d, %d ) AS T ",
                            escapeReserved(attr), escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName),
                            tuple_start, tuple_end-tuple_start));
                else
                    queryStm = connection.prepareStatement(String.format("SELECT MIN( %s ) AS MinDom FROM %s.%s",
                            escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName)));

                ResultSet rs = null;
                try {
                    rs = queryStm.executeQuery();

                    rs.next();
                    numericalAttrDomMin.put(attr, rs.getDouble("MinDom"));
                } finally {
                    if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return numericalAttrDomMin;
    }

    public Hashtable<String, Double> retrieveNumericalMax (List<String> numericalSchema, String databaseName, String relationName) {

        Hashtable<String, Double> numericalAttrDomMax = new Hashtable<>();

        PreparedStatement queryStm = null;

        try {

            for (String attr: numericalSchema) {

                if (isPagination)
                    queryStm = connection.prepareStatement(String.format("SELECT MAX( %s ) AS MaxDom FROM ( SELECT %s FROM %s.%s LIMIT %d, %d ) AS T ",
                            escapeReserved(attr), escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName),
                            tuple_start, tuple_end-tuple_start));
                else
                    queryStm = connection.prepareStatement(String.format("SELECT MAX( %s ) AS MaxDom FROM %s.%s",
                            escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName)));

                ResultSet rs = null;
                try {
                    rs = queryStm.executeQuery();

                    rs.next();
                    numericalAttrDomMax.put(attr, rs.getDouble("MaxDom"));
                } finally {
                    if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return numericalAttrDomMax;
    }



    public Hashtable<String, Integer> retrieveCategoricalVarDomSize(List<String> categoricalSchema,
                                                                    String databaseName, String relationName) {

        Hashtable<String, Integer> attrDomSizeDict = new Hashtable<>();

        PreparedStatement queryStm = null;

        try {

            for (String attr: categoricalSchema) {

                if (isPagination)
                    queryStm = connection.prepareStatement(String.format("SELECT Count(T.%s) as DomSize FROM (SELECT DISTINCT %s FROM %s.%s LIMIT %d, %d) T",
                            escapeReserved(attr), escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName),
                            tuple_start, tuple_end-tuple_start));
                else
                    queryStm = connection.prepareStatement(String.format("SELECT count(DISTINCT %s ) AS DomSize FROM %s.%s",
                            escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName)));

                ResultSet rs = null;
                try {
                    rs = queryStm.executeQuery();
                    rs.next();
                    attrDomSizeDict.put(attr, rs.getInt("DomSize"));
                } finally {
                    if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return attrDomSizeDict;
    }

    public Hashtable<String, Integer> retrieveCategoricalVarDomSize(List<String> categoricalSchema, int limit,
                                                                    boolean isAscent, String databaseName, String relationName) {

        Hashtable<String, Integer> attrDomSizeDict = new Hashtable<>();

        PreparedStatement queryStm = null;
        try {

            for (String attr: categoricalSchema) {
                queryStm = null;
                if (isAscent)
                    queryStm = connection.prepareStatement(String.format("SELECT count(DISTINCT %s ) AS DomSize FROM ( " +
                                    "SELECT * FROM %s.%s order by tid asc limit %s) AS T;",
                            escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName), limit));
                else
                    queryStm = connection.prepareStatement(String.format("SELECT count(DISTINCT %s ) AS DomSize FROM ( " +
                                    "SELECT * FROM %s.%s order by tid dsec limit %s) AS T;",
                            escapeReserved(attr), preventSQLInjection(databaseName), preventSQLInjection(relationName), limit));


                ResultSet rs = null;
                try {
                    rs = queryStm.executeQuery();

                    rs.next();
                    attrDomSizeDict.put(attr, rs.getInt("DomSize"));
                } finally {
                    if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return attrDomSizeDict;
    }

    public List<String> retrieveSchema(String databaseName, String relationName) {

        List<String> schema = new ArrayList<>();

        PreparedStatement queryStm = null;
        try {

            if (isPagination)
                queryStm = connection.prepareStatement(String.format("SELECT * FROM %s.%s LIMIT %d, %d",
                        preventSQLInjection(databaseName), preventSQLInjection(relationName), tuple_start,
                        tuple_end-tuple_start));
            else
                queryStm = connection.prepareStatement(String.format("SELECT * FROM %s.%s",
                        preventSQLInjection(databaseName), preventSQLInjection(relationName)));

            ResultSet rs = null;

            try {
                rs = queryStm.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); i++)
                    schema.add(rsmd.getColumnName(i));

            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return schema;
    }

    public List<String> retrieveSchemaType(String databaseName, String relationName) {
        List<String> schemaType = new ArrayList<>();

        PreparedStatement queryStm = null;
        try {

            if (isPagination)
                queryStm = connection.prepareStatement(String.format("SELECT * FROM %s.%s LIMIT %d, %d",
                        preventSQLInjection(databaseName), preventSQLInjection(relationName), tuple_start,
                        tuple_end-tuple_start));
            else
                queryStm = connection.prepareStatement(String.format("SELECT * FROM %s.%s",
                        preventSQLInjection(databaseName), preventSQLInjection(relationName)));

            ResultSet rs = null;

            try {
                rs = queryStm.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); i++)
                    schemaType.add(rsmd.getColumnTypeName(i));

            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return schemaType;
    }

    public ResultSet retrieveTuple(String databaseName, String relationName, int tupleID) {

        ResultSet rs = null;
        PreparedStatement queryStm = null;
        try {

            logger.debug(String.format("Retrieving tuple of database (%s.%s: t_%d)", databaseName, relationName, tupleID));

            String query = String.format("SELECT * FROM %s.%s AS T WHERE T.tid = ?",
                    preventSQLInjection(databaseName), preventSQLInjection(relationName));

            queryStm = connection.prepareStatement(query);

            queryStm.setInt(1, tupleID);

            logger.debug(String.format("Start to execute query: %s", queryStm.toString()));
            rs = queryStm.executeQuery();

            logger.debug("Query execution finished");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        if (rs == null)
            logger.error("No query result.");
        assert rs != null;

        return rs;
    }

    public Tuple retrieveTuple(String databaseName, String relationName, int tupleID, List<String> schema) {

        Tuple tuple = new Tuple(databaseName, relationName, tupleID);
        PreparedStatement queryStm = null;
        try {

            logger.debug(String.format("Retrieving tuple of database (%s.%s: t_%d)", databaseName, relationName, tupleID));

            String query = String.format("SELECT * FROM %s.%s AS T WHERE T.tid = ?;",
                    preventSQLInjection(databaseName), preventSQLInjection(relationName));

            queryStm = connection.prepareStatement(query);

            queryStm.setInt(1, tupleID);

            ResultSet rs = null;

            try {
                rs = queryStm.executeQuery();

                while (rs.next()) {
                    for (String key : schema) {
                        tuple.addToTuple(key, rs.getString(key));
                    }
                }

            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        if (tuple.getTuple() == null)
            logger.error("No query result.");

        return tuple;
    }

    public PreparedStatement queryOnSingleCell(String databaseName, String relationName, int tupleID, String attribute) {

        PreparedStatement queryStm = null;
        try {

            logger.debug(String.format("Retrieving value of the cell (%s.%s: t_%d[%s])", databaseName, relationName, tupleID, attribute));

            String query = String.format("SELECT %s FROM %s.%s AS T WHERE T.tid = ?;",
                    escapeReserved(attribute), preventSQLInjection(databaseName), preventSQLInjection(relationName));

            queryStm = connection.prepareStatement(query);

            queryStm.setInt(1, tupleID);

            logger.debug(String.format("Start to execute query: %s", queryStm.toString()));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (queryStm == null)
            logger.warn("No query statement.");
        assert queryStm != null;
        return queryStm;
    }

    public void manipulationExecution(String query) {

        PreparedStatement queryStm = null;
        try {

            queryStm = connection.prepareStatement(query);

            queryStm.executeUpdate();

            logger.debug("Query execution finished.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (queryStm != null)
                try { queryStm.close(); } catch (SQLException e) {e.printStackTrace();}
        }
    }

    public PreparedStatement queryExecution(String query) {

        PreparedStatement queryStm = null;
        try {

            queryStm = connection.prepareStatement(query);

            logger.info(String.format("Start to execute query: %s", query));
            logger.debug("Query execution finished.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (queryStm == null)
            logger.warn("No query statement.");
        assert queryStm != null;

        return queryStm;
    }

    public List<PreparedStatement> multiQueryExecution(List<String> queries) {

        List<PreparedStatement> preparedStatements = new ArrayList<>();

        logger.info(String.format("Start to execute %d queries.", queries.size()));
        for (String q: queries) {
            logger.debug(String.format("Start to execute query: %s", q));
            PreparedStatement queryStm;

            try {
                queryStm = connection.prepareStatement(q);

                preparedStatements.add(queryStm);

                logger.debug("Query execution finished.");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        if (preparedStatements.isEmpty())
            logger.error("Empty query preparedStatements.");
        assert !preparedStatements.isEmpty();

        return preparedStatements;

    }

    // https://stackoverflow.com/questions/39892449/safe-way-to-use-table-name-as-parameter-in-jdbc-query
    private String preventSQLInjection(String tableName) {
        if (tableName.isEmpty())
            return null;
        else
            return tableName.replace("`", "``");
    }

    private String escapeReserved (String attributeName) {
        return "`" + attributeName + "`";
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", uName='" + uName + '\'' +
                '}';
    }
}
