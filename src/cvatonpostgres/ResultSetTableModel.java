package cvatonpostgres;

/**
 * Table Model based on JDBC 2.0 : An JTable model is defined for a SQL
 * statement.
 */

import java.sql.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class ResultSetTableModel extends AbstractTableModel {

    public static final long serialVersionUID = 87823239;

    ResultSetTableModel(String sql, JComboBox edItems) {
        this.sql = sql;
        editableItems = edItems;

        //String  url = "jdbc:oracle:thin:@delphi.cs.csubak.edu:1521:dbs01",
        //        user="TGradebook", passwd="c3m4p2s";
        try {
                // DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            // cnn = DriverManager.getConnection(url, user, passwd);
            cnn = DBConnection.getConnection();
            cnn.setAutoCommit(true);
            stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    void getResultSet() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            resultSet = DBConnection.getResultSet(sql);
            getColumnTitles();
            resultSet.last();
            recordCount = resultSet.getRow();
            resultSet.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void reload() {
        getResultSet();
        currentRow = 0;
        fireTableDataChanged();
    }

    void getColumnTitles() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            columnCount = meta.getColumnCount();
            columnTitles = new Vector<String>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                columnTitles.add(meta.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCellEditable(int row, int col) {
        if ( Globals.loadingTime ) return true;
        // Notice that column 0, 1, 2, 3, 4, 5 should not allowed to change.
        // They are CID/CRN, SID, Last, firt name, Ranking and Total,and need
        // no changes. Ranking and total are calculated by stored procedure.
        // Starting editable coloumn is 6 which is letter grade.
        if ( col >= 0 && col < 6 ) return false;
        if ( col == columnCount - 1 ) return true;
        
        if (col == 6 + editableItems.getSelectedIndex()) return true;
        else return false;
    }

    public int getRowCount() {
        return recordCount;
    }

    public int getColumnCount() {
        return columnTitles.size();
    }

    public String getColumnName(int col) {
        return columnTitles.get(col);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        // System.out.printf("Column[%d] =%s\n", col,  getColumnName(col));
        if (col == columnCount - 1) {
            return String.class;
        }
        switch (col) {  // jTable's column numbers start with 0. The first two columns are removed.
            // The first coloum is 2. The resultset col start from 1, no column is removed.
            case 0:
            case 1:
            case 4:
                return Integer.class;  // For Rank column
            case 2:
            case 3:
            case 6:
                return String.class; // For last, firstname, and Grade 
            default:
                return Float.class;    // for homeworks, midterms and final
        }
    }

    public Object getValueAt(int row, int col) {
        try {
            if ( resultSet.isClosed() ) return null;
            resultSet.relative(row - currentRow);
            currentRow = row;
            //System.out.printf("getValueAt( %d, %d)\n", row, col);
            // If col is comments column.
            if (col == columnCount - 1) {
                return resultSet.getString(col + 1);
            }
            switch (col) {
                case 0:
                case 1:
                case 4: // For Rank column in JTable whose column numbers start with 0
                    return resultSet.getInt(col + 1);
                case 2:
                case 3:
                case 6: // For last, firstname, letter Grade and comments 
                    return resultSet.getString(col + 1);
                default: // for total, assignments midterms and final columns
                    return resultSet.getFloat(col + 1);
            }
        } catch (SQLException e) { //System.out.printf("Error in getValue(%d, %d)\n", row, col); 
            e.printStackTrace();
        }
        return " ";
    }

    public String resultRowToString(int row) {
        String tmp = "";
        try {
            resultSet.relative(row - currentRow);
            currentRow = row;
            for (int i = 1; i <= columnCount; i++) {
                tmp = tmp + resultSet.getString(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public void setValueAt(Object obj, int row, int col) {
        if ( ! isCellEditable( row, col ))  return;
        try {
            resultSet.relative(row - currentRow);
            currentRow = row;
        // The column count changes, and the comment is not Float class,
            // Thie followwing if-statement will shorten the code.
            if (col == columnCount - 1) {
                resultSet.updateString(col + 1, (String) obj);
            } else {
                switch (col) {
                    case 0:
                    case 1:
                    case 4: // For cid, sid, Rank column. Column 0 and 1 are removed for this applicaiton
                        resultSet.updateInt(col + 1, (int) obj);
                        break;
                    // last, firstname, letter Grade and comments 
                    case 2:
                    case 3:
                    case 6:
                        resultSet.updateString(col + 1, (String) obj);
                        break;
                    default: // for total, assignments midterms and final columns
                        resultSet.updateFloat(col + 1, Float.parseFloat(obj.toString()));
                        break;
                }
            }
            Globals.loadingTime = true;
            resultSet.updateRow();
            Globals.loadingTime = false;
        } catch (SQLException e) {
            Globals.loadingTime = false;
            e.printStackTrace();
        }
    }

    public void addRow(Object[] newValues) {
        try {
            resultSet.moveToInsertRow();
            for (int i = 0; i < newValues.length; i++) // resultSet.updateString( i+1, newValues[i] );
            {
                switch (i) {
                    case 4: // For Rank column
                        resultSet.updateInt(i + 1, (int) newValues[i]);
                    case 2:
                    case 3:
                    case 6:  // For last, firstname, and Grade 
                        resultSet.updateString(i + 1, (String) newValues[i]);
                    default: // for total, assignments midterms and final columns
                        resultSet.updateFloat(i + 1, (float) newValues[i]);
                }
            }
            resultSet.insertRow();
            fireTableDataChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // The parameter is the model row, correcsponding to row in the current display of table. 
    public Object removeRow(int row) {
        try {
            resultSet.absolute(row + 1);  // currentRow = row;
            resultSet.deleteRow();
            // currentRow = 0;
            recordCount--;
         // getResultSet();
            // fireTableDataChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Hello";
    }

    public void saveData() {
        try {
            cnn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void requery(String sql) {
        this.sql = sql;
        getResultSet();
        fireTableStructureChanged();
        //fireTableDataChanged();
        currentRow = 0;
    }

    public void removeRowBy(int key) {
        try {
            cstmt = cnn.prepareCall("{ call removeAddressBy (?) }");
            cstmt.setInt(1, key);
            cstmt.executeUpdate();
            reload();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int nextAddressID() {
        try {
            cstmt = cnn.prepareCall("{? = call nextAddressID}");
            cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
            cstmt.executeQuery();
            int n = cstmt.getInt(1);
            return n;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void clear() {
        try { resultSet.close(); } catch (Exception e) { e.printStackTrace();}
        fireTableDataChanged();
    }
    // Data members
    String sql = null;
    Connection cnn;
    Statement stmt;
    CallableStatement cstmt;
    ResultSet resultSet = null;
    Vector<String> columnTitles;
    int currentRow = 0, columnCount = 0, recordCount = 0;
    boolean sortAsc = true;
    JComboBox editableItems = null;
}
