package kullanici.dao;

import kullanici.model.User;
import util.DBConnection;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public void upgradeTableForAvatars() {
        String sql = "ALTER TABLE users ADD COLUMN avatarPath VARCHAR(255) DEFAULT NULL";
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ignored) {
        }
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, passwordHash, avatarPath) VALUES (?,?,?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getAvatarPath());
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
            u.setPasswordHash(rs.getString("passwordHash"));
            u.setAvatarPath(rs.getString("avatarPath"));
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

    public boolean updateAvatar(int userId, String newAvatarPath) {
        String sql = "UPDATE users SET avatarPath = ? WHERE id = ?";
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