package com.vehiclerental.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class to manage database connections using JDBC.
 * Reads configuration from db.properties or falls back to standard defaults.
 */
public class DBConnection {

    // Default configuration constants (can be edited directly here or in db.properties)
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/vehicle_rental_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String driver = DEFAULT_DRIVER;
    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASS;

    static {
        loadProperties();
        try {
            // Load and register MySQL JDBC driver
            Class.forName(driver);
            System.out.println("[DBConnection] MySQL JDBC Driver loaded successfully: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Loads database configuration from classpath resource 'db.properties' if available.
     */
    private static void loadProperties() {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                driver = props.getProperty("db.driver", DEFAULT_DRIVER);
                url = props.getProperty("db.url", DEFAULT_URL);
                user = props.getProperty("db.user", DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASS);
                System.out.println("[DBConnection] Configuration loaded from db.properties.");
            } else {
                System.out.println("[DBConnection] db.properties not found; using built-in defaults.");
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Warning: Could not read db.properties, using defaults. " + e.getMessage());
        }
    }

    /**
     * Establishes and returns a connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to connect to MySQL database at: " + url);
            System.err.println("[DBConnection] User: " + user + " | Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Closes AutoCloseable resources safely without throwing exceptions.
     */
    public static void close(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Convenient close helper for PreparedStatement and ResultSet.
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        close(rs);
        close(stmt);
        close(conn);
    }
}
