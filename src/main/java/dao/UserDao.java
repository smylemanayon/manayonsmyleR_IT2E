package com.scc.dao;

import com.scc.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public boolean register(String username, String password) {
        String sql = "INSERT INTO users(username, password, role, status) VALUES(?, ?, 'VOTER', 'PENDING')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getInt("id"), rs.getString("username"),
                            rs.getString("password"), rs.getString("role"), rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role"), rs.getString("status")));
            }
        } catch (SQLException e) {
            System.out.println("Unable to list users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId) {
        return updateUserField("UPDATE users SET status='ACTIVE' WHERE id=? AND role='VOTER'", userId, null);
    }

    public boolean updateRole(int userId, String role) {
        String sql = "UPDATE users SET role=? WHERE id=? AND id<>1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Role update failed: " + e.getMessage());
            return false;
        }
    }

    private boolean updateUserField(String sql, int id, String unused) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("User update failed: " + e.getMessage());
            return false;
        }
    }
}
