package magaza.dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    // Satın alma kaydet
    public void save(int userId, int gameId, double price) throws SQLException {
        String sql = "INSERT INTO purchases (user_id, game_id, purchase_date, price) VALUES (?,?,?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        ps.setLong(3, System.currentTimeMillis());
        ps.setDouble(4, price);
        ps.executeUpdate();
    }

    // Kullanıcı bu oyunu satın almış mı?
    public boolean hasPurchased(int userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM purchases WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeQuery().next();
    }

    // Kullanıcının satın aldığı oyunların id'lerini getir
    public List<Integer> getPurchasedGameIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT game_id FROM purchases WHERE user_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt("game_id"));
        }
        return ids;
    }
}