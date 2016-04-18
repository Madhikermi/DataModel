package datamodellogger.gui;

import datamodellogger.db.DatabaseOperations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CustomGUI {

    @FXML
    final TextField _ISname = new TextField();
    @FXML
    final ComboBox _DBvendor = new ComboBox();
    @FXML
    final TextField _DBurl = new TextField();
    @FXML
    final TextField _DBuser = new TextField();
    @FXML
    final TextField _DBname = new TextField();
    @FXML
    final PasswordField _DBpassword = new PasswordField();
    @FXML
    Dialog<DatabaseConnection> dialog = new Dialog<>();

    @FXML
    public void createAddDataSourceDialog(Stage stage) {
        dialog.setTitle("Enter Database Connection");
        dialog.setHeaderText("Enter database connection parameters");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 20, 10));
        _DBvendor.getItems().addAll(
                "Oracle",
                "MS SQL",
                "MySQL",
                "PostgreSQL"
        );

        //Grid Setup
        grid.add(new Label("Information System Name:"), 1, 0);
        grid.add(_ISname, 2, 0);
        grid.add(new Label("Vendor:"), 1, 2);
        grid.add(_DBvendor, 2, 2);
        _DBvendor.setPromptText("Select this first");
        grid.add(new Label("DB URL:"), 1, 3);
        grid.add(_DBurl, 2, 3);
        _DBurl.setPromptText("127.0.0.1:port");
        grid.add(new Label("DB Name (or SID):"), 1, 4);
        grid.add(_DBname, 2, 4);
        grid.add(new Label("Username:"), 1, 5);
        grid.add(_DBuser, 2, 5);
        _DBuser.setPromptText("sysadmin");
        grid.add(new Label("Password:"), 1, 6);
        grid.add(_DBpassword, 2, 6);
        _DBvendor.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                _DBurl.setDisable(false);
                switch (newValue) {
                    case "MS SQL":
                        _DBurl.setText("127.0.0.1:1433");
                        _DBname.setPromptText("Insert");
                        _DBname.setDisable(false);
                        break;
                    case "Oracle":
                        _DBurl.setText("127.0.0.1:1521");
                        _DBname.setPromptText("orcl");
                        _DBname.setDisable(false);
                        break;
                    case "MySQL":
                        _DBurl.setText("127.0.0.1:3306");
                        _DBname.setPromptText("Insert");
                        _DBname.setDisable(false);
                        break;
                }
            }
        });

        dialog.getDialogPane().setContent(grid);
        ButtonType deployLoggerButton = new ButtonType("DEPLOY LOGGER", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(deployLoggerButton);
        dialog.setResultConverter(new Callback<ButtonType, DatabaseConnection>() {
            @Override
            public DatabaseConnection call(ButtonType b) {
                if (b == deployLoggerButton) {
                    String dbVendor = _DBvendor.getSelectionModel().getSelectedItem().toString();
                    DatabaseConnection dbc = new DatabaseConnection(_ISname.getText(), dbVendor, _DBurl.getText(), _DBuser.getText(), _DBname.getText(), _DBpassword.getText());
                    handleDeployLoggerButton(dbc);
                    return dbc;
                }
                return null;
            }
        });
        dialog.showAndWait();
    }

    public void handleDeployLoggerButton(DatabaseConnection dbc) {
        DatabaseOperations dbops = new DatabaseOperations();
        String DBvendor = dbc.getDBvendor();
        String DBurl = dbc.getDBurl();
        String DBname = dbc.getDBname();
        String DBuser = dbc.getDBuser();
        String DBpassword = dbc.getDBpassword();
        if (DBvendor.equalsIgnoreCase("Oracle")) {
            if (dbops.deployToOracle(DBurl, DBname, DBuser, DBpassword)) {
                saveDBConnectionParamtersToFile(dbc.toString());
            }
        } else if (DBvendor.equalsIgnoreCase("MS SQL")) {
            if (dbops.deployToMSServer(DBurl, DBname, DBuser, DBpassword)) {
                saveDBConnectionParamtersToFile(dbc.toString());
            }
        } else {
            System.out.println("We currently support MS SQL and Oracle");
        }
        dialog.close();
    }

    private static void saveDBConnectionParamtersToFile(String text) {
        try {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("config.dat"), true))) {
                bw.write(text);
                bw.newLine();
            }
        } catch (Exception e) {
        }
    }
}
