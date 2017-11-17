/**
 * Backup and Restore table data to and from a text file.
 */
package cvatonpostgres;

import java.sql.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import wang.app.gradebookOnPostgres.DBConnection;

@SuppressWarnings("unchecked")
class ExportImport {

    static PBMornitor pbMtr = null;
    static JProgressBar progressBar = null;
    static Connection cnn = DBConnection.getConnection();
    static Statement stmt = null;
    static CallableStatement cStmt = null;
    static ResultSet res = null;
    static ResultSetMetaData meta = null;

    static Vector<String> colNames = null;
    // static Vector<String>  vAllTableNames = null; 
    static JComboBox<String> cbxTables = null;
    static String colSeparator = "|";
    static JTextArea taContents = null;
    static String fileName = null;
    static int totalNumOfRecords = 0;
    static int gSucceededCount, gFailedCount;

    public static int resultSetToFile(ResultSet res, FileWriter fwtr, String colSeparator,
            int colCount, PBMornitor pbMtr) {
        return resultSetToFile(res, fwtr, "", colSeparator, "\n", colCount, pbMtr);
    }

    // Output rows of result set into a file. The format of a row beginning with rowBegin string and ending with
    // rowEnd string. the columns are separated by column separator. To output records as a HTML table rows,
    // call the function with following parameters:   
    // 			resultSetToFile( res, wftr, "<TR><TD>", "</TD><TD>", </TD></TR>\n"
    public static int resultSetToFile(ResultSet res, FileWriter fwtr, String rowBegin,
            String colSeparator, String rowEnd, int colCount, PBMornitor pbMtr) {
        String colString = null;
        gSucceededCount = gFailedCount = 0;
        try {
            while (res.next()) {
                pbMtr.setValue(pbMtr.getValue() + 1);

                // Write the beining of a line for a record.
                colString = res.getString(1);
                colString = colString == null ? " " : colString.replaceAll("[\n\r\f]", " ");
                fwtr.write(rowBegin + colString);

                // write rest of columns separated with column separator character.
                for (int i = 2; i <= colCount; i++) {
                    colString = res.getString(i);
                    colString = colString == null ? " " : colString.replaceAll("[\n\r\f]", " ");
                    fwtr.write(colSeparator + " " + colString);
                }
                fwtr.write(rowEnd);
                gSucceededCount++;
            }
            return gSucceededCount;
        } catch (SQLException e) {
            gFailedCount++;
        } catch (IOException e2) {
            gFailedCount++;
        }
        return gSucceededCount;
    }

    public static void postMessage(String str) {
        taContents.append(str);
        taContents.paintImmediately(taContents.getBounds());
        try {
            Thread.sleep(5);
        } catch (Exception e) {
        }
    }

    public static int doExport(String fName, JComboBox jbx, JTextArea ta, JProgressBar jb,
            int totalRecords) {
        
        fileName = fName; // always use the given table.
        if (cbxTables == null) {
            cbxTables = jbx;
        }
        if (taContents == null) {
            taContents = ta;
        }
        if (progressBar == null) {
            progressBar = jb;
        }

        totalNumOfRecords = totalRecords;

        String tableName = ((String) cbxTables.getSelectedItem()).trim().toUpperCase();
        FileWriter fileWriter = null;

        int k = 0, linesWritten = 0;
        postMessage(String.format("\n%5s %25s %17s %17s\n", "No",
                "Exported Table Name", "RCs Downloaded", "RCs Failed"));

        try {

            pbMtr = new PBMornitor(progressBar);
            pbMtr.start(0, totalNumOfRecords);
            new Thread(new PBThread(pbMtr)).start();

            //fileWriter = new FileWriter( fileName, blAppend ); 
            fileWriter = new FileWriter(fileName);

            if (tableName.equals("ALL TABLES")) {
                int cnt = cbxTables.getItemCount();

                // "All Tables" is an added "table" in the combobox, that is why
                // k is from 1 to cnt -1.
                for (int i = 1; i < cnt; i++) {
                    tableName = (String) cbxTables.getItemAt(i).toUpperCase();
                    k += exportSelectedTable(i, fileWriter, tableName);
                }

            } else {
                k = exportSelectedTable(1, fileWriter, tableName);
            }

            fileWriter.close();
            pbMtr.end();
            postMessage("\n" + k + ((k == 1 ? " table is" : " tables are")) + "  backed up to ["
                    + fileName.substring(fileName.lastIndexOf("/") + 1) + "]\nin folder "
                    + fileName.substring(0, fileName.lastIndexOf("/") + 1) + ".\n\n");    //.substring(fileName.lastIndexOf("/") + 1) + "\n");
            return k;
        } catch (IOException e) {
            e.printStackTrace();
            return k;
        }
    }

