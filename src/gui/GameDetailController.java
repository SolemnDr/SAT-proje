package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button; // BUNU EKLEDİK
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GameDetailController {

    @FXML private ImageView coverImageView;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label summaryLabel;
    @FXML private Button addToCartButton; // BUNU EKLEDİK

    private int currentGameId;
    private double currentGamePrice; // Sepete eklerken fiyatı bilmemiz lazım

    public void loadGameData(int gameId) {
        this.currentGameId = gameId;

        String sql = "SELECT name, summary, price, cover_url FROM games WHERE id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));

                currentGamePrice = rs.getDouble("price");
                priceLabel.setText(String.format(java.util.Locale.US, "%.2f TL", currentGamePrice));

                String summary = rs.getString("summary");
                summaryLabel.setText((summary == null || summary.isEmpty()) ? "Bu oyun için henüz bir özet girilmemiş." : summary);

                String coverUrl = rs.getString("cover_url");
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    if (coverUrl.startsWith("//")) coverUrl = "https:" + coverUrl;
                    coverUrl = coverUrl.replace("t_thumb", "t_cover_big");
                    coverImageView.setImage(new Image(coverUrl, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToCart() {
        try {
            int currentUserId = util.SessionManager.getCurrentUserId();
            magaza.service.GameService gameService = new magaza.service.GameService();

            // 1. GERÇEK VERİTABANI SEPETİNE EKLE (Backend'in hata vermemesi için)
            gameService.addToCart(currentUserId, currentGameId);

            // 2. ARAYÜZ HAFIZASINA DA EKLE (Sepetim sayfasında oyunları görebilmemiz için)
            util.CartService.addGame(currentGameId);

            // 3. Üst barı güncelle ve butonu yeşile boyayıp kilitle
            if (MainController.instance != null) {
                MainController.instance.updateCartUI(currentGamePrice);
            }

            addToCartButton.setText("Sepete Eklendi ✔");
            addToCartButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-opacity: 1;");
            addToCartButton.setDisable(true);

        } catch (Exception e) {
            // Eğer oyun zaten veritabanında sepetteyse veya daha önce satın alınmışsa burası çalışır
            System.out.println("Sepete eklenemedi: " + e.getMessage());
            addToCartButton.setText("Zaten Eklendi/Alındı");
            addToCartButton.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;");
            addToCartButton.setDisable(true);
        }
    }

    @FXML
    private void handleBackToStore() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("store.fxml"));
            Node storePage = loader.load();
            StackPane contentArea = (StackPane) nameLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(storePage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}