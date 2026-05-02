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
        int currentUserId = util.Session.getCurrentUserId();
        try {
            kutuphane.dao.LibraryDAO libDAO = new kutuphane.dao.LibraryDAO();
            libDAO.createTablesIfNotExists();
            List<magaza.model.Game> myGames = libDAO.getVisibleLibrary(currentUserId);

            if (myGames != null && !myGames.isEmpty()) {
                renderGames(myGames);
            } else {
                gamesGrid.getChildren().clear();
                Label emptyLabel = new Label("Kütüphanenizde henüz oyun bulunmuyor.");
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
        String coverUrl = game.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            if (coverUrl.startsWith("//")) coverUrl = "https:" + coverUrl;
            coverUrl = coverUrl.replace("t_thumb", "t_cover_big");
            try {
                iv.setImage(new Image(coverUrl, true));
            } catch (Exception ex) {
                System.out.println("Resim yüklenemedi: " + game.getName());
            }
        }

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Button playBtn = new Button("Detaylar"); // İstersen "Oyna" olarak bırak
        playBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        playBtn.setPrefWidth(120);

        playBtn.setOnAction(event -> {
            try {
                // 1. ŞÜPHELİ: Dosya adı büyük/küçük harf uyumu.
                // Eğer sol taraftaki dosya ağacında dosyanın adı GameDetail.fxml veya gamedetail.fxml ise
                // aşağıdaki tırnak içindeki ismi BİREBİR onunla aynı yapmalısın!
                String fxmlDosyaAdi = "game_detail.fxml";

                java.net.URL fxmlUrl = getClass().getResource(fxmlDosyaAdi);

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
                javafx.scene.Node detailPage = loader.load();

                gui.GameDetailController controller = loader.getController();
                controller.setGame(game);

                // Ekranı değiştirme
                javafx.scene.Parent root = playBtn.getScene().getRoot();
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) root.lookup("#contentArea");

                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(detailPage);
                } else {
                    System.out.println("HATA: contentArea paneli arayüzde bulunamadı!");
                }
            } catch (Exception e) {
                System.out.println("Oyun detay sayfası yüklenirken bir hata oluştu!");
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