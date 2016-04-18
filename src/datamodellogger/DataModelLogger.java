package datamodellogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DataModelLogger extends Application {

    

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        Image ico = new Image("images/DataLoggerIcon.png");
        stage.setResizable(false);
        stage.getIcons().add(ico);
        stage.setTitle("Data Model Logger");
        stage.setScene(scene);
      
        stage.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

   

}
