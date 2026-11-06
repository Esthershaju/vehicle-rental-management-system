/**
 * ==============================================================
 * Vehicle Rental Management System - Database Specification & Data
 * ==============================================================
 * This file contains:
 * 1. Connection Configurations (MySQL & JDBC)
 * 2. Complete Schema & Table Definitions (columns, types, constraints, foreign keys)
 * 3. Seed / Default Database Records (Users, Vehicles, Customers, Rentals)
 * 4. Helper Utility Methods for mock queries in JavaScript
 * 
 * Compatible with:
 * - Node.js (require('./database-details.js'))
 * - Browser (window.DATABASE_DETAILS)
 * - Windows Script Host (cscript //nologo)
 */

var DATABASE_DETAILS = {
  // -------------------------------------------------------------------------
  // 1. Connection Configuration
  // -------------------------------------------------------------------------
  connection: {
    dialect: "mysql",
    host: "localhost",
    port: 3306,
    database: "vehicle_rental_management_system",
    user: "root",
    password: "YOUR_LOCAL_MYSQL_PASSWORD",
    charset: "utf8mb4",
    collation: "utf8mb4_unicode_ci",
    timezone: "UTC",
    ssl: false,
    allowPublicKeyRetrieval: true,

    // JDBC Driver & Connection String (used by Java backend)
    driverClass: "com.mysql.cj.jdbc.Driver",
    jdbcUrl: "jdbc:mysql://localhost:3306/vehicle_rental_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",

    // Standard MySQL URI (used by Node.js mysql2 / Sequelize / Prisma)
    connectionUri: "mysql://localhost:3306/vehicle_rental_management_system"
  },

  // -------------------------------------------------------------------------
  // 2. Table Schemas & Constraints
  // -------------------------------------------------------------------------
  tables: {
    users: {
      tableName: "users",
      description: "Stores administrator and staff authentication credentials",
      primaryKey: "user_id",
      columns: {
        user_id: { type: "INT", autoIncrement: true, nullable: false, primaryKey: true },
        username: { type: "VARCHAR(50)", nullable: false, unique: true },
        password: { type: "VARCHAR(255)", nullable: false },
        created_at: { type: "TIMESTAMP", "default": "CURRENT_TIMESTAMP" }
      }
    },

    vehicles: {
      tableName: "vehicles",
      description: "Stores vehicle inventory catalog, specs, daily pricing, and live availability",
      primaryKey: "vehicle_id",
      columns: {
        vehicle_id: { type: "INT", autoIncrement: true, nullable: false, primaryKey: true },
        vehicle_number: { type: "VARCHAR(20)", nullable: false, unique: true, description: "License / Plate Number" },
        vehicle_name: { type: "VARCHAR(100)", nullable: false },
        vehicle_type: { type: "VARCHAR(50)", nullable: false, description: "Category: Sedan, SUV, Electric, Sports, Luxury" },
        brand: { type: "VARCHAR(50)", nullable: true },
        model: { type: "VARCHAR(50)", nullable: true },
        rental_price: { type: "DECIMAL(10,2)", nullable: false, description: "Daily hire rate in INR (₹)" },
        availability_status: { type: "VARCHAR(20)", "default": "AVAILABLE", allowedValues: ["AVAILABLE", "RENTED", "MAINTENANCE"] },
        created_at: { type: "TIMESTAMP", "default": "CURRENT_TIMESTAMP" }
      }
    },

    customers: {
      tableName: "customers",
      description: "Stores registered customer contact information and portal credentials",
      primaryKey: "customer_id",
      columns: {
        customer_id: { type: "INT", autoIncrement: true, nullable: false, primaryKey: true },
        name: { type: "VARCHAR(100)", nullable: false },
        phone: { type: "VARCHAR(15)", nullable: false },
        email: { type: "VARCHAR(100)", nullable: false, unique: true },
        password: { type: "VARCHAR(255)", nullable: false, "default": "" },
        address: { type: "VARCHAR(255)", nullable: true },
        created_at: { type: "TIMESTAMP", "default": "CURRENT_TIMESTAMP" }
      }
    },

    rentals: {
      tableName: "rentals",
      description: "Stores rental transactions, agreement dates, charges, and statuses",
      primaryKey: "rental_id",
      foreignKeys: [
        { column: "customer_id", referencedTable: "customers", referencedColumn: "customer_id", onDelete: "RESTRICT", onUpdate: "CASCADE" },
        { column: "vehicle_id", referencedTable: "vehicles", referencedColumn: "vehicle_id", onDelete: "RESTRICT", onUpdate: "CASCADE" }
      ],
      columns: {
        rental_id: { type: "INT", autoIncrement: true, nullable: false, primaryKey: true },
        customer_id: { type: "INT", nullable: false },
        vehicle_id: { type: "INT", nullable: false },
        rental_date: { type: "DATE", nullable: false },
        return_date: { type: "DATE", nullable: true },
        total_amount: { type: "DECIMAL(10,2)", nullable: true, description: "Calculated: days * vehicle.rental_price" },
        rental_status: { type: "VARCHAR(20)", "default": "ACTIVE", allowedValues: ["ACTIVE", "RETURNED", "CANCELLED"] },
        created_at: { type: "TIMESTAMP", "default": "CURRENT_TIMESTAMP" }
      }
    }
  },

  // -------------------------------------------------------------------------
  // 3. Seed / Default Database Records
  // -------------------------------------------------------------------------
  data: {
    users: [
      {
        user_id: 1,
        username: "admin",
        password: "CHANGE_ME_BEFORE_USE",
        role: "ADMIN",
        created_at: "2026-09-23 11:30:00"
      }
    ],

    vehicles: [
      {
        vehicle_id: 1,
        vehicle_number: "VH-101",
        vehicle_name: "Toyota Camry",
        vehicle_type: "Sedan",
        brand: "Toyota",
        model: "Camry 2023",
        rental_price: 3000.00,
        availability_status: "AVAILABLE",
        created_at: "2026-09-23 11:30:00"
      },
      {
        vehicle_id: 2,
        vehicle_number: "VH-102",
        vehicle_name: "Honda CR-V",
        vehicle_type: "SUV",
        brand: "Honda",
        model: "CR-V 2024",
        rental_price: 4000.00,
        availability_status: "AVAILABLE",
        created_at: "2026-09-23 11:30:00"
      },
      {
        vehicle_id: 3,
        vehicle_number: "VH-103",
        vehicle_name: "Tesla Model 3",
        vehicle_type: "Electric",
        brand: "Tesla",
        model: "Model 3 Long Range",
        rental_price: 6000.00,
        availability_status: "RENTED",
        created_at: "2026-09-23 11:30:00"
      },
      {
        vehicle_id: 4,
        vehicle_number: "VH-104",
        vehicle_name: "Ford Mustang GT",
        vehicle_type: "Sports",
        brand: "Ford",
        model: "Mustang V8 2023",
        rental_price: 8500.00,
        availability_status: "AVAILABLE",
        created_at: "2026-09-23 11:30:00"
      },
      {
        vehicle_id: 5,
        vehicle_number: "VH-105",
        vehicle_name: "Hyundai Tucson",
        vehicle_type: "SUV",
        brand: "Hyundai",
        model: "Tucson Limited",
        rental_price: 3500.00,
        availability_status: "AVAILABLE",
        created_at: "2026-09-23 11:30:00"
      },
      {
        vehicle_id: 6,
        vehicle_number: "VH-106",
        vehicle_name: "BMW 330i Luxury",
        vehicle_type: "Luxury",
        brand: "BMW",
        model: "3 Series 2023",
        rental_price: 7500.00,
        availability_status: "AVAILABLE",
        created_at: "2026-09-23 11:30:00"
      }
    ],

    customers: [
      {
        customer_id: 1,
        name: "John Smith",
        phone: "555-0101",
        email: "john.smith@example.com",
        password: "CHANGE_ME_BEFORE_USE",
        address: "124 Oak Street, New York, NY",
        created_at: "2026-09-23 11:30:00"
      },
      {
        customer_id: 2,
        name: "Emma Johnson",
        phone: "555-0102",
        email: "emma.johnson@example.com",
        password: "CHANGE_ME_BEFORE_USE",
        address: "456 Maple Avenue, Los Angeles, CA",
        created_at: "2026-09-23 11:30:00"
      },
      {
        customer_id: 3,
        name: "Michael Brown",
        phone: "555-0103",
        email: "michael.brown@example.com",
        password: "CHANGE_ME_BEFORE_USE",
        address: "789 Pine Road, Austin, TX",
        created_at: "2026-09-23 11:30:00"
      },
      {
        customer_id: 4,
        name: "Sarah Davis",
        phone: "555-0104",
        email: "sarah.davis@example.com",
        password: "CHANGE_ME_BEFORE_USE",
        address: "321 Elm Street, Miami, FL",
        created_at: "2026-09-23 11:30:00"
      },
      {
        customer_id: 5,
        name: "David Wilson",
        phone: "555-0105",
        email: "david.wilson@example.com",
        password: "CHANGE_ME_BEFORE_USE",
        address: "654 Cedar Boulevard, Seattle, WA",
        created_at: "2026-09-23 11:30:00"
      }
    ],

    rentals: [
      {
        rental_id: 1,
        customer_id: 1,
        vehicle_id: 3,
        rental_date: "2026-10-14",
        return_date: "2026-10-17",
        total_amount: 18000.00,
        rental_status: "ACTIVE",
        created_at: "2026-10-14 08:30:00"
      },
      {
        rental_id: 2,
        customer_id: 2,
        vehicle_id: 1,
        rental_date: "2026-10-07",
        return_date: "2026-10-10",
        total_amount: 9000.00,
        rental_status: "RETURNED",
        created_at: "2026-10-07 09:00:00"
      }
    ]
  },

  // -------------------------------------------------------------------------
  // 4. Utility Query Methods (Mock ORM / Query Helpers)
  // -------------------------------------------------------------------------
  helpers: {
    // Get all available vehicles
    getAvailableVehicles: function() {
      var list = [];
      var vehicles = DATABASE_DETAILS.data.vehicles;
      for (var i = 0; i < vehicles.length; i++) {
        if (vehicles[i].availability_status === "AVAILABLE") {
          list.push(vehicles[i]);
        }
      }
      return list;
    },

    // Find vehicle by ID
    getVehicleById: function(id) {
      var vehicles = DATABASE_DETAILS.data.vehicles;
      for (var i = 0; i < vehicles.length; i++) {
        if (vehicles[i].vehicle_id === Number(id)) {
          return vehicles[i];
        }
      }
      return null;
    },

    // Find customer by email
    getCustomerByEmail: function(email) {
      var target = String(email || "").toLowerCase();
      var customers = DATABASE_DETAILS.data.customers;
      for (var i = 0; i < customers.length; i++) {
        if (customers[i].email.toLowerCase() === target) {
          return customers[i];
        }
      }
      return null;
    },

    // Authenticate user (admin or customer)
    authenticate: function(usernameOrEmail, password, role) {
      role = role || "admin";
      if (role === "admin") {
        var users = DATABASE_DETAILS.data.users;
        for (var i = 0; i < users.length; i++) {
          if (users[i].username === usernameOrEmail && users[i].password === password) {
            return users[i];
          }
        }
      } else {
        var customers = DATABASE_DETAILS.data.customers;
        for (var j = 0; j < customers.length; j++) {
          if (customers[j].email === usernameOrEmail && customers[j].password === password) {
            return customers[j];
          }
        }
      }
      return null;
    },

    // Get all rentals with joined customer and vehicle details
    getJoinedRentals: function() {
      var rentals = DATABASE_DETAILS.data.rentals;
      var customers = DATABASE_DETAILS.data.customers;
      var vehicles = DATABASE_DETAILS.data.vehicles;
      var result = [];

      for (var i = 0; i < rentals.length; i++) {
        var rental = rentals[i];
        var customer = null;
        for (var c = 0; c < customers.length; c++) {
          if (customers[c].customer_id === rental.customer_id) {
            customer = customers[c];
            break;
          }
        }
        var vehicle = null;
        for (var v = 0; v < vehicles.length; v++) {
          if (vehicles[v].vehicle_id === rental.vehicle_id) {
            vehicle = vehicles[v];
            break;
          }
        }

        var item = {};
        for (var key in rental) {
          if (Object.prototype.hasOwnProperty.call(rental, key)) {
            item[key] = rental[key];
          }
        }
        item.customer_name = customer ? customer.name : "N/A";
        item.customer_phone = customer ? customer.phone : "N/A";
        item.customer_email = customer ? customer.email : "N/A";
        item.vehicle_name = vehicle ? vehicle.vehicle_name : "N/A";
        item.vehicle_number = vehicle ? vehicle.vehicle_number : "N/A";
        item.rental_price = vehicle ? vehicle.rental_price : 0;
        result.push(item);
      }
      return result;
    },

    // Calculate rental fee based on duration
    calculateRentalFee: function(dailyRate, startDateStr, endDateStr) {
      var start = new Date(startDateStr);
      var end = new Date(endDateStr);
      var diffTime = Math.abs(end - start);
      var diffDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
      return diffDays * dailyRate;
    }
  }
};

// ---------------------------------------------------------------------------
// Export for Universal Compatibility (Browser, Node.js & WSH)
// ---------------------------------------------------------------------------
if (typeof module !== "undefined" && module.exports) {
  module.exports = DATABASE_DETAILS;
}
if (typeof window !== "undefined") {
  window.DATABASE_DETAILS = DATABASE_DETAILS;
}
