package gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import magaza.service.GameService;

public class PaymentController {

    @FXML private TextField cardNameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private Label cardTypeLabel;
    @FXML private Label errorLabel;
    @FXML private Button payButton;

    private double amountToPay;
    private final GameService gameService = new GameService();

    public void setTotalAmount(double amount) {
        this.amountToPay = amount;
        payButton.setText(String.format(java.util.Locale.US, "%.2f TL ÖDE", amount));
    }

    @FXML
    public void initialize() {
        // 1. KART İSMİ: Sadece harf ve boşluk kabul eder, yazarken otomatik BÜYÜK harf yapar.
        cardNameField.textProperty().addListener((obs, oldText, newText) -> {
            String clean = newText.replaceAll("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]", "");
            if (!newText.equals(clean)) {
                cardNameField.setText(clean.toUpperCase(java.util.Locale.forLanguageTag("tr-TR")));
            }
        });

        // 2. KART NUMARASI: Sadece Rakam, Maksimum 16 Hane. Yanda logoyu değiştirir.
        cardNumberField.textProperty().addListener((obs, oldText, newText) -> {
            // Önce kullanıcının girdiği her şeyi rakamlar kalacak şekilde temizle
            String clean = newText.replaceAll("[^\\d]", "");

            // 16 rakamı geçmesini engelle
            if (clean.length() > 16) {
                clean = clean.substring(0, 16);
            }

            // Her 4 rakamda bir araya boşluk ekleyen algoritma
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(clean.charAt(i));
            }

            // Ekrandaki yazı ile bizim formatlı yazımız farklıysa günceller (Sonsuz döngüyü önler)
            if (!newText.equals(formatted.toString())) {
                cardNumberField.setText(formatted.toString());
            } else {
                // Ekran zaten formatlıysa logoyu belirlemeye gönder (boşluksuz halini yolluyoruz)
                detectCardType(clean);
            }
        });

        // 3. SON KULLANMA TARİHİ: Sadece Rakam, Max 4 Hane. Ay 12'yi geçemez, araya otomatik '/' atar.
        expiryField.textProperty().addListener((obs, oldText, newText) -> {
            String clean = newText.replaceAll("[^\\d]", "");
            if (clean.length() > 4) clean = clean.substring(0, 4);

            if (clean.length() >= 2) {
                int month = Integer.parseInt(clean.substring(0, 2));
                if (month > 12) clean = "12" + (clean.length() > 2 ? clean.substring(2) : "");
                else if (month == 0 && clean.length() == 2) clean = "01" + (clean.length() > 2 ? clean.substring(2) : "");
            }

            StringBuilder formatted = new StringBuilder(clean);
            if (clean.length() > 2) {
                formatted.insert(2, "/");
            }

            if (!newText.equals(formatted.toString())) {
                expiryField.setText(formatted.toString());
            }
        });

        // 4. CVV: Sadece Rakam, Maksimum 3 Hane.
        cvvField.textProperty().addListener((obs, oldText, newText) -> {
            String clean = newText.replaceAll("[^\\d]", "");
            if (clean.length() > 3) clean = clean.substring(0, 3);
            if (!newText.equals(clean)) {
                cvvField.setText(clean);
            }
        });
    }

    private void detectCardType(String number) {
        String cleanNumber = number.replaceAll("\\s+", "");

        if (cleanNumber.startsWith("4")) {
            cardTypeLabel.setText("VISA");
            cardTypeLabel.setStyle("-fx-background-color: #1a1f71; -fx-text-fill: white;");
        } else if (cleanNumber.startsWith("5")) {
            cardTypeLabel.setText("MASTER");
            cardTypeLabel.setStyle("-fx-background-color: #eb001b; -fx-text-fill: white;");
        } else if (cleanNumber.startsWith("9")) {
            cardTypeLabel.setText("TROY");
            // Troy için göze hoş gelen, diğerlerinden ayırt edici şık bir turkuaz/teal tonu
            cardTypeLabel.setStyle("-fx-background-color: #00a8a8; -fx-text-fill: white;");
        } else {
            cardTypeLabel.setText("?");
            cardTypeLabel.setStyle("-fx-background-color: #4a4acc; -fx-text-fill: white;");
        }
    }

    // KARTIN HEM MARKASINI HEM DE GERÇEKLİĞİNİ DOĞRULAMA
    private boolean isStrictlyValidCard(String number) {
        String cleanNumber = number.replaceAll("\\s+", "");

        // 1. KURAL: Visa (4), Mastercard (5) veya Troy (9) ile başlamak ZORUNDA
        if (!(cleanNumber.startsWith("4") || cleanNumber.startsWith("5") || cleanNumber.startsWith("9"))) {
            return false;
        }

        // 2. KURAL: Luhn Algoritması (Rastgele sallanan sayıları anında yakalar)
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleanNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @FXML
    private void handlePayment() {
        String name = cardNameField.getText();
        String number = cardNumberField.getText();
        String cvv = cvvField.getText();
        String exp = expiryField.getText();

        // Boşluk kontrolü
        if (name.isEmpty() || number.isEmpty() || cvv.isEmpty() || exp.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        // Hane eksikliği kontrolü
        if (number.length() < 15) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Kart numarası 15 veya 16 haneli olmalıdır.");
            return;
        }

        // Tarih ve Yıl kontrolü (2026 yılından öncesini ve geçmiş ayları engelleme)
        if (exp.length() != 5) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Geçersiz Tarih! (Örn: 12/28)");
            return;
        }

        int mm = Integer.parseInt(exp.substring(0, 2));
        int yy = Integer.parseInt(exp.substring(3, 5));

        // 2026 yılı 5. (Mayıs) ayındayız. Buna göre hesaplanır.
        if (yy < 26 || (yy == 26 && mm < 5)) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Kartınızın son kullanma tarihi geçmiş!");
            return;
        }
        if (!isStrictlyValidCard(number)) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Reddedildi! Sadece geçerli Visa, Mastercard veya Troy kartları kabul edilmektedir.");
            return;
        }

        errorLabel.setStyle("-fx-text-fill: #e0e0ff;");
        errorLabel.setText("Ödeme işleniyor, lütfen bekleyin...");

        try {
            int currentUserId = 1;
            gameService.purchaseCart(currentUserId, number);

            util.CartService.clearCart();

            if (MainController.instance != null) {
                MainController.instance.resetCartUI();
            }

            errorLabel.setStyle("-fx-text-fill: #4caf50;");
            errorLabel.setText("✅ Ödeme Başarılı! Oyunlar kütüphanenize eklendi.");
            payButton.setDisable(true);
            payButton.setText("İŞLEM TAMAMLANDI");

        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelPayment() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("cart.fxml"));
            Node cartPage = loader.load();
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) errorLabel.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cartPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}