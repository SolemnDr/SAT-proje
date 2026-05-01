package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    public static MainController instance;

    @FXML private StackPane contentArea;
    @FXML private Label cartInfoLabel;

    private int cartItemCount = 0;
    private double cartTotalPrice = 0.0;

    @FXML
    public void initialize() {
        instance = this;
        showStore();
    }
    public void syncCartUI(int count, double total) {
        this.cartItemCount = count;
        this.cartTotalPrice = total;
        if (cartInfoLabel != null) {
            cartInfoLabel.setText(String.format(java.util.Locale.US, "Sepet: %d Ürün (%.2f TL)", count, total));
        }
    }

    public void updateCartUI(double addedPrice) {
        cartItemCount++;
        cartTotalPrice += addedPrice;
        cartInfoLabel.setText(String.format(java.util.Locale.US, "Sepet: %d Ürün (%.2f TL)", cartItemCount, cartTotalPrice));
    }
    public void resetCartUI() {
        cartItemCount = 0;
        cartTotalPrice = 0.0;
        if (cartInfoLabel != null) {
            cartInfoLabel.setText("Sepet: 0 Ürün (0.00 TL)");
        }
    }

    @FXML
    private void showCartPage() {
        // Önceden sadece konsola yazdırıyordu, şimdi sepet ekranını yüklüyor
        loadPage("cart.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node page = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
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
        System.out.println("Kütüphane ekranına geçiliyor...");
    }

    @FXML
    private void showSocial() {
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