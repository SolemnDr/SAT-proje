package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import magaza.model.Game;
import magaza.service.GameService;
import util.CartService;
import javafx.scene.control.Button;
import kullanici.model.User;

import java.util.List;

public class MainController {

    public static MainController instance;

    @FXML private StackPane contentArea;
    @FXML private Label cartInfoLabel;

    private int cartItemCount = 0;
    private double cartTotalPrice = 0.0;

    @FXML private Button publisherPanelButton;

    // Veritabanı işlemleri için GameService'i ekledik
    private final GameService gameService = new GameService();

    @FXML
    public void initialize() {
        instance = this;

        checkUserRole(); // Önce rolü kontrol et ve butonu ayarla
        loadUserCart();

        User user = util.Session.getCurrentUser();

        // EĞER GELİŞTİRİCİYSE MAĞAZAYI DEĞİL, PANELİ AÇ
        if (user != null && user.getRole() == 1) {
            showPublisherPanel();
        } else {
            showStore(); // Oyuncuysa normal devam et
        }
    }

    // YENİ EKLENEN METOT: Veritabanı ile Arayüzü Eşitler
    private void loadUserCart() {
        try {
            int currentUserId = util.Session.getCurrentUserId();
            List<Game> cartGames = gameService.getCart(currentUserId);

            // RAM'deki geçici sepeti temizle (eski kalıntılar olmasın)
            CartService.clearCart();

            if (cartGames != null && !cartGames.isEmpty()) {
                int count = cartGames.size();
                double total = 0.0;

                for (Game game : cartGames) {
                    total += game.getPrice();
                    // RAM'e (CartService) de oyunların ID'sini ekliyoruz ki mağazada "Zaten Sepette" uyarısı çalışsın
                    CartService.addToCart(game.getId());
                }

                // Senin hazırladığın mükemmel metodu çağırıp UI'ı güncelliyoruz
                syncCartUI(count, total);
            } else {
                resetCartUI();
            }
        } catch (Exception e) {
            System.out.println("Açılışta sepet yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }

    private void checkUserRole() {
        User user = util.Session.getCurrentUser();
        if (user != null) {
            System.out.println("DEBUG: Giriş yapan kullanıcının rolü: " + user.getRole());
        }

        // Güvenlik: Eğer buton FXML'de bulunamadıysa metodu durdur, çökme olmasın
        if (publisherPanelButton == null) {
            System.out.println("Hata: main_layout.fxml içinde 'publisherPanelButton' bulunamadı!");
            return;
        }

        if (user != null && user.getRole() == 1) {
            publisherPanelButton.setVisible(true);
            publisherPanelButton.setManaged(true);
        } else {
            publisherPanelButton.setVisible(false);
            publisherPanelButton.setManaged(false);
        }
    }

    @FXML
    private void showPublisherPanel() {
        loadPage("publisher_panel.fxml");
    }

    public void syncCartUI(int count, double total) {
        this.cartItemCount = count;
        this.cartTotalPrice = total;
        if (cartInfoLabel != null) {
            cartInfoLabel.setText(String.format(java.util.Locale.US, "Sepet: %d Ürün (%.2f TL)", count, total));
        }
    }

    public void updateCartUI(double addedPrice) {
        cartItemCount++;
        cartTotalPrice += addedPrice;
        cartInfoLabel.setText(String.format(java.util.Locale.US, "Sepet: %d Ürün (%.2f TL)", cartItemCount, cartTotalPrice));
    }

    public void resetCartUI() {
        cartItemCount = 0;
        cartTotalPrice = 0.0;
        if (cartInfoLabel != null) {
            cartInfoLabel.setText("Sepet: 0 Ürün (0.00 TL)");
        }
    }

    @FXML
    private void showCartPage() {
        loadPage("cart.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node page = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (Exception e) {
            System.out.println("Sayfa yüklenirken hata oluştu: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    private void showStore() {
        loadPage("store.fxml");
    }

    @FXML
    private void showLibrary() {
        loadPage("library.fxml");
    }

    @FXML
    private void showSocial() {
        loadPage("social.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            // YENİ EKLENDİ: Çıkış yaparken Oturumu ve Sepet RAM'ini tamamen temizliyoruz
            util.Session.setCurrentUser(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 960, 560));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}