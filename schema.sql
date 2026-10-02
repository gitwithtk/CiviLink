-- ============================================================
--  CiviLink Database Schema  (SQL Server)
--  Run this script once to create all tables
-- ============================================================

CREATE DATABASE CiviLinkDB;
GO
USE CiviLinkDB;
GO

-- ── Users (base table for all roles) ──────────────────────
CREATE TABLE Users (
    userID       INT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(100)  NOT NULL,
    email        NVARCHAR(150)  NOT NULL UNIQUE,
    phone        NVARCHAR(20),
    username     NVARCHAR(80)   NOT NULL UNIQUE,
    passwordHash NVARCHAR(256)  NOT NULL,
    role         NVARCHAR(20)   NOT NULL CHECK (role IN ('CITIZEN','OFFICER','MANAGER')),
    isActive     BIT            NOT NULL DEFAULT 1,
    createdAt    DATETIME       NOT NULL DEFAULT GETDATE()
);
GO

-- ── Officer extra data ─────────────────────────────────────
CREATE TABLE Officers (
    officerID  INT PRIMARY KEY REFERENCES Users(userID),
    department NVARCHAR(100) NOT NULL
);
GO

-- ── Complaint categories ───────────────────────────────────
CREATE TABLE ComplaintCategories (
    categoryID   INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- ── SLA rules per category ─────────────────────────────────
CREATE TABLE SLA (
    slaID       INT IDENTITY(1,1) PRIMARY KEY,
    categoryID  INT NOT NULL REFERENCES ComplaintCategories(categoryID),
    defaultDays INT NOT NULL DEFAULT 7
);
GO

-- ── Complaints ─────────────────────────────────────────────
CREATE TABLE Complaints (
    complaintID      INT IDENTITY(1,1) PRIMARY KEY,
    trackingID       NVARCHAR(20)   NOT NULL UNIQUE,
    citizenID        INT            NOT NULL REFERENCES Users(userID),
    categoryID       INT            NOT NULL REFERENCES ComplaintCategories(categoryID),
    description      NVARCHAR(MAX)  NOT NULL,
    location         NVARCHAR(200)  NOT NULL,
    submissionDate   DATETIME       NOT NULL DEFAULT GETDATE(),
    expectedResDate  DATE,
    status           NVARCHAR(30)   NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','ASSIGNED','IN_PROGRESS','AWAITING','RESOLVED','REOPENED','CLOSED')),
    isOverdue        BIT            NOT NULL DEFAULT 0
);
GO

-- ── Complaint timeline ─────────────────────────────────────
CREATE TABLE ComplaintTimeline (
    timelineID   INT IDENTITY(1,1) PRIMARY KEY,
    complaintID  INT           NOT NULL REFERENCES Complaints(complaintID),
    status       NVARCHAR(30)  NOT NULL,
    updateTime   DATETIME      NOT NULL DEFAULT GETDATE(),
    notes        NVARCHAR(MAX)
);
GO

-- ── Assignments ────────────────────────────────────────────
CREATE TABLE Assignments (
    assignmentID  INT IDENTITY(1,1) PRIMARY KEY,
    complaintID   INT      NOT NULL REFERENCES Complaints(complaintID),
    officerID     INT      NOT NULL REFERENCES Users(userID),
    managerID     INT      NOT NULL REFERENCES Users(userID),
    assignedDate  DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- ── Notifications ──────────────────────────────────────────
CREATE TABLE Notifications (
    notificationID INT IDENTITY(1,1) PRIMARY KEY,
    userID         INT            NOT NULL REFERENCES Users(userID),
    message        NVARCHAR(500)  NOT NULL,
    date           DATETIME       NOT NULL DEFAULT GETDATE(),
    status         NVARCHAR(10)   NOT NULL DEFAULT 'UNREAD' CHECK (status IN ('UNREAD','READ'))
);
GO

-- ── Reports ────────────────────────────────────────────────
CREATE TABLE Reports (
    reportID       INT IDENTITY(1,1) PRIMARY KEY,
    managerID      INT           NOT NULL REFERENCES Users(userID),
    reportType     NVARCHAR(50)  NOT NULL,
    generatedDate  DATETIME      NOT NULL DEFAULT GETDATE(),
    status         NVARCHAR(20)  NOT NULL DEFAULT 'GENERATED'
);
GO

-- ── Seed: categories + SLAs ───────────────────────────────
INSERT INTO ComplaintCategories (categoryName) VALUES
    ('Sanitation'),('Road Damage'),('Water Supply'),
    ('Electricity'),('Street Lighting'),('Noise Complaint'),('Other');

INSERT INTO SLA (categoryID, defaultDays) VALUES
    (1,5),(2,10),(3,7),(4,3),(5,7),(6,2),(7,7);

-- ── Seed: demo manager ────────────────────────────────────
-- password = "admin123" (store hashed in production)
INSERT INTO Users (name,email,phone,username,passwordHash,role)
VALUES ('Admin Manager','admin@civilink.pk','0300-0000000','admin','admin123','MANAGER');
GO


SELECT * FROM COMPLAINTS
SELECT * FROM USERS
SELECT * FROM Assignments
SELECT * FROM ComplaintCategories
SELECT * FROM ComplaintTimeline
SELECT * FROM Notifications
SELECT * FROM Officers
SELECT * FROM Reports
SELECT * FROM SLA
SELECT * FROM User