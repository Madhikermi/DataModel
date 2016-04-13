package datamodellogger.gui;

import datamodellogger.db.DatabaseOperations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

public class CustomGUI {

    final TextField ISname = new TextField();
    final ComboBox DBvendor = new ComboBox();
    final TextField DBurl = new TextField();
    final TextField DBuser = new TextField();
    final TextField DBname = new TextField();
    final PasswordField DBpassword = new PasswordField();

    public void createAddDataSourceDialog(Stage stage) {
        Dialog dlg = new Dialog(stage, "Add Data Source");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 20, 10));

        DBvendor.getItems().addAll(
                "Oracle",
                "MS SQL",
                "MySQL",
                "PostgreSQL"
        );

        //Grid Setup
        grid.add(new Label("Information System Name:"), 0, 0);
        grid.add(ISname, 1, 0);

        grid.add(new Label("Vendor:"), 0, 2);
        grid.add(DBvendor, 1, 2);
        DBvendor.setPromptText("Select this first");
        grid.add(new Label("DB URL:"), 0, 3);
        grid.add(DBurl, 1, 3);
        DBurl.setPromptText("127.0.0.1:port");
        grid.add(new Label("DB Name:"), 0, 4);
        grid.add(DBname, 1, 4);
        grid.add(new Label("Username:"), 0, 5);
        grid.add(DBuser, 1, 5);
        DBuser.setPromptText("sysadmin");
        grid.add(new Label("Password:"), 0, 6);
        grid.add(DBpassword, 1, 6);

        //Properties and Listerner Setup
        ButtonBar.setType(actionDeployLogger, ButtonType.OK_DONE);

        DBvendor.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                DBurl.setDisable(false);
                if (newValue.equals("MS SQL")) {
                    DBurl.setPromptText("127.0.0.1:1433");
                    DBname.setPromptText("Insert");
                    DBname.setDisable(false);
                } else if (newValue.equals("Oracle")) {
                    DBurl.setPromptText("127.0.0.1:1521");
                    DBname.setPromptText("Not needed");
                    DBname.setDisable(true);
                } else if (newValue.equals("MySQL")) {
                    DBurl.setPromptText("127.0.0.1:5432");
                    DBname.setPromptText("Insert");
                    DBname.setDisable(false);
                } else if (newValue.equals("MySQL")) {
                    DBurl.setPromptText("127.0.0.1:3306");
                    DBname.setPromptText("Insert");
                    DBname.setDisable(false);
                }
            }
        });

        dlg.setContent(grid);
        dlg.getActions().addAll(actionDeployLogger, Dialog.Actions.CANCEL);
        dlg.show();
    }

    final Action actionDeployLogger = new AbstractAction("Deploy Database Logger!") {
        // This method is called when the login button is clicked ...
        @Override
        public void handle(ActionEvent ae) {
            DatabaseOperations dbops = new DatabaseOperations();
            String Local_ISname = ISname.getText();
            String Local_DBvendor =DBvendor.getValue().toString();
            String Local_DBurl = DBurl.getText();
            String Local_DBname = DBname.getText();
            String Local_DBuser = DBuser.getText();
            String Local_DBpassword = DBpassword.getText();
            String Local_Port = "1521";
            String Local_Sid = "orcl";
            if (Local_DBvendor.equalsIgnoreCase("Oracle")) {
                if (dbops.connectToOracle(Local_DBurl, Local_DBuser, Local_DBpassword, Local_Port, Local_Sid)) {
                    String Entry = Local_ISname + ',' + Local_DBvendor + ',' + Local_DBurl + ',' + Local_DBname + ',' + Local_DBuser + ',' + Local_DBpassword + ',' + Local_Port + ',' + Local_Sid;
                    writeToFile(Entry);
                }
                //connectToMSServer(String DB_URL,String DB_NAME ,String USER, String PASS, String PORT, String SID)
            } else if (Local_DBvendor.equalsIgnoreCase("MS SQL")) {
                Local_Port="1433";
                if (dbops.connectToMSServer(Local_DBurl,Local_DBname,Local_DBuser, Local_DBpassword, Local_Port, Local_Sid)) {
                    String Entry = Local_ISname + ',' + Local_DBvendor + ',' + Local_DBurl + ',' + Local_DBname + ',' + Local_DBuser + ',' + Local_DBpassword + ',' + Local_Port + ',' + Local_Sid;
                    writeToFile(Entry);
                }
            }else {
                System.out.println("We currently support MS SQL and Oracle");
            }

            Dialog d = (Dialog) ae.getSource();

            d.hide();
        }
    };

    public static void writeToFile(String text) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("config.dat"), true));
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
        }
    }
}
