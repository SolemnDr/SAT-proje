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
            connection = DriverManager.getConnection("jdbc:sqlite:gamestore.db");
            createTables();
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, email TEXT UNIQUE NOT NULL, passwordHash TEXT NOT NULL, avatarPath TEXT, role TEXT DEFAULT 'USER')");
            stmt.execute("CREATE TABLE IF NOT EXISTS friends (user_id INTEGER, friend_id INTEGER, status TEXT DEFAULT 'PENDING', PRIMARY KEY (user_id, friend_id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS games (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, summary TEXT, cover_url TEXT, genres TEXT, rating REAL, release_date INTEGER, price REAL, publisher_id INTEGER, discount_percent REAL DEFAULT 0, sales_count INTEGER DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS cart (user_id INTEGER, game_id INTEGER, PRIMARY KEY (user_id, game_id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS purchases (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, game_id INTEGER, purchase_date INTEGER, price REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender_id INTEGER NOT NULL, receiver_id INTEGER NOT NULL, message_text TEXT NOT NULL, sent_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS library (user_id INTEGER, game_id INTEGER, purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_hidden INTEGER DEFAULT 0, PRIMARY KEY (user_id, game_id), FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (game_id) REFERENCES games(id))");

            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price, cover_url) VALUES " +
                    "(1, 'The Witcher 3: Wild Hunt', 'Açık dünya RPG şaheseri', 'RPG', 9.8, 29.99, '//images.igdb.com/igdb/image/upload/t_thumb/co1wyy.jpg')");

            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price, cover_url) VALUES " +
                    "(2, 'Cyberpunk 2077', 'Fütüristik aksiyon RPG', 'Action, RPG', 8.5, 39.99, '//images.igdb.com/igdb/image/upload/t_thumb/co4hk9.jpg')");

            stmt.execute("INSERT OR IGNORE INTO games (id, name, summary, genres, rating, price, cover_url) VALUES " +
                    "(3, 'Red Dead Redemption 2', 'Vahşi batı macerası', 'Adventure', 9.9, 59.99, '//images.igdb.com/igdb/image/upload/t_thumb/co1q1f.jpg')");

            stmt.execute("INSERT OR IGNORE INTO purchases (id, user_id, game_id, price) VALUES (1, 1, 1, 29.99)");
            stmt.execute("INSERT OR IGNORE INTO purchases (id, user_id, game_id, price) VALUES (2, 1, 3, 59.99)");
        }
    }
}