package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import kullanici.dao.UserDAO;
import kullanici.model.User;
import sosyal.dao.FriendDAO;
import sosyal.dao.MessageDAO;
import util.Session;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SocialController {
    @FXML private ListView<User> friendsListView;
    @FXML private ListView<String> messagesListView;
    @FXML private TextField messageInput;
    @FXML private Label chatHeaderLabel;

    private FriendDAO friendDAO;
    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private User selectedFriend;
    private int currentUserId;

    @FXML
    public void initialize() {
        // 1. Nesneleri ve Session'ı kur
        friendDAO = new FriendDAO();
        messageDAO = new MessageDAO();
        userDAO = new UserDAO();
        currentUserId = util.Session.getCurrentUserId();

        // 2. Listelerin stilini mor tema yap
        String listStyle = "-fx-background-color: #1a1a2e; " +
                "-fx-control-inner-background: #1a1a2e; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #5352ed; " +
                "-fx-border-radius: 10; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white;";

        friendsListView.setStyle(listStyle);
        messagesListView.setStyle(listStyle);

        // 3. Önce veritabanından gerçek arkadaşları yükle
        loadFriends();

        // 4. Test kullanıcılarını ekle (loadFriends'ten sonra ekle ki silinmesinler)
        User test1 = new User();
        test1.setUsername("Recaizade Mahmut Ekrem");
        test1.setId(998);

        User test2 = new User();
        test2.setUsername("Barış Alper Yılmaz");
        test2.setId(999);

        friendsListView.getItems().addAll(test1, test2);

        // 5. Arkadaş seçildiğinde yapılacak işlem
        friendsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedFriend = newVal;
                chatHeaderLabel.setText(selectedFriend.getUsername() + " ile Sohbet");
                loadMessages();
            }
        });
    }

    private void loadFriends() {
        friendsListView.getItems().clear();
        try {
            List<User> friends = friendDAO.getFriendsList(currentUserId);
            if (friends != null) {
                friendsListView.getItems().addAll(friends);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        messagesListView.getItems().clear();
        if (selectedFriend == null) return;

        // 1. EN ÜSTTEKİ ŞIK TARİH (Sadece sayfa açıldığında 1 kere)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm");
        String tarihSeridi = "─────────  " + now.format(dtf) + "  ─────────";
        messagesListView.getItems().add(tarihSeridi);

        try {
            List<String> history = messageDAO.getConversation(currentUserId, selectedFriend.getId());
            if (history != null && !history.isEmpty()) {
                for (String msg : history) {
                    // --- KRİTİK DÜZELTME: Mesajın başındaki [2026-05-01 18:08:23] kısmını tamamen siler ---
                    // Bu regex hem tarihi hem saati hem de köşeli parantezleri temizler
                    String temizMesaj = msg.replaceAll("^\\[.*?\\]\\s*", "");

                    // Eğer veritabanından gelen mesajda hala "Ben:" veya "Sen:" varsa onları korur
                    messagesListView.getItems().add(temizMesaj);
                }
            } else {
                messagesListView.getItems().add("Sistem: Henüz bir mesaj yok. İlk mesajı sen gönder!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && selectedFriend != null) {

            // Sistem mesajını temizle
            messagesListView.getItems().removeIf(m -> m.startsWith("Sistem:"));

            // Ekrana sadece "Sen: mesaj" olarak bas (Zaman damgası ekleme)
            messagesListView.getItems().add("Sen: " + text);
            messageInput.clear();
            messagesListView.scrollTo(messagesListView.getItems().size() - 1);

            try {
                messageDAO.sendMessage(currentUserId, selectedFriend.getId(), text);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleAddFriend() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("GameStore | Sosyal");
        dialog.setHeaderText("🎮 Oyuncu Ara & Ekle");
        dialog.setGraphic(null); // Soru işaretini kaldırır

        DialogPane pane = dialog.getDialogPane();

        // Mor tema ve beyaz başlık yazısı
        pane.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #5352ed; -fx-border-width: 2;");
        pane.lookup(".header-panel").setStyle("-fx-background-color: #1a1a2e;");

        pane.lookup(".header-panel").lookup(".label").setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 16px;");
        pane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Giriş kutusu (Beyaz yazı)
        dialog.getEditor().setStyle("-fx-background-color: #2a2a4a; -fx-text-fill: white; -fx-font-weight: bold;");

        // Butonlar
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String username = result.get().trim();
            if (!username.isEmpty()) {
                try {
                    Optional<User> userOpt = userDAO.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User newFriend = userOpt.get();
                        if (newFriend.getId() == currentUserId) {
                            showAlert(Alert.AlertType.WARNING, "Uyarı", "Kendini arkadaş olarak ekleyemezsin!");
                            return;
                        }
                        boolean success = friendDAO.sendFriendRequest(currentUserId, newFriend.getId());
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Başarılı", username + " kullanıcısına istek gönderildi.");
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Uyarı", "Bu kişiyle zaten bağlantınız var.");
                        }
                    } else {
                        // Yanlış isim girince hata verme kısmı
                        showAlert(Alert.AlertType.ERROR, "Oyuncu Bulunamadı", "Girdiğiniz '" + username + "' kullanıcı adıyla eşleşen bir oyuncu yok.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Sistem Hatası", "Bir hata oluştu.");
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("GameStore | " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.setGraphic(null);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #5352ed; -fx-border-width: 2;");

        pane.lookupAll(".label").forEach(node ->
                node.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;"));

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #5352ed; -fx-text-fill: white; -fx-font-weight: bold;");

        alert.showAndWait();
    }
}