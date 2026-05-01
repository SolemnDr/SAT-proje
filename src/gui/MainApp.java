package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Sadece bu metot kalsın, diğer start metodunu sil!
        Parent root = FXMLLoader.load(getClass().getResource("/gui/main_layout.fxml"));
        primaryStage.setTitle("Oyun Kütüphanesi");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}