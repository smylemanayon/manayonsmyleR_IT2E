package com.scc.dao;

import com.scc.model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {
    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs(user_id, action_type, description) VALUES(?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Activity log error: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        return fetchLogs(null);
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        return fetchLogs(userId);
    }

    private List<ActivityLog> fetchLogs(Integer userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp " +
                "FROM activity_logs " +
                (userId == null ? "" : "WHERE user_id = ? ") +
                "ORDER BY timestamp DESC, id DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("action_type"), rs.getString("description"),
                            rs.getString("timestamp")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Unable to read logs: " + e.getMessage());
        }
        return logs;
    }

    public boolean clearAllLogs() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM activity_logs")) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Unable to clear logs: " + e.getMessage());
            return false;
        }
    }
}
