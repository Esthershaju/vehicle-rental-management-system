# Vehicle Rental Management System

A complete, production-grade web application built using **pure Java, Jakarta Servlets, JSP, JDBC, MySQL, HTML5, CSS3, and JavaScript**, packaged with **Apache Maven**, and designed to run seamlessly on **Apache Tomcat 10.1+**.

---

## 1. Introduction

The **Vehicle Rental Management System** is designed for vehicle rental businesses to automate and digitize fleet operations. It enables customers to browse real-time available vehicle inventory and allows administrators to manage vehicle inventory, maintain customer directories, record rental agreements, enforce availability constraints, and process vehicle returns.

This project follows the industry-standard **Model-View-Controller (MVC)** architectural pattern and is designed to meet university and college evaluation requirements (MCA, B.Tech, BCA, B.Sc Computer Science).

---

## 2. Objectives

- **Digitize Rental Operations**: Eliminate manual paper logging and Excel spreadsheets for vehicle hires.
- **Show Real-Time Availability**: Instantly reflect whether a car is `AVAILABLE` or `RENTED`.
- **Maintain Consistency**: Enforce database-level ACID transactions so when a car is rented, it automatically transitions to `RENTED`, and when returned, it reverts to `AVAILABLE`.
- **Accurate Billing**: Automatically compute rental charges based on rental duration (in days) and daily rental rate.
- **Data Integrity & Security**: Prevent SQL injection using `PreparedStatement`, prevent duplicate vehicle numbers and customer emails, and secure management pages using session authentication.

---

## 3. Technologies Used

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Programming Language** | Java (JDK) | 17 / 21 | Core backend business logic & models |
| **Server & Container** | Apache Tomcat | 10.1.x | Web container for Servlets & JSP |
| **Web Layer (Controller)** | Jakarta Servlets | 6.0 | Request dispatching, validation & session control |
| **Presentation Layer (View)** | JSP & JSTL | 3.1 & 3.0 | Dynamic HTML rendering with Expression Language |
| **Frontend Styling** | Vanilla CSS3 | Custom Design System | Responsive layout, modern dashboard & cards |
| **Frontend Behavior** | JavaScript (ES6) | Vanilla JS | Client-side validation & dynamic price calculation |
| **Data Access Layer** | JDBC & MySQL Connector/J | 8.3.0 | Database connectivity and SQL query execution |
| **Database Management** | MySQL Server | 8.0+ | Relational database storage |
| **Build & Dependency Tool** | Apache Maven | 3.9+ | Compilation, dependency management, WAR packaging |

*No heavy frameworks (Spring Boot, Hibernate, Node.js, or Python) are used, adhering strictly to core Java enterprise guidelines.*

---

## 4. Main Modules

### Module 1 – User Authentication & Session Management
- Admin login with credentials stored in MySQL (`users` table).
- Customer registration and login with credentials stored in MySQL (`customers` table).
- Server-side validation of submitted credentials and registration fields.
- Session creation on successful login, session-ID rotation after authentication, and invalidation on logout.
- `AuthFilter` protects dashboard and management routes, including direct dashboard JSP/HTML requests, and redirects unauthenticated users to `/login`.

### Module 2 – Executive Dashboard
- Summarizes live Key Performance Indicators (KPIs):
  - **Total Fleet Count**
  - **Available Vehicles**
  - **Currently Rented Vehicles**
  - **Total Registered Customers**
  - **Total Rental Transactions**
- Quick action navigation shortcuts to add vehicles, customers, or create rentals.
- Recent activity feed showing recent rentals with one-click return processing.

### Module 3 – Vehicle Inventory Management
- Full CRUD functionality (Add, View, Update, Delete).
- Fields: `vehicle_id`, `vehicle_number`, `vehicle_name`, `vehicle_type`, `brand`, `model`, `rental_price`, `availability_status`.
- Keyword search by vehicle name, registration number, category, or brand.
- Quick filter for `AVAILABLE` vehicles only.
- Safeguard: Prevents deletion of vehicles actively assigned to ongoing rentals.

