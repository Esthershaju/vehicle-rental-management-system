package com.vehiclerental.dao;

import com.vehiclerental.model.Customer;
import com.vehiclerental.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer entity.
 * Handles database operations related to customers.
 */
public class CustomerDAO {

    /**
     * Adds a new customer to the database.
     */
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email, address, password) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getPassword() != null ? customer.getPassword() : "");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error adding customer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all customers ordered by customer_id descending.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, address FROM customers ORDER BY customer_id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error fetching all customers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves a single customer by ID.
     */
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT customer_id, name, phone, email, address FROM customers WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error fetching customer by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Updates an existing customer.
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setInt(5, customer.getCustomerId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error updating customer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Deletes a customer by ID.
     */
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error deleting customer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Searches customers by name, email, or phone.
     */
    public List<Customer> searchCustomer(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, address FROM customers " +
                     "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR address LIKE ? " +
                     "ORDER BY customer_id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, pattern);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error searching customers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Checks if an email is already used by another customer.
     */
    public boolean isEmailExists(String email, int excludeCustomerId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ? AND customer_id <> ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email.trim());
            stmt.setInt(2, excludeCustomerId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error checking duplicate email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Checks if a customer currently has any active rentals.
     */
    public boolean hasActiveRentals(int customerId) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE customer_id = ? AND rental_status = 'ACTIVE'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error checking active rentals for customer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Returns total count of customers.
     */
    public int getTotalCustomersCount() {
        String sql = "SELECT COUNT(*) FROM customers";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error counting customers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return 0;
    }

    /**
     * Authenticates a customer using their email and password.
     */
    public Customer authenticate(String email, String password) {
        if (email == null || password == null) return null;
        String sql = "SELECT customer_id, name, phone, email, address, password FROM customers WHERE email = ? AND password = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email.trim());
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error during customer authentication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Registers a new customer if email is not duplicate.
     */
    public boolean registerCustomer(Customer customer) {
        if (customer == null || customer.getEmail() == null) return false;
        if (isEmailExists(customer.getEmail(), 0)) {
            return false;
        }
        return addCustomer(customer);
    }

    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        String pwd = null;
        try {
            pwd = rs.getString("password");
        } catch (SQLException ignored) {
            // In queries where password was omitted
        }
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                pwd != null ? pwd : ""
        );
    }
}
