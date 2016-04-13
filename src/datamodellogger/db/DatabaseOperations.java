package datamodellogger.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DatabaseOperations {

    static final String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    @FXML
    private TextField taskname;
    private Connection conn = null;
    String CONSTRING = null;

    public DatabaseOperations() {
        //Do DB connect 
        //You have to figure out if we have an MSSQL or ORACLE DB 
    }

    public boolean connectToOracle(String DB_URL, String USER, String PASS, String PORT, String SID) {
        CONSTRING = "jdbc:oracle:thin:@" + DB_URL + ":" + PORT + ":" + SID;
        System.out.println(CONSTRING);
        try {
            Class.forName(ORACLE_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            System.out.println("Connected To Oracle");
            System.out.println("################# Deleting Temporary Table Started #################");
            deleteTempTables();
            System.out.println("################# Deleting Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Creating  Temporary Table Started #################");
            createTempTables();
            System.out.println("################# Creating Temporary Table Finished #################");
            deployTriggers();
            conn.close();
            System.out.println("Disconnected To Oracle");
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found");
            return false;
        } catch (SQLException ex) {
            System.out.println("Sql Exception");
            return false;
        } catch (Exception e) {
            System.out.println("Error Occured");
            return false;
        }
    }

    public boolean connect(String DB_URL, String USER, String PASS, String PORT, String SID) {
        try {
            CONSTRING = "jdbc:oracle:thin:@" + DB_URL + ":" + PORT + ":" + SID;
            Class.forName(ORACLE_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            System.out.println("Connected via start Button");
            TruncateTempTable();
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class Not found Exception");
        } catch (SQLException ex) {
            System.out.println("Sql exception during connection from start Button");
        }
        return false;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("Error Closing Connection");
        }
    }

    public boolean connectToMSServer(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String SID) {
        CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
        try {
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING);
            System.out.println("Connected to MysqlServer");
            System.out.println("################# Deleting Temporary Table Started #################");
            deleteTempTablesMSSQL();
            System.out.println("################# Deleting Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Creating  Temporary Table Started #################");
            createTempTablesMSSQL();
            alterTempTablesMSSQL();
            creatTblColUpMSSQL();
            System.out.println("################# Creating Temporary Table Finished #################");
            deployTriggersMSSQL();
            deployTriggersColsMSSQL();
            conn.close();
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found");
            return false;
        } catch (SQLException ex) {
            System.out.println("Sql Exception during connection in MSSQLSERVER");
            return false;
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            return false;
        }
    }

    public boolean connectMSSQL(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String SID) {
        try {
            CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING);
            System.out.println("MMSQL Connection via start Button");
            TruncateTempTableMSSQL();
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class Not found Exception");
        } catch (SQLException ex) {
            System.out.println("Sql exception during connection from start Button");
        }
        return false;
    }

    public void deployDBLogger() {
        // Get schema > Table names
        // Create Triggers iterating trough table names   
        // Create LOG_TEMP_DB
    }

    public void cleanupLoggingDB() {
        // truncate table <Table>
        // Clean LOG_TEMP_DB
    }

    public void reportResults() {
        // Read what is in LOG_TEMP_DB and print out, remember the taskname
        String loggedTaskName = taskname.getText();
    }

    public void deleteTempTables() {
        try {
            String newQuery = "drop table ZZZ_TMP_COL_UPDATE";
            Statement stmt = null;
            stmt = conn.createStatement();
            if (newQuery != null) {
                ResultSet executeQuery = stmt.executeQuery(newQuery);
                System.out.println("ZZZ_TMP_COL_UPDATE deleted Successfully. ");
            }
        } catch (SQLException e) {
            System.out.println("SSSSSSQL Exception during deleting temporary Tables");
        } catch (NullPointerException e) {
            System.out.println("Null Pointer Exception");
        }
        String query = null;
        List TmpTables = getTables(true);
        for (int i = 0; i < TmpTables.size(); i++) {
            Statement stmt = null;
            query = "Drop Table " + TmpTables.get(i);
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                    System.out.println(TmpTables.get(i) + " deleted Successfully. ");
                }
            } catch (SQLException e) {
                System.out.println("SQL Exception during deleting temporary Tables");
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }
    }

    public void createTempTables() {
        String query = null;
        List Tables = getTables(false);
        for (int i = 0; i < Tables.size(); i++) {
            Statement stmt = null;
            query = "create table ZZZ_TMP_" + Tables.get(i) + " as select * from " + Tables.get(i) + " where 1=2";
            String query1 = "alter table ZZZ_TMP_" + Tables.get(i) + " add TS timestamp";
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                    ResultSet executeQuery1 = stmt.executeQuery(query1);
                    System.out.println("ZZZ_TMP_" + Tables.get(i) + " created Successfully. ");
                }
            } catch (SQLException e) {
                System.out.println("SQL Exception occured in Creating temporary tables");
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }
        query = "create table ZZZ_TMP_COL_UPDATE (TS timestamp default sysdate , updatedetails varchar2(2000))";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            if (query != null) {
                stmt.executeQuery(query);
                System.out.println("ZZZ_TMP_COL_UPDATE" + " created Successfully. ");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception occured in Creating temporary tables");
        } catch (NullPointerException e) {
            System.out.println("Null Pointer Exception");
        }
    }

    public void deployTriggersCols() {
        List MainTables = getTables(false);
        for (int i = 0; i < MainTables.size(); i++) {
            String TableName = MainTables.get(i).toString();
            System.out.println(TableName);
            String strTrigger = "CREATE OR REPLACE TRIGGER col_tri_"
                    + TableName
                    + " after UPDATE ON "
                    + TableName
                    + " FOR EACH ROW"
                    + "  BEGIN \n";
            String ColumnLists = getColumnForTrigger(TableName);
            String[] ColumnList = ColumnLists.split("::");
            String[] Columns = ColumnList[0].split(",");
            for (int j = 0; j < Columns.length; j++) {
                String TriggerQuery = "'" + TableName + "::" + Columns[j] + "::' ||" + ":old." + Columns[j] + "  || '::' ||" + ":new." + Columns[j];
                String col = "IF UPDATING('" + Columns[j] + "') THEN \n"
                        + "insert into ZZZ_TMP_COL_UPDATE (TS,updatedetails) values (sysdate, " + TriggerQuery + ");\n"
                        + "END IF; \n";
                strTrigger = strTrigger + col;
            }
            strTrigger = strTrigger + " " + " END;";
            System.out.println(strTrigger);
            try {
                Statement stmt = null;
                stmt = conn.createStatement();
                stmt.executeQuery(strTrigger);
                System.out.println("Trigger for " + TableName + " created succesfully.....");
            } catch (SQLException ex) {
                System.out.println("Error");
            }
        }

    }

    public void deployTriggers() {
        List MainTables = getTables(false);
        System.out.println("");
        System.out.println("");
        System.out.println("################# Trigger creation Started #################");
        for (int i = 0; i < MainTables.size(); i++) {
            Statement stmt = null;
            String TableName = MainTables.get(i).toString();
//            String ColumnList = getColumnForTrigger(TableName);
            String ColumnLists = getColumnForTrigger(TableName);
            String[] ColumnList = ColumnLists.split("::");
            String TriggerQuery = "create or replace trigger ZZZ_TRI_" + TableName
                    + " after insert on " + TableName + " for each row begin insert into ZZZ_TMP_" + TableName + "(" + ColumnList[0] + ",TS)"
                    + " values (" + ColumnList[1] + ",sysdate) ; end;";
//            String TriggerQuery = "create or replace trigger ZZZ_TRI_" + TableName
//                    + " after insert on " + TableName + " for each row begin insert into ZZZ_TMP_" + TableName
//                    + " values (" + ColumnList + ") ; end;";
            try {
                stmt = conn.createStatement();
                System.out.println(TriggerQuery);
                stmt.executeQuery(TriggerQuery);
                System.out.println("Trigger for " + TableName + " created succesfully.....");
            } catch (SQLException ex) {
                System.out.println("Sql Exception During Creation of trigger");
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
            }
        }
        deployTriggersCols();
        System.out.println("################# Trigger creation Completed #################");
    }

    public String getColumnForTrigger(String Table_Name) {
        String triggerColumnvalue = "";
        String triggerColumn = "";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String query = "select column_name from all_tab_columns where table_name=" + "'" + Table_Name + "'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                triggerColumn = triggerColumn + rs.getString(1) + ",";
                triggerColumnvalue = triggerColumnvalue + ":new." + rs.getString(1) + ",";
            }
        } catch (SQLException ex) {
            System.out.println("SQL Eror");
        }
        String collistvalue = triggerColumnvalue.substring(0, triggerColumnvalue.length() - 1);
        String collist = triggerColumn.substring(0, triggerColumn.length() - 1);
        return collist + "::" + collistvalue;
    }

    public List getTables(boolean IsTemp) {
        List MainTables = new ArrayList();
        String query = "null";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            if (IsTemp) {
                query = "select table_name from user_tables where table_name like 'ZZZ_TMP%'";
            } else {
                query = "select table_name from user_tables where table_name not like 'ZZZ_TMP%'";
            }
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                MainTables.add(rs.getString(1));
            }
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("SQL Errror");
        }
        return MainTables;
    }

    public void cleanSchema() {
        deleteTempTables();
    }

    public List returnTableCount() {
        List tmpTable = new ArrayList();
        tmpTable = getTables(true);
        List resultTable = new ArrayList();
        Statement stmt = null;
        for (int i = 0; i < tmpTable.size(); i++) {
            try {
                stmt = conn.createStatement();
                String query = "select count(*) cnt from " + tmpTable.get(i);
                ResultSet rss = stmt.executeQuery(query);
                while (rss.next()) {
                    String res = rss.getString(1);
                    if (!res.equalsIgnoreCase("0")) {
                        resultTable.add(tmpTable.get(i) + ":" + res);
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Sql Exception in result List");
            }
        }
        return resultTable;

    }

    public void TruncateTempTable() {
        String query = null;
        List TmpTables = getTables(true);
        System.out.println("################# Temp Table cleaning started #################");
        for (int i = 0; i < TmpTables.size(); i++) {
            Statement stmt = null;
            query = "truncate Table " + TmpTables.get(i);
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                    System.out.println(TmpTables.get(i) + " truncated Successfully. ");
                }
            } catch (SQLException e) {
                System.out.println("SQL Exception during deleting temporary Tables");
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }
    }

    public List getTablesMSSQL(boolean IsTemp) {
        List MainTables = new ArrayList();
        String query = "null";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            if (IsTemp) {
                query = "select table_name from information_schema.tables where table_name like 'ZZZ_TMP%'";
            } else {
                query = "select table_name from information_schema.tables where table_name not like 'ZZZ_TMP%'";
            }
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                MainTables.add(rs.getString(1));
            }
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("SQL Error");
        }
        return MainTables;
    }

    public void createTempTablesMSSQL() {
        String query = null;
        List Tables = getTablesMSSQL(false);
        for (int i = 0; i < Tables.size(); i++) {
            String tblName = Tables.get(i).toString();
            Statement stmt = null;
            query = "select * into ZZZ_TMP_" + tblName + " from " + tblName + " where 1=2";
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    stmt.executeQuery(query);
                    System.out.println("ZZZ_TMP_" + tblName + " created Successfully. ");
                }
            } catch (SQLException e) {
                if (e.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println("ZZZ_TMP_" + tblName + " created Successfully. ");
                } else {
                    System.out.println(e.fillInStackTrace());
                }
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }

    }

    public void alterTempTablesMSSQL() {
        String query = null;
        List Tables = getTablesMSSQL(false);
        for (int i = 0; i < Tables.size(); i++) {
            String tblName = Tables.get(i).toString();
            Statement stmt = null;
            query = "alter table ZZZ_TMP_" + Tables.get(i) + " add TS datetime NOT NULL default CURRENT_TIMESTAMP";
            //ALTER TABLE YourTable ADD CONSTRAINT DF_YourTable DEFAULT GETDATE() FOR YourColumn
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    stmt.executeQuery(query);
                    System.out.println("ZZZ_TMP_" + tblName + " altered Successfully. ");
                }
            } catch (SQLException e) {
                if (e.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println("ZZZ_TMP_" + tblName + " altered Successfully.. ");
                } else {
                    System.out.println(e.fillInStackTrace());
                }
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }

    }

    public void creatTblColUpMSSQL() {
        try {
            Statement stmt = null;
            String query = "create table ZZZ_TMP_COL_UPDATE (TS datetime , updatedetails varchar(2000))";
            stmt = conn.createStatement();
            if (query != null) {

                stmt.executeQuery(query);
                System.out.println("ZZZ_TMP_COL_UPDATE created Successfully. ");
            }
        } catch (SQLException e) {
            if (e.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                System.out.println("ZZZ_TMP_COL_UPDATE created Successfully. ");
            } else {
                System.out.println(e.fillInStackTrace());
            }
        }
    }

    public void deleteTempTablesMSSQL() {
        String query = null;
        List TmpTables = getTablesMSSQL(true);
        for (int i = 0; i < TmpTables.size(); i++) {
            Statement stmt = null;
            query = "Drop Table " + TmpTables.get(i);
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                }
                System.out.println(TmpTables.get(i) + " deleted Successfully. ");
            } catch (SQLException e) {
                if (e.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println(TmpTables.get(i) + " deleted Successfully. ");
                } else {
                    System.out.println(e.fillInStackTrace());
                }
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }

    }

    public void deployTriggersColsMSSQL() {
        List MainTables = getTablesMSSQL(false);
        for (Object MainTable : MainTables) {
            Statement stmt = null;
            String firstPart = "CREATE TRIGGER col_tri_" + MainTable + " ON " + MainTable + " FOR UPDATE AS IF @@ROWCOUNT = 0 RETURN ";
            String secondPart = " ";
            String TableName = MainTable.toString();
            String columnLists = getColumnForTriggerMSSQL(TableName);
            for (String retval : columnLists.split(",")) {
                String col = retval;
                //String ifCaluse = "IF UPDATE (" + col + ") BEGIN PRINT '" + col.substring(1, col.length() - 1) + "' END";
                String ifCaluse = "IF UPDATE (" + col + ") BEGIN insert into ZZZ_TMP_COL_UPDATE values(GETDATE(),'"+TableName +"::"+col+"') END";
                secondPart = secondPart + " " + ifCaluse;
            }
            String trgString = firstPart + secondPart;
            String DropTriggerQuery = "Drop TRIGGER col_tri_" + TableName;
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery(DropTriggerQuery);
                } catch (SQLException ex) {
                    if (!ex.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                        System.out.println(ex.fillInStackTrace().toString());
                    }
                }
                stmt.executeQuery(trgString);
                System.out.println("Trigger for  Employee created succesfully.....");
            } catch (SQLException ex) {
                if (ex.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println("Trigger Created Successfully");
                } else {
                    System.out.println(ex.fillInStackTrace().toString());
                }
            }
        }
    }

    public void deployTriggersMSSQL() {
        List MainTables = getTablesMSSQL(false);
        System.out.println("");
        System.out.println("");
        System.out.println("################# Trigger creation Started #################");

        for (int i = 0; i < MainTables.size(); i++) {
            Statement stmt = null;

            String TableName = MainTables.get(i).toString();
            String DropTriggerQuery = "Drop TRIGGER ZZZ_TRI_" + TableName;
            String cols = getColumnForTriggerMSSQL(TableName);
            //String TriggerQuery = "CREATE TRIGGER ZZZ_TRI_" + TableName + " ON " + TableName + " FOR INSERT  AS  insert into ZZZ_TMP_" + TableName + " values select * from inserted  GO";
            String TriggerQuery = "CREATE TRIGGER ZZZ_TRI_" + TableName + " ON " + TableName + " FOR INSERT  AS  insert into ZZZ_TMP_" + TableName + "(" + cols + ", ts) select " + cols + ",CURRENT_TIMESTAMP   from inserted  GO";
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery(DropTriggerQuery);
                } catch (SQLException ex) {
                    if (!ex.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                        System.out.println(ex.fillInStackTrace().toString());
                    }
                }
                stmt.executeQuery(TriggerQuery);
                System.out.println("Trigger for  Employee created succesfully.....");
            } catch (SQLException ex) {
                if (ex.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println("Trigger Created Successfully");
                } else {
                    System.out.println(ex.fillInStackTrace().toString());
                }
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
            }
        }
        System.out.println("################# Trigger creation Completed #################");
    }

    public String getColumnForTriggerMSSQL(String Table_Name) {
        String triggerColumn = "\"";
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String query = "select column_name from INFORMATION_SCHEMA.COLUMNS where table_name=" + "'" + Table_Name + "'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                triggerColumn = triggerColumn + rs.getString(1) + "\",\"";
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error");
        }
        return triggerColumn.substring(0, triggerColumn.length() - 2);
    }

    public List returnTableCountMSSQL() {
        List tmpTable = new ArrayList();
        tmpTable = getTablesMSSQL(true);
        List resultTable = new ArrayList();
        Statement stmt = null;

        for (int i = 0; i < tmpTable.size(); i++) {
            try {
                stmt = conn.createStatement();
                String query = "select count(*) cnt from " + tmpTable.get(i);
                ResultSet rss = stmt.executeQuery(query);
                while (rss.next()) {
                    String res = rss.getString(1);
                    if (!res.equalsIgnoreCase("0")) {
                        resultTable.add(tmpTable.get(i) + ":" + res);
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Sql Exception in result List");
            }

        }
        return resultTable;
    }

    public void TruncateTempTableMSSQL() {
        System.out.println("################# Temp Table cleaning started #################");
        String query = null;
        List TmpTables = getTablesMSSQL(true);

        for (int i = 0; i < TmpTables.size(); i++) {
            Statement stmt = null;

            query = "truncate Table " + TmpTables.get(i);
            try {
                stmt = conn.createStatement();
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                }
            } catch (SQLException e) {
                if (e.fillInStackTrace().toString().equalsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.")) {
                    System.out.println(TmpTables.get(i) + " truncated Successfully. ");
                } else {
                    System.out.println(e.fillInStackTrace());
                }
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }
        }
        System.out.println("################# Temp Table cleaning Completed #################");
    }
}
