package kullanici.service;

import kullanici.dao.UserDAO;
import kullanici.model.User;
import kullanici.model.UserRole;
import util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public enum AuthResult {
        SUCCESS,
        WRONG_PASSWORD,
        USER_NOT_FOUND,
        USERNAME_TAKEN,
        EMAIL_TAKEN
    }

    public AuthResult register(String username, String email, String password, UserRole role) {
        try {
            if (userDAO.findByUsername(username).isPresent()) return AuthResult.USERNAME_TAKEN;
            if (userDAO.emailExists(email)) return AuthResult.EMAIL_TAKEN;

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
            if (!PasswordUtil.verify(password, user.get().getPasswordHash())) return AuthResult.WRONG_PASSWORD;
            return AuthResult.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.USER_NOT_FOUND;
        }
    }
}