### Module 4 – Customer Directory Management
- Full CRUD functionality (Add, View, Update, Delete).
- Fields: `customer_id`, `name`, `phone`, `email`, `address`.
- Validation for phone format and valid email regex.
- Enforces unique email constraint to prevent duplicate registrations.
- Safeguard: Prevents deletion of customers with active rentals.

### Module 5 – Rental & Return Operations
- Fields: `rental_id`, `customer_id`, `vehicle_id`, `rental_date`, `return_date`, `total_amount`, `rental_status`.
- Statuses: `ACTIVE`, `RETURNED`.
- **Atomic Business Rules**:
  1. A vehicle can only be rented if its status is `AVAILABLE`.
  2. Creating a rental inserts the rental record AND updates the vehicle's status to `RENTED` inside a database transaction (`setAutoCommit(false)`).
  3. Processing a return updates the rental to `RETURNED`, records the return date and final charge, AND updates the vehicle back to `AVAILABLE`.
- Live dynamic rental fee calculator: `total_amount = days × rental_price` (minimum 1 day).

---

## 5. MVC Architecture & System Communication

```
  ┌────────────────────────────────────────────────────────────┐
  │                    BROWSER / CLIENT                        │
  │   (index.jsp, dashboard.jsp, vehicles/, customers/, etc.)  │
  └─────────────────────────────┬──────────────────────────────┘
                                │ HTTP GET / POST
                                ▼
  ┌────────────────────────────────────────────────────────────┐
  │                   CONTROLLER LAYER                         │
  │     LoginServlet, DashboardServlet, VehicleServlet,        │
  │           CustomerServlet, RentalServlet, AuthFilter       │
  └─────────────────────────────┬──────────────────────────────┘
                                │ Calls DAO Methods
                                ▼
  ┌────────────────────────────────────────────────────────────┐
  │                    DATA ACCESS LAYER                       │
  │         UserDAO, VehicleDAO, CustomerDAO, RentalDAO        │
  └─────────────────────────────┬──────────────────────────────┘
                                │ PreparedStatement & Transactions
                                ▼
  ┌────────────────────────────────────────────────────────────┐
  │                  DATABASE UTILITY LAYER                    │
  │              DBConnection (Loads db.properties)            │
  └─────────────────────────────┬──────────────────────────────┘
                                │ JDBC Driver (MySQL Connector/J)
                                ▼
  ┌────────────────────────────────────────────────────────────┐
  │                     MYSQL DATABASE                         │
  │           vehicle_rental_management_system                 │
  │         (tables: users, vehicles, customers, rentals)      │
  └────────────────────────────────────────────────────────────┘
```

### Communication Flow:
1. **Request**: The user interacts with the UI (e.g., clicks "Create Rental").
2. **Controller (Servlet)**: The corresponding Servlet (`RentalServlet`) receives the HTTP request, parses form parameters, validates inputs, and ensures security.
3. **Model & DAO**: The Servlet instantiates Model objects (`Rental`, `Vehicle`) and calls DAO methods (`RentalDAO.createRental()`).
4. **JDBC Execution**: The DAO acquires a `Connection` from `DBConnection`, executes SQL queries using parameterized `PreparedStatement`s, and handles transactions (`commit()` / `rollback()`).
5. **Response / View**: The Servlet stores result data as request attributes and forwards the request to the appropriate JSP view (or redirects via Post-Redirect-Get).
6. **Rendering**: The JSP dynamically displays data using Expression Language (`${...}`) and CSS badges without embedded Java scriptlet mess.

---

## 6. Database Design

Database Name: `vehicle_rental_management_system`

### Table 1: `users`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| `password` | VARCHAR(255) | NOT NULL | Login password |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation timestamp |

### Table 2: `vehicles`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `vehicle_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique vehicle ID |
| `vehicle_number` | VARCHAR(20) | UNIQUE, NOT NULL | License plate / Registration |
| `vehicle_name` | VARCHAR(100) | NOT NULL | Make & model display name |
| `vehicle_type` | VARCHAR(50) | NOT NULL | Category (Sedan, SUV, EV, etc.) |
| `brand` | VARCHAR(50) | NULL | Vehicle manufacturer |
| `model` | VARCHAR(50) | NULL | Model year and trim |
| `rental_price` | DECIMAL(10,2) | NOT NULL | Daily rental rate |
| `availability_status` | VARCHAR(20) | DEFAULT 'AVAILABLE' | 'AVAILABLE' or 'RENTED' |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Date vehicle registered |

### Table 3: `customers`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `customer_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique customer identifier |
| `name` | VARCHAR(100) | NOT NULL | Customer full name |
| `phone` | VARCHAR(15) | NOT NULL | Contact telephone number |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| `address` | VARCHAR(255) | NULL | Billing or street address |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration date |

### Table 4: `rentals`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `rental_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique rental contract ID |
| `customer_id` | INT | NOT NULL, FK -> customers | Customer who rented |
| `vehicle_id` | INT | NOT NULL, FK -> vehicles | Vehicle hired |
| `rental_date` | DATE | NOT NULL | Start date of rental |
| `return_date` | DATE | NULL | Date vehicle returned |
| `total_amount` | DECIMAL(10,2) | NULL | Calculated total fee |
| `rental_status` | VARCHAR(20) | DEFAULT 'ACTIVE' | 'ACTIVE' or 'RETURNED' |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Transaction timestamp |

**Foreign Key Integrity Constraints:**
- `fk_rentals_customer`: `FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT`
- `fk_rentals_vehicle`: `FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE RESTRICT`

---

## 7. Project Folder Structure

```
vehicle-rental-management-system/
├── pom.xml                               # Maven project descriptor & dependencies
├── README.md                             # Comprehensive documentation & setup guide
├── build.bat                             # One-click Windows build script
├── deploy-to-tomcat.bat                  # One-click build, deploy, & run script
│
├── database/
│   └── vehicle_rental_management_system.sql  # Complete schema & sample data SQL
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── vehiclerental/
        │           ├── model/
        │           │   ├── User.java         # User model
        │           │   ├── Vehicle.java      # Vehicle entity
        │           │   ├── Customer.java     # Customer entity
        │           │   └── Rental.java       # Rental entity & join fields
        │           ├── dao/
        │           │   ├── UserDAO.java      # Login & authentication queries
        │           │   ├── VehicleDAO.java   # Vehicle CRUD & count queries
        │           │   ├── CustomerDAO.java  # Customer CRUD & queries
        │           │   └── RentalDAO.java    # Atomic rental & return transactions
        │           ├── servlet/
        │           │   ├── AuthFilter.java       # Session authentication filter
        │           │   ├── LoginServlet.java     # Login controller (/login)
        │           │   ├── RegisterServlet.java  # Customer registration controller (/register)
        │           │   ├── LogoutServlet.java    # Logout controller (/logout)
        │           │   ├── DashboardServlet.java # Metric dashboard controller (/dashboard)
        │           │   ├── VehicleServlet.java   # Vehicle CRUD controller (/vehicles)
        │           │   ├── CustomerServlet.java  # Customer CRUD controller (/customers)
        │           │   └── RentalServlet.java    # Rental operations controller (/rentals)
        │           └── util/
        │               └── DBConnection.java # JDBC connection manager & config
        │
        ├── resources/
        │   └── db.properties             # Database credentials (URL, User, Password)
        │
        └── webapp/
            ├── WEB-INF/
            │   └── web.xml               # Deployment descriptor
            ├── index.html               # Public vehicle fleet catalog
            ├── login.jsp                 # Admin/customer login and customer registration
            ├── dashboard.jsp             # Executive KPI dashboard
            ├── customer/
            │   └── dashboard.jsp        # Session-protected customer dashboard
            ├── vehicles/
            │   ├── list.jsp              # Fleet table, search & filters
            │   ├── add.jsp               # Add vehicle form
            │   └── edit.jsp              # Edit vehicle form
            ├── customers/
            │   ├── list.jsp              # Customer directory & search
            │   ├── add.jsp               # Register customer form
            │   └── edit.jsp              # Edit customer form
            ├── rentals/
            │   ├── list.jsp              # Active & historical rentals
            │   ├── add.jsp               # New rental form with dynamic calculation
            │   └── return.jsp            # Vehicle return confirmation & settlement
            ├── css/
            │   └── style.css             # Unified modern stylesheet
            └── js/
                └── validation.js         # Client-side validation & live calculator
```

