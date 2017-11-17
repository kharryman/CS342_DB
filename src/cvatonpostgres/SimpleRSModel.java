package cvatonpostgres;

import java.sql.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class SimpleRSModel extends AbstractTableModel {

    SimpleRSModel(String sql) {
        getResultSet(sql);
        getColumnTitles();
    }

    void getResultSet(String sql) {
        try {
            resultSet = DBConnection.getResultSet(sql);
            resultSet.last();
            rowCount = resultSet.getRow();
            ResultSetMetaData meta = resultSet.getMetaData();
            columnCount = meta.getColumnCount();
            resultSet.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }
    public String getColumnName(int col) {
        return vColTitles.get(col);
    }
    
    public Object getValueAt(int row, int col) {
        try {
            resultSet.relative(row - currentRow);
            currentRow = row;
            return resultSet.getString(col + 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return " ";
    }
    /* 
     public void  setValueAt( Object obj, int row, int col ) {
     try {
     resultSet.relative (row - currentRow); currentRow = row;
     resultSet.updateString(col+1, (String) obj);
     resultSet.updateRow();
     cnn.commit();
     } catch (SQLException e) { e.printStackTrace(); }
     }
     */

    public void requery(String sql) {
        getResultSet(sql);
        fireTableDataChanged();
        // currentRow = 0;
    }
    void getColumnTitles() {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                vColTitles.add(meta.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Data members
    String sql = null;
    ResultSet resultSet;
    int currentRow = 0, columnCount = 0, rowCount = 0;
    Vector<String> vColTitles = new Vector<String>();
}
