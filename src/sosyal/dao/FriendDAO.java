package sosyal.dao;

import kullanici.model.User;
import kullanici.model.UserRole;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    // 1. TABLO OLUŞTURMA (PENDING ve ACCEPTED durumları eklendi)
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS friendships (" +
                "user_id INTEGER, " +
                "friend_id INTEGER, " +
                "status VARCHAR(20) DEFAULT 'PENDING', " + // İlk eklendiğinde beklemeye düşer
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, friend_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (friend_id) REFERENCES users(id))";
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. ARKADAŞLIK İSTEĞİ GÖNDERME
    public boolean sendFriendRequest(int senderId, int receiverId) {
        if (senderId == receiverId) return false;
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Zaten istek atılmışsa veya eklilerse patlar ve false döner
        }
    }

    // 3. İSTEĞİ KABUL ETME
    public boolean acceptFriendRequest(int senderId, int receiverId) {
        // İsteği kabul edince durumu ACCEPTED yapıyoruz ve çift yönlü olması için tersini de ekliyoruz
        String updateSql = "UPDATE friendships SET status = 'ACCEPTED' WHERE user_id = ? AND friend_id = ?";
        String insertReverseSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'ACCEPTED')";

        try (Connection conn = DBConnection.get();
             PreparedStatement psUpdate = conn.prepareStatement(updateSql);
             PreparedStatement psInsert = conn.prepareStatement(insertReverseSql)) {

            // Bekleyen isteği güncelle
            psUpdate.setInt(1, senderId);
            psUpdate.setInt(2, receiverId);
            int rows = psUpdate.executeUpdate();

            if (rows > 0) {
                // Karşı tarafın listesine de beni ekle
                psInsert.setInt(1, receiverId);
                psInsert.setInt(2, senderId);
                psInsert.executeUpdate();
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. BEKLEYEN İSTEKLERİ GETİRME (Sadece bana gelen istekler)
    public List<User> getPendingRequests(int userId) throws SQLException {
        List<User> pendingUsers = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.role FROM users u " +
                "JOIN friendships f ON u.id = f.user_id " +
                "WHERE f.friend_id = ? AND f.status = 'PENDING'";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setRole(UserRole.valueOf(rs.getString("role")));
            pendingUsers.add(u);
        }
        return pendingUsers;
    }

    // 5. ONAYLANMIŞ ARKADAŞLARI GETİRME
    public List<User> getFriendsList(int userId) throws SQLException {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.role FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'ACCEPTED'";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setRole(UserRole.valueOf(rs.getString("role")));
            friends.add(u);
        }
        return friends;
    }
    //Arkadaş silme
    public boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setInt(3, friendId);
            ps.setInt(4, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}