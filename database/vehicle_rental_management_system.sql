-- ==============================================================
-- Vehicle Rental Management System Database Script
-- Database: vehicle_rental_management_system
-- Target DBMS: MySQL 8.0+
-- ==============================================================

-- 1. Create Database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS vehicle_rental_management_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vehicle_rental_management_system;

-- 2. Drop existing tables in reverse dependency order (if re-running)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS rentals;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================
-- 3. Table: users
-- Stores administrator and staff login credentials
-- ==============================================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================================================
-- 4. Table: vehicles
-- Stores vehicle inventory and real-time availability status
-- ==============================================================
CREATE TABLE vehicles (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_name VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    rental_price DECIMAL(10,2) NOT NULL,
    availability_status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================================================
-- 5. Table: customers
-- Stores registered customer information
-- ==============================================================
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL DEFAULT '',
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================================================
-- 6. Table: rentals
-- Stores vehicle rental transactions and status
-- ==============================================================
CREATE TABLE rentals (
    rental_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    rental_date DATE NOT NULL,
    return_date DATE,
    total_amount DECIMAL(10,2),
    rental_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rentals_customer FOREIGN KEY (customer_id) 
        REFERENCES customers (customer_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_rentals_vehicle FOREIGN KEY (vehicle_id) 
        REFERENCES vehicles (vehicle_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================================================
-- 7. Sample Data Insertion
-- ==============================================================

-- 7.1 Set the admin password placeholder before importing this sample data.
INSERT INTO users (username, password) VALUES 
('admin', 'CHANGE_ME_BEFORE_USE');

-- 7.2 Vehicles (Sample vehicles with INR rental rates)
INSERT INTO vehicles (vehicle_number, vehicle_name, vehicle_type, brand, model, rental_price, availability_status) VALUES
('VH-101', 'Toyota Camry', 'Sedan', 'Toyota', 'Camry 2023', 3000.00, 'AVAILABLE'),
('VH-102', 'Honda CR-V', 'SUV', 'Honda', 'CR-V 2024', 4000.00, 'AVAILABLE'),
('VH-103', 'Tesla Model 3', 'Electric', 'Tesla', 'Model 3 Long Range', 6000.00, 'RENTED'),
('VH-104', 'Ford Mustang GT', 'Sports', 'Ford', 'Mustang V8 2023', 8500.00, 'AVAILABLE'),
('VH-105', 'Hyundai Tucson', 'SUV', 'Hyundai', 'Tucson Limited', 3500.00, 'AVAILABLE'),
('VH-106', 'BMW 330i Luxury', 'Luxury', 'BMW', '3 Series 2023', 7500.00, 'AVAILABLE');

-- 7.3 Customers (At least 5 sample customers)
INSERT INTO customers (name, phone, email, address) VALUES
('John Smith', '555-0101', 'john.smith@example.com', '124 Oak Street, New York, NY'),
('Emma Johnson', '555-0102', 'emma.johnson@example.com', '456 Maple Avenue, Los Angeles, CA'),
('Michael Brown', '555-0103', 'michael.brown@example.com', '789 Pine Road, Austin, TX'),
('Sarah Davis', '555-0104', 'sarah.davis@example.com', '321 Elm Street, Miami, FL'),
('David Wilson', '555-0105', 'david.wilson@example.com', '654 Cedar Boulevard, Seattle, WA');

-- 7.4 Sample Rentals (1 Active rental matching the RENTED vehicle, 1 Returned rental)
-- Rental 1: Active rental for VH-103 (Tesla Model 3) by John Smith (3 days @ ₹6,000 = ₹18,000)
INSERT INTO rentals (customer_id, vehicle_id, rental_date, return_date, total_amount, rental_status) VALUES
(1, 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 18000.00, 'ACTIVE');

-- Rental 2: Completed/Returned rental for VH-101 (Toyota Camry) by Emma Johnson (3 days @ ₹3,000 = ₹9,000)
INSERT INTO rentals (customer_id, vehicle_id, rental_date, return_date, total_amount, rental_status) VALUES
(2, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY), 9000.00, 'RETURNED');
