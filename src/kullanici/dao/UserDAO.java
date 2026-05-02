package kullanici.dao;

import kullanici.model.User;
import util.DBConnection;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    // 1. KULLANICIYI KAYDET (Role bilgisini de ekliyoruz)
    public void save(User user) throws SQLException {
        // AvatarPath sütununu sorgudan çıkardık, sadece 4 parametre kaldı
        String sql = "INSERT INTO users (username, email, passwordHash, role) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getRole()); // 0: Oyuncu, 1: Yayıncı
            ps.executeUpdate();
        }
    }

    // 2. KULLANICIYI BUL (Giriş yaparken rolünü de okuyoruz)
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("passwordHash"));
                u.setRole(rs.getInt("role")); // Rol bilgisini veritabanından çekiyoruz
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    // 3. E-POSTA KONTROLÜ
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        }
    }

    // 4. VERİTABANI GÜNCELLEME (Role sütununu ekler)
    public void upgradeTableForRoles() {
        String sql = "ALTER TABLE users ADD COLUMN role INTEGER DEFAULT 0";
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Veritabanı Güncellendi: 'role' sütunu eklendi.");
        } catch (SQLException e) {
            // Sütun zaten varsa hata mesajını görmezden geliyoruz
            if (e.getMessage().contains("duplicate column name") || e.getMessage().contains("already exists")) {
                System.out.println("ℹ️ Bilgi: 'role' sütunu zaten mevcut, işlem atlandı.");
            } else {
                System.err.println("❌ Veritabanı güncellenirken hata: " + e.getMessage());
            }
        }
    }
}