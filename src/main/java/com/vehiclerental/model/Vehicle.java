package com.vehiclerental.model;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Model class representing a vehicle in the rental inventory.
 */
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private int vehicleId;
    private String vehicleNumber;
    private String vehicleName;
    private String vehicleType;
    private String brand;
    private String model;
    private double rentalPrice;
    private String availabilityStatus; // 'AVAILABLE' or 'RENTED'

    // Default constructor
    public Vehicle() {
        this.availabilityStatus = "AVAILABLE";
    }

    // Constructor without vehicleId (for inserting new vehicles)
    public Vehicle(String vehicleNumber, String vehicleName, String vehicleType, 
                   String brand, String model, double rentalPrice, String availabilityStatus) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.rentalPrice = rentalPrice;
        this.availabilityStatus = (availabilityStatus != null && !availabilityStatus.trim().isEmpty()) 
                ? availabilityStatus : "AVAILABLE";
    }

    // Full constructor
    public Vehicle(int vehicleId, String vehicleNumber, String vehicleName, String vehicleType, 
                   String brand, String model, double rentalPrice, String availabilityStatus) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.rentalPrice = rentalPrice;
        this.availabilityStatus = availabilityStatus;
    }

    // Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    // Helper method to check availability
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.availabilityStatus);
    }

    // Helper method for nicely formatted currency in JSP
    public String getFormattedPrice() {
        DecimalFormat df = new DecimalFormat("₹#,##0.00");
        return df.format(this.rentalPrice);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleId=" + vehicleId +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", vehicleName='" + vehicleName + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", rentalPrice=" + rentalPrice +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                '}';
    }
}
