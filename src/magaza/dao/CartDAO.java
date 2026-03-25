package magaza.dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    // Sepete ekle
    public void add(int userId, int gameId) throws SQLException {
        String sql = "INSERT INTO cart (user_id, game_id) VALUES (?,?)";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        ps.executeUpdate();
    }

    // Sepetten çıkar
    public void remove(int userId, int gameId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        ps.executeUpdate();
    }

    // Sepeti temizle
    public void clear(int userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    // Sepetteki oyun id'lerini getir
    public List<Integer> getCartGameIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT game_id FROM cart WHERE user_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt("game_id"));
        }
        return ids;
    }

    // Oyun sepette var mı?
    public boolean isInCart(int userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM cart WHERE user_id = ? AND game_id = ?";
        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeQuery().next();
    }
}