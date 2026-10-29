# Blood Bank Management System

A database-driven desktop application designed to manage blood donors, donations, hospital records, blood inventory, and blood requests through a JavaFX interface and Oracle SQL database.

## Overview

The **Blood Bank Management System** aims to simplify essential blood bank operations by organizing donor information, tracking donations, maintaining blood inventory, and managing hospital blood requests.

The project combines a JavaFX desktop application with database scripts and a layered Java architecture to support structured data management.

## Features

* **Donor Management** — Maintain and manage donor records.
* **Donation Management** — Record blood donations and associated information.
* **Hospital Management** — Organize hospital records.
* **Blood Inventory Management** — Manage blood stock information.
* **Blood Request Management** — Handle blood requests through the application.
* **JavaFX Interface** — Desktop interface built using JavaFX and FXML.
* **Database Integration** — SQL scripts for schema creation, sample data, and database logic.
* **Layered Architecture** — Separation of controllers, data-access objects, models, and services.

## Tech Stack

| Technology      | Purpose                         |
| --------------- | ------------------------------- |
| Java 21         | Application development         |
| JavaFX          | Desktop user interface          |
| FXML            | UI layout                       |
| Maven           | Build and dependency management |
| Oracle Database | Relational data storage         |
| SQL             | Database schema and operations  |

## Project Structure

```text
Blood-Bank-Management-System/
├── database/
│   ├── 01_schema.sql
│   ├── 02_seed.sql
│   └── 03_logic.sql
│
├── java-app/
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       └── main/
│           ├── java/bloodbank/
│           │   ├── controller/
│           │   ├── dao/
│           │   ├── model/
│           │   └── service/
│           └── resources/bloodbank/
│               ├── dashboard.fxml
│               ├── donors.fxml
│               ├── donations.fxml
│               ├── hospitals.fxml
│               ├── inventory.fxml
│               └── requests.fxml
│
└── README.md
```

## Prerequisites

Before running the Java application, ensure you have:

* JDK 21
* Apache Maven
* Oracle Database configured for the supplied SQL scripts
* SQL*Plus or another compatible database client

Verify your Java and Maven installations:

```bash
java -version
mvn -version
```

## Setup and Execution

### 1. Clone the repository

```bash
git clone https://github.com/RitanuMaiti/Blood-Bank-Management-System.git
cd Blood-Bank-Management-System
```

### 2. Configure the database

Review the SQL scripts in the `database/` directory and execute them in the intended order against your Oracle database.

**Important:** Inspect scripts before execution, particularly any statements that drop or recreate tables. Back up existing data before running destructive scripts.

### 3. Configure database connectivity

Open:

`java-app/src/main/java/bloodbank/DBConnection.java`

Configure the Oracle JDBC URL and database credentials for your local environment. Never commit real passwords or other secrets to the repository.

### 4. Compile the application

```bash
cd java-app
mvn clean compile
```

### 5. Launch the application

```bash
mvn javafx:run
```

Database-dependent functionality requires a valid database configuration and a compatible schema.

## Architecture

The Java application is organized into distinct layers:

* **Controllers:** Handle user interactions with the interface.
* **DAO Layer:** Manages database access.
* **Models:** Represent application entities.
* **Service Layer:** Organizes application operations and business logic.
* **FXML Resources:** Define the JavaFX screens.

This structure improves code organization and separates interface logic from database operations.

## Future Enhancements

Potential areas for further development include:

* Stronger blood-group compatibility and allocation validation.
* Improved input validation and error handling.
* Transaction management and data consistency checks.
* Automated unit and integration testing.
* Inventory analytics and reporting.
* Secure external configuration for database credentials.

## Contributors

Developed as a collaborative academic project.

* **Ritanu Maiti**
* **Soham Roy**

## License

No license has been specified for this repository. Please contact the repository maintainers regarding usage and redistribution permissions.
