package com.vehiclerental.model;

import java.io.Serializable;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing a vehicle rental transaction.
 */
public class Rental implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rentalId;
    private int customerId;
    private int vehicleId;
    private Date rentalDate;
    private Date returnDate;
    private double totalAmount;
    private String rentalStatus; // 'ACTIVE' or 'RETURNED'

    // Additional display fields populated from JOIN queries
    private String customerName;
    private String customerPhone;
    private String vehicleNumber;
    private String vehicleName;
    private String vehicleType;
    private double dailyRate;

    // Default constructor
    public Rental() {
        this.rentalStatus = "ACTIVE";
    }

    // Constructor for creating a new rental
    public Rental(int customerId, int vehicleId, Date rentalDate, Date returnDate, 
                  double totalAmount, String rentalStatus) {
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.rentalStatus = (rentalStatus != null && !rentalStatus.trim().isEmpty()) 
                ? rentalStatus : "ACTIVE";
    }

    // Full constructor
    public Rental(int rentalId, int customerId, int vehicleId, Date rentalDate, 
                  Date returnDate, double totalAmount, String rentalStatus) {
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.rentalStatus = rentalStatus;
    }

    // Getters and Setters
    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Date getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(Date rentalDate) {
        this.rentalDate = rentalDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    // Helper to check if active
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.rentalStatus);
    }

    // Helper to check if returned or completed
    public boolean isReturned() {
        return "RETURNED".equalsIgnoreCase(this.rentalStatus) || "COMPLETED".equalsIgnoreCase(this.rentalStatus);
    }

    // Calculate duration in days
    public long getRentalDays() {
        if (rentalDate == null) {
            return 1;
        }
        Date end = (returnDate != null) ? returnDate : new Date(System.currentTimeMillis());
        long days = ChronoUnit.DAYS.between(rentalDate.toLocalDate(), end.toLocalDate());
        return (days <= 0) ? 1 : days;
    }

    // Duration helper alias
    public long getRentalDurationDays() {
        return getRentalDays();
    }

    // Vehicle rental price / daily rate alias
    public double getVehicleRentalPrice() {
        return dailyRate;
    }

    // Formatted currency helper
    public String getFormattedTotalAmount() {
        DecimalFormat df = new DecimalFormat("₹#,##0.00");
        return df.format(this.totalAmount);
    }

    @Override
    public String toString() {
        return "Rental{" +
                "rentalId=" + rentalId +
                ", customerId=" + customerId +
                ", vehicleId=" + vehicleId +
                ", rentalDate=" + rentalDate +
                ", returnDate=" + returnDate +
                ", totalAmount=" + totalAmount +
                ", rentalStatus='" + rentalStatus + '\'' +
                '}';
    }
}
