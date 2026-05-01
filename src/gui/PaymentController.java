package gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import magaza.service.GameService;
import util.CartService;

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
        // 1. KART İSMİ: Sadece harf ve boşluk kabul eder, otomatik BÜYÜK harf yapar.
        cardNameField.textProperty().addListener((obs, oldText, newText) -> {
            String clean = newText.replaceAll("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]", "");
            if (!newText.equals(clean)) {
                cardNameField.setText(clean.toUpperCase(java.util.Locale.forLanguageTag("tr-TR")));
            }
        });

        // 2. KART NUMARASI: Sadece Rakam, Max 16 Hane ve her 4 rakamda bir otomatik boşluk!
        cardNumberField.textProperty().addListener((obs, oldText, newText) -> {
            // Sadece rakamları al
            String clean = newText.replaceAll("[^\\d]", "");
            if (clean.length() > 16) clean = clean.substring(0, 16);

            // Her 4 rakamda bir boşluk ekle
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(clean.charAt(i));
            }

            if (!newText.equals(formatted.toString())) {
                cardNumberField.setText(formatted.toString());
            } else {
                detectCardType(clean); // Rakam tipine göre sağdaki logoyu değiştir
            }
        });

        // 3. SON KULLANMA TARİHİ: Max 4 Hane. Ay 12'yi geçemez, araya otomatik '/' atar.
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

    private void detectCardType(String cleanNumber) {
        if (cleanNumber.startsWith("4")) {
            cardTypeLabel.setText("VISA");
            cardTypeLabel.setStyle("-fx-background-color: #1a1f71; -fx-text-fill: white;");
        } else if (cleanNumber.startsWith("5")) {
            cardTypeLabel.setText("MASTER");
            cardTypeLabel.setStyle("-fx-background-color: #eb001b; -fx-text-fill: white;");
        } else if (cleanNumber.startsWith("9")) {
            cardTypeLabel.setText("TROY");
            cardTypeLabel.setStyle("-fx-background-color: #00a8a8; -fx-text-fill: white;");
        } else {
            cardTypeLabel.setText("?");
            cardTypeLabel.setStyle("-fx-background-color: #4a4acc; -fx-text-fill: white;");
        }
    }

    // KARTIN HEM MARKASINI HEM DE GERÇEKLİĞİNİ DOĞRULAYAN ZIRH (Luhn Algoritması)
    private boolean isStrictlyValidCard(String number) {
        String cleanNumber = number.replaceAll("\\s+", "");

        if (!(cleanNumber.startsWith("4") || cleanNumber.startsWith("5") || cleanNumber.startsWith("9"))) {
            return false;
        }

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
        // Veritabanına gönderirken ve test ederken boşlukları temizleyerek alıyoruz
        String number = cardNumberField.getText().replaceAll("\\s+", "");
        String cvv = cvvField.getText();
        String exp = expiryField.getText();

        if (name.isEmpty() || number.isEmpty() || cvv.isEmpty() || exp.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        if (number.length() < 15) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Kart numarası 15 veya 16 haneli olmalıdır.");
            return;
        }

        if (exp.length() != 5) {
            errorLabel.setStyle("-fx-text-fill: #ff4c4c;");
            errorLabel.setText("Geçersiz Tarih! (Örn: 12/28)");
            return;
        }

        int mm = Integer.parseInt(exp.substring(0, 2));
        int yy = Integer.parseInt(exp.substring(3, 5));

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
            // HARDCODED "1" SİLİNDİ, YERİNE SESSION MANAGER EKLENDİ!
            int currentUserId = util.Session.getCurrentUserId();

            gameService.purchaseCart(currentUserId, number);

            // Arayüzün geçici sepet hafızasını temizle
            CartService.clearCart();

            // Ana ekrandaki üst barı sıfırla ("0 Ürün")
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