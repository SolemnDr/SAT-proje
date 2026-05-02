package kullanici.service;

import kullanici.dao.UserDAO;
import kullanici.model.User;
import util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private User loggedInUser;

    public enum AuthResult {
        SUCCESS,
        WRONG_PASSWORD,
        USER_NOT_FOUND,
        USERNAME_TAKEN,
        EMAIL_TAKEN,
        WEAK_PASSWORD
    }

    public AuthResult register(String username, String email, String password, int role) {
        // 1. Şifre uzunluk kontrolü
        if (password == null || password.trim().length() < 6) {
            return AuthResult.WEAK_PASSWORD;
        }

        try {
            // 2. Kullanıcı adı veya email zaten var mı?
            if (userDAO.findByUsername(username).isPresent()) return AuthResult.USERNAME_TAKEN;
            if (userDAO.emailExists(email)) return AuthResult.EMAIL_TAKEN;

            // 3. Yeni kullanıcı nesnesini oluştur ve doldur
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setRole(role); // 0 veya 1 olarak buradan set ediyoruz
            u.setPasswordHash(PasswordUtil.hash(password));
            u.setAvatarPath(null); // Başlangıçta avatar yok

            // 4. Veritabanına kaydet
            userDAO.save(u);
            return AuthResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.USER_NOT_FOUND; // Genel hata durumu
        }
    }

    public AuthResult login(String username, String password) {
        try {
            var user = userDAO.findByUsername(username);

            if (user.isEmpty()) return AuthResult.USER_NOT_FOUND;

            if (!PasswordUtil.verify(password, user.get().getPasswordHash())) return AuthResult.WRONG_PASSWORD;

            this.loggedInUser = user.get();
            return AuthResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.USER_NOT_FOUND;
        }
    }


    public User getLoggedInUser() {
        return loggedInUser;
    }
}