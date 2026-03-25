package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:gamestore.db");
            createTables();
        }
        return connection;
    }
    private static void createTables() throws SQLException {
        // Kullanıcılar Tablosu
        String sqlUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER'
                )
                """;
        // Oyunlar Tablosu
        String sqlGames = """
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    summary TEXT,
                    cover_url TEXT,
                    genres TEXT,
                    rating REAL,
                    release_date INTEGER,
                    price REAL,
                    publisher_id INTEGER,
                    discount_percent REAL DEFAULT 0,
                    sales_count INTEGER DEFAULT 0
                )
                """;
        // Sepet Tablosu (Bir kullanıcı bir oyunu sepete 1 kez ekleyebilir)
        String sqlCart = """
                CREATE TABLE IF NOT EXISTS cart (
                    user_id INTEGER,
                    game_id INTEGER,
                    PRIMARY KEY (user_id, game_id)
                )
                """;
        // Satın Alımlar Tablosu
        String sqlPurchases = """
                CREATE TABLE IF NOT EXISTS purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    game_id INTEGER,
                    purchase_date INTEGER,
                    price REAL
                )
                """;

        connection.createStatement().execute(sqlUsers);
        connection.createStatement().execute(sqlGames);
        connection.createStatement().execute(sqlCart);
        connection.createStatement().execute(sqlPurchases);
    }
}