    public static int exportSelectedTable(int ordNum, FileWriter fwtr, String tableName) {
        int succeededCount = 0, failedCount = 0;

        try {
            res = DBConnection.getResultSet("SELECT * FROM " + tableName);
            meta = res.getMetaData();
            int cnt = meta.getColumnCount();
            fwtr.write("\nTABLE NAME" + colSeparator + tableName + colSeparator + cnt + "\n");
            succeededCount = resultSetToFile(res, fwtr, colSeparator, cnt, pbMtr);
            failedCount = gFailedCount;
            fwtr.flush();
            postMessage(String.format("%5d %25s %17d %17d\n",
                    ordNum, tableName, succeededCount, failedCount));
            res.close();
            return 1;

        } catch (SQLException e1) {
            e1.printStackTrace();
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
            }
            return 0;
        } catch (IOException e2) {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
            }
            e2.printStackTrace();
            return 0;
        }
    }

    /**
     * Import Data from a text file. The data for S stored in a text with the
     * following format: Table name| S | 4	-- indicate relation S has 4
     * attributes s6| Wang| 99| Bakersfield	-- each row is a record and fields
     * separated by "|".
     *
     * The word "Table Name" or "TableName" can be in any cases, and must not be
     * preceeded by any other non white space letter.
     *
     * To insert Date, Time and Timestamp data, the following methods can be
     * used: 1. The Date, Time and Timestamp string should have the following
     * format in your data file: Date:	yyyy-mm-dd Time:	hh:mm:ss Timestamp:
     * yyyy-mm-dd hh:mm:dd.ffffffffff where ffffffffff is nano seconds 2. Use
     * Date, Time and Timestamp's static valueOf(): static Date valueOf( String
     * dateString); static Time valueOf( String timeString); static Timestamp
     * valueOf( String timestampString); to convert a date/time/timestamp string
     * to a date, time or/ a timestamp object, and then 3. Use the
     * PreparedStatement's set functions to set the parameter:
     * preparedStmt.setDate( i, dateObject); preparedStmt.setTime( i,
     * timeObject); preparedStmt.setTimestamp( i, timestampObject); 4. Notice
     * that the DATE type of Oracle 8.05 is actually TIMESTAMP type,
     * postMessage("\n No Importing Table Name Inserted Failed\n");
     *
     * and the format of date should be yyyy-mm-dd hh:mm:ss.ffffffffff.
     */
    static PreparedStatement pStmt = null;
    static int fieldCount, lineImported = 0, lineFailed = 0, lineSkipped = 0,
            numOfLinesRead = 0, numOfTablesImported = 0, blankLine = 0;
    static long totalProcessed, maxToProcess;
    static String tableName = null;
    static boolean needPaint = true;

    /* Import one or all tables
     * Return the number of tables whose records are imported.
     */
    public static int doImport(String fName, JComboBox jbx, JTextArea ta, JProgressBar jb) {

        fileName = fName; // always use the given table.
        if (cbxTables == null) {
            cbxTables = jbx;
        }
        if (taContents == null) {
            taContents = ta;
        }
        if (progressBar == null) {
            progressBar = jb;
        }

        pbMtr = new PBMornitor(progressBar);
        pbMtr.start(0, totalNumOfRecords);
        new Thread(new PBThread(pbMtr)).start();
        tableName = ((String) cbxTables.getSelectedItem()).trim().toUpperCase();

        int k = 0;
        lineImported = lineFailed = 0;

        if (tableName.equals("ALL TABLES")) {
            tableName = "";
        }
        
        k = importSelectedTable(fileName, tableName);
        return k;
    }

    public static int importSelectedTable(String fileName, String tbName) {

        String line = null;
        boolean tableRequiredAndExisted = false;

        postMessage(String.format("\n%5s %25s %17s %17s\n", "No",
                "Imported Table Name", "RCs Inserted", "RCs Failed"));

        fieldCount = numOfTablesImported = blankLine = lineSkipped = 0;
        needPaint = false;

        try {

            File file = new File(fileName);
            maxToProcess = (int) file.length(); // File length in bytes (long).
            totalProcessed = 0;

            boolean succ = false;
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

            // Set minimum and maximum values of the progressbar.
            // For import, the maximum value is the number of lines in the file,
            // which is the total number of records plus 2 times number of tables.
            pbMtr.start(0, (int) maxToProcess);

            while ((line = fileReader.readLine()) != null) {
                numOfLinesRead++;

                totalProcessed += line.length() + 1;
                needPaint = true;
                pbMtr.setValue((int) totalProcessed);

                if (line.trim().equals("")) {
                    blankLine++;
                    continue;
                }  // Skip blank line.

                /**
                 * Process a "TABLE NAME" line. 1. Extract table name, and
                 * attribute count, 2. Determine whether the table needs to be
                 * imported? 3. Test whether table is in the database? 4. If
                 * either 2 or 3 is true, skip over lines until next TABLE NAME
                 * line. 5. Read and import following record lines.
                 *
                 */
                if (line.indexOf("TABLENAME") == 0 || line.indexOf("TABLE NAME") == 0) {
                    /**
                     * The "TABLE NAME" indicates an beginning of a new section
                     * of record and indicates an end of previous section of
                     * another table. If it indicates the end of previous table,
                     * print out previous table import info.
                     *
                     */
                    if (needPaint) {
                        postImportStatus(0);
                    }

                    // The following function will extract table name ans store it in tableName
                    // and extact the numbe of of columns and stored the fieldCount.
                    extractTableNameAndFieldCount(line);

                    /**
                     * If only required one table, and a table is imported, then
                     * stop.
                     */
                    if (!tbName.trim().equals("") && numOfTablesImported > 0) {
                        break;
                    }

                    // Is the table required to be imported?
                    tableRequiredAndExisted = tbName.trim().equals("") || // Are all tables imported? 
                            tbName.equals(tableName);     // Is the table the required table?

                    if (!tableRequiredAndExisted) {
                        continue;    // If not required, continue with next line.   
                    }

                    /**
                     * Test whether the table exists in database. The table
                     * appeared in file, prepare an "INSERT INTO". If failed,
                     * table doesn't exist in database.
                     */
                    tableRequiredAndExisted = prepareInsertStatement(tableName);
                    if (!tableRequiredAndExisted) {
                        numOfTablesImported++;
                        postImportStatus(1);
                        continue;
                    }

                    numOfTablesImported++;
                    needPaint = true;
                    continue;   // continue with next time.
                }

                if (!tableRequiredAndExisted) {
                    continue; // The record line is not needed, and skip over it.
                }

                succ = addRecordToCurrentTable(line, fieldCount); // Insert a record.

                if (succ) {
                    lineImported++;
                } else {
                    lineFailed++;
                }

            } // end of process one line.

        } catch (IOException e) {
            e.printStackTrace();
            return numOfTablesImported;
        }
        if (needPaint) {
            postImportStatus(0); //  post successful status
        }
        pbMtr.end();
        postMessage(
                String.format("\nData loaded into %d %s from file [%s]\nin folder [%s].\n\n",
                        numOfTablesImported, (numOfTablesImported == 1) ? "table" : "tables",
                        fileName.substring(fileName.lastIndexOf("/") + 1),
                        fileName.substring(0, fileName.lastIndexOf("/") + 1)) 
        );
        return numOfTablesImported;
    }

    /**
     * From "TABLENAME | name | fieldCount" line, extract table name and the
     * number of attributes.
     */
    public static void extractTableNameAndFieldCount(String line) {
        StringTokenizer tkz = new StringTokenizer(line, colSeparator);
        tkz.nextToken();         //skip the program reserved work "TABLENAME" 
        tableName = tkz.nextToken().trim();
        fieldCount = Integer.parseInt(tkz.nextToken().trim());
    }

    /**
     * Prepared an INSERT statement, and test whether there is a match in the
     * database for required table.
     */
    public static boolean prepareInsertStatement(String tableName) {
        StringBuffer buf = new StringBuffer();
        buf.append("INSERT INTO " + tableName + " VALUES(?");
        for (int i = 1; i < fieldCount; i++) {
            buf.append(", ?");
        }
        buf.append(")");
        try {
            // if ( pStmt != null ) pStmt.close();
            pStmt = DBConnection.cnn.prepareStatement(buf.toString());

            //if ( stmt == null ) stmt = cnn.createStatement();
            // res  = stmt.executeQuery("select * from " + tableName + " WHERE rownum < 2" );
            res = DBConnection.getResultSet("select * from " + tableName + " LIMIT 2");
            meta = res.getMetaData();
            res.close();
            // taContents.append("SQL statement: " + buf.toString() + " built successfully!\n" );
            return true;
        } catch (SQLException e) {
            // e.printStackTrace();
            // taContents.append("SQL statement: " + buf.toString() + " failed!\n" );
            return false;
        }
    }

    public static boolean addRecordToCurrentTable(String line, int fieldCnt) {
        StringTokenizer tkz = new StringTokenizer(line, colSeparator);
        String colStr = null;
        int i=0;
        try {
            pStmt.clearParameters();
            for (i = 1; i <= fieldCnt; i++) {
                colStr = tkz.nextToken().trim();
            

                switch (meta.getColumnType(i)) {
                    case Types.DATE:
                        pStmt.setDate(i, java.sql.Date.valueOf(colStr));
                        break;
                    case Types.TIME:
                        pStmt.setTime(i, Time.valueOf(colStr));
                        break;
                    case Types.TIMESTAMP: // Oracle DATE type is TIMESTAMP type.
                        pStmt.setTimestamp(i, Timestamp.valueOf(colStr));
                        break;
                    case Types.SMALLINT:
                        if (colStr == null || colStr.equals("")) colStr = new String("-1");
                        pStmt.setInt(i, Integer.parseInt(colStr));
                        break;
                    case Types.INTEGER:
                        pStmt.setInt(i, Integer.parseInt(colStr));
                        break;
                    case Types.REAL:
                        pStmt.setFloat(i, Float.parseFloat(colStr));
                        break;
                    default:
                        pStmt.setString(i, colStr);
                        break;
                }
            }
            pStmt.execute();
            return true;
        } catch (SQLException e) {
            System.out.printf("Insert Record error: table [%s] inserted records [%d], Line from file: \n %s\n",
                    tableName, lineImported, line);
            try { System.out.printf("Field number[%d], value [%s], type [[%d]\n", i, colStr, meta.getColumnType(i) ); } 
            catch (Exception e2) {}

            e.printStackTrace();
            // System.exit(-123);
            return false;
        }
    }

    public static void postImportStatus(int status) {
        String str = "";
        if (numOfTablesImported < 1) {
            return;
        }
        switch (status) {
            case 0: // a successful import
                str = String.format("%5d %25s %17d %17d\n",
                        numOfTablesImported, tableName, lineImported, lineFailed);
                break;
            case 1: // Cannot parepare an "INSERT INTO TABLE" statement
                str = String.format("%5d  %25s %s\n", numOfTablesImported, tableName,
                        "\t  in file, not in database!");
                break;
        }
        postMessage(str);
        needPaint = false;
        lineImported = lineFailed = 0;
    }
}

