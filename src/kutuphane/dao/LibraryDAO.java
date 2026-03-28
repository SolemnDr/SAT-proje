package kutuphane.dao;

import magaza.model.Game;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryDAO {

    // 1. TABLOLARI OLUŞTUR (Kütüphane, Özel Listeler ve Liste-Oyun Bağlantısı)
    public void createTablesIfNotExists() {
        String sqlLibrary = "CREATE TABLE IF NOT EXISTS library (" +
                "user_id INTEGER, " +
                "game_id INTEGER, " +
                "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_hidden INTEGER DEFAULT 0, " + // 0: Görünür, 1: Gizli
                "PRIMARY KEY (user_id, game_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (game_id) REFERENCES games(id))";

        String sqlCollections = "CREATE TABLE IF NOT EXISTS collections (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "name VARCHAR(50) NOT NULL)";

        String sqlCollectionGames = "CREATE TABLE IF NOT EXISTS collection_games (" +
                "collection_id INTEGER, " +
                "game_id INTEGER, " +
                "PRIMARY KEY (collection_id, game_id))";

        try (Statement stmt = DBConnection.get().createStatement()) {
            stmt.execute(sqlLibrary);
            stmt.execute(sqlCollections);
            stmt.execute(sqlCollectionGames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- KÜTÜPHANE TEMEL İŞLEMLERİ ---

    public boolean checkOwnership(int userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM library WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeQuery().next();
    }

    public boolean addGameToLibrary(int userId, int gameId) {
        try {
            if (checkOwnership(userId, gameId)) return false;
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

    // Sadece görünür olan oyunları getir (Arkadaşların da bu metodu kullanacak!)
    public List<Game> getVisibleLibrary(int userId) throws SQLException {
        List<Game> userGames = new ArrayList<>();
        // is_hidden = 0 olanları çekeriz
        String sql = "SELECT g.* FROM games g " +
                "JOIN library l ON g.id = l.game_id " +
                "WHERE l.user_id = ? AND l.is_hidden = 0";

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
            g.setPrice(rs.getDouble("price"));
            userGames.add(g);
        }
        return userGames;
    }

    // Oyunu gizle veya göster
    public void setGameHiddenStatus(int userId, int gameId, boolean isHidden) throws SQLException {
        String sql = "UPDATE library SET is_hidden = ? WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, isHidden ? 1 : 0);
        ps.setInt(2, userId);
        ps.setInt(3, gameId);
        ps.executeUpdate();
    }

    // --- ÖZEL LİSTE (KOLEKSİYON) İŞLEMLERİ ---

    // Yeni Liste Oluştur (Örn: "Favorilerim")
    public void createCollection(int userId, String collectionName) throws SQLException {
        String sql = "INSERT INTO collections (user_id, name) VALUES (?, ?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setString(2, collectionName);
        ps.executeUpdate();
    }

    // Listeye Oyun Ekle
    public void addGameToCollection(int collectionId, int gameId) throws SQLException {
        String sql = "INSERT INTO collection_games (collection_id, game_id) VALUES (?, ?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, collectionId);
        ps.setInt(2, gameId);
        ps.executeUpdate();
    }
}