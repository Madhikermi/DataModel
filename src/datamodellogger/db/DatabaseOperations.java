package datamodellogger.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DatabaseOperations {

    static final String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    @FXML
    private TextField taskname;
    private Connection conn = null;
    String ConnectionString = null;

    private List getDeployedTriggerOracle() {
        Statement stmt = null;
        String query = "select TRIGGER_NAME from USER_TRIGGERS where upper(TRIGGER_NAME) like 'ZZZ_TRI%' or  upper(TRIGGER_NAME) like 'COL_TRI%' ";
        List Tmptriggers = new ArrayList();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Tmptriggers.add(rs.getString(1));
            }
            return Tmptriggers;
        } catch (SQLException ex) {
            System.out.println("Error getting list of deployed Trigger");
            return Tmptriggers;
        }
    }

    private boolean deleteDeployedTriggerOracle() {
        List TmpTables = getDeployedTriggerOracle();
        String query = null;
        try {
            for (int i = 0; i < TmpTables.size(); i++) {
                Statement stmt = null;
                stmt = conn.createStatement();
                query = "Drop Trigger " + TmpTables.get(i);
                if (query != null) {
                    ResultSet executeQuery = stmt.executeQuery(query);
                    System.out.println(TmpTables.get(i) + " deleted Successfully. ");
                }
            }
            return true;
        } catch (SQLException ex) {
            System.out.println("Error during delete of temporary trigger");
            return false;
        }
    }

    private void deleteTempTables() {
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
        try {
            String newQuery = "drop table ZZZ_TMP_COL_UPDATE";
            Statement stmt = null;
            stmt = conn.createStatement();
            if (newQuery != null) {
                ResultSet executeQuery = stmt.executeQuery(newQuery);
                System.out.println("ZZZ_TMP_COL_UPDATE deleted Successfully. ");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception during deleting temporary Tables");
        } catch (NullPointerException e) {
            System.out.println("Null Pointer Exception");
        }
    }

    private void createTempTables() {
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
                System.out.println(query);
                System.out.println(query1);
                return;
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

    private void deployTriggersCols() {
        List MainTables = getTables(false);
        for (int i = 0; i < MainTables.size(); i++) {
            String TableName = MainTables.get(i).toString();
            String triName = "col_tri_" + TableName;
            String strTrigger = "CREATE OR REPLACE TRIGGER "
                    + triName
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
            try {
                Statement stmt = null;
                stmt = conn.createStatement();
                stmt.executeQuery(strTrigger);
                System.out.println("Trigger " + triName + " for " + TableName + " created succesfully.....");
            } catch (SQLException ex) {
                System.out.println("Error");
            }
        }

    }

    private void deployTriggers() {
        List MainTables = getTables(false);
        for (int i = 0; i < MainTables.size(); i++) {
            Statement stmt = null;
            String TableName = MainTables.get(i).toString();
            String ColumnLists = getColumnForTrigger(TableName);
            String[] ColumnList = ColumnLists.split("::");
            String triName = "ZZZ_TRI_" + TableName;
            String TriggerQuery = "create or replace trigger " + triName
                    + " after insert on " + TableName + " for each row begin insert into ZZZ_TMP_" + TableName + "(" + ColumnList[0] + ",TS)"
                    + " values (" + ColumnList[1] + ",sysdate) ; end;";
            try {
                stmt = conn.createStatement();
                stmt.executeQuery(TriggerQuery);
                System.out.println("Trigger " + triName + " for " + TableName + " created succesfully.....");
            } catch (SQLException ex) {
                System.out.println("Sql Exception During Creation of trigger");
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
            }
        }
        deployTriggersCols();
    }

    private String getColumnForTrigger(String Table_Name) {
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

    private List getTables(boolean IsTemp) {
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

    private void TruncateTempTables() {
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

    private boolean connectOracle(String DB_URL, String DB_NAME, String USER, String PASS) {
        String[] parts = DB_URL.split(":");
        ConnectionString = "jdbc:oracle:thin:@" + parts[0] + ":" + parts[1] + ":" + DB_NAME;
        try {
            Class.forName(ORACLE_DRIVER);
            conn = DriverManager.getConnection(ConnectionString, USER, PASS);
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class Not found Exception");
        } catch (SQLException ex) {
            System.out.println("Sql exception during connection from start Button");
        }
        return false;
    }

    private boolean closeConnection() {
        try {
            conn.close();
            return true;
        } catch (SQLException ex) {
            System.out.println("Error Closing Connection");
            return false;
        }
    }

    public boolean deployToOracle(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectOracle(DB_URL, DB_NAME, USER, PASS)) {
            System.out.println("Connected To Oracle");
            System.out.println("################# Deleting Temporary Table Started #################");
            deleteTempTables();
            System.out.println("################# Deleting Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Creating  Temporary Table Started #################");
            createTempTables();
            System.out.println("################# Creating Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Deleting Temporary Trigger Started #################");
            deleteDeployedTriggerOracle();
            System.out.println("################# Deleting Temporary Trigger Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Creating  Temporary Trigger Started #################");
            deployTriggers();
            System.out.println("################# Creating Temporary Trigger Finished #################");
            if (closeConnection()) {
                System.out.println("Disconnected To Oracle");
                return true;
            }
        }
        return false;
    }

    public boolean startLoggingOracle(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectOracle(DB_URL, DB_NAME, USER, PASS)) {
            System.out.println("Connected To Oracle");
            TruncateTempTables();
            if (closeConnection()) {
                System.out.println("Disconnected To Oracle");
                return true;
            }
        }
        return false;
    }

    public void cleanSchema(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectOracle(DB_URL, DB_NAME, USER, PASS)) {
            deleteTempTables();
            deleteDeployedTriggerOracle();
        }
        if (closeConnection()) {
            System.out.println("Disconnected To Oracle");
        }
    }

    public List returnTableCount(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectOracle(DB_URL, DB_NAME, USER, PASS)) {
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
                    return null;
                }
            }
            if (closeConnection()) {
                System.out.println("Disconnected To Oracle");
                return resultTable;
            }

        }
        return null;
    }

    public void reportResults() {
        // Read what is in LOG_TEMP_DB and print out, remember the taskname
        String loggedTaskName = taskname.getText();
    }

    ////////////////////////////MS SQL SERVER ///////////////////////////////
    private boolean connectMSSQL(String DB_URL, String DB_NAME, String USER, String PASS) {
        String[] parts = DB_URL.split(":");
        try {
            ConnectionString = "jdbc:sqlserver://" + parts[0] + ":" + parts[1] + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(ConnectionString);
            return true;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class Not found Exception");
        } catch (SQLException ex) {
            System.out.println("Sql exception during starting up connection");
        }
        return false;
    }

    private boolean closeConnectionMSSQL() {
        try {
            conn.close();
            return true;
        } catch (SQLException ex) {
            System.out.println("Error Closing Connection");
            return false;
        }
    }

    private List getTablesMSSQL(boolean IsTemp) {
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

    private void createTempTablesMSSQL() {
        String query = null;
        List Tables = getTablesMSSQL(false);
        for (int i = 0; i < Tables.size(); i++) {
            String tblName = Tables.get(i).toString();
            Statement stmt = null;
            query = getCreateTableMsSql(tblName);
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

    private String getCreateTableMsSql(String tablename) {
        String firstPart = "create table ZZZ_TMP_" + tablename + "(";
        String middlePart = "";
        String lastPart = ")";
        Connection conn;
        try {
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(ConnectionString);
            Statement stmt = null;
            stmt = conn.createStatement();
            //String query = "SELECT name from syscolumns where id = Object_ID('" + tablename + "') and colstat & 1 = 1";
            String query = "SELECT distinct c.name, t.Name FROM sys.columns c INNER JOIN sys.types t ON c.user_type_id = t.user_type_id LEFT OUTER JOIN  sys.index_columns ic ON ic.object_id = c.object_id AND ic.column_id = c.column_id LEFT OUTER JOIN sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id WHERE c.object_id = OBJECT_ID('" + tablename + "')";
            ResultSet rs = stmt.executeQuery(query);
            String dType = null;
            while (rs.next()) {
                if (rs.getString(2).equalsIgnoreCase("varchar")) {
                    dType = "varchar(max)";
                } else if (rs.getString(2).equalsIgnoreCase("uniqueidentifier")) {
                    dType = "varchar(max)";
                } else if (rs.getString(2).equalsIgnoreCase("nvarchar")) {
                    dType = "nvarchar(max)";
                }else if (rs.getString(2).equalsIgnoreCase("char")) {
                    dType = "char(max)";
                }else {
                    dType = rs.getString(2);
                }
                middlePart = middlePart + " " + "\""+  rs.getString(1)+ "\"" + " " + dType + ", ";
            }
            middlePart = middlePart + " TS datetime NOT NULL default CURRENT_TIMESTAMP";
            String Query2 = firstPart + middlePart + lastPart;
            return Query2;

        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found");
            return null;

        } catch (SQLException ex) {
            System.out.println("Sql Exception during connection in MSSQLSERVER");
            return null;

        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            return null;
        }
    }

    private void creatTblColUpMSSQL() {
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

    private void deleteTempTablesMSSQL() {
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

    private void deployTriggersColsMSSQL() {
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
                String ifCaluse = "IF UPDATE (" + col + ") BEGIN insert into ZZZ_TMP_COL_UPDATE values(GETDATE(),'" + TableName + "::" + col + "') END";
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

    private void deployTriggersMSSQL() {
        List MainTables = getTablesMSSQL(false);
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
    }

    private String getColumnForTriggerMSSQL(String Table_Name) {
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

    private void TruncateTempTableMSSQL() {
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

    public boolean deployToMSServer(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectMSSQL(DB_URL, DB_NAME, USER, PASS)) {
            System.out.println("Connected to MysqlServer");
            System.out.println("################# Deleting Temporary Table Started #################");
            deleteTempTablesMSSQL();
            System.out.println("################# Deleting Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Creating  Temporary Table Started #################");
            createTempTablesMSSQL();
            creatTblColUpMSSQL();
            System.out.println("################# Creating Temporary Table Finished #################");
            System.out.println("");
            System.out.println("");
            System.out.println("################# Trigger creation Started #################");
            deployTriggersMSSQL();
            deployTriggersColsMSSQL();
            System.out.println("################# Trigger creation Completed #################");
        }
        if (closeConnectionMSSQL()) {
            System.out.println("Disconnected To MSSQL Server");
            return true;
        }

        return false;
    }

    public boolean startLoggingMSSQL(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectMSSQL(DB_URL, DB_NAME, USER, PASS)) {
            System.out.println("Connected To MSSQL");
            TruncateTempTableMSSQL();
            if (closeConnectionMSSQL()) {
                System.out.println("Disconnected To MSSQL");
                return true;
            }
        }
        return false;
    }

    public List returnTableCountMSSQL(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectMSSQL(DB_URL, DB_NAME, USER, PASS)) {
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
            if (closeConnectionMSSQL()) {
                System.out.println("Disconnected To MSSQL");
                return resultTable;
            }

        }
        return null;
    }

    public void cleanSchemaMSSQL(String DB_URL, String DB_NAME, String USER, String PASS) {
        if (connectMSSQL(DB_URL, DB_NAME, USER, PASS)) {
            deleteTempTablesMSSQL();
        }
        if (closeConnectionMSSQL()) {
            System.out.println("Disconnected To MSSQL");
        }
    }
}
