package com.scc.dao;

import com.scc.model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(int userId, String action, String details) {
        String query = "INSERT INTO activity_logs (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error recording activity log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT * FROM activity_logs ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("activity_id"),
                        rs.getInt("user_id"),
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching activity logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("activity_id"),
                            rs.getInt("user_id"),
                            rs.getString("action"),
                            rs.getString("details"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching logs by user ID: " + e.getMessage());
        }
        return logs;
    }

    public boolean clearAllLogs() {
        String query = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            System.out.println("Error clearing logs: " + e.getMessage());
            return false;
        }
    }
}