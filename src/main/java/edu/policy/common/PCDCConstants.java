package edu.policy.common;

import edu.policy.dbms.MySQLConnectionManager;
import edu.policy.dbms.MySQLConnectionManagerDBCP;
import edu.policy.dbms.PGSQLConnectionManager;

import java.sql.Connection;

public class PCDCConstants {

    public static String DBMS_CHOICE;
    public static String DBMS_LOCATION;
    public static String DBMS_CREDENTIALS;
    public static String TABLE_NAME;
    public static String DATE_FORMAT;
    public static String TIME_FORMAT;
    public static String TIMESTAMP_FORMAT;

    public static final String MYSQL_DBMS = "mysql";
    public static final String PGSQL_DBMS = "postgres";

    public static final String SETTING = "settings";

    public static final String CONFIG = "config/";
    public static final String CRED_HQ = "credentials/";

    public static final String DEFAULT_OPEN_HIDE_FLAG = "open";

    private static Connection connection;



    public static Connection getDBMSConnection(){
        if (connection == null) {
            if(PCDCConstants.DBMS_CHOICE.equalsIgnoreCase(PCDCConstants.MYSQL_DBMS))
                connection = MySQLConnectionManagerDBCP.getInstance().getConnection();
            else if(PCDCConstants.DBMS_CHOICE.equalsIgnoreCase(PCDCConstants.PGSQL_DBMS))
                connection = PGSQLConnectionManager.getInstance().getConnection();
            else
                System.out.println("DBMS choice not set or unknown DBMS");
        }
        return connection;
    }

}


