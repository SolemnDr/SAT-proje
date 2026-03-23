package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("login.fxml")
        );

        if (loader.getLocation() == null) {
            return;
        }

        Scene scene = new Scene(loader.load());
        stage.setTitle("GameStore");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}