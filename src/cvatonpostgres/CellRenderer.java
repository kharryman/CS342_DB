/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cvatonpostgres;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Keith Harryman
 */
public class CellRenderer extends DefaultTableCellRenderer {

    CvatJFrame cvatJFrame;

    public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
        //if (cvatJFrame.myFrame.selectedTable.equals("bolsTable") || cvatJFrame.myFrame.selectedTable.equals("bolVehiclesTable")  || cvatJFrame.myFrame.selectedTable.equals("loadedVehiclesTable")) {
            
            if (obj.toString().split(" ")[1].toUpperCase().equals("LOAD")) {
                cell.setBackground(Color.CYAN);
            } else if (obj.toString().split(" ")[1].toUpperCase().equals("UNLOAD")) {
                cell.setBackground(Color.LIGHT_GRAY);
            } 
            cell.setForeground(Color.black);
            ((DefaultTableCellRenderer) cell).setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        /*}
        else{
            ((DefaultTableCellRenderer) cell).setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        }
*/
        return cell;
    }
}
