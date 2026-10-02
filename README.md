# CiviLink – Public Service Delivery MIS
**SDA Project | FAST NUCES Islamabad | Section CS-C**
**Team: Taimiah Khalid (24i-0626) | Nimra Zafar (24i-0891)**

---

## Project Structure

```
CiviLink/
├── schema.sql                         ← Run this FIRST in SQL Server
│
└── src/
    └── civilink/
        ├── main/
        │   └── Main.java              ← Application entry point
        │
        ├── database/
        │   ├── DBHandler.java         ← Singleton DB connection (GoF)
        │   ├── UserDAO.java           ← User/Officer/Manager CRUD
        │   ├── ComplaintDAO.java      ← Complaint + Timeline CRUD
        │   └── DAOs.java              ← Assignment + Notification + Report DAOs
        │
        ├── model/
        │   ├── User.java              ← Abstract base (Abstraction)
        │   ├── Citizen.java           ← Extends User
        │   ├── Officer.java           ← Extends User
        │   ├── Manager.java           ← Extends User
        │   ├── Complaint.java         ← Core domain entity
        │   ├── ComplaintTimeline.java ← Timeline entry
        │   ├── Assignment.java        ← Complaint-Officer link
        │   └── SupportModels.java     ← Notification, Report, SLA, Category
        │
        ├── service/
        │   └── NotificationService.java ← GoF Service/Observer, Singleton
        │
        ├── controller/
        │   ├── AuthController.java    ← Login, Register, Officer management
        │   ├── ComplaintController.java ← UC1-4, UC6-7, UC10
        │   └── AssignmentController.java ← UC8-9 + ReportController (UC11)
        │
        └── ui/
            ├── LoginScreen.java       ← JavaFX login (extends Application)
            ├── RegisterScreen.java    ← Citizen registration
            ├── UIUtils.java           ← Shared UI helpers
            ├── CitizenDashboard.java  ← UC1 Submit, UC2 Track, UC3 Timeline, UC4 Reopen
            ├── OfficerDashboard.java  ← UC6 View Assigned, UC7 Update Status
            └── ManagerDashboard.java  ← UC8 Assign, UC9 Workload, UC10 Overdue,
                                          UC11 Reports, UC12 Officer Accounts
```

---

## Setup Instructions

### 1. Database Setup
1. Open **SQL Server Management Studio**
2. Run `schema.sql` — creates `CiviLinkDB` with all tables and seed data
3. Default admin login created: username=`admin`, password=`admin123`

### 2. Update DB Credentials
Edit `src/civilink/database/DBHandler.java`:
```java
private static final String SERVER   = "localhost";    // or your SQL Server IP
private static final String PORT     = "1433";
private static final String DATABASE = "CiviLinkDB";
private static final String USER     = "sa";
private static final String PASSWORD = "YourPassword123";
```

### 3. Add Dependencies (IntelliJ / Eclipse)
Add these JARs to your project classpath:
- **JavaFX SDK** – download from https://gluonhq.com/products/javafx/
  - Add: `javafx.controls`, `javafx.fxml`, `javafx.graphics`
- **SQL Server JDBC Driver** – download from Microsoft
  - JAR: `mssql-jdbc-12.x.x.jre11.jar`

### 4. VM Arguments (IntelliJ Run Config)
```
--module-path "C:\path\to\javafx-sdk\lib"
--add-modules javafx.controls,javafx.fxml
```

### 5. Run
Run `civilink.main.Main` as the main class.

---

## Use Cases Implemented

| # | Use Case | Role | Screen |
|---|----------|------|--------|
| 1 | Submit Complaint | Citizen | CitizenDashboard |
| 2 | View Complaint Status | Citizen | CitizenDashboard |
| 3 | View Complaint Timeline | Citizen + Officer | CitizenDashboard / OfficerDashboard |
| 4 | Reopen Complaint | Citizen | CitizenDashboard |
| 5 | Login | All | LoginScreen |
| 6 | View Assigned Complaints | Officer | OfficerDashboard |
| 7 | Update Complaint Status | Officer | OfficerDashboard |
| 8 | Assign Complaint to Officer | Manager | ManagerDashboard |
| 9 | Monitor Officer Workload | Manager | ManagerDashboard |
| 10 | Monitor & Flag Overdue | System/Manager | Auto on startup + ManagerDashboard |
| 11 | Generate Performance Reports | Manager | ManagerDashboard |
| 12 | Manage Officer Accounts | Manager | ManagerDashboard |

---

## Design Patterns Applied

| Pattern | Type | Where |
|---------|------|-------|
| Singleton | GoF Creational | `DBHandler`, `NotificationService`, all Controllers |
| Observer (Service) | GoF Behavioural | `NotificationService` decoupled from controllers |
| Controller | GRASP | `AuthController`, `ComplaintController`, `AssignmentController`, `ReportController` |
| Information Expert | GRASP | Each DAO is expert on its own table |
| Creator | GRASP | Controllers create domain objects (Complaint, Citizen, Officer) |
| Low Coupling | GRASP | Controllers call `NotificationService`, never `NotificationDAO` directly |
| High Cohesion | GRASP | Each class has one clear responsibility |
| Protected Variation | GRASP | `User` abstract class shields UI from role changes |

---

## OOP Principles

| Principle | Where |
|-----------|-------|
| **Abstraction** | `User` abstract class with `getRole()` abstract method |
| **Inheritance** | `Citizen`, `Officer`, `Manager` extend `User` |
| **Polymorphism** | `getRole()` overridden in each subclass; `User` reference used in login |
| **Encapsulation** | All model fields private, accessed via getters/setters |

---

## Architecture: 3-Layer Separation

```
┌─────────────────────────────────┐
│  UI Layer  (civilink.ui)        │  JavaFX screens only – no SQL here
│  Calls controllers only         │
├─────────────────────────────────┤
│  Business Logic (civilink.      │  Controllers + Models + Service
│  controller / model / service)  │  Input validation, domain logic
├─────────────────────────────────┤
│  Data Layer  (civilink.database)│  DAOs + DBHandler – SQL only here
│  JDBC + SQL Server              │
└─────────────────────────────────┘
```

---

## Default Test Accounts

After running `schema.sql`:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Manager |

Register new citizens via the UI. Create officers via Manager → Manage Officers.
