package com.scc.dao;

import com.scc.model.User;
import java.sql.*;

public class UserDao {

    public boolean registerUser(String username, String password, String role, String status) {
        String query = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error during registration: " + e.getMessage());
            return false;
        }
    }

    public User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
        }
        return null;
    }
}