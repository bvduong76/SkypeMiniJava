/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author NQH
 */
public class ConnectionUtils {
    private static final String host="localhost:1433";
    private static final String dbName="DACK_JAVA";
    private static final String username="sa";
    private static final String password="123";
    public static Connection getConnection() throws ClassNotFoundException, SQLException{
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        //jdbc:sqlserver://localhost:1433;databaseName=DACK_JAVA
        String connectionURL = "jdbc:sqlserver://" + host+";databaseName="+dbName ;
        Connection conn = DriverManager.getConnection(connectionURL, username,password);
        return conn;
    }
    

}
