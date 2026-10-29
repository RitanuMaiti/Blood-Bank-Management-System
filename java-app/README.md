# Blood Bank Management System - Java Application

JavaFX frontend and Java backend for the Blood Bank Management System.

## Technologies
- Java 21 LTS
- JavaFX 21
- Maven
- Oracle JDBC (`ojdbc11`)
- JUnit 5 & H2 (for isolated in-memory test suite)

## Quick Start

### 1. Database Configuration
Copy `db.properties.example` to `db.properties` and edit credentials, or set environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### 2. Run Tests
Execute the 45 automated unit & integration tests:

    mvn test

### 3. Compile and Run Application

    mvn clean compile
    mvn javafx:run
