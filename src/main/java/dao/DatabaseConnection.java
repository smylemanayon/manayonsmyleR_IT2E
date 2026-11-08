
package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public final class DatabaseConnection {

    // SQLite database file
    private static final String URL = "jdbc:sqlite:voting.db";

    private DatabaseConnection() {
    }

    // Connect to SQLite
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Display the exact database location
    public static void showDatabaseLocation() {
        File database = new File("voting.db");

        System.out.println("================================");
        System.out.println("DATABASE CONNECTION");
        System.out.println("Database: voting.db");
        System.out.println("Location: " + database.getAbsolutePath());
        System.out.println("================================");
    }
}
