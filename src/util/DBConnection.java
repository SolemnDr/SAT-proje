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
        String kullaniciSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'USER'
            )
            """;

        String oyunSql = """
        CREATE TABLE IF NOT EXISTS games (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
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

        String satinaalmaSql = """
        CREATE TABLE IF NOT EXISTS purchases (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            game_id INTEGER NOT NULL,
            purchase_date INTEGER NOT NULL,
            price REAL NOT NULL
        )
        """;
        String sepetSql = """
        CREATE TABLE IF NOT EXISTS cart (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            game_id INTEGER NOT NULL
        )
        """;

        connection.createStatement().execute(sepetSql);
        connection.createStatement().execute(satinaalmaSql);
        connection.createStatement().execute(kullaniciSql);
        connection.createStatement().execute(oyunSql);
    }
}