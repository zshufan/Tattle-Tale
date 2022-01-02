package edu.policy.dbms;


import edu.policy.common.PCDCConstants;
import edu.policy.common.PCDCException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnectionManager {

    private static final MySQLConnectionManager _instance = new MySQLConnectionManager();
    private static String SERVER;
    private static String PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;
    private static Connection connection;

    private final Logger logger = LogManager.getLogger(MySQLConnectionManager.class);

    private MySQLConnectionManager() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(String.valueOf
                    (Paths.get(PCDCConstants.CRED_HQ + PCDCConstants.MYSQL_DBMS)) + ".properties");

            logger.info(String.format("MySQL configuration file: %s",
                    (Paths.get(PCDCConstants.CRED_HQ + PCDCConstants.MYSQL_DBMS)) + ".properties") );


            Properties props = new Properties();
            props.load(inputStream);

            SERVER = props.getProperty("server");
            PORT = props.getProperty("port");
            USER = props.getProperty("user");
            PASSWORD = props.getProperty("password");

            logger.info(String.format("SERVER: %s; PORT: %s", SERVER, PORT));
            logger.info("--- user " + USER + " password " + PASSWORD + "---");

        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static MySQLConnectionManager getInstance() {
        return _instance;
    }


    public Connection getConnection() throws PCDCException {
        if (connection != null)
            return connection;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/mysql?useServerPrepStmts=true&useCursorFetch=true", SERVER, PORT), USER, PASSWORD);
            logger.info("--- Connected to MySQL DATABASE on server " + SERVER + "---");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new PCDCException("Error Connecting to MySQL");
        }
    }
}
