package com.scc.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteDao {
    public boolean hasVoted(int userId) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM votes WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.out.println("Vote check failed: " + e.getMessage());
            return false;
        }
    }

    public boolean castVote(int userId, int candidateId) {
        String sql = "INSERT INTO votes(user_id, candidate_id) VALUES(?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, candidateId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Vote was not recorded: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getResults() {
        List<String[]> results = new ArrayList<>();
        String sql = """
            SELECT c.id, c.name, c.position, c.party, COUNT(v.id) AS total
            FROM candidates c LEFT JOIN votes v ON c.id = v.candidate_id
            GROUP BY c.id, c.name, c.position, c.party
            ORDER BY c.position, total DESC, c.name
        """;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(new String[] {
                    String.valueOf(rs.getInt("id")), rs.getString("name"),
                    rs.getString("position"), rs.getString("party"),
                    String.valueOf(rs.getInt("total"))
            });
        } catch (SQLException e) {
            System.out.println("Unable to calculate results: " + e.getMessage());
        }
        return results;
    }
}
