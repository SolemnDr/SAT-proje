package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kullanici.model.UserRole;
import kullanici.service.AuthService;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    // roleComboBox TAMAMEN SİLİNDİ
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    // initialize metodu sadece ComboBox'u dolduruyordu, artık ona da gerek kalmadığı için silindi.

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        // KAPSAM DARALTMA: Kayıt olan herkes otomatik olarak 'USER' (Oyuncu) rolünü alır.
        UserRole role = UserRole.USER;

        AuthService.AuthResult result = authService.register(username, email, password, role);

        switch (result) {
            case SUCCESS -> goToLogin(); // kayıt başarılı, login'e dön
            case USERNAME_TAKEN -> errorLabel.setText("Bu kullanıcı adı zaten alınmış.");
            case EMAIL_TAKEN    -> errorLabel.setText("Bu e-posta zaten kayıtlı.");
            case WEAK_PASSWORD  -> errorLabel.setText("Şifreniz en az 6 karakter olmalıdır."); // Servisindeki bu güzel kontrolü de arayüze yansıttım
            default             -> errorLabel.setText("Bir hata oluştu.");
        }
    }

    @FXML
    private void handleLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("login.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}