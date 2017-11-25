/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cvatonpostgres;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Keith Harryman
 */
public class SimpleTableModel extends AbstractTableModel{
    
    private List<List<String>> myTable;
    private List<String> myColumns;
    
    SimpleTableModel(List<List<String>> myTable, List<String> columns){        
        this.myTable = myTable;
        this.myColumns = columns;
    }
    
    @Override
    public String getColumnName(int col) {
       return this.myColumns.get(col);
    }

    @Override
    public int getRowCount() {
           return this.myTable.size();
    }

    @Override
    public int getColumnCount() {
        int ret=0;
        if (this.myTable.size()>0){
           ret = this.myTable.get(0).size();
        }
        return ret;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        return this.myTable.get(rowIndex).get(columnIndex);
    }
    
}
