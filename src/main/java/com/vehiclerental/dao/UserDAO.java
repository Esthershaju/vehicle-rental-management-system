package com.vehiclerental.dao;

import com.vehiclerental.model.User;
import com.vehiclerental.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User entity.
 * Handles database operations related to user authentication.
 */
public class UserDAO {

    /**
     * Authenticates user against the database using username and password.
     * Uses PreparedStatement to prevent SQL injection.
     *
     * @param username The entered username
     * @param password The entered plain password
     * @return User object if credentials are valid, null otherwise
     */
    public User login(String username, String password) {
        String sql = "SELECT user_id, username FROM users WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole("ADMIN");
                user.setFullName("Administrator");
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error during user login: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Retrieves a user by primary key ID.
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, password FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching user by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }
}
