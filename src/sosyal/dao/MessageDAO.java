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

    // 3. İKİ KİŞİ ARASINDAKİ SOHBET GEÇMİŞİNİ GETİRME (Gerçek Nicknameler ile)
    public static List<String> getConversation(int user1Id, int user2Id) throws SQLException {
        List<String> conversation = new ArrayList<>();

        // INNER JOIN kullanarak mesajı gönderen kişinin 'username' bilgisini de users tablosundan çekiyoruz
        String sql = "SELECT m.sender_id, m.message_text, m.sent_at, u.username " +
                "FROM messages m " +
                "JOIN users u ON m.sender_id = u.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                "ORDER BY m.sent_at ASC";

        PreparedStatement ps = DBConnection.get().prepareStatement(sql);
        ps.setInt(1, user1Id);
        ps.setInt(2, user2Id);
        ps.setInt(3, user2Id);
        ps.setInt(4, user1Id);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String senderName = rs.getString("username"); // Veritabanından gelen gerçek oyuncu adı
            String text = rs.getString("message_text");
            String time = rs.getString("sent_at");

            // Artık "Ben/Arkadaş" yerine direkt gönderenin adını (Örn: "Halis: Merhaba") yazdırıyoruz
            conversation.add("[" + time + "] " + senderName + ": " + text);
        }
        return conversation;
    }
}