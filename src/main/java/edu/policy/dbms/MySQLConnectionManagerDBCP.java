package edu.policy.dbms;


import edu.policy.common.PCDCConstants;
import edu.policy.common.PCDCException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnectionManagerDBCP {

    private static final MySQLConnectionManagerDBCP _instance = new MySQLConnectionManagerDBCP();
    private static String SERVER;
    private static String PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;
    private static Connection connection;

    private final Logger logger = LogManager.getLogger(MySQLConnectionManagerDBCP.class);

    private MySQLConnectionManagerDBCP() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(String.valueOf
                    (Paths.get(PCDCConstants.CRED_HQ + PCDCConstants.MYSQL_DBMS)) + ".properties");

            logger.info(String.format("MySQL configuration file: %s",
                    (Paths.get(PCDCConstants.CRED_HQ + PCDCConstants.MYSQL_DBMS)) + ".properties") );


            Properties props = new Properties();
            props.load(inputStream);

            SERVER = props.getProperty("server");
            PORT = props.getProperty("port");
            DATABASE = props.getProperty("database");
            USER = props.getProperty("user");
            PASSWORD = props.getProperty("password");

            logger.info(String.format("SERVER: %s; PORT: %s", SERVER, PORT));
            logger.info("--- user " + USER + " password " + PASSWORD + " database " + DATABASE + "---");

        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static MySQLConnectionManagerDBCP getInstance() {
        return _instance;
    }


    public Connection getConnection() throws PCDCException {

        if (connection != null)
            return connection;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
//            Class.forName("org.mariadb.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BasicDataSource dataSource = new BasicDataSource();

            dataSource.setUrl(String.format("jdbc:mysql://%s:%s/mysql", SERVER, PORT));
            dataSource.setUsername(USER);
            dataSource.setPassword(PASSWORD);

            connection = dataSource.getConnection();

            logger.info("--- Connected to MySQL DATABASE on server " + SERVER + " user " + dataSource.getUsername() + "---");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new PCDCException("Error Connecting to MySQL");
        }
    }
}
