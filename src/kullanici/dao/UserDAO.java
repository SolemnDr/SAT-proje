package kullanici.dao;

import kullanici.model.User;
import kullanici.model.UserRole;
import util.DBConnection;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    // 1. VERİTABANINA AVATAR SÜTUNUNU GÜVENLİCE EKLEYEN METOT (Uygulama başlarken 1 kez çalışsa yeter)
    public void upgradeTableForAvatars() {
        String sql = "ALTER TABLE users ADD COLUMN avatar_path VARCHAR(255) DEFAULT NULL";
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
            System.out.println("Veritabanına profil fotoğrafı sütunu eklendi.");
        } catch (SQLException e) {
            // Sütun zaten varsa SQLite hata fırlatır, bu hatayı yutuyoruz (önemsiz).
        }
    }

    public void save(User user) throws SQLException {
        // SQL Sorgusuna avatar_path eklendi
        String sql = "INSERT INTO users (username, email, password_hash, role, avatar_path) VALUES (?,?,?,?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getRole().name());
        ps.setString(5, user.getAvatarPath()); // Yeni eklendi
        ps.executeUpdate();
    }

    public boolean isUserExists(String username, String email) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.get();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setRole(UserRole.valueOf(rs.getString("role")));
            u.setAvatarPath(rs.getString("avatar_path")); // Yeni eklendi
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, email);
        return ps.executeQuery().next();
    }

    // KULLANICININ FOTOĞRAFINI GÜNCELLEME METODU
    // Tuğalp arayüzde "Profil Fotoğrafını Değiştir" butonuna basınca burası çalışacak.
    public boolean updateAvatar(int userId, String newAvatarPath) {
        String sql = "UPDATE users SET avatar_path = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, newAvatarPath);
            ps.setInt(2, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}