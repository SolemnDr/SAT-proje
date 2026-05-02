package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import magaza.model.Game;
import magaza.service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoreController {

    @FXML private FlowPane gamesContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;

    private final GameService gameService = new GameService();
    private int currentPage = 1;
    private final int pageSize = 20;

    @FXML
    public void initialize() {
        categoryBox.getItems().addAll(
                "Tüm Kategoriler", "Action", "Adventure", "RPG", "Shooter",
                "Strategy", "Simulator", "Sport", "Racing", "Fighting", "Puzzle"
        );
        categoryBox.getSelectionModel().selectFirst();

        // Kategori değiştiğinde
        categoryBox.setOnAction(e -> {
            currentPage = 1;
            loadGames();
        });

        // Arama kutusunda "Enter" tuşuna basıldığında
        searchField.setOnAction(e -> {
            currentPage = 1;
            loadGames();
        });

        loadGames();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        loadGames();
    }

    private void loadGames() {
        gamesContainer.getChildren().clear();

        // Kullanıcıya bilgi ver
        Label loadingLabel = new Label("Oyunlar aranıyor, lütfen bekleyin...");
        loadingLabel.setStyle("-fx-text-fill: #a0a0c0; -fx-font-size: 16px;");
        gamesContainer.getChildren().add(loadingLabel);

        // ARAMA VE VERİTABANI İŞLEMİNİ ARKA PLANA (Yeni Thread) ALIYORUZ Kİ EKRAN DONMASIN!
        new Thread(() -> {
            try {
                // Türkçe karakter sorununu çözen İngilizce küçültme
                String searchText = searchField.getText().trim().toLowerCase(java.util.Locale.ENGLISH);
                String selectedCategory = categoryBox.getValue();

                List<Game> allMatches = null;
                List<Game> gamesToDisplay = new ArrayList<>();
                boolean isPaginationFromDB = false;

                // 1. VERİLERİ GETİRME AŞAMASI
                if (!searchText.isEmpty()) {
                    allMatches = gameService.searchByName(searchText);
                } else if (!selectedCategory.equals("Tüm Kategoriler")) {
                    allMatches = gameService.getAllGames();
                } else {
                    gamesToDisplay = gameService.getGamesByPage(currentPage, pageSize);
                    isPaginationFromDB = true;
                }

                // 2. KATEGORİ FİLTRESİNİ UYGULAMA
                if (!selectedCategory.equals("Tüm Kategoriler") && allMatches != null) {
                    allMatches = allMatches.stream()
                            .filter(g -> {
                                if (g.getGenres() == null) return false;

                                String gameGenres = g.getGenres().toLowerCase(java.util.Locale.ENGLISH);
                                String selectedCat = selectedCategory.toLowerCase(java.util.Locale.ENGLISH);

                                if (selectedCat.equals("action")) {
                                    return gameGenres.contains("action") ||
                                            gameGenres.contains("hack and slash") ||
                                            gameGenres.contains("beat 'em up");
                                }
                                else if (selectedCat.equals("rpg")) {
                                    return gameGenres.contains("rpg") ||
                                            gameGenres.contains("role-playing");
                                }
                                else if (selectedCat.equals("simulator") || selectedCat.equals("simulation")) {
                                    return gameGenres.contains("simulat");
                                }
                                else {
                                    return gameGenres.contains(selectedCat);
                                }
                            })
                            .collect(Collectors.toList());
                }

                // 3. ARAMA/FİLTRE SONUÇLARINI SAYFALAMA
                if (!isPaginationFromDB && allMatches != null) {
                    int fromIndex = (currentPage - 1) * pageSize;
                    int toIndex = Math.min(fromIndex + pageSize, allMatches.size());
                    if (fromIndex < allMatches.size()) {
                        gamesToDisplay = allMatches.subList(fromIndex, toIndex);
                    }
                }

                final List<Game> finalGames = gamesToDisplay;
                final boolean disableNext = isPaginationFromDB
                        ? (finalGames == null || finalGames.size() < pageSize)
                        : (allMatches == null || (currentPage * pageSize) >= allMatches.size());

                // 4. EKRANA BASMA İŞLEMİ (UI Thread'e dönüyoruz)
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();

                    if (finalGames != null && !finalGames.isEmpty()) {
                        for (Game game : finalGames) {
                            gamesContainer.getChildren().add(createGameCard(game));
                        }
                    } else {
                        Label emptyLabel = new Label("Bu kategoride veya isimde oyun bulunamadı.");
                        emptyLabel.setStyle("-fx-text-fill: #ff4c4c; -fx-font-size: 16px;");
                        gamesContainer.getChildren().add(emptyLabel);
                    }

                    pageLabel.setText("Sayfa " + currentPage);
                    prevPageBtn.setDisable(currentPage == 1);
                    nextPageBtn.setDisable(disableNext);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    gamesContainer.getChildren().clear();
                    Label errorLbl = new Label("Veritabanı çekilirken hata oluştu!");
                    errorLbl.setStyle("-fx-text-fill: red;");
                    gamesContainer.getChildren().add(errorLbl);
                });
            }
        }).start();
    }

    @FXML
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadGames();
        }
    }

    @FXML
    private void nextPage() {
        currentPage++;
        loadGames();
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2a2a5a; -fx-padding: 15; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefWidth(200);

        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(170);
        coverImage.setFitHeight(230);
        coverImage.setPreserveRatio(true);
        coverImage.setStyle("-fx-background-color: #1a1a2e;");

        try {
            String url = game.getCoverUrl();
            if (url != null && !url.trim().isEmpty()) {
                if (url.startsWith("//")) url = "https:" + url;
                url = url.replace("t_thumb", "t_cover_big");
                coverImage.setImage(new Image(url, true));
            } else {
                System.out.println("Oyun için görsel URL'si girilmemiş: " + game.getName());
            }
        } catch (Exception e) {
            System.out.println("Resim yüklenirken hata oluştu: " + game.getName());
        }

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setMaxWidth(180);
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // --- İNDİRİM MATEMATİĞİ BURAYA EKLENDİ ---
        double originalPrice = (game.getPrice() > 0) ? game.getPrice() : 0.0;
        double discount = game.getDiscountPercent();
        double finalPrice = originalPrice;

        if (discount > 0) {
            finalPrice = originalPrice * (1 - (discount / 100.0));
        }

        Label priceLabel = new Label(String.format(java.util.Locale.US, "%.2f TL", finalPrice));
        priceLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-font-size: 16px;");

        if (discount > 0) {
            priceLabel.setText(String.format(java.util.Locale.US, "%.2f TL (-%%%.0f)", finalPrice, discount));
            priceLabel.setStyle("-fx-text-fill: #ffce00; -fx-font-weight: bold; -fx-font-size: 16px;"); // İndirimliyse sarı dikkat çeker
        }
        // ------------------------------------------

        card.getChildren().addAll(coverImage, nameLabel, priceLabel);

        card.setOnMouseClicked(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("game_detail.fxml"));
                Node detailPage = loader.load();

                GameDetailController controller = loader.getController();
                controller.loadGameData(game.getId());

                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) gamesContainer.getScene().lookup("#contentArea");
                contentArea.getChildren().clear();
                contentArea.getChildren().add(detailPage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3a3a7a; -fx-padding: 15; -fx-background-radius: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2a2a5a; -fx-padding: 15; -fx-background-radius: 10; -fx-cursor: hand;"));

        return card;
    }
}