class PBThread implements Runnable {

    PBMornitor pbMtr = null;

    public PBThread(PBMornitor mtr) {
        pbMtr = mtr;
    }

    public void run() {
        while (!pbMtr.done) {
            pbMtr.paint();
            //try { Thread.sleep (500); } catch ( InterruptedException e ) { }
        }
    }
}

class PBMornitor {

    boolean done = false;
    JProgressBar pb = null;
    int x, y, w, h;
    int oldValue = 0;   // previous progressbar value that has been printed.

    public PBMornitor(JProgressBar pre) {
        pb = pre;
        pb.setStringPainted(true);
        pb.setValue(oldValue);
    }

    public void setLocation() {
        Point p = pb.getLocation();
        x = p.x;
        y = p.y;
        Dimension d = pb.getSize();
        w = d.width;
        h = d.height;
    }

    synchronized public void setValue(int v) { //pb.setValue( v );  
        pb.setValue(v);
        notify();
    }

    synchronized public void paint() {
        while (oldValue == pb.getValue()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        pb.paintImmediately(x, y, w, h);
        oldValue = pb.getValue();
        setLocation();
    }

    synchronized public void start(int min, int max) {
        pb.setMinimum(min);
        pb.setMaximum(max);
    }

    synchronized public void end() {
        pb.setValue(pb.getMinimum());
        done = true;
    }

    synchronized public int getValue() {
        return pb.getValue();
    }
}
