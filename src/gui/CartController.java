package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.CartService;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalPriceLabel;

    private double currentTotal = 0.0;

    @FXML
    public void initialize() {
        loadCartItems();
    }

    private void loadCartItems() {
        cartItemsContainer.getChildren().clear();
        currentTotal = 0.0;

        List<Integer> gameIds = CartService.getCart();

        // Eğer sepet boşsa
        if (gameIds.isEmpty()) {
            Label emptyLabel = new Label("Sepetiniz şu an boş. Mağazadan oyun ekleyebilirsiniz.");
            emptyLabel.setStyle("-fx-text-fill: #6060a0; -fx-font-size: 18px;");
            cartItemsContainer.getChildren().add(emptyLabel);
            totalPriceLabel.setText("0.00 TL");

            // Ana ekrandaki üst barı sıfırla
            if (MainController.instance != null) {
                MainController.instance.syncCartUI(0, 0.0);
            }
            return;
        }

        // Seçili oyunları veritabanından çekme sorgusu
        String placeholders = String.join(",", java.util.Collections.nCopies(gameIds.size(), "?"));
        String sql = "SELECT id, name, price, cover_url FROM games WHERE id IN (" + placeholders + ")";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < gameIds.size(); i++) {
                ps.setInt(i + 1, gameIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String coverUrl = rs.getString("cover_url");

                currentTotal += price; // Toplam tutarı hesapla

                // Her bir oyun için şık bir satır oluşturup ekrana bas
                HBox itemRow = createCartItemRow(id, name, price, coverUrl);
                cartItemsContainer.getChildren().add(itemRow);
            }

            totalPriceLabel.setText(String.format(java.util.Locale.US, "%.2f TL", currentTotal));

            // Ana ekrandaki sağ üst barı (sayı ve fiyatı) anlık olarak senkronize et
            if (MainController.instance != null) {
                MainController.instance.syncCartUI(gameIds.size(), currentTotal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createCartItemRow(int id, String name, double price, String coverUrl) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #2a2a5a; -fx-padding: 15; -fx-background-radius: 8;");

        // Kapak Fotoğrafı
        ImageView img = new ImageView();
        img.setFitWidth(80);
        img.setFitHeight(110);
        img.setPreserveRatio(true);
        try {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                if (coverUrl.startsWith("//")) coverUrl = "https:" + coverUrl;
                coverUrl = coverUrl.replace("t_thumb", "t_cover_big");
                img.setImage(new Image(coverUrl, true));
            }
        } catch (Exception e) {
            System.out.println("Resim yüklenemedi");
        }

        // Oyun Adı
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        nameLbl.setPrefWidth(500);
        nameLbl.setWrapText(true);

        // Fiyat
        Label priceLbl = new Label(String.format(java.util.Locale.US, "%.2f TL", price));
        priceLbl.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 20px; -fx-font-weight: bold;");
        priceLbl.setPrefWidth(150);

        // Sepetten Çıkar Butonu
        Button removeBtn = new Button("X");
        removeBtn.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            try {
                int currentUserId = 1; // Tuğalp'in sistemi gelince dinamik olacak
                // 1. Veritabanından sil
                new magaza.service.GameService().removeFromCart(currentUserId, id);
            } catch (Exception ex) {
                System.out.println("Veritabanından silinirken hata: " + ex.getMessage());
            }

            // 2. Arayüz hafızasından sil
            CartService.removeGame(id);

            // 3. Ekranı yenile (Bu sayede üst bar da otomatik güncellenecek)
            loadCartItems();
        });

        row.getChildren().addAll(img, nameLbl, priceLbl, removeBtn);
        return row;
    }

    @FXML
    private void handleCheckout() {
        if (currentTotal == 0) return;

        try {
            // Ödeme sayfasına yönlendir
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("payment.fxml"));
            Node paymentPage = loader.load();

            // Toplam tutarı ödeme ekranına gönder
            PaymentController controller = loader.getController();
            controller.setTotalAmount(currentTotal);

            // Ana ekrana bas
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) cartItemsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(paymentPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}