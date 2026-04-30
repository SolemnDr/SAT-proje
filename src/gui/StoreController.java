package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StoreController {

    @FXML
    private FlowPane gamesContainer;

    @FXML
    public void initialize() {
        gamesContainer.getChildren().clear(); // Sahte oyunları temizle
        loadRealGamesFromDatabase(); // Gerçek oyunları çek
    }

    private void loadRealGamesFromDatabase() {
        // Performans için şimdilik ilk 50 oyunu çekiyoruz (989 resmi aynı anda indirmek arayüzü dondurabilir)
        String sql = "SELECT id, name, price, cover_url FROM games LIMIT 50";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getInt("price"); // Veritabanında price REAL ise getDouble da kullanılabilir
                String coverUrl = rs.getString("cover_url");

                VBox gameCard = createGameCard(id, name, price, coverUrl);
                gamesContainer.getChildren().add(gameCard);
            }

        } catch (Exception e) {
            System.out.println("Oyunlar veritabanından çekilirken hata oluştu!");
            e.printStackTrace();
        }
    }

    private VBox createGameCard(int id, String name, double price, String coverUrl) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2a2a5a; -fx-background-radius: 10; -fx-padding: 15; -fx-cursor: hand;");
        card.setPrefSize(200, 300);

        // GERÇEK KAPAK FOTOĞRAFI
        ImageView imageView = new ImageView();
        imageView.setFitWidth(160);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        try {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                // IGDB URL'leri genelde "//images.igdb..." diye başlar, başına "https:" ekliyoruz
                if (coverUrl.startsWith("//")) {
                    coverUrl = "https:" + coverUrl;
                }
                // IGDB'nin ufak ikonları (t_thumb) yerine daha net kapakları (t_cover_big) çekiyoruz
                coverUrl = coverUrl.replace("t_thumb", "t_cover_big");

                // true parametresi resmi "Arka Planda Yükle" demektir, böylece program donmaz!
                Image image = new Image(coverUrl, true);
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Resim yüklenemedi: " + name);
        }

        // Oyun Adı
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        // İsim uzunsa alt satıra geçsin
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLabel.setMaxWidth(180);

        // Fiyat
        Label priceLabel = new Label(price <= 0 ? "Ücretsiz" : price + " TL");
        priceLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 14px; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, nameLabel, priceLabel);

        // Hover (Üzerine gelme) efektleri
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3a3a7a; -fx-background-radius: 10; -fx-padding: 15; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2a2a5a; -fx-background-radius: 10; -fx-padding: 15; -fx-cursor: hand;"));

        // Oyuna Tıklama (Şimdilik sadece konsola ID'sini yazdırsın, sonra Detay sayfasına bağlayacağız)
        card.setOnMouseClicked(e -> {
            System.out.println("Tıklanan Oyun ID: " + id + " | Seçilen Oyun: " + name);
        });

        return card;
    }
}