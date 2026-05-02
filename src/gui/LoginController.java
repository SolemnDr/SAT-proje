package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kullanici.service.AuthService;
import util.Session;

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

        if (result == AuthService.AuthResult.SUCCESS) {
            Session.setCurrentUser(authService.getLoggedInUser());
            int role = authService.getLoggedInUser().getRole();

            try {
                // EĞER GELİŞTİRİCİYSE PANELİNE, DEĞİLSE MAĞAZAYA
                String fxmlFile = (role == 1) ? "publisher_panel.fxml" : "main_layout.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(loader.load(), 1280, 720));
                stage.centerOnScreen();
            } catch (Exception e) { e.printStackTrace(); }
        } else if (result == AuthService.AuthResult.USER_NOT_FOUND) {
            errorLabel.setText("Kullanıcı bulunamadı.");
        } else if (result == AuthService.AuthResult.WRONG_PASSWORD) {
            errorLabel.setText("Şifre hatalı.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}