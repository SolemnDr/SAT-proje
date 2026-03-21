package gui;

import javafx.collections.FXCollections;
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
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Kullanıcı", "Yayıncı"
        ));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String roleStr  = roleComboBox.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }

        UserRole role = roleStr.equals("Yayıncı") ? UserRole.PUBLISHER : UserRole.USER;

        AuthService.AuthResult result = authService.register(username, email, password, role);

        switch (result) {
            case SUCCESS -> goToLogin(); // kayıt başarılı, login'e dön
            case USERNAME_TAKEN -> errorLabel.setText("Bu kullanıcı adı zaten alınmış.");
            case EMAIL_TAKEN    -> errorLabel.setText("Bu e-posta zaten kayıtlı.");
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
                    getClass().getResource("/fxml/login.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
