📦 StockBridge Backend
🧾 Overview

StockBridge Backend is a Spring Boot REST API designed for a multi-branch inventory and stock transfer management system.

It provides secure services for inventory tracking, inter-branch stock transfers, multi-level approvals, and centralized administration across distributed locations.

🎯 System Purpose

The system is built to:

Manage inventory across multiple branches in real time
Track stock levels per location accurately
Handle inter-branch stock transfer workflows
Support multi-stage approval processes
Provide secure role-based access control for all operations
👥 User Roles
🏪 Branch Staff
Create stock transfer requests
View local branch inventory
🧑‍💼 Branch Manager
Review and approve or reject transfer requests (first approval level)
🏢 Head Office / Inventory Control
Perform final approval of transfers
Oversee global stock movement and consistency
⚙️ System Administrator
Manage users, roles, and branch configurations
Maintain system-wide control and monitoring
🔁 Transfer Workflow

Stock movement follows a controlled lifecycle:

Transfer request created by branch staff
Branch manager review and decision
Head office final approval
Stock movement between branches
Inventory updates and confirmation
🛠️ Technology Stack
Java 17+
Spring Boot
Spring Security
JWT Authentication
Spring Data JPA
Flyway Database Migrations
MySQL / PostgreSQL
Maven
🗄️ Database Management

This system uses Flyway for structured database version control.

All migrations are stored in db/migration
Schema changes are versioned and traceable
Migrations run automatically on application startup
🔐 Security
JWT-based authentication system
Role-based access control (RBAC)
Password encryption using BCrypt
Protected REST endpoints for secure operations
📡 Core Modules
Authentication
Login
Token-based session management
Inventory Management
Create and manage inventory items
Track stock per branch
Monitor low-stock conditions
Stock Transfers
Create transfer requests between branches
Multi-level approval workflow
Track full transfer lifecycle
User & Branch Management
Manage system users
Assign roles and permissions
Configure branch structure
📊 Transfer Status Lifecycle
PENDING
APPROVED_BY_MANAGER
APPROVED_BY_HO
IN_TRANSIT
RECEIVED
COMPLETED
🚀 Setup Instructions
# Clone repository
git clone https://github.com/yvesmugisha901/stockbridge-backend.git

# Navigate into project
cd stockbridge-backend

# Build project
mvn clean install

# Run application
mvn spring-boot:run
🧪 Database Migration
mvn flyway:migrate
