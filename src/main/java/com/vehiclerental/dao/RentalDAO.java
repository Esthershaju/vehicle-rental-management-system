package com.vehiclerental.dao;

import com.vehiclerental.model.Rental;
import com.vehiclerental.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rental entity.
 * Handles database operations and atomic transactions for vehicle rentals and returns.
 */
public class RentalDAO {

    /**
     * Creates a rental and updates vehicle status to 'RENTED' within an atomic transaction.
     * Fails if the vehicle is not currently AVAILABLE.
     *
     * @param rental Rental object with customerId, vehicleId, rentalDate, returnDate, totalAmount
     * @return true if both the rental was recorded and vehicle status updated; false otherwise
     */
    public boolean createRental(Rental rental) {
        String checkVehicleSql = "SELECT availability_status FROM vehicles WHERE vehicle_id = ? FOR UPDATE";
        String insertRentalSql = "INSERT INTO rentals (customer_id, vehicle_id, rental_date, return_date, total_amount, rental_status) " +
                                 "VALUES (?, ?, ?, ?, ?, ?)";
        String updateVehicleSql = "UPDATE vehicles SET availability_status = 'RENTED' WHERE vehicle_id = ?";

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Verify vehicle exists and is AVAILABLE
            checkStmt = conn.prepareStatement(checkVehicleSql);
            checkStmt.setInt(1, rental.getVehicleId());
            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.err.println("[RentalDAO] Error: Vehicle ID " + rental.getVehicleId() + " does not exist.");
                conn.rollback();
                return false;
            }

            String currentStatus = rs.getString("availability_status");
            if (!"AVAILABLE".equalsIgnoreCase(currentStatus)) {
                System.err.println("[RentalDAO] Error: Vehicle ID " + rental.getVehicleId() + " is already " + currentStatus);
                conn.rollback();
                return false;
            }

            // 2. Insert rental record
            insertStmt = conn.prepareStatement(insertRentalSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, rental.getCustomerId());
            insertStmt.setInt(2, rental.getVehicleId());
            insertStmt.setDate(3, rental.getRentalDate());
            insertStmt.setDate(4, rental.getReturnDate());
            insertStmt.setDouble(5, rental.getTotalAmount());
            insertStmt.setString(6, (rental.getRentalStatus() != null) ? rental.getRentalStatus() : "ACTIVE");

