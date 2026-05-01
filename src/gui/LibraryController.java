package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import magaza.model.Game;
import java.util.ArrayList;
import java.util.List;

public class LibraryController {
    // FXML dosyasındaki fx:id="gamesGrid" ile birebir aynı olmalı
    @FXML private FlowPane gamesGrid;
    @FXML private TextField searchField;
    private final magaza.service.GameService gameService = new magaza.service.GameService();

    @FXML
    public void initialize() {
        // Oturumdaki gerçek kullanıcı ID'sini al
        int currentUserId = util.Session.getCurrentUserId();

        try {
            // Veritabanından bu kullanıcının SATIN ALDIĞI oyunları çek
            List<Game> myGames = gameService.getPurchasedGames(currentUserId);

            if (myGames != null && !myGames.isEmpty()) {
                // Oyun varsa ekrana diz
                renderGames(myGames);
            } else {
                // Kütüphane boşsa şık bir uyarı göster
                gamesGrid.getChildren().clear();
                Label emptyLabel = new Label("Kütüphanenizde henüz oyun bulunmuyor. Mağazaya göz atıp maceralara atılabilirsiniz!");
                emptyLabel.setStyle("-fx-text-fill: #7a7a9a; -fx-font-size: 16px; -fx-font-style: italic;");
                gamesGrid.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            System.out.println("Kütüphane yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }

    private Game createGameWithImage(String name, String url) {
        Game g = new Game();
        g.setName(name);
        // Hata veren getDescription() yerine direkt description alanına set ediyoruz
        g.setDescription(url);
        return g;
    }
    private void renderGames(List<Game> games) {
        if (gamesGrid == null) return;
        gamesGrid.getChildren().clear();
        for (Game game : games) {
            gamesGrid.getChildren().add(createGameCard(game));
        }
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2a2a4a; -fx-background-radius: 15; -fx-padding: 15;");
        card.setPrefWidth(220);

        ImageView iv = new ImageView();
        iv.setFitWidth(190);
        iv.setFitHeight(260);
        iv.setPreserveRatio(true);

        // Background loading kasmayı engeller
        Image img = new Image(game.getCoverUrl(), true);
        iv.setImage(img);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Button playBtn = new Button("Detaylar"); // İstersen "Oyna" olarak bırak
        playBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        playBtn.setPrefWidth(120);

        // --- YENİ EKLENEN KISIM: Tıklama Olayı (Event) ---
        playBtn.setOnAction(event -> {
            try {
                // Oyun detay sayfasını (gameDetail.fxml) yükle
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("gameDetail.fxml"));
                javafx.scene.Node detailPage = loader.load();

                // Detay sayfasına hangi oyuna tıklandığını haber ver
                // (Eğer GameDetailController'ının adı farklıysa ona göre düzelt)
                gui.GameDetailController controller = loader.getController();
                controller.setGame(game);

                // MainController'daki contentArea'yı bulup içine detay sayfasını göm
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) playBtn.getScene().lookup("#contentArea");
                contentArea.getChildren().clear();
                contentArea.getChildren().add(detailPage);
            } catch (Exception e) {
                System.out.println("Oyun detay sayfası açılamadı!");
                e.printStackTrace();
            }
        });
        // --------------------------------------------------

        card.getChildren().addAll(iv, nameLabel, playBtn);
        return card;
    }

    private Game createMockGame(String name, String imgUrl) {
        Game g = new Game();
        g.setName(name);
        g.setDescription(imgUrl);
        return g;
    }
}