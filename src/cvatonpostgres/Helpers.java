/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cvatonpostgres;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Keith Harryman
 */
public class Helpers {
    private CvatJFrame cvatJframe;
    
    public Helpers(CvatJFrame _cvatJFrame){
        this.cvatJframe = _cvatJFrame;
    }

    static void checkDataPath() {
        if (!new File(Globals.cvatData).exists()) {
            boolean succ = new File(Globals.cvatData + "/onlineData").mkdirs();
            if (!succ) {
                System.exit(-1);
            }
            new File(Globals.cvatData + "/backupData").mkdir();
            //System.out.printf("After create two data folders\n");
        }
    }

    static String getFileName(String dir) {
        JFileChooser chooser = new JFileChooser(dir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "dat", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //System.out.println("getPath() : " + chooser.getSelectedFile().getName());
            //System.out.println("getAbsolutePath() : " + chooser.getSelectedFile().getAbsolutePath());
            //try { System.out.println("getCanonicalPath() : " + chooser.getSelectedFile().getCanonicalPath());} catch (Exception e) {}
            return chooser.getSelectedFile().getAbsolutePath(); // .getName() - File name only;
        }
        return null;
    }

    static void printFileOnTextArea(String fileName, JTextArea ta) {
        String line = null;
        BufferedReader in = null;
        int cnt = 0;
        ta.setText("");
        try {
            in = new BufferedReader(new FileReader(fileName));
            while ((line = in.readLine()) != null) {
                ta.append(line + "\n");
                cnt++;
            }
        } catch (Exception e) {
        }
        try {
            in.close();
        } catch (Exception e2) {
        }
        ta.append(String.format("\nFile [%s] contains %d lines.\n",
                fileName.substring(fileName.lastIndexOf("/") + 1), cnt));
    }

    public String infoBox()
    {
        String[] values = {"BOTTOM", "TOP"};
        //JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        Object selected = JOptionPane.showInputDialog(null, "LOAD TOP OR BOTTOM?", "Selection", JOptionPane.DEFAULT_OPTION, null, values, 0);
        return selected.toString();
    }
    
    public void infoMessage(String title){
        JOptionPane.showMessageDialog(null, "", title, JOptionPane.INFORMATION_MESSAGE);       
        
    }
    

}
