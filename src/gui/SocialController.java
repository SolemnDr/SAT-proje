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
    @FXML private ListView<User> requestsListView;

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
        loadRequests();

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
    private void loadRequests() {
        if (requestsListView == null) return;
        requestsListView.getItems().clear();
        try {
            // Sizin DAO'daki metot!
            List<User> requests = friendDAO.getPendingRequests(currentUserId);
            if (requests != null) {
                requestsListView.getItems().addAll(requests);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAcceptRequest() {
        User selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            // Sizin DAO'daki acceptFriendRequest metodu!
            boolean success = friendDAO.acceptFriendRequest(selectedRequest.getId(), currentUserId);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Kabul Edildi", selectedRequest.getUsername() + " artık arkadaşın!");
                loadRequests();
                loadFriends();
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata", "İstek kabul edilemedi.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen kabul etmek için bir istek seçin.");
        }
    }

    @FXML
    private void handleRejectRequest() {
        User selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            // Sizin DAO'daki removeFriend metodu!
            boolean success = friendDAO.removeFriend(currentUserId, selectedRequest.getId());
            if (success) {
                loadRequests();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen reddetmek için bir istek seçin.");
        }
    }

    private void loadMessages() {
        messagesListView.getItems().clear();
        if (selectedFriend == null) return;

        try {
            List<String> history = messageDAO.getConversation(currentUserId, selectedFriend.getId());
            if (history == null || history.isEmpty()) {
                messagesListView.getItems().add("Sistem: Henüz bir mesaj yok.");
                return;
            }

            // Tarih ve Saat Formatı (Örn: 01 Mayıs 2026 | 21:30)
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm");
            java.time.LocalDateTime sonEtiketZamani = null;

            for (String msg : history) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[(.*?)\\]");
                java.util.regex.Matcher matcher = pattern.matcher(msg);

                if (matcher.find()) {
                    String zamanStr = matcher.group(1);

                    // UTC'den Yerel Saate Çeviri (3 saat farkı kapatır)
                    java.time.LocalDateTime utcZamani = java.time.LocalDateTime.parse(zamanStr,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    java.time.LocalDateTime mesajZamani = utcZamani.atZone(java.time.ZoneId.of("UTC"))
                            .withZoneSameInstant(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();

                    // --- 5 DAKİKA KONTROLÜ ---
                    // Eğer bu ilk mesajsa VEYA son etiketten itibaren 5 dakika geçmişse YENİ ETİKET AT
                    if (sonEtiketZamani == null || java.time.Duration.between(sonEtiketZamani, mesajZamani).toMinutes() >= 5) {
                        String ayirici = "─────────  " + mesajZamani.format(dtf) + "  ─────────";
                        messagesListView.getItems().add(ayirici);
                        sonEtiketZamani = mesajZamani; // Son etiket zamanını güncelle
                    }
                }

                // Mesajın başındaki [tarih] kalabalığını siler
                String temizMesaj = msg.replaceAll("^\\[.*?\\]\\s*", "");
                messagesListView.getItems().add(temizMesaj);
            }

            messagesListView.scrollTo(messagesListView.getItems().size() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && selectedFriend != null) {
            try {
                // 1. Önce veritabanına kaydet
                boolean success = messageDAO.sendMessage(currentUserId, selectedFriend.getId(), text);

                if (success) {
                    messageInput.clear();
                    // 2. KRİTİK DÜZELTME: Ekranı elle güncellemek yerine loadMessages'ı çağır
                    // Böylece 10 dakika geçmişse o meşhur "zaman çizgisi" anında belirir.
                    loadMessages();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Mesaj gönderilemedi.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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