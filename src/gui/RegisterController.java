package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kullanici.service.AuthService;
import util.Session;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // İŞTE EKSİK OLAN VE KIRMIZI YANAN KISIM BURASIYDI:
    @FXML private CheckBox publisherCheckBox;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        // CheckBox işaretli ise 1, değilse 0 değerini alıyoruz
        int role = publisherCheckBox.isSelected() ? 1 : 0;
        System.out.println("DEBUG: Kayıt isteği gönderiliyor. Seçilen Rol: " + role);

        // Tek bir kayıt çağrısı yeterli:
        AuthService.AuthResult result = authService.register(username, email, password, role);

        switch (result) {
            case SUCCESS -> goToLogin();
            case USERNAME_TAKEN -> errorLabel.setText("Bu kullanıcı adı zaten alınmış.");
            case EMAIL_TAKEN    -> errorLabel.setText("Bu e-posta zaten kayıtlı.");
            case WEAK_PASSWORD  -> errorLabel.setText("Şifreniz en az 6 karakter olmalıdır.");
            default             -> errorLabel.setText("Bir hata oluştu.");
        }
    }

    @FXML
    private void handleLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}