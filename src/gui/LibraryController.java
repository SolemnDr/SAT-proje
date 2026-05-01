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

    @FXML
    public void initialize() {
        // 1. Veritabanından gelen veya oluşturulan oyunları listeye ekle
        List<Game> myGames = new ArrayList<>();

        // ÖRNEK: İsme göre resim atayan bir yapı kuruyoruz
        myGames.add(createGameWithImage("The Witcher 3", "https://images.igdb.com/igdb/image/upload/t_cover_big/co1sf5.jpg"));
        myGames.add(createGameWithImage("Cyberpunk 2077", "https://images.igdb.com/igdb/image/upload/t_cover_big/co2mjt.jpg"));
        myGames.add(createGameWithImage("Red Dead Redemption 2", "https://images.igdb.com/igdb/image/upload/t_cover_big/co1q1f.jpg"));

        renderGames(myGames);
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
        Image img = new Image(game.getDescription(), true);
        iv.setImage(img);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Button playBtn = new Button("Oyna");
        playBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        playBtn.setPrefWidth(120);

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