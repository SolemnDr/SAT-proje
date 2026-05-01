package sosyal.dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public MessageDAO() {
        // Tablo oluşturan metodunun adı neyse onu çağır
        createTable();
    }
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id INTEGER, " +
                "receiver_id INTEGER, " +
                "message_text TEXT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (sender_id) REFERENCES users(id), " +
                "FOREIGN KEY (receiver_id) REFERENCES users(id))";

        try (java.sql.Statement stmt = util.DBConnection.get().createStatement()) {
            stmt.execute(sql);
        } catch (java.sql.SQLException e) {
            System.out.println("Mesaj tablosu oluşturulurken hata!");
            e.printStackTrace();
        }
    }
    // 2. MESAJ GÖNDERME
    public static boolean sendMessage(int senderId, int receiverId, String messageText) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.get().prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, messageText);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. İKİ KİŞİ ARASINDAKİ SOHBET GEÇMİŞİNİ GETİRME (Tarih Sıralı)
    // Tuğalp sohbet penceresini açtığında bu metot çalışıp eski mesajları dizecek.
    // Şimdilik mesajları String olarak döndürüyoruz, isterseniz ileride Message nesnesine çevirebilirsiniz.
    public static List<String> getConversation(int user1Id, int user2Id) throws SQLException {
        List<String> conversation = new ArrayList<>();

        // Hem benim ona attığım hem onun bana attığı mesajları zaman sırasına göre getirir
        String sql = "SELECT sender_id, message_text, sent_at FROM messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY sent_at ASC";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, user1Id);
        ps.setInt(2, user2Id);
        ps.setInt(3, user2Id);
        ps.setInt(4, user1Id);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int senderId = rs.getInt("sender_id");
            String text = rs.getString("message_text");
            String time = rs.getString("sent_at");

            // Arayüzde kimin yazdığı belli olsun diye basit bir format
            String prefix = (senderId == user1Id) ? "Ben: " : "Arkadaş: ";
            conversation.add("[" + time + "] " + prefix + text);
        }
        return conversation;
    }
}