package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import magaza.model.Game; // setGame için eklendi
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GameDetailController {

    @FXML private ImageView coverImageView;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label summaryLabel;
    @FXML private Button addToCartButton;

    // YORUM SİSTEMİ İÇİN YENİ EKLENEN FXML DEĞİŞKENLERİ
    @FXML private ListView<String> reviewsListView;
    @FXML private TextField reviewInput;
    @FXML private ComboBox<Integer> ratingComboBox;

    private int currentGameId;
    private double currentGamePrice;
    private final magaza.dao.ReviewDAO reviewDAO = new magaza.dao.ReviewDAO();

    @FXML
    public void initialize() {
        // Puanlama kutusuna 1'den 5'e kadar seçenekleri ekliyoruz
        if (ratingComboBox != null) {
            ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
        }
    }

    // İŞTE KÜTÜPHANEDEN GELEN HATAYI ÇÖZEN O EKSİK METOT!
    public void setGame(Game game) {
        // Obje geldiğinde içinden ID'yi alıp normal yükleme metodumuzu çağırıyoruz
        loadGameData(game.getId());
    }

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

                // OYUN YÜKLENDİĞİNDE YORUMLARI DA GETİR
                loadReviews(gameId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReviews(int gameId) {
        if (reviewsListView == null) return;

        reviewsListView.getItems().clear();

        try {
            // DAO sınıfındaki hazır metodu kullanıyoruz!
            java.util.List<magaza.model.Review> reviews = reviewDAO.getGameReviews(gameId);

            if (reviews == null || reviews.isEmpty()) {
                reviewsListView.getItems().add("Bu oyun için henüz yorum yapılmamış. İlk yorumu sen yaz!");
            } else {
                for (magaza.model.Review r : reviews) {
                    // Yıldız sayısına göre emoji oluştur (Örn: ⭐⭐⭐)
                    String starString = "⭐".repeat(Math.max(0, r.getRating()));

                    // Listeye şık bir formatta ekle (Senin DAO username'i de getiriyor, harika!)
                    reviewsListView.getItems().add(r.getUsername() + " (" + starString + "):\n" + r.getComment());
                }
            }
        } catch (Exception e) {
            System.out.println("Yorumlar yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }

    // --- YENİ: YORUM GÖNDERME METODU ---
    @FXML
    private void handleSubmitReview() {
        int userId = util.Session.getCurrentUserId();
        Integer rating = ratingComboBox.getValue();
        String comment = reviewInput.getText().trim();

        if (rating == null || comment.isEmpty()) {
            System.out.println("Lütfen bir puan seçin ve yorum yazın!");
            return;
        }

        // Yine DAO'daki efsane addReview metodumuzu kullanıyoruz
        boolean isSuccess = reviewDAO.addReview(userId, currentGameId, rating, comment);

        if (isSuccess) {
            // Gönderdikten sonra kutuları temizle ve listeyi yenile
            reviewInput.clear();
            ratingComboBox.setValue(null);
            loadReviews(currentGameId);
            System.out.println("Yorum başarıyla eklendi/güncellendi!");
        } else {
            System.out.println("Yorum eklenirken veritabanı hatası!");
        }
    }

    @FXML
    private void handleAddToCart() {
        try {
            int currentUserId = util.Session.getCurrentUserId();
            magaza.service.GameService gameService = new magaza.service.GameService();

            gameService.addToCart(currentUserId, currentGameId);
            util.CartService.addToCart(currentGameId); // Metot adını addToCart olarak düzelttim (senin util.CartService'e göre)

            if (MainController.instance != null) {
                MainController.instance.updateCartUI(currentGamePrice);
            }

            addToCartButton.setText("Sepete Eklendi ✔");
            addToCartButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-opacity: 1;");
            addToCartButton.setDisable(true);

        } catch (Exception e) {
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