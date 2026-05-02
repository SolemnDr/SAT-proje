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

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalPriceLabel;

    private double currentTotal = 0.0;

    @FXML
    public void initialize() {
        syncCartFromDB();
        loadCartItems();
    }

    private void syncCartFromDB() {
        int currentUserId = util.Session.getCurrentUserId();

        // HAYALET ÜRÜN KALKANI V2: Sadece mağazada HALA VAR OLAN oyunları say!
        // Mağazadan silinmiş ama sepette kalmış kalıntıları (JOIN ile) filtreliyoruz.
        String sql = "SELECT DISTINCT c.game_id FROM cart c JOIN games g ON c.game_id = g.id WHERE c.user_id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            util.CartService.clearCart();
            while (rs.next()) {
                util.CartService.addToCart(rs.getInt("game_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCartItems() {
        cartItemsContainer.getChildren().clear();
        currentTotal = 0.0;

        int currentUserId = util.Session.getCurrentUserId();

        // İNDİRİM SÜTUNU VE HAYALET KALKANI EKLENDİ
        String sql = "SELECT DISTINCT g.id, g.name, g.price, g.discount_percent, g.cover_url " +
                "FROM games g " +
                "JOIN cart c ON g.id = c.game_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            boolean hasItems = false;

            while (rs.next()) {
                hasItems = true;
                int id       = rs.getInt("id");
                String name  = rs.getString("name");
                double originalPrice = rs.getDouble("price");
                double discount = rs.getDouble("discount_percent");
                String cover = rs.getString("cover_url");

                // MATEMATİK ZAMANI
                double finalPrice = originalPrice;
                if (discount > 0) {
                    finalPrice = originalPrice * (1 - (discount / 100.0));
                }

                currentTotal += finalPrice; // Toplama indirimli fiyat ekleniyor
                cartItemsContainer.getChildren().add(createCartItemRow(id, name, originalPrice, finalPrice, discount, cover));

                if (!util.CartService.getCart().contains(id)) {
                    util.CartService.addToCart(id);
                }
            }

            if (!hasItems) {
                Label emptyLabel = new Label("Sepetiniz şu an boş. Mağazadan oyun ekleyebilirsiniz.");
                emptyLabel.setStyle("-fx-text-fill: #6060a0; -fx-font-size: 18px;");
                cartItemsContainer.getChildren().add(emptyLabel);
                totalPriceLabel.setText("0.00 TL");
                if (MainController.instance != null) MainController.instance.syncCartUI(0, 0.0);
                return;
            }

            totalPriceLabel.setText(String.format(java.util.Locale.US, "%.2f TL", currentTotal));

            if (MainController.instance != null) {
                MainController.instance.syncCartUI(util.CartService.getCart().size(), currentTotal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createCartItemRow(int id, String name, double originalPrice, double finalPrice, double discount, String coverUrl) {
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

        // FİYAT GÖSTERİMİ (İndirimliyse üstü çizili tasarım)
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        priceBox.setPrefWidth(150);

        if (discount > 0) {
            Label origLbl = new Label(String.format(java.util.Locale.US, "%.2f TL", originalPrice));
            origLbl.setStyle("-fx-text-fill: #a0a0c0; -fx-font-size: 14px; -fx-strikethrough: true;");

            Label finalLbl = new Label(String.format(java.util.Locale.US, "%.2f TL", finalPrice));
            finalLbl.setStyle("-fx-text-fill: #ffce00; -fx-font-size: 20px; -fx-font-weight: bold;");

            priceBox.getChildren().addAll(origLbl, finalLbl);
        } else {
            Label finalLbl = new Label(String.format(java.util.Locale.US, "%.2f TL", finalPrice));
            finalLbl.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 20px; -fx-font-weight: bold;");
            priceBox.getChildren().add(finalLbl);
        }

        // Sepetten Çıkar Butonu
        Button removeBtn = new Button("X");
        removeBtn.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            try {
                int currentUserId = util.Session.getCurrentUserId();
                new magaza.service.GameService().removeFromCart(currentUserId, id);
            } catch (Exception ex) {
                System.out.println("Veritabanından silinirken hata: " + ex.getMessage());
            }

            CartService.removeGame(id);
            loadCartItems();
        });

        row.getChildren().addAll(img, nameLbl, priceBox, removeBtn);
        return row;
    }

    @FXML
    private void handleCheckout() {
        if (currentTotal == 0) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("payment.fxml"));
            Node paymentPage = loader.load();

            PaymentController controller = loader.getController();
            controller.setTotalAmount(currentTotal); // İndirimli tutar gidiyor!

            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) cartItemsContainer.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(paymentPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}