            int insertedRows = insertStmt.executeUpdate();
            if (insertedRows == 0) {
                conn.rollback();
                return false;
            }

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rental.setRentalId(generatedKeys.getInt(1));
                }
            }

            // 3. Update vehicle status to RENTED
            updateStmt = conn.prepareStatement(updateVehicleSql);
            updateStmt.setInt(1, rental.getVehicleId());
            int updatedRows = updateStmt.executeUpdate();

            if (updatedRows == 0) {
                conn.rollback();
                return false;
            }

            // Commit transaction
            conn.commit();
            System.out.println("[RentalDAO] Rental #" + rental.getRentalId() + " created successfully. Vehicle #" + rental.getVehicleId() + " marked RENTED.");
            return true;

        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error during rental creation: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DBConnection.close(rs);
            DBConnection.close(checkStmt);
            DBConnection.close(insertStmt);
            DBConnection.close(updateStmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return false;
    }

    /**
     * Completes a vehicle return in an atomic transaction:
     * 1. Updates rental status to 'RETURNED', sets actual return_date, and final total_amount.
     * 2. Updates the associated vehicle's availability_status back to 'AVAILABLE'.
     */
    public boolean returnVehicle(int rentalId, Date returnDate, double totalAmount) {
        String findRentalSql = "SELECT vehicle_id, rental_status FROM rentals WHERE rental_id = ? FOR UPDATE";
        String updateRentalSql = "UPDATE rentals SET rental_status = 'RETURNED', return_date = ?, total_amount = ? WHERE rental_id = ?";
        String updateVehicleSql = "UPDATE vehicles SET availability_status = 'AVAILABLE' WHERE vehicle_id = ?";

        Connection conn = null;
        PreparedStatement findStmt = null;
        PreparedStatement rentalStmt = null;
        PreparedStatement vehicleStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Find rental and verify it is ACTIVE
            findStmt = conn.prepareStatement(findRentalSql);
            findStmt.setInt(1, rentalId);
            rs = findStmt.executeQuery();

            if (!rs.next()) {
                System.err.println("[RentalDAO] Error: Rental ID " + rentalId + " not found.");
                conn.rollback();
                return false;
            }

            int vehicleId = rs.getInt("vehicle_id");
            String status = rs.getString("rental_status");
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                System.err.println("[RentalDAO] Error: Rental ID " + rentalId + " is already " + status);
                conn.rollback();
                return false;
            }

            // 2. Update rental status and return date/amount
            rentalStmt = conn.prepareStatement(updateRentalSql);
            rentalStmt.setDate(1, returnDate);
            rentalStmt.setDouble(2, totalAmount);
            rentalStmt.setInt(3, rentalId);
            int rentalRows = rentalStmt.executeUpdate();

            if (rentalRows == 0) {
                conn.rollback();
                return false;
            }

            // 3. Mark vehicle as AVAILABLE
            vehicleStmt = conn.prepareStatement(updateVehicleSql);
            vehicleStmt.setInt(1, vehicleId);
            int vehicleRows = vehicleStmt.executeUpdate();

            if (vehicleRows == 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            System.out.println("[RentalDAO] Rental #" + rentalId + " returned successfully. Vehicle #" + vehicleId + " marked AVAILABLE.");
            return true;

        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error returning vehicle: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DBConnection.close(rs);
            DBConnection.close(findStmt);
            DBConnection.close(rentalStmt);
            DBConnection.close(vehicleStmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all rentals with customer and vehicle details joined.
     */
    public List<Rental> getAllRentals() {
        String sql = "SELECT r.rental_id, r.customer_id, r.vehicle_id, r.rental_date, r.return_date, r.total_amount, r.rental_status, " +
                     "c.name AS customer_name, c.phone AS customer_phone, " +
                     "v.vehicle_number, v.vehicle_name, v.vehicle_type, v.rental_price " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "ORDER BY r.rental_id DESC";
        return executeQuery(sql);
    }

    /**
     * Retrieves all rentals for a specific customer.
     */
    public List<Rental> getRentalsByCustomerId(int customerId) {
        String sql = "SELECT r.rental_id, r.customer_id, r.vehicle_id, r.rental_date, r.return_date, r.total_amount, r.rental_status, " +
                     "c.name AS customer_name, c.phone AS customer_phone, " +
                     "v.vehicle_number, v.vehicle_name, v.vehicle_type, v.rental_price " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "WHERE r.customer_id = " + customerId + " " +
                     "ORDER BY r.rental_id DESC";
        return executeQuery(sql);
    }

    /**
     * Retrieves a single rental by ID with joined customer and vehicle details.
     */
    public Rental getRentalById(int rentalId) {
        String sql = "SELECT r.rental_id, r.customer_id, r.vehicle_id, r.rental_date, r.return_date, r.total_amount, r.rental_status, " +
                     "c.name AS customer_name, c.phone AS customer_phone, " +
                     "v.vehicle_number, v.vehicle_name, v.vehicle_type, v.rental_price " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "WHERE r.rental_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rentalId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractRentalFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error fetching rental by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Searches rentals by customer name, phone, vehicle name, number, or rental status.
     */
    public List<Rental> searchRentals(String keyword) {
        String sql = "SELECT r.rental_id, r.customer_id, r.vehicle_id, r.rental_date, r.return_date, r.total_amount, r.rental_status, " +
                     "c.name AS customer_name, c.phone AS customer_phone, " +
                     "v.vehicle_number, v.vehicle_name, v.vehicle_type, v.rental_price " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "WHERE c.name LIKE ? OR c.phone LIKE ? OR v.vehicle_name LIKE ? OR v.vehicle_number LIKE ? OR r.rental_status LIKE ? " +
                     "ORDER BY r.rental_id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Rental> list = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractRentalFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error searching rentals: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Updates rental status directly.
     */
    public boolean updateRentalStatus(int rentalId, String status) {
        String sql = "UPDATE rentals SET rental_status = ? WHERE rental_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, rentalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error updating rental status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves recent rentals for the dashboard view.
     */
    public List<Rental> getRecentRentals(int limit) {
        String sql = "SELECT r.rental_id, r.customer_id, r.vehicle_id, r.rental_date, r.return_date, r.total_amount, r.rental_status, " +
                     "c.name AS customer_name, c.phone AS customer_phone, " +
                     "v.vehicle_number, v.vehicle_name, v.vehicle_type, v.rental_price " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "ORDER BY r.rental_id DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Rental> list = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractRentalFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error fetching recent rentals: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Returns total count of all rentals.
     */
    public int getTotalRentalsCount() {
        return getCount("SELECT COUNT(*) FROM rentals");
    }

    /**
     * Returns count of active rentals.
     */
    public int getActiveRentalsCount() {
        return getCount("SELECT COUNT(*) FROM rentals WHERE rental_status = 'ACTIVE'");
    }

    private int getCount(String sql) {
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
            System.err.println("[RentalDAO] Error counting: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return 0;
    }

    private List<Rental> executeQuery(String sql) {
        List<Rental> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractRentalFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in executeQuery: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    private Rental extractRentalFromResultSet(ResultSet rs) throws SQLException {
        Rental rental = new Rental(
                rs.getInt("rental_id"),
                rs.getInt("customer_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("rental_date"),
                rs.getDate("return_date"),
                rs.getDouble("total_amount"),
                rs.getString("rental_status")
        );
        rental.setCustomerName(rs.getString("customer_name"));
        rental.setCustomerPhone(rs.getString("customer_phone"));
        rental.setVehicleNumber(rs.getString("vehicle_number"));
        rental.setVehicleName(rs.getString("vehicle_name"));
        rental.setVehicleType(rs.getString("vehicle_type"));
        rental.setDailyRate(rs.getDouble("rental_price"));
        return rental;
    }
}
