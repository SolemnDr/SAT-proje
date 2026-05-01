package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public void start(Stage primaryStage) throws Exception {
        // BURASI ÇOK ÖNEMLİ: Uygulamanın ilk açacağı sayfa login.fxml olmalı!
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));

        primaryStage.setTitle("GameStore");
        primaryStage.setScene(new Scene(root)); // Ekran boyutları varsa ekleyebilirsin
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}