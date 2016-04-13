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
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author abuda
 */
public class FXMLDocumentController implements Initializable {

    private String T_ISname = "";
    private String T_Vendor = "";
    private String T_DBUrl = "";
    private String T_DBName = "";
    private String T_UserName = "";
    private String T_Password = "";
    private String T_Port = "";
    private String T_Sid = "";
    HashMap<String,String> map = new HashMap<String,String>();

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
    private Button addtest;
    
    @FXML
    private void handleaddtest(ActionEvent event) {
      if (selectDataSource.getSelectionModel().getSelectedItem() == null ) {
            Dialogs.create()
            .owner(stage)
            .title("Information Dialog")
            .message("Please select the DataSource from the dropdown menu")
            .showInformation();
         return;
        }
        resultArea.setText("");
        if (ParseSource(map.get(selectDataSource.getSelectionModel().getSelectedItem().toString()))) {
            dbops = new DatabaseOperations();
            logger = new GuiLogger();
            if (T_Vendor.equalsIgnoreCase("Oracle")) {
               if (dbops.connect(T_DBUrl, T_UserName, T_Password, T_Port, T_Sid)) {
                   dbops.deployTriggersCols();
            }

            } else {
               Dialogs.create()
               .owner(stage)
               .title("Information Dialog")
               .masthead("Look, an Information Dialog")
               .message("Oooooops something went wrong!")
               .showInformation();
               }
            }
    }
    
    @FXML
    private void handleStartButton(ActionEvent event) {
        if (selectDataSource.getSelectionModel().getSelectedItem() == null ) {
            Dialogs.create()
            .owner(stage)
            .title("Information Dialog")
            .message("Please select the DataSource from the dropdown menu")
            .showInformation();
         return;
        }
        resultArea.setText("");
        if (ParseSource(map.get(selectDataSource.getSelectionModel().getSelectedItem().toString()))) {
            dbops = new DatabaseOperations();
            logger = new GuiLogger();
            if (T_Vendor.equalsIgnoreCase("Oracle")) {
               if (dbops.connect(T_DBUrl, T_UserName, T_Password, T_Port, T_Sid)) {
                   statusbar.setText("Logging started");
                   logger.startGUILogger(taskname.getText());
                   startButton.setDisable(true);
                   stop.setDisable(false);
            }


         } else if (T_Vendor.equalsIgnoreCase("MS SQL")) {
            if (dbops.connectMSSQL(T_DBUrl,T_DBName,T_UserName, T_Password, T_Port, T_Sid)) {
               statusbar.setText("Logging started");
               logger.startGUILogger(taskname.getText());
               startButton.setDisable(true);
               stop.setDisable(false);
            }
            } else {
               Dialogs.create()
               .owner(stage)
               .title("Information Dialog")
               .masthead("Look, an Information Dialog")
               .message("Currently we support Oracle and Mssql!")
               .showInformation();
               }
            }
    }

    @FXML
    private void handleStopButton(ActionEvent event) {
        if (T_Vendor.equalsIgnoreCase("Oracle")) {
            List results = dbops.returnTableCount();
            String output = "";
            if (results.size() != 0) {
                for (int i = 0; i < results.size(); i++) {
                    output = output + results.get(i);
                    resultArea.setText(output);
                }
            } else {
                resultArea.setText("Nothing Changed");
            }

            dbops.closeConnection();
            startButton.setDisable(false);
            stop.setDisable(true);
            logger.stopGUILogger();
            statusbar.setText("Logging stopped");
        } else if (T_Vendor.equalsIgnoreCase("MS SQL")) {
            List results = dbops.returnTableCountMSSQL();
            String output = "";
            if (results.size() != 0) {
                for (int i = 0; i < results.size(); i++) {
                    output = output + results.get(i);
                    resultArea.setText(output);
                }
            } else {
                resultArea.setText("Nothing Changed");
            }

            dbops.closeConnection();
            startButton.setDisable(false);
            stop.setDisable(true);
            logger.stopGUILogger();
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
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
             String line;
            selectDataSource.getItems().clear();
            while ((line = bufferedReader.readLine()) != null) {
                map.put(getIsName(line), line);
                selectDataSource.getItems().add(getIsName(line));
            }
            fileReader.close();

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
        T_Port = result[6];
        T_Sid = result[7];
        return true;
    }
    
    private String getIsName(String text) {
        String[] result = text.split(",");
        return result[0];
    }

}
