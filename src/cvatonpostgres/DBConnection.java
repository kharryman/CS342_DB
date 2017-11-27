package cvatonpostgres;

/**
 * Database Connection
 */
import java.sql.*;
import java.sql.SQLException;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class DBConnection {

    public static Connection cnn = null;
    public static Statement stmt = null;
    public static CallableStatement cstmt;
    public static String hostName = "postgres";
    public static String dbInstanceName = "cvat";   // database name.
    public static String dbUser = "postgres";
    public static String passwd = "c3m4p2s";
    //public static String url = "jdbc:postgresql://delphi.cs.csub.edu:5432";
    public static String url = "jdbc:postgresql://localhost:5432/cvat";
    //public static String url = "jdbc:oracle:thin:@delphi.cs.csubak.edu:1521:dbs01";

    public static boolean isConnected() {
        return (cnn != null);
    }

    public static Connection getConnection() {
        try {
            System.out.println("getConnection called");
            if (cnn != null && cnn.isValid(3)) {
                return cnn;
            }
            cnn = DriverManager.getConnection(url, dbUser, passwd);
            System.out.println("cnn=" + cnn);
            cnn.setAutoCommit(true);
            return cnn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getResultSet(String sql) {
        try {
            if (cnn == null) {
                getConnection();
            }
            stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet res = stmt.executeQuery(sql);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
     public static void executeSQL(String sql) {
        try {
            if (cnn == null) {
                getConnection();
            }
            stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
