package magaza.dao;

import magaza.model.Review;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // 1. YORUMLAR TABLOSUNU OLUŞTUR
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS reviews (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "game_id INTEGER, " +
                "rating INTEGER CHECK(rating >= 1 AND rating <= 5), " + // 1-5 yıldız kuralı
                "comment TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (game_id) REFERENCES games(id), " +
                "UNIQUE(user_id, game_id))"; // Bir kullanıcı bir oyuna sadece 1 yorum yapabilir
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. YORUM VE PUAN EKLEME (Modül 5.2)
    public boolean addReview(int userId, int gameId, int rating, String comment) {
        // Kullanıcı daha önce yorum yaptıysa REPLACE ile üzerine yazar (Günceller)
        String sql = "REPLACE INTO reviews (user_id, game_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, gameId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. BİR OYUNUN TÜM YORUMLARINI GETİRME (Oyun Detay Ekranı İçin)
    public List<Review> getGameReviews(int gameId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        // INNER JOIN ile yorumu yapan kullanıcının adını (username) da çekiyoruz
        String sql = "SELECT r.*, u.username FROM reviews r " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.game_id = ? ORDER BY r.created_at DESC";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, gameId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Review rev = new Review();
            rev.setId(rs.getInt("id"));
            rev.setUserId(rs.getInt("user_id"));
            rev.setUsername(rs.getString("username")); // Ekrana basmak için çok önemli!
            rev.setGameId(rs.getInt("game_id"));
            rev.setRating(rs.getInt("rating"));
            rev.setComment(rs.getString("comment"));
            rev.setCreatedAt(rs.getString("created_at"));
            reviews.add(rev);
        }
        return reviews;
    }

    // 4. OYUNUN ORTALAMA PUANINI HESAPLAMA
    public double getAverageRating(int gameId) throws SQLException {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, gameId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            // Null gelirse 0.0 döndür, aksi takdirde virgülden sonra 1 basamaklı (Örn: 4.5) döndür
            double avg = rs.getDouble("avg_rating");
            return Math.round(avg * 10.0) / 10.0;
        }
        return 0.0;
    }
}