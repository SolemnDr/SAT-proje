package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Uygulama açılır açılmaz ortada Mağaza gözüksün
        showStore();
    }

    // Ortadaki boşluğa istediğimiz FXML'i yükleyen sihirli metot
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node page = loader.load();
            contentArea.getChildren().clear(); // Önceki sayfayı temizle
            contentArea.getChildren().add(page); // Yeni sayfayı ekrana bas
        } catch (Exception e) {
            System.out.println("Sayfa yüklenirken hata oluştu: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    private void showStore() {
        loadPage("store.fxml");
    }

    @FXML
    private void showLibrary() {
        // Onur kütüphane ekranını bitirince burayı aktif edeceğiz
        // loadPage("library.fxml");
        System.out.println("Kütüphane ekranına geçiliyor...");
    }

    @FXML
    private void showSocial() {
        // Onur sosyal ekranı bitirince burayı aktif edeceğiz
        // loadPage("social.fxml");
        System.out.println("Sosyal ekrana geçiliyor...");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 960, 560));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}