package com.scc.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDao {
    public record Candidate(int id, String name, String position, String party) {}

    public List<Candidate> getAllCandidates() {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates ORDER BY position, id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Candidate(rs.getInt("id"), rs.getString("name"),
                    rs.getString("position"), rs.getString("party")));
        } catch (SQLException e) {
            System.out.println("Candidate error: " + e.getMessage());
        }
        return list;
    }

    public boolean addCandidate(String name, String position, String party) {
        String sql = "INSERT INTO candidates(name, position, party) VALUES(?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, position);
            ps.setString(3, party);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Unable to add candidate: " + e.getMessage());
            return false;
        }
    }
}
