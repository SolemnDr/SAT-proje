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
    @FXML private Label averageRatingLabel;

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
                try {
                    double avg = reviewDAO.getAverageRating(gameId);
                    if (avg > 0.0) {
                        // Puan varsa altın sarısı renkte göster
                        averageRatingLabel.setText(String.format(java.util.Locale.US, "Ortalama Puan: %.1f / 5.0 ⭐", avg));
                    } else {
                        averageRatingLabel.setText("Henüz puan verilmemiş");
                    }
                } catch (Exception e) {
                    System.out.println("Ortalama puan çekilemedi!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkGameStatus(gameId);
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

    @FXML
    private void handleSubmitReview() {
        int userId = util.Session.getCurrentUserId();

        // --- 1. ADIM: KULLANICI BU OYUNA SAHİP Mİ KONTROLÜ ---
        try {
            magaza.service.GameService gameService = new magaza.service.GameService();
            java.util.List<magaza.model.Game> myGames = gameService.getPurchasedGames(userId);

            // Kullanıcının oyunları içinde şu anki oyunun ID'si var mı diye bakıyoruz
            boolean ownsGame = myGames.stream().anyMatch(g -> g.getId() == currentGameId);

            if (!ownsGame) {
                // Sahip değilse ekrana uyarı ver ve metodu durdur
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("GameStore | İnceleme Uyarısı");
                alert.setHeaderText(null);
                alert.setContentText("Bu oyunu inceleyebilmek için önce satın alıp kütüphanenize eklemelisiniz!");

                DialogPane pane = alert.getDialogPane();
                pane.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #5352ed; -fx-border-width: 2;");
                pane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"));

                alert.showAndWait();
                return; // İşlemi iptal et!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ----------------------------------------------------

        // --- 2. ADIM: YORUMU VERİTABANINA YAZMA ---
        Integer rating = ratingComboBox.getValue();
        String comment = reviewInput.getText().trim();

        if (rating == null || comment.isEmpty()) {
            System.out.println("Lütfen bir puan seçin ve yorum yazın!");
            return;
        }

        boolean isSuccess = reviewDAO.addReview(userId, currentGameId, rating, comment);

        if (isSuccess) {
            reviewInput.clear();
            ratingComboBox.setValue(null);
            loadReviews(currentGameId);
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
    // --- YENİ: BUTON DURUMUNU KONTROL EDEN METOT ---
    private void checkGameStatus(int gameId) {
        int currentUserId = util.Session.getCurrentUserId();
        try {
            magaza.service.GameService gameService = new magaza.service.GameService();

            // 1. Oyun zaten kütüphanede var mı?
            boolean isOwned = gameService.getPurchasedGames(currentUserId).stream().anyMatch(g -> g.getId() == gameId);

            // 2. Oyun zaten sepette mi?
            boolean isInCart = util.CartService.getCart().contains(gameId);

            if (isOwned) {
                // Eğer oyuna sahipse butonu koyu renk yap ve kilitle
                addToCartButton.setText("Kütüphanenizde Var");
                addToCartButton.setStyle("-fx-background-color: #2a2a5a; -fx-text-fill: #a0a0b0; -fx-border-color: #5352ed; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12;");
                addToCartButton.setDisable(true);
            } else if (isInCart) {
                // Eğer oyun sepetteyse butonu turuncu yap ve kilitle
                addToCartButton.setText("Zaten Sepette");
                addToCartButton.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12; -fx-background-radius: 8;");
                addToCartButton.setDisable(true);
            } else {
                // Hiçbiri değilse normal sepet butonunu göster
                addToCartButton.setText("Sepete Ekle");
                addToCartButton.setStyle("-fx-background-color: #5352ed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
                addToCartButton.setDisable(false);
            }
        } catch (Exception e) {
            System.out.println("Oyun durumu kontrol edilemedi: " + e.getMessage());
        }
    }
}