
package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initialize() throws Exception {

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {

            st.execute("PRAGMA foreign_keys = ON");

            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'VOTER',
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS candidates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    position TEXT NOT NULL,
                    party TEXT NOT NULL DEFAULT 'Independent'
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS votes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    candidate_id INTEGER NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id) REFERENCES users(id),
                    FOREIGN KEY(candidate_id) REFERENCES candidates(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // CREATE DEFAULT ADMIN
            try (PreparedStatement admin = con.prepareStatement(
                    "INSERT OR IGNORE INTO users(username, password, role, status) VALUES(?, ?, 'ADMIN', 'ACTIVE')")) {

                admin.setString(1, "admin");
                admin.setString(2, PasswordUtil.hash("admin123"));
                admin.executeUpdate();
            }

            // DEFAULT CANDIDATES
            st.execute("""
                INSERT INTO candidates(name, position, party)
                SELECT 'Smyle Manayon', 'President', 'Unity Party'
                WHERE NOT EXISTS (SELECT 1 FROM candidates)
            """);

            st.execute("""
                INSERT INTO candidates(name, position, party)
                SELECT 'Jehoash Orendain', 'President', 'Progress Party'
                WHERE (SELECT COUNT(*) FROM candidates) = 1
            """);

            st.execute("""
                INSERT INTO candidates(name, position, party)
                SELECT 'Sayago John Kiefer', 'President', 'Independent'
                WHERE (SELECT COUNT(*) FROM candidates) = 2
            """);

            // UPDATE EXISTING CANDIDATE NAMES
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE candidates SET name = ? WHERE id = ?")) {

                ps.setString(1, "Smyle Manayon");
                ps.setInt(2, 1);
                ps.executeUpdate();

                ps.setString(1, "Jehoash Orendain");
                ps.setInt(2, 2);
                ps.executeUpdate();

                ps.setString(1, "Sayago John Kiefer");
                ps.setInt(2, 3);
                ps.executeUpdate();
            }

            System.out.println("Database initialized successfully!");

        }
    }
}
