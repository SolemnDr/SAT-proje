package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import magaza.model.Game;
import magaza.service.GameService;
import util.Session;

import java.sql.SQLException;
import java.util.List;

public class PublisherPanelController {

    @FXML private TableView<Game> myGamesTable;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, Double> colPrice;
    @FXML private TableColumn<Game, Integer> colSales;
    @FXML private TableColumn<Game, Double> colDiscount;

    @FXML private TextField newNameField, newPriceField, newGenresField, newCoverUrlField, discountField;
    @FXML private TextArea newSummaryArea;

    private final GameService gameService = new GameService();

    @FXML
    public void initialize() {
        // Tablo sütunlarını Game modelindeki fieldlar ile eşleştiriyoruz
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

    @FXML
    private void handleAddGame() {
        try {
            Game g = new Game();
            g.setName(newNameField.getText());
            g.setPrice(Double.parseDouble(newPriceField.getText()));
            g.setGenres(newGenresField.getText());
            g.setCoverUrl(newCoverUrlField.getText());
            g.setSummary(newSummaryArea.getText());
            g.setPublisherId(Session.getCurrentUserId());

            // DAO içindeki save metodunu tetikleyen servis çağrısı
            magaza.dao.GameDAO dao = new magaza.dao.GameDAO();
            dao.save(g);

            clearForm();
            refreshTable();
            showAlert("Başarılı", "Oyununuz mağazaya eklendi!");
        } catch (Exception e) {
            showAlert("Hata", "Lütfen tüm alanları doğru girdiğinizden emin olun.");
        }
    }

    @FXML
    private void handleApplyDiscount() {
        Game selected = myGamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

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