package com.scc.dao;

import com.scc.model.Membership;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean recordPayment(int userId, String planType, String startDate, String expiryDate, double amountPaid, int recordedByAdminId) {
        String query = "INSERT INTO memberships (user_id, plan_type, start_date, expiry_date, amount_paid) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, planType);
            pstmt.setString(3, startDate);
            pstmt.setString(4, expiryDate);
            pstmt.setDouble(5, amountPaid);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                String logDetails = String.format("Admin %d recorded payment of PHP %.2f for User ID %d (%s Plan)", recordedByAdminId, amountPaid, userId, planType);
                logDao.logAction(recordedByAdminId, "PAYMENT_RECORDED", logDetails);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error recording payment: " + e.getMessage());
        }
        return false;
    }

    public List<Membership> getAllPayments() {
        List<Membership> memberships = new ArrayList<>();
        String query = "SELECT * FROM memberships ORDER BY membership_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                memberships.add(new Membership(
                        rs.getInt("membership_id"),
                        rs.getInt("user_id"),
                        rs.getString("plan_type"),
                        rs.getString("start_date"),
                        rs.getString("expiry_date"),
                        rs.getDouble("amount_paid")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching payment history: " + e.getMessage());
        }
        return memberships;
    }

    public List<Membership> getPaymentsByUserId(int userId) {
        List<Membership> memberships = new ArrayList<>();
        String query = "SELECT * FROM memberships WHERE user_id = ? ORDER BY membership_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    memberships.add(new Membership(
                            rs.getInt("membership_id"),
                            rs.getInt("user_id"),
                            rs.getString("plan_type"),
                            rs.getString("start_date"),
                            rs.getString("expiry_date"),
                            rs.getDouble("amount_paid")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user payments: " + e.getMessage());
        }
        return memberships;
    }
}