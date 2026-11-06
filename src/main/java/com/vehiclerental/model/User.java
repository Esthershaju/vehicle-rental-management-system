package com.vehiclerental.model;

import java.io.Serializable;

/**
 * Model class representing an administrative system user.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String password;
    private String role = "ADMIN"; // "ADMIN" or "CUSTOMER"
    private Integer customerId;
    private String fullName;

    // Default constructor
    public User() {
    }

    // Constructor without userId
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "ADMIN";
    }

    // Full constructor for admin
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = "ADMIN";
    }

    // Constructor with role and customerId
    public User(int userId, String username, String password, String role, Integer customerId, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role != null ? role : "ADMIN";
        this.customerId = customerId;
        this.fullName = fullName;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", customerId=" + customerId +
                '}';
    }
}
