package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kullanici.service.AuthService;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        AuthService.AuthResult result = authService.login(username, password);

        switch (result) {
            case SUCCESS -> {
                errorLabel.setText("");
                // Ana ekrana geçiş buraya gelecek
            }
            case USER_NOT_FOUND -> errorLabel.setText("Kullanıcı bulunamadı.");
            case WRONG_PASSWORD -> errorLabel.setText("Şifre hatalı.");
            default -> errorLabel.setText("Bir hata oluştu.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}