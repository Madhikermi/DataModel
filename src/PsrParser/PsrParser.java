/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PsrParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Manik
 */
public class PsrParser {

    private String folderName = null;
    private static final int BUFFER_SIZE = 4096;
    private String inMhtFile;
    private String outMhtFile;
    private String DBUrl;
    private String DBName;
    private String UserName;
    private String Password;
    private String logfilePath;
    private String fname;

    public void processMhtResult(String LogPath, String Fname, String T_DBUrl, String T_DBName, String T_UserName, String T_Password) {
        DBUrl = T_DBUrl;
        DBName = T_DBName;
        UserName = T_UserName;
        Password = T_Password;
        logfilePath = LogPath;
        fname = Fname;
        outMhtFile = LogPath + "\\" + fname + "\\";
        inMhtFile = outMhtFile + "\\" + fname + "_log.zip";
        
        System.out.println("logfilePath" + "");
        

        //  chkZipCreationComplete();
        //unzip(zipFilePath, destDirectory);
        //  inMhtFile = getMhtFileName();
        //  outMhtFile = inMhtFile.substring(0, inMhtFile.length() - 4) + "_out.mht";
        //    System.out.println(inMhtFile);
        //    System.out.println(outMhtFile);
        //mainTest();
    }

    private boolean chkZipCreationComplete() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(logfilePath);
            dir.register(watcher, ENTRY_CREATE);
            System.out.println("Watch Service registered for dir: " + dir.getFileName());
            Boolean CreationComplete = true;
            while (CreationComplete) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    return false;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    System.out.println(kind.name() + ": " + fileName);
                    if (kind == ENTRY_MODIFY
                            && fileName.toString().equals("DirectoryWatchDemo.java")) {
                        System.out.println("My source file has changed!!!");
                        return true;
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return false;
    }

    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(folderName);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public String getMhtFileName() {
        String mhtFile = null;
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fname = listOfFiles[i].getName();
                String ext = fname.substring(fname.length() - 4, fname.length());
                if (ext.equalsIgnoreCase(".mht")) {
                    return folderName + "\\" + fname;
                }
            }
        }
        return mhtFile;
    }

    private void mainTest() {
        List parts = getParts(inMhtFile);
        String firstPart = parts.get(0).toString();
        String secondPart = parts.get(1).toString();
        String toinsertone = "<table border=\"1\">\n"
                + "	<tr>\n"
                + "	<td>\n"
                + "	<b>Data Model Chages: </b>\n"
                + "	</td>\n"
                + "	</tr>\n"
                + "	<tr>\n"
                + "	<td>\n"
                + "	<font color=\"red\">";
        String toinserttwo = "</font>\n"
                + "	</td>\n"
                + "	</tr>\n"
                + "	</table>\n";
        String finalStr = "";
        String strtofind = "<p ID=\"ProblemStepP\">";
        int ctr = 0;
        int i = firstPart.indexOf(strtofind);
        while (i >= 0) {
            ctr++;
            i = firstPart.indexOf(strtofind, i + 1);
        }
        ctr = ctr / 3;
        i = 0;
        int begin = 0;
        int end = 0;
        int loopctr = 0;
        i = firstPart.indexOf(strtofind);
        while (i >= 0) {
            end = i + strtofind.length();
            String substr = firstPart.substring(begin, end);
            String actionTime = getDate(firstPart.substring(i, i + 100));
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            Date date;
            try {
                date = formatter.parse(actionTime);
                String capturedTime = formatter.format(date);
                String[] DBUrlparts = DBUrl.split(":");
                finalStr = finalStr + substr + toinsertone + getMatchedData(DBUrlparts[0], DBName, UserName, Password, DBUrlparts[1], capturedTime) + toinserttwo;
            } catch (ParseException ex) {
                Logger.getLogger(PsrParser.class.getName()).log(Level.SEVERE, null, ex);
            }

            i = firstPart.indexOf(strtofind, i + 1);
            begin = end;
            loopctr++;
            if (loopctr == ctr) {
                i = -1;
            }
        }
        finalStr = finalStr + firstPart.substring(begin, firstPart.length());
        String towrite = finalStr + secondPart;
        writeMht(towrite);
    }

    public List getParts(String fname) {
        FileInputStream fis = null;
        List MainTables = new ArrayList();
        try {
            File file = new File(fname);
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            int lastindex = str.indexOf("Content-Type: image/jpeg");
            String firstPart = str.substring(0, lastindex - 55);
            String lastPart = str.substring(lastindex - 54, str.length());
            MainTables.add(firstPart);
            MainTables.add(lastPart);
            return MainTables;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                return null;
            }
        }
    }

    private void writeMht(String text) {
        try (PrintStream out = new PrintStream(new FileOutputStream(outMhtFile))) {
            out.print(text);
        } catch (FileNotFoundException ex) {
            System.out.println("Error");
        }
    }

    private static String getDate(String text) {
        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(\\(.*\\))";	// Round Braces 1

        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String rbraces1 = m.group(1);
            return rbraces1.toString().substring(1, rbraces1.toString().length() - 1);
        }
        return null;
    }

    private String getMatchedData(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String caputuredDate) {
        try {
            String htmlDate = caputuredDate;
            String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Connection conn = null;
            String CONSTRING = "";
            CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            try {
                Statement stmt = null;
                stmt = conn.createStatement();
                String query = "select * from ZZZ_TMP_COL_UPDATE";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    Date date;
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("y-M-d HH:mm:ss");
                        date = formatter.parse(rs.getString(1));
                        SimpleDateFormat sdfDestination = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                        String updatedTime = sdfDestination.format(date);
                        if (caputuredDate.equals(updatedTime)) {
                            caputuredDate = caputuredDate + " <br> " + rs.getString(2);
                        }
                    } catch (ParseException ex) {
                        System.out.println("Errir");
                        return caputuredDate;
                    }

                }
            } catch (SQLException ex) {
                System.out.println("SQL Error");
                return caputuredDate;
            }
            conn.close();
            String insertedtable = getMatchedDataInsert(DB_URL, DB_NAME, USER, PASS, PORT, htmlDate);
            return caputuredDate + insertedtable;
        } catch (ClassNotFoundException ex) {
            return caputuredDate;

        } catch (SQLException ex) {
            return caputuredDate;
        }

    }

    private String getMatchedDataInsert(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String caputuredDate) {
        try {

            String updatedTable = "<br > Updated Tables: ";
            String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Connection conn = null;
            String CONSTRING = "";
            CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            Statement stmt = conn.createStatement();
            String query = "select table_name from information_schema.tables where table_name like 'ZZZ_TMP%'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String tableName = rs.getString(1);
                Statement stmttblcnt = conn.createStatement();
                String queryTblCount = "select count(*) from " + tableName;
                ResultSet rstblcnt = stmttblcnt.executeQuery(queryTblCount);
                while (rstblcnt.next()) {
                    if (!"0".equals(rstblcnt.getString(1))) {
                        Statement stmtDataLevel = conn.createStatement();
                        String queryDataLevel = "select * from " + tableName;
                        ResultSet rsDataLevel = stmtDataLevel.executeQuery(queryDataLevel);
                        while (rsDataLevel.next()) {
                            SimpleDateFormat formatter = new SimpleDateFormat("y-M-d HH:mm:ss");
                            Date date = formatter.parse(rsDataLevel.getString("TS"));
                            SimpleDateFormat sdfDestination = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                            String updatedTime = sdfDestination.format(date);
                            if (caputuredDate.equals(updatedTime)) {
                                updatedTable = " " + updatedTable + "  <br> " + tableName;
                            }
                        }
                    }
                }

            }
            if (updatedTable.contentEquals("<br > Updated Tables: ")) {
                System.out.println("111");
                return "";
            } else {
                return updatedTable;

            }
        } catch (SQLException ex) {
            System.out.println("SQL Error");
            return "";
        } catch (ClassNotFoundException ex) {
            return "";
        } catch (ParseException ex) {
            return "";
        }

    }

}