---

## 8. Database Setup Instructions

### 8.1 How to Install MySQL
1. Download MySQL Community Server (8.0+) from [mysql.com/downloads](https://dev.mysql.com/downloads/mysql/).
2. Run the MySQL installer, choose "Server only" or "Developer Default", set a root password (e.g. `root`), and complete installation.
3. Ensure the Windows MySQL service is running (e.g. `MySQL80`).

### 8.2 How to Create the Database & Load Data

#### Method A: Using the Automated JavaScript Database Injector (Recommended)
You can inject all necessary tables, constraints, and seed data directly into the MySQL CLI backend using the JavaScript injector (`database/inject-database.js`). It reads schema and records directly from `database/database-details.js` and automatically detects the local MySQL CLI binary.

- **Option 1 (One-Click Batch Runner)**:
  ```cmd
  inject-database.bat
  ```
  *(Or with fresh reset)*:
  ```cmd
  inject-database.bat --fresh
  ```

- **Option 2 (Using Native Windows JavaScript Host - No Node.js required)**:
  ```cmd
  cscript //nologo database\inject-database.js
  ```

- **Option 3 (Using Node.js if installed)**:
  ```bash
  node database/inject-database.js
  ```

#### Method B: Direct MySQL CLI Pipe
Open Command Prompt or PowerShell and pipe the `.sql` file directly:

```bash
mysql -u root -p < "database\vehicle_rental_management_system.sql"
```
*(Enter your MySQL root password when prompted).*

#### Method C: MySQL Workbench
Open **MySQL Workbench**, open `database/vehicle_rental_management_system.sql`, and click **Execute (Lightning icon)**.

### 8.3 How to Configure Database Username & Password
Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties`, then set your local database password:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/vehicle_rental_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_LOCAL_MYSQL_PASSWORD
```
The local `db.properties` file is ignored by Git. Set `database/database-details.js`'s connection password locally before running the database injector.

---

## 9. How to Build and Deploy to Apache Tomcat

### Prerequisites
- **JDK 17 or JDK 21** installed and configured (`JAVA_HOME` set).
- **Apache Maven 3.9+** (or use the Maven located in the scratch directory).
- **Apache Tomcat 10.1+** (for Jakarta EE 10 compatibility).

### Method 1: Using the Automated Batch Script (Fastest)
1. Double-click `build.bat` to compile and create `target/vehicle-rental.war`.
2. Double-click `deploy-to-tomcat.bat` to copy the WAR to Tomcat and launch the server.

### Method 2: Standard Command Line
1. Open terminal inside `vehicle-rental-management-system/`:
   ```bash
   mvn clean package
   ```
2. Copy `target/vehicle-rental.war` into your Tomcat `webapps/` folder:
   ```bash
   copy target\vehicle-rental.war C:\path\to\apache-tomcat-10.1.x\webapps\
   ```
3. Start Tomcat:
   ```bash
   C:\path\to\apache-tomcat-10.1.x\bin\startup.bat
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:8080/vehicle-rental/
   ```

### Method 3: In Eclipse / IntelliJ IDEA / NetBeans
1. Open the project as an **Existing Maven Project**.
2. Add a new Server Configuration pointing to **Apache Tomcat 10.1.x**.
3. Add `vehicle-rental-management-system` to Tomcat deployments.
4. Click **Run / Debug**.

---

## 10. Sample Login Setup

| Role | Username | Password | Notes |
|---|---|---|---|
| **System Administrator** | `admin` | Set before importing | Replace `CHANGE_ME_BEFORE_USE` in the sample database data with a unique password before importing. |

---

## 11. Step-by-Step Testing Guide

Follow these 15 test scenarios to demonstrate system functionality during review:

| # | Test Scenario | Steps | Expected Result |
|---|---|---|---|
| **1** | **Public Fleet Catalog** | Visit `http://localhost:8080/vehicle-rental/` | Available and rented vehicles display with photos, categories, and prices. |
| **2** | **Session Protection** | Attempt accessing `http://localhost:8080/vehicle-rental/dashboard` or `dashboard.html` directly | AuthFilter intercepts and redirects to `/login?error=auth_required`. |
| **3** | **Invalid Admin Login** | Enter `admin` / `wrongpass` on login page | Displays an invalid administrator credentials error. |
| **4** | **Successful Login** | Use the admin password configured before importing sample data. | Redirects to `/dashboard` with greeting and summary cards. |
| **5** | **Executive Dashboard** | Check KPI numbers | Displays accurate counts of total vehicles, available vehicles, rented vehicles, customers, and rentals. |
| **6** | **Add Vehicle** | Go to Vehicles > "Add New Vehicle", enter details, submit | New vehicle is saved, appears in list with status `AVAILABLE`. |
| **7** | **Duplicate Vehicle Validation** | Try adding another vehicle with an existing plate number | Rejection with message: "A vehicle with number '...' already exists." |
| **8** | **Update Vehicle** | Click "Edit" on a vehicle, change price or details, save | Updated details reflect immediately. |
| **9** | **Search Vehicle** | Enter "SUV" or a plate number into search box | Table dynamically filters to matching records. |
| **10** | **Add Customer** | Go to Customers > "Register Customer", enter name, phone, email | New customer is saved. Email format and duplicate checks are enforced. |
| **11** | **Create Rental** | Go to Rentals > "Create New Rental", select customer, available vehicle, dates | Vehicle availability status flips to `RENTED`. Total price auto-calculated. |
| **12** | **Rental Availability Rule** | Try renting the same vehicle again | The vehicle no longer appears in the "Available Vehicles" dropdown. |
| **13** | **Return Vehicle** | Click "Return Vehicle" on an active rental, pick return date | Rental status updates to `RETURNED`. Associated vehicle status flips back to `AVAILABLE`. |
| **14** | **Cascade Safety Check** | Attempt to delete a vehicle with an active rental | Deletion blocked with error message: "Cannot delete vehicle: assigned to an ACTIVE rental." |
| **15** | **Logout** | Click "Logout" in navbar | Session is invalidated; redirects to login page with success notification. |

Customer authentication can also be verified by opening `/login?role=register`, creating a customer account, and then signing in through `/login?role=customer`. The customer dashboard is available only through the authenticated `/customer/dashboard` route.

---

## 12. Important Files to Highlight in Viva / Project Review

When presenting this project to evaluators:

1. **`DBConnection.java` (`util`)**:
   - Explain how JDBC driver is loaded and connection properties are loaded from `db.properties`.
   - Mention resource management using `try-with-resources` or `DBConnection.close()` to prevent memory and connection leaks.
2. **`RentalDAO.java` (`dao`)**:
   - Highlight the **Database Transaction**:
     ```java
     conn.setAutoCommit(false);
     // 1. Insert rental record
     // 2. Update vehicle availability_status to 'RENTED'
     conn.commit();
     ```
   - Explain why transactions are mandatory to keep the rental record and vehicle inventory synchronized.
3. **`AuthFilter.java` (`servlet`)**:
   - Explain how Jakarta Filter intercepts requests to `/dashboard`, `/vehicles/*`, `/customers/*`, and `/rentals/*` to protect against unauthorized URL tampering.
4. **`RentalServlet.java` (`servlet`)**:
   - Explain the calculation of rental duration using Java `ChronoUnit.DAYS.between()` and daily price multiplication.
5. **`validation.js` (`webapp/js`)**:
   - Explain two-tier validation: instant client-side feedback in JavaScript for better user experience, backed by strict server-side validation in Servlets.
