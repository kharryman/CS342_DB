package cvatonpostgres;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.table.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;

public class ResultSetTextFields implements FocusListener { 

    JTextField txtFd[]	    = null;
    ResultSet resultSet		= null;
    boolean	txtfdchanged	= false;
    Color	changedColor	= Color.RED;
    int		rsStart, rsEnd; 

    public ResultSetTextFields ( ResultSet rs, JTextField txtFd[] ) {
	setup( rs, txtFd, 1, txtFd.length + 1 );
    }
    public void setup ( ResultSet rs, JTextField txtFd[], int rsStart, int rsEnd ) {
	this.rsStart = rsStart; this.rsEnd = rsEnd;
	resultSet = rs;
	this.txtFd = txtFd; 

	try {
            Object obj;
	    resultSet.first();
	    for ( int i = 0, j = rsStart; j < rsEnd; i ++, j++ ) {
                obj = resultSet.getString(j);
		txtFd[i].setText( obj == null? "" : obj.toString()); 
		txtFd[i].addFocusListener( this );
	    }
	} catch ( Exception e) { e.printStackTrace(); }
    }
    public void focusGained (FocusEvent e ) {}

    public void focusLost ( FocusEvent ev ) {
	Object obj = ev.getSource();
	for ( int i = 0; i < txtFd.length; i ++ )
	    if ( obj == txtFd[i] ) 
		try {
		    if ( txtFd[i].getText().compareTo( resultSet.getString( i + rsStart )) != 0 ) {
			txtfdchanged = true ;
			txtFd[i].setForeground( changedColor );
		    }
		} catch (Exception e) { e.printStackTrace(); }
    }

    public void save ( ) {
	if ( ! txtfdchanged ) return;
	txtfdchanged = false;
	try { 
	    for ( int i = 0; i < txtFd.length; i++ ) 
		if ( txtFd[i].getForeground() == changedColor ) {
		    //resultSet.updateString(i+rsStart, txtFd[i].getText()) ;
		    resultSet.updateInt(i+rsStart, Integer.parseInt(txtFd[i].getText().toString()));
                    txtFd[i].setForeground( Color.BLACK );
                    
		}

	    resultSet.updateRow();
	    //DBConnection.cnn.commit();
	} catch (Exception e) { e.printStackTrace(); }
    }
   public void clear() {
       // resultSet.close();
       for ( int i = 0; i < this.txtFd.length; i ++ ) txtFd[i].setText("");
   }
}

