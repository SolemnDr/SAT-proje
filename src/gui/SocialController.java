package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import kullanici.dao.UserDAO;
import kullanici.model.User;
import sosyal.dao.FriendDAO;
import sosyal.dao.MessageDAO;
import util.Session;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialController {

    @FXML private VBox friendsMenuPane;
    @FXML private VBox requestsMenuPane;
    @FXML private VBox addFriendMenuPane;
    @FXML private VBox profileMenuPane;
    @FXML private VBox chatPane;

    @FXML private ListView<User> friendsListView;
    @FXML private ListView<User> requestsListView;
    @FXML private ListView<String> messagesListView;

    @FXML private TextField messageInput;
    @FXML private TextField searchFriendInput;
    @FXML private Label chatHeaderLabel;
    @FXML private Label notificationBadge;
    @FXML private Button notificationButton;

    @FXML private Label profileUsernameLabel;
    @FXML private Label profileEmailLabel;

    private FriendDAO friendDAO;
    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private User selectedFriend;
    private int currentUserId;

    @FXML
    public void initialize() {
        friendDAO = new FriendDAO();
        messageDAO = new MessageDAO();
        userDAO = new UserDAO();
        currentUserId = Session.getCurrentUserId();

        String listStyle = "-fx-background-color: #1a1a2e; -fx-control-inner-background: #1a1a2e;"
                + "-fx-background-radius: 10; -fx-border-color: #5352ed; -fx-border-radius: 10;"
                + "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: white;";

        if (friendsListView  != null) friendsListView.setStyle(listStyle);
        if (messagesListView != null) messagesListView.setStyle(listStyle);
        if (requestsListView != null) requestsListView.setStyle(listStyle);

        if (notificationBadge != null) notificationBadge.setVisible(false);

        showLeftPane(friendsMenuPane);
        setupFriendsListCellFactory();
        setupRequestsListCellFactory();
        loadFriends();
        loadRequests();

        if (friendsListView != null) {
            friendsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedFriend = newVal;
                    if (chatHeaderLabel != null)
                        chatHeaderLabel.setText(selectedFriend.getUsername() + " ile Sohbet");
                    loadMessages();
                }
            });
        }
    }

    private void showLeftPane(VBox paneToShow) {
        VBox[] leftPanes = { friendsMenuPane, requestsMenuPane, addFriendMenuPane, profileMenuPane };
        for (VBox pane : leftPanes) {
            if (pane != null) {
                boolean show = pane == paneToShow;
                pane.setVisible(show);
                pane.setManaged(show);
            }
        }
    }

    @FXML private void handleOpenRequests()   { loadRequests(); showLeftPane(requestsMenuPane); }
    @FXML private void handleOpenAddFriend()  { showLeftPane(addFriendMenuPane); }
    @FXML private void handleBackToFriends()  { loadFriends(); showLeftPane(friendsMenuPane); }
    @FXML private void handleBackFromProfile(){ showLeftPane(friendsMenuPane); }

    // ── Arkadaş listesi hücreleri ─────────────────────────────────────────

    private void setupFriendsListCellFactory() {
        if (friendsListView == null) return;
        friendsListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label("⭐ " + user.getUsername().toUpperCase());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    MenuButton optionsButton = new MenuButton("⋮");
                    optionsButton.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: white;"
                                    + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");

                    // Menü item'leri beyaz yazı renginde
                    MenuItem viewProfileItem = new MenuItem("Profili Görüntüle");
                    viewProfileItem.setStyle("-fx-text-fill: white; -fx-background-color: #2a2a4a;");
                    viewProfileItem.setOnAction(e -> openProfilePage(user));

                    MenuItem removeFriendItem = new MenuItem("Arkadaşı Sil");
                    removeFriendItem.setStyle("-fx-text-fill: white; -fx-background-color: #2a2a4a;");
                    removeFriendItem.setOnAction(e -> handleRemoveFriendAction(user));

                    // Menü arkaplanı koyu
                    optionsButton.setPopupSide(javafx.geometry.Side.BOTTOM);
                    optionsButton.getItems().addAll(viewProfileItem, removeFriendItem);
                    optionsButton.getStylesheets().add(
                            "data:text/css,.menu-button .context-menu{-fx-background-color:#2a2a4a;}"
                                    + ".menu-button .menu-item:focused{-fx-background-color:#3a3a6a;}"
                                    + ".menu-button .label{-fx-text-fill:white;}");

                    hbox.getChildren().addAll(nameLabel, spacer, optionsButton);
                    setGraphic(hbox);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    // ── İstek listesi hücreleri (kabul/reddet satır içinde) ───────────────

    private void setupRequestsListCellFactory() {
        if (requestsListView == null) return;
        requestsListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setPadding(new javafx.geometry.Insets(4, 6, 4, 6));

                    Label nameLabel = new Label("👤 " + user.getUsername());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button acceptBtn = new Button("✔");
                    acceptBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;"
                            + "-fx-font-weight: bold; -fx-cursor: hand;"
                            + "-fx-background-radius: 5; -fx-padding: 4 8;");
                    acceptBtn.setOnAction(e -> {
                        boolean ok = friendDAO.acceptFriendRequest(user.getId(), currentUserId);
                        if (ok) { loadRequests(); loadFriends(); }
                    });

                    Button rejectBtn = new Button("✖");
                    rejectBtn.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;"
                            + "-fx-font-weight: bold; -fx-cursor: hand;"
                            + "-fx-background-radius: 5; -fx-padding: 4 8;");
                    rejectBtn.setOnAction(e -> {
                        boolean ok = friendDAO.removeFriend(currentUserId, user.getId());
                        if (ok) loadRequests();
                    });

                    hbox.getChildren().addAll(nameLabel, spacer, acceptBtn, rejectBtn);
                    setGraphic(hbox);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    // ── Veri yükleme ──────────────────────────────────────────────────────

    private void loadFriends() {
        if (friendsListView == null) return;
        friendsListView.getItems().clear();
        try {
            List<User> friends = friendDAO.getFriendsList(currentUserId);
            if (friends != null) friendsListView.getItems().addAll(friends);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadRequests() {
        if (requestsListView == null) return;
        requestsListView.getItems().clear();
        try {
            List<User> requests = friendDAO.getPendingRequests(currentUserId);
            boolean hasRequests = requests != null && !requests.isEmpty();
            if (hasRequests) requestsListView.getItems().addAll(requests);

            // Bildirim: istek varsa 🔔 → 🔔● (nokta görünür), yoksa normal
            if (notificationBadge != null) {
                notificationBadge.setVisible(hasRequests);
                notificationBadge.setManaged(hasRequests);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Arkadaş silme ─────────────────────────────────────────────────────

    private void handleRemoveFriendAction(User userToRemove) {
        if (userToRemove == null) return;
        boolean success = friendDAO.removeFriend(currentUserId, userToRemove.getId());
        if (success) {
            loadFriends();
            if (selectedFriend != null && selectedFriend.getId() == userToRemove.getId()) {
                selectedFriend = null;
                if (messagesListView != null) messagesListView.getItems().clear();
                if (chatHeaderLabel != null)  chatHeaderLabel.setText("Sohbet");
            }
        }
    }

    // ── Profil sayfası ────────────────────────────────────────────────────

    private void openProfilePage(User user) {
        if (profileUsernameLabel != null)
            profileUsernameLabel.setText("🎮 " + user.getUsername().toUpperCase());
        if (profileEmailLabel != null)
            profileEmailLabel.setText("E-posta: " + (user.getEmail() != null ? user.getEmail() : "Gizli"));

        // Eski oyun listesini temizle
        if (profileMenuPane != null) {
            profileMenuPane.getChildren().removeIf(n -> "friendGames".equals(n.getUserData()));
        }

        // Arkadaşın oyunlarını yükle
        loadFriendGames(user);

        showLeftPane(profileMenuPane);
    }

    private void loadFriendGames(User user) {
        if (profileMenuPane == null) return;

        Label gamesTitle = new Label("🎮 Sahip Olduğu Oyunlar");
        gamesTitle.setStyle("-fx-text-fill: #a0a0ff; -fx-font-weight: bold; -fx-font-size: 13px;");
        gamesTitle.setUserData("friendGames");

        ListView<String> gamesList = new ListView<>();
        gamesList.setUserData("friendGames");
        gamesList.setPrefHeight(200);
        gamesList.setStyle("-fx-background-color: #1a1a2e; -fx-control-inner-background: #1a1a2e;"
                + "-fx-border-color: #5352ed; -fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-text-fill: white;");

        try {
            magaza.service.GameService gameService = new magaza.service.GameService();
            java.util.List<magaza.model.Game> games = gameService.getPurchasedGames(user.getId());

            if (games == null || games.isEmpty()) {
                gamesList.getItems().add("Bu kullanıcının henüz oyunu yok.");
            } else {
                for (magaza.model.Game g : games) {
                    // Sadece oyunun ismini ekliyoruz, fiyat kısmını sildik[cite: 1]
                    gamesList.getItems().add("  " + g.getName());
                }
            }
        } catch (Exception e) {
            gamesList.getItems().add("Oyunlar yüklenirken hata oluştu.");
            e.printStackTrace();
        }

        profileMenuPane.getChildren().addAll(gamesTitle, gamesList);
    }
    // ── Mesajlar ──────────────────────────────────────────────────────────

    private void loadMessages() {
        if (messagesListView == null) return;
        messagesListView.getItems().clear();
        if (selectedFriend == null) return;
        try {
            List<String> history = messageDAO.getConversation(currentUserId, selectedFriend.getId());
            if (history == null || history.isEmpty()) return;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm");
            LocalDateTime sonEtiketZamani = null;

            for (String msg : history) {
                Matcher matcher = Pattern.compile("\\[(.*?)\\]").matcher(msg);
                if (matcher.find()) {
                    LocalDateTime utc = LocalDateTime.parse(matcher.group(1),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime local = utc.atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    if (sonEtiketZamani == null || Duration.between(sonEtiketZamani, local).toMinutes() >= 5) {
                        messagesListView.getItems().add("─────────  " + local.format(dtf) + "  ─────────");
                        sonEtiketZamani = local;
                    }
                }
                messagesListView.getItems().add(msg.replaceAll("^\\[.*?\\]\\s*", ""));
            }
            messagesListView.scrollTo(messagesListView.getItems().size() - 1);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSendMessage() {
        if (messageInput == null) return;
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && selectedFriend != null) {
            try {
                if (messageDAO.sendMessage(currentUserId, selectedFriend.getId(), text)) {
                    messageInput.clear();
                    loadMessages();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleExecuteAddFriend() {
        if (searchFriendInput == null) return;
        String username = searchFriendInput.getText().trim();
        if (username.isEmpty()) return;

        try {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isPresent()) {
                User newFriend = userOpt.get();

                // Kendine istek gönderme kontrolü zaten var
                if (newFriend.getId() == currentUserId) {
                    showFeedback("Kendinize istek gönderemezsiniz.", "#ff4c4c");
                    return;
                }

                // YENİ: Yayıncı kontrolü (Role 1 = Yayıncı/Geliştirici)
                if (newFriend.getRole() == 1) {
                    showFeedback("Yayıncılara arkadaşlık isteği gönderilemez.", "#ff4c4c");
                    return;
                }

                friendDAO.sendFriendRequest(currentUserId, newFriend.getId());
                searchFriendInput.clear();
                showFeedback("İstek gönderildi! ", "#4caf50");
            } else {
                showFeedback("Kullanıcı bulunamadı.", "#ff4c4c");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showFeedback("Bir hata oluştu.", "#ff4c4c");
        }
    }

    private void showFeedback(String message, String color) {
        // addFriendMenuPane içine geçici bir label ekle
        if (addFriendMenuPane == null) return;

        // Eski feedback label'ı varsa kaldır
        addFriendMenuPane.getChildren().removeIf(
                node -> "feedbackLabel".equals(node.getUserData())
        );

        Label feedback = new Label(message);
        feedback.setUserData("feedbackLabel");
        feedback.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        addFriendMenuPane.getChildren().add(feedback);

        // 3 saniye sonra otomatik kaybol
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(3)
        );
        pause.setOnFinished(e -> addFriendMenuPane.getChildren().remove(feedback));
        pause.play();
    }

    // Artık kullanılmıyor ama FXML'den referans gelebilir diye bırakıldı
    @FXML private void handleAcceptRequest() {}
    @FXML private void handleRejectRequest() {}
}