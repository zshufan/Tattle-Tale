package edu.policy.dbms;

import edu.policy.common.PCDCConstants;
import edu.policy.common.PCDCException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PGSQLConnectionManager {

    private static PGSQLConnectionManager _instance = new PGSQLConnectionManager();
    private Properties props;
    private static String SERVER;
    private static String PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;
    private static Connection connection;

    private PGSQLConnectionManager() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(String.valueOf
                    (Paths.get(PCDCConstants.CRED_HQ + PCDCConstants.DBMS_LOCATION.toLowerCase(),
                            PCDCConstants.DBMS_CREDENTIALS.toLowerCase())) + ".properties");
            props = new Properties();
            props.load(inputStream);

            SERVER = props.getProperty("server");
            PORT = props.getProperty("port");
            DATABASE = props.getProperty("database");
            USER = props.getProperty("user");
            PASSWORD = props.getProperty("password");

        } catch (
                IOException ie) {
        }
    }

    public static PGSQLConnectionManager getInstance() {
        return _instance;
    }


    public Connection getConnection() throws PCDCException {
        if(connection != null)
            return  connection;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(
                    String.format("jdbc:postgresql://%s:%s/%s", SERVER, PORT, DATABASE), USER, PASSWORD);

            System.out.println("--- Connected to pSQL " + DATABASE + " on server " + SERVER + "---");


            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new PCDCException("Error Connecting to Postgres");
        }
    }
}
