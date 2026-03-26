package kutuphane.dao;

import magaza.model.Game;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryDAO {

    // 1. TABLOYU OTOMATİK OLUŞTUR (Eğer yoksa)
    // Bu metodu program başlarken bir kere çağırmak iyi olabilir veya DB'yi elle güncelleyebilirsiniz.
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS library (" +
                "user_id INTEGER, " +
                "game_id INTEGER, " +
                "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, game_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (game_id) REFERENCES games(id))";
        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. SAHİPLİK KONTROLÜ (MODÜL 2.5)
    // Tuğalp mağazada "Satın Al" butonunu çizerken bu metodu çağıracak.
    // Eğer 'true' dönerse butonu "Kütüphanede" olarak değiştirecek ve tıklanmasını engelleyecek.
    public boolean checkOwnership(int userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM library WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeQuery().next();
    }

    // 3. OYUN SATIN ALMA / KÜTÜPHANEYE EKLEME
    // Kullanıcı ödemeyi başarıyla yapınca bu metot çalışacak.
    public boolean addGameToLibrary(int userId, int gameId) {
        try {
            // Önce sahiplik kontrolü yapıyoruz ki yanlışlıkla 2 kere eklenmesin
            if (checkOwnership(userId, gameId)) {
                return false;
            }

            String sql = "INSERT INTO library (user_id, game_id) VALUES (?, ?)";
            PreparedStatement ps = DBConnection.get().prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, gameId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. KÜTÜPHANEYİ LİSTELEME (MODÜL 3.2)
    // Adamın "Kütüphanem" sekmesine girdiğinde göreceği kendi oyunları.
    // JOIN işlemi ile library tablosundaki ID'leri, games tablosundaki gerçek oyun verileriyle eşleştiriyoruz.
    public List<Game> getUserLibrary(int userId) throws SQLException {
        List<Game> userGames = new ArrayList<>();

        String sql = "SELECT g.* FROM games g " +
                "JOIN library l ON g.id = l.game_id " +
                "WHERE l.user_id = ?";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Game g = new Game();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            g.setSummary(rs.getString("summary"));
            g.setCoverUrl(rs.getString("cover_url"));
            g.setGenres(rs.getString("genres"));
            g.setRating(rs.getDouble("rating"));
            g.setReleaseDate(rs.getLong("release_date"));
            g.setPrice(rs.getDouble("price"));
            // (Eğer modelinizde eksik setter varsa onları atlayabilirsiniz)
            userGames.add(g);
        }
        return userGames;
    }
}