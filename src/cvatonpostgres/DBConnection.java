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
    public static String hostName = "Postgres"; 
    public static String dbInstanceName = "cvat";   // database name.
    public static String dbUser = "cvat", 
            passwd = "c3m4p2s",
            url = "jdbc:postgresql://localhost:5432/cvat";

    public static boolean isConnected() {
        return  ( cnn != null ) ;
    }

    public static Connection getConnection() {
        try {
            System.out.println("getConnection called");
            if (cnn != null && cnn.isValid(3)) {
                return cnn;
            }
            cnn = DriverManager.getConnection(url, dbUser, passwd ) ;
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
            // stmt = cnn.createStatement( ); //ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // System.out.printf("SQL :", sql);
            ResultSet res = stmt.executeQuery(sql);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //TO HELP ORDER BY FOREIGN KEY,AND REFERENTIAL CONTRAINTS:
    public static Vector<String> getAllTableNames() {
        ResultSet res = getResultSet("select tname from vtable_in_order");
        Vector<String> vAllTables = new Vector<String>(10); // All table names in the schema.   
        resultSetToVector(res, vAllTables);
        return vAllTables;

    }
    
     // The stored function getRecordCount() cannot be implemented in postgres due to dynamic sql statement
     // "SELECT COUNT(*) INTO cnt FROM x_table" is not implemented. We have to use different way.
     // A temporary method is used that will not work if a table is added or removed from database.
    public static int getRecordCountsOfAllTables() {
        try {
            cstmt = cnn.prepareCall("{ ? = call getRecordCount( ) }");
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.execute();
            return cstmt.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void resultSetToVector(ResultSet res, Vector<String> v) {
        try {
            String newName;
            boolean save = true; 
            while (res.next()) {
                 save = true;
                 newName = res.getString(1);
                 for ( int i = 0 ; i < v.size(); i ++) 
                     if (newName.compareTo(v.get(i)) == 0 ) {
                     save = false; 
                     break;
                    }
                if ( save ) v.add(newName) ;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void resultSetToVector(ResultSet res, Vector<String> v1, Vector<String> v2) {
        try {
            while (res.next()) {
                v1.add(res.getString(1));
                v2.add(res.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void orderTableNamesByRefConstraints(Vector<String> allTable, Vector<String> pkTable, Vector<String> fkTable, Vector<String> resultTable) {
        String name = null;
        int pkPos, fkPos;
        boolean progressMade = false;  // True if one or more tabls is added to resultTable
        while (allTable.size() > 0) {
            for (int i = 0; i < allTable.size(); i++) {
                name = allTable.get(i);
                fkPos = isAt(name, fkTable);
                pkPos = isAt(name, pkTable);
                if (fkPos < 0) {
                    resultTable.add(name);
                    allTable.remove(i);
                    progressMade = true;

                    // Remove all referential constraints in which name appeared as table referenced
                    // by other tables in their foreign key constraints.
                    while (pkPos >= 0) {
                        // move one referential constraint with PK table = name
                        pkTable.remove(pkPos);
                        fkTable.remove(pkPos);
                        // Find out whether the  same table appear in another ref. constraint.
                        // If it does, remove the ref. constraint again.
                        pkPos = isAt(name, pkTable);
                    }
                }
            }

            // If there is a recurisve referential constraint then the table cannot be added in.
            // The recursive ref. constraint is  caused by the first row in pkTable and fkTable,
            // Move the first referential constraint will proceed the process.
            if (!progressMade) {
                pkTable.remove(0);
                fkTable.remove(0);
            }
            progressMade = false;

        }
    }

    public static int isAt(String str, Vector<String> v) {
        if (str == null || v == null || v.size() < 1) {
            return -1;
        }
        for (int i = 0; i < v.size(); i++) {
            if (str.equals(v.get(i))) {
                return i;
            }
        }

        return -1;
    }

}
