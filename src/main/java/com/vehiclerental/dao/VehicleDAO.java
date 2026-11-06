package com.vehiclerental.dao;

import com.vehiclerental.model.Vehicle;
import com.vehiclerental.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Vehicle entity.
 * Handles all database operations relating to vehicles.
 */
public class VehicleDAO {

    /**
     * Adds a new vehicle into the database.
     */
    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, vehicle.getVehicleNumber());
            stmt.setString(2, vehicle.getVehicleName());
            stmt.setString(3, vehicle.getVehicleType());
            stmt.setString(4, vehicle.getBrand());
            stmt.setString(5, vehicle.getModel());
            stmt.setDouble(6, vehicle.getRentalPrice());
            stmt.setString(7, vehicle.getAvailabilityStatus());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vehicle.setVehicleId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error adding vehicle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all vehicles ordered by vehicle_id descending.
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status " +
                     "FROM vehicles ORDER BY vehicle_id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error fetching all vehicles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves only vehicles with availability_status = 'AVAILABLE'.
     */
    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status " +
                     "FROM vehicles WHERE availability_status = 'AVAILABLE' ORDER BY vehicle_name ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error fetching available vehicles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves a vehicle by its ID.
     */
    public Vehicle getVehicleById(int vehicleId) {
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status " +
                     "FROM vehicles WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractVehicleFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error fetching vehicle by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Updates an existing vehicle's details.
     */
    public boolean updateVehicle(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET vehicle_number = ?, vehicle_name = ?, vehicle_type = ?, brand = ?, " +
                     "model = ?, rental_price = ?, availability_status = ? WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicle.getVehicleNumber());
            stmt.setString(2, vehicle.getVehicleName());
            stmt.setString(3, vehicle.getVehicleType());
            stmt.setString(4, vehicle.getBrand());
            stmt.setString(5, vehicle.getModel());
            stmt.setDouble(6, vehicle.getRentalPrice());
            stmt.setString(7, vehicle.getAvailabilityStatus());
            stmt.setInt(8, vehicle.getVehicleId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error updating vehicle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Updates only the availability status of a vehicle.
     */
    public boolean updateAvailability(int vehicleId, String status) {
        String sql = "UPDATE vehicles SET availability_status = ? WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, vehicleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error updating availability: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Deletes a vehicle by its ID.
     */
    public boolean deleteVehicle(int vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error deleting vehicle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(null, stmt, conn);
        }
        return false;
    }

    /**
     * Searches vehicles matching name, number, brand, model, or type.
     */
    public List<Vehicle> searchVehicles(String keyword) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status " +
                     "FROM vehicles WHERE vehicle_name LIKE ? OR vehicle_number LIKE ? OR brand LIKE ? OR model LIKE ? OR vehicle_type LIKE ? " +
                     "ORDER BY vehicle_id DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error searching vehicles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Checks if a vehicle number already exists (excluding the current vehicleId if editing).
     */
    public boolean isVehicleNumberExists(String vehicleNumber, int excludeVehicleId) {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE vehicle_number = ? AND vehicle_id <> ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicleNumber.trim());
            stmt.setInt(2, excludeVehicleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error checking duplicate vehicle number: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Checks if a vehicle is currently associated with an active rental.
     */
    public boolean hasActiveRentals(int vehicleId) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE vehicle_id = ? AND rental_status = 'ACTIVE'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error checking active rentals: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Returns total count of vehicles.
     */
    public int getTotalVehiclesCount() {
        return getCount("SELECT COUNT(*) FROM vehicles");
    }

    /**
     * Returns count of available vehicles.
     */
    public int getAvailableVehiclesCount() {
        return getCount("SELECT COUNT(*) FROM vehicles WHERE availability_status = 'AVAILABLE'");
    }

    /**
     * Returns count of rented vehicles.
     */
    public int getRentedVehiclesCount() {
        return getCount("SELECT COUNT(*) FROM vehicles WHERE availability_status = 'RENTED'");
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
            System.err.println("[VehicleDAO] Error getting count: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return 0;
    }

    private Vehicle extractVehicleFromResultSet(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("vehicle_number"),
                rs.getString("vehicle_name"),
                rs.getString("vehicle_type"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getDouble("rental_price"),
                rs.getString("availability_status")
        );
    }
}
