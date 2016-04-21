/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamodellogger;

import datamodellogger.db.DatabaseOperations;
import datamodellogger.gui.CustomGUI;
import datamodellogger.report.GuiLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import PsrParser.*;

public class FXMLDocumentController implements Initializable {

    private String T_ISname = "";
    private String T_Vendor = "";
    private String T_DBUrl = "";
    private String T_DBName = "";
    private String T_UserName = "";
    private String T_Password = "";
    private String T_Port = "";
    private String T_Sid = "";
    HashMap<String, String> map = new HashMap<String, String>();

    DatabaseOperations dbops;
    GuiLogger logger;
    @FXML
    private Stage stage;
    @FXML
    private TextField ISname;
    @FXML
    private TextField dburl;
    @FXML
    private TextField dbusername;
    @FXML
    private TextField dbpassword;
    @FXML
    private TextField statusbar;
    @FXML
    private TextField taskname;
    @FXML
    private TabPane maintab;
    @FXML
    private Tab settingTab;
    @FXML
    private Button startButton;
    @FXML
    private Button stop;
    @FXML
    private Button deployLoggerButton;
    @FXML
    private TextArea resultArea;
    @FXML
    ChoiceBox selectDataSource;

    @FXML
    private void handleStartButton(ActionEvent event) {
        if (selectDataSource.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText("Info");
            alert.setContentText("Please select the DataSource from the dropdown menu");
            alert.show();
            return;
        }
        resultArea.setText("");
        if (ParseSource(map.get(selectDataSource.getSelectionModel().getSelectedItem().toString()))) {
            dbops = new DatabaseOperations();
            logger = new GuiLogger();
            if (T_Vendor.equalsIgnoreCase("Oracle")) {
                if (dbops.startLoggingOracle(T_DBUrl, T_DBName, T_UserName, T_Password)) {
                    statusbar.setText("Logging started");
                    logger.startGUILogger(taskname.getText());
                    startButton.setDisable(true);
                    stop.setDisable(false);
                }

            } else if (T_Vendor.equalsIgnoreCase("MS SQL")) {

                if (dbops.startLoggingMSSQL(T_DBUrl, T_DBName, T_UserName, T_Password)) {
                    statusbar.setText("Logging started");
                    logger.startGUILogger(taskname.getText());
                    startButton.setDisable(true);
                    stop.setDisable(false);
                }
            } else {

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText("Info");
                alert.setContentText("\"Currently we support only Oracle and MS_SQL!");
                alert.show();
            }
        }
    }

    @FXML
    private void handleStopButton(ActionEvent event) {
        PsrParser psr = new PsrParser();
        if (T_Vendor.equalsIgnoreCase("Oracle")) {
            List results = dbops.returnTableCount(T_DBUrl, T_DBName, T_UserName, T_Password);
            String output = "";
            if (!results.isEmpty()) {
                for (int i = 0; i < results.size(); i++) {
                    output = output + results.get(i);
                    resultArea.setText(output);
                }
            } else {
                resultArea.setText("Nothing Changed");
            }
            startButton.setDisable(false);
            stop.setDisable(true);
            logger.stopGUILogger();
            statusbar.setText("Logging stopped");
        } else if (T_Vendor.equalsIgnoreCase("MS SQL")) {
            List results = dbops.returnTableCountMSSQL(T_DBUrl, T_DBName, T_UserName, T_Password);
            String output = "";
            if (!results.isEmpty()) {
                for (int i = 0; i < results.size(); i++) {
                    output = output + results.get(i);
                    resultArea.setText(output);
                }
            } else {
                resultArea.setText("Nothing Changed");
            }
            startButton.setDisable(false);
            stop.setDisable(true);
            logger.stopGUILogger();
            String INPUT_ZIP_FILE = logger.getfilePath() + "\\" + logger.getfileName();
            String OUTPUT_FOLDER = logger.getfilePath() + "\\" + logger.taskName();
            boolean unzip = true;
            int ctr=0;
            while (unzip) {
                try {
                    psr.processMhtResult(INPUT_ZIP_FILE, OUTPUT_FOLDER,T_DBUrl, T_DBName, T_UserName, T_Password);
                    unzip=false;
                } catch (Exception ex) {
                    if(ctr==10000){
                        System.out.println(ctr+ ": Error Unzipping File " + INPUT_ZIP_FILE);
                        unzip=false;
                    }
                    ctr++;
                }
            }
            statusbar.setText("Logging stopped");
        } else {
            System.out.println("Only Oracle and MSSQL Server are supported");
        }
    }

    @FXML
    private void handleAddNew(ActionEvent event) {
        CustomGUI addDialog = new CustomGUI();
        addDialog.createAddDataSourceDialog(stage);
        updateCombofromFile();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusbar.setDisable(true);
        statusbar.setText("Please Click Start Button to Record");
        updateCombofromFile();
        //  stage.setResizable(false);
        stop.setDisable(true);

    }

    public void updateCombofromFile() {
        try {
            map.clear();
            File file = new File("config.dat");
            try (FileReader fileReader = new FileReader(file)) {
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                selectDataSource.getItems().clear();
                while ((line = bufferedReader.readLine()) != null) {
                    map.put(getIsName(line), line);
                    selectDataSource.getItems().add(getIsName(line));
                }
            }

        } catch (IOException e) {
            statusbar.setText("No Configuration Found Please Add new........");
        }
    }

    private boolean ParseSource(String text) {
        String[] result = text.split(",");
        T_ISname = result[0];
        T_Vendor = result[1];
        T_DBUrl = result[2];
        T_DBName = result[3];
        T_UserName = result[4];
        T_Password = result[5];
        return true;
    }

    private String getIsName(String text) {
        String[] result = text.split(",");
        return result[0];
    }

}
