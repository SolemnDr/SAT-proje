package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // v2'yi sildik, orijinal isme geri döndük
            connection = DriverManager.getConnection("jdbc:sqlite:gamestore.db");
            createTables();
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Tabloları Kurulumu
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, email TEXT UNIQUE, password_hash TEXT, role TEXT DEFAULT 'USER')");
            stmt.execute("CREATE TABLE IF NOT EXISTS games (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, summary TEXT, cover_url TEXT, genres TEXT, rating REAL, release_date INTEGER, price REAL, publisher_id INTEGER, discount_percent REAL DEFAULT 0, sales_count INTEGER DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS cart (user_id INTEGER, game_id INTEGER, PRIMARY KEY (user_id, game_id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS purchases (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, game_id INTEGER, purchase_date INTEGER, price REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender_id INTEGER NOT NULL, receiver_id INTEGER NOT NULL, message_text TEXT NOT NULL, sent_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // 2. MAĞAZAYA OYUN EKLE (Mağaza boş kalmasın)
            // id'leri bilerek veriyoruz ki sistem aynı oyunu 2 kere eklemeye çalışmasın
            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price) VALUES (1, 'The Witcher 3: Wild Hunt', 'Harika bir RPG', 'RPG', 9.8, 29.99)");
            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price) VALUES (2, 'Cyberpunk 2077', 'Aksiyon dolu', 'Action', 8.5, 39.99)");
            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price) VALUES (3, 'Red Dead Redemption 2', 'Vahşi batı', 'Adventure', 9.9, 59.99)");

            // 3. KÜTÜPHANEYE OYUN EKLE (Senin için örnek satın alım)
            // user_id = 1 (Sen), game_id = 1 ve 3 numaralı oyunları almışsın gibi gösteriyoruz
            stmt.execute("INSERT OR IGNORE INTO purchases (id, user_id, game_id, price) VALUES (1, 1, 1, 29.99)");
            stmt.execute("INSERT OR IGNORE INTO purchases (id, user_id, game_id, price) VALUES (2, 1, 3, 59.99)");
        }
    }
}