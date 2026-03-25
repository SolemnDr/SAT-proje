package kullanici.dao;

import kullanici.model.User;
import kullanici.model.UserRole;
import util.DBConnection;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, role) VALUES (?,?,?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getRole().name());
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
}