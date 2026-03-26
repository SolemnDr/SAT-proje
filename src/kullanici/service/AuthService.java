package kullanici.service;

import kullanici.dao.UserDAO;
import kullanici.model.User;
import kullanici.model.UserRole;
import util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // Giriş ve Kayıt işlemleri sonucunda dönecek cevaplar
    public enum AuthResult {
        SUCCESS,
        WRONG_PASSWORD,
        USER_NOT_FOUND,
        USERNAME_TAKEN,
        EMAIL_TAKEN,
        WEAK_PASSWORD // Şifre kuralı hatası eklendi
    }

    public AuthResult register(String username, String email, String password, UserRole role) {
        // 1. ŞİFRE KURAL KONTROLÜ: Şifre boş olamaz ve en az 6 karakter olmalı
        if (password == null || password.trim().length() < 6) {
            return AuthResult.WEAK_PASSWORD;
        }

        try {
            // 2. VARLIK KONTROLÜ: Bu kullanıcı adı veya e-posta zaten var mı?
            if (userDAO.findByUsername(username).isPresent()) return AuthResult.USERNAME_TAKEN;
            if (userDAO.emailExists(email)) return AuthResult.EMAIL_TAKEN;

            // 3. GÜVENLİ KAYIT: Sorun yoksa şifreyi BCrypt ile şifrele ve kaydet
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPasswordHash(PasswordUtil.hash(password));
            u.setRole(role);

            userDAO.save(u);
            return AuthResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.USER_NOT_FOUND;
        }
    }

    public AuthResult login(String username, String password) {
        try {
            var user = userDAO.findByUsername(username);

            if (user.isEmpty()) return AuthResult.USER_NOT_FOUND;

            // Veritabanındaki şifreli halini, kullanıcının girdiği düz şifreyle kıyasla
            if (!PasswordUtil.verify(password, user.get().getPasswordHash())) return AuthResult.WRONG_PASSWORD;

            return AuthResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.USER_NOT_FOUND;
        }
    }
}