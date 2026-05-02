package magaza.dao;

import magaza.model.Game;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    // Oyun kaydet
    public void save(Game game) throws SQLException {
        // Aynı isimde oyun varsa ekleme
        String check = "SELECT 1 FROM games WHERE name = ?";
        PreparedStatement ps2 = DBConnection.get().prepareStatement(check);
        ps2.setString(1, game.getName());
        if (ps2.executeQuery().next()) return;

        String sql = "INSERT INTO games (name, summary, cover_url, genres, rating, release_date, price, publisher_id) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, game.getName());
        ps.setString(2, game.getSummary());
        ps.setString(3, game.getCoverUrl());
        ps.setString(4, game.getGenres());
        ps.setDouble(5, game.getRating());
        ps.setLong(6, game.getReleaseDate());
        ps.setDouble(7, game.getPrice());
        ps.setInt(8, game.getPublisherId());
        ps.executeUpdate();
    }

    // İndirim uygula (GameService burayı veya alttakini çağırabilir, ikisini de güvenliğe aldım)
    public void applyDiscount(int gameId, double discountPercent) throws SQLException {
        String sql = "UPDATE games SET discount_percent = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setDouble(1, discountPercent);
            ps.setInt(2, gameId);
            ps.executeUpdate();
            System.out.println("✅ Oyun ID: " + gameId + " için %" + discountPercent + " indirim uygulandı.");
        }
    }

    // İsim çakışması olmasın diye GameService 'updateDiscount' çağırırsa diye köprü kurduk
    public void updateDiscount(int gameId, double discountPercent) throws SQLException {
        applyDiscount(gameId, discountPercent);
    }

    // Tüm oyunları getir
    public List<Game> findAll() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        ResultSet rs = DBConnection.get().createStatement().executeQuery(sql);
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Sayfalama (Pagination) destekli tüm oyunları getir
    public List<Game> findAllWithPagination(int limit, int offset) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games LIMIT ? OFFSET ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, limit);
        ps.setInt(2, offset);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // İsme göre ara
    public List<Game> findByName(String name) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE name LIKE ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, "%" + name + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Kategoriye göre ara
    public List<Game> findByGenre(String genre) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE genres LIKE ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, "%" + genre + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // ResultSet'i Game nesnesine çevir (ALT TİRELİ discount_percent'e DİKKAT)
    private Game mapRow(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setSummary(rs.getString("summary"));
        g.setCoverUrl(rs.getString("cover_url"));
        g.setGenres(rs.getString("genres"));
        g.setRating(rs.getDouble("rating"));
        g.setReleaseDate(rs.getLong("release_date"));
        g.setPrice(rs.getDouble("price"));
        g.setPublisherId(rs.getInt("publisher_id"));

        // Veritabanında discount_percent diye kayıtlı, onu okuyoruz!
        g.setDiscountPercent(rs.getDouble("discount_percent"));
        g.setSalesCount(rs.getInt("sales_count"));
        return g;
    }

    // Oyun sil
    public void delete(int gameId) throws SQLException {
        String sql = "DELETE FROM games WHERE id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, gameId);
        ps.executeUpdate();
    }

    // Oyun güncelle
    public void update(Game game) throws SQLException {
        String sql = "UPDATE games SET name=?, summary=?, cover_url=?, genres=?, rating=?, price=?, publisher_id=? WHERE id=?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setString(1, game.getName());
        ps.setString(2, game.getSummary());
        ps.setString(3, game.getCoverUrl());
        ps.setString(4, game.getGenres());
        ps.setDouble(5, game.getRating());
        ps.setDouble(6, game.getPrice());
        ps.setInt(7, game.getPublisherId());
        ps.setInt(8, game.getId());
        ps.executeUpdate();
    }

    // Publisher'ın oyunlarını getir
    public List<Game> findByPublisher(int publisherId) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE publisher_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, publisherId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Fiyata göre sırala
    public List<Game> findAllSortedByPrice() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY price ASC";
        ResultSet rs = DBConnection.get().createStatement().executeQuery(sql);
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Puana göre sırala
    public List<Game> findAllSortedByRating() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY rating DESC";
        ResultSet rs = DBConnection.get().createStatement().executeQuery(sql);
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Çok satanları getir
    public List<Game> findBestSellers() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY sales_count DESC LIMIT 10";
        ResultSet rs = DBConnection.get().createStatement().executeQuery(sql);
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // İndirimli oyunları getir
    public List<Game> findDiscounted() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE discount_percent > 0 ORDER BY discount_percent DESC";
        ResultSet rs = DBConnection.get().createStatement().executeQuery(sql);
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    // Satış sayısını artır
    public void incrementSalesCount(int gameId) throws SQLException {
        String sql = "UPDATE games SET sales_count = sales_count + 1 WHERE id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, gameId);
        ps.executeUpdate();
    }

    // ID'ye göre tek bir oyun getir
    public Game findById(int id) throws SQLException {
        String sql = "SELECT * FROM games WHERE id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    // Gelişmiş Çoklu Filtreleme
    public List<Game> searchGamesAdvanced(String genre, Double maxPrice, String sortBy) throws SQLException {
        List<Game> games = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM games WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (genre != null && !genre.trim().isEmpty()) {
            sql.append(" AND genres LIKE ?");
            params.add("%" + genre + "%");
        }

        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if ("PRICE_ASC".equals(sortBy)) {
            sql.append(" ORDER BY price ASC");
        } else if ("PRICE_DESC".equals(sortBy)) {
            sql.append(" ORDER BY price DESC");
        } else if ("RATING_DESC".equals(sortBy)) {
            sql.append(" ORDER BY rating DESC");
        }

        PreparedStatement ps = DBConnection.get().prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            games.add(mapRow(rs));
        }
        return games;
    }

    public List<Game> getRecommendationsForUser(int userId) throws SQLException {
        List<Game> recommended = new ArrayList<>();
        String sql = "SELECT DISTINCT g.* FROM games g " +
                "JOIN purchases l ON l.user_id = ? " +
                "JOIN games ug ON l.game_id = ug.id " +
                "WHERE g.genres LIKE '%' || ug.genres || '%' " +
                "AND g.id NOT IN (SELECT game_id FROM purchases WHERE user_id = ?) " +
                "ORDER BY g.rating DESC LIMIT 5";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            recommended.add(mapRow(rs));
        }
        return recommended;
    }

    // KULLANICININ SATIN ALDIĞI OYUNLARI GETİR
    public List<Game> getPurchasedGames(int userId) throws SQLException {
        List<Game> ownedGames = new ArrayList<>();
        String sql = "SELECT games.* FROM games INNER JOIN purchases ON games.id = purchases.game_id WHERE purchases.user_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            ownedGames.add(mapRow(rs));
        }
        return ownedGames;
    }

    // Tablo güncelleme (discount_percent olarak düzeltildi!)
    public void upgradeTableForDiscounts() {
        String sql = "ALTER TABLE games ADD COLUMN discount_percent REAL DEFAULT 0.0";
        try (java.sql.Statement stmt = util.DBConnection.get().createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Veritabanı Güncellendi: 'discount_percent' sütunu eklendi.");
        } catch (java.sql.SQLException e) {
            if (e.getMessage().contains("duplicate column name") || e.getMessage().contains("already exists")) {
                System.out.println("ℹ️ Bilgi: 'discount_percent' sütunu zaten mevcut.");
            } else {
                System.out.println("❌ İndirim sütunu eklenirken hata: " + e.getMessage());
            }
        }
    }
}