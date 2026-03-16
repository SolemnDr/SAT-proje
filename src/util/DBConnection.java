package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    public static Connection get() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:gamestore.db");
            createTables();
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER'
                )
                """;
        connection.createStatement().execute(sql);
    }
}