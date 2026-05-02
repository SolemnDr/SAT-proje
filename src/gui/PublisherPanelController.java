package gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import magaza.model.Game;
import magaza.service.GameService;
import util.Session;

import java.util.List;

public class PublisherPanelController {

    @FXML private TabPane mainTabPane;
    @FXML private Tab addEditTab;
    @FXML private Button submitGameBtn;

    @FXML private TableView<Game> myGamesTable;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, Double> colPrice;
    @FXML private TableColumn<Game, Integer> colSales;
    @FXML private TableColumn<Game, Double> colDiscount;

    @FXML private TextField newNameField, newPriceField, newGenresField, newCoverUrlField, discountField;
    @FXML private TextArea newSummaryArea;

    private final GameService gameService = new GameService();
    private Integer editingGameId = null; // Düzenlenen oyunun ID'sini tutar. Boşsa "Yeni Ekleme" modundadır.

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSales.setCellValueFactory(new PropertyValueFactory<>("salesCount"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));

        refreshTable();
    }

    private void refreshTable() {
        try {
            int publisherId = Session.getCurrentUserId();
            List<Game> games = gameService.getPublisherGames(publisherId);
            myGamesTable.setItems(FXCollections.observableArrayList(games));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- YENİ EKLENEN METOT: DÜZENLEME MODUNA GEÇİŞ ---
    @FXML
    private void handleEditSelectedGame() {
        Game selected = myGamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen düzenlemek için tablodan bir oyun seçin.");
            return;
        }

        editingGameId = selected.getId();

        // Verileri kutulara doldur
        newNameField.setText(selected.getName());
        newPriceField.setText(String.valueOf(selected.getPrice()));
        newGenresField.setText(selected.getGenres() == null ? "" : selected.getGenres());
        newCoverUrlField.setText(selected.getCoverUrl() == null ? "" : selected.getCoverUrl());
        newSummaryArea.setText(selected.getSummary() == null ? "" : selected.getSummary());

        // Arayüzü "Güncelleme" stiline dönüştür
        submitGameBtn.setText("Değişiklikleri Kaydet");
        submitGameBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");
        addEditTab.setText("Oyunu Düzenle");

        // Sekmeyi otomatik olarak değiştir
        mainTabPane.getSelectionModel().select(addEditTab);
    }

    // --- GÜNCELLENEN METOT: HEM EKLEME HEM KAYDETME YAPAR ---
    @FXML
    private void handleSubmitGame() {
        try {
            magaza.dao.GameDAO dao = new magaza.dao.GameDAO();

            if (editingGameId == null) {
                // 1. YENİ EKLEME MODU
                Game g = new Game();
                g.setName(newNameField.getText());
                g.setPrice(Double.parseDouble(newPriceField.getText()));
                g.setGenres(newGenresField.getText());
                g.setCoverUrl(newCoverUrlField.getText());
                g.setSummary(newSummaryArea.getText());
                g.setPublisherId(Session.getCurrentUserId());

                dao.save(g);
                showAlert("Başarılı", "Oyununuz mağazaya eklendi!");
            } else {
                // 2. GÜNCELLEME MODU
                // Oyuna ait yıldız puanlarının (rating) sıfırlanmaması için önce eski oyunu veritabanından çekiyoruz!
                Game existingGame = dao.findById(editingGameId);
                if(existingGame != null) {
                    existingGame.setName(newNameField.getText());
                    existingGame.setPrice(Double.parseDouble(newPriceField.getText()));
                    existingGame.setGenres(newGenresField.getText());
                    existingGame.setCoverUrl(newCoverUrlField.getText());
                    existingGame.setSummary(newSummaryArea.getText());

                    dao.update(existingGame);
                    showAlert("Başarılı", "Oyun bilgileri başarıyla güncellendi!");
                }
            }

            // İşlem bitince formu temizle, tabloyu yenile ve sekmeyi eski haline getir
            clearForm();
            refreshTable();
            resetFormState();

        } catch (Exception e) {
            showAlert("Hata", "Lütfen tüm alanları doğru girdiğinizden emin olun. (Fiyat için sadece sayı kullanın vs.)");
            e.printStackTrace();
        }
    }

    // Formu normal "Ekleme" moduna döndüren yardımcı metot
    private void resetFormState() {
        editingGameId = null;
        submitGameBtn.setText("Oyunu Mağazada Yayınla");
        submitGameBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");
        addEditTab.setText("Yeni Oyun Yükle");
        mainTabPane.getSelectionModel().select(0); // Tablo sekmesine geri fırlatır
    }

    @FXML
    private void handleApplyDiscount() {
        Game selected = myGamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen indirim uygulamak için tablodan bir oyun seçin.");
            return;
        }

        try {
            double rate = Double.parseDouble(discountField.getText());
            gameService.applyDiscount(selected.getId(), rate);
            refreshTable();
            discountField.clear();
        } catch (Exception e) {
            showAlert("Hata", "Geçerli bir indirim oranı girin.");
        }
    }

    @FXML
    private void handleDeleteGame() {
        Game selected = myGamesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                gameService.deleteGame(selected.getId());
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Uyarı", "Lütfen silmek için tablodan bir oyun seçin.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            util.Session.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 960, 560));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        newNameField.clear();
        newPriceField.clear();
        newGenresField.clear();
        newCoverUrlField.clear();
        newSummaryArea.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}