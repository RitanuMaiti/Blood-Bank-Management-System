# Blood Bank Management System

A robust, database-driven desktop application engineered to manage blood donors, donations, hospital accounts, inventory stock, and emergency blood requests through a JavaFX interface and an Oracle Database backend.

---

## Architecture Overview

The system follows a strict multi-tiered enterprise architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                 JavaFX User Interface Layer                 │
│   (Dashboard, Donors, Donations, Inventory, Hospitals,      │
│               Emergency Requests & Allocation)              │
└──────────────────────────────┬──────────────────────────────┘
                               │ User Interactions & Events
┌──────────────────────────────▼──────────────────────────────┐
│                   Controller & UI Wiring                    │
│   (DashboardController, DonorController, DonationController,│
│    InventoryController, HospitalController, RequestController)│
└──────────────────────────────┬──────────────────────────────┘
                               │ Business Operations
┌──────────────────────────────▼──────────────────────────────┐
│                    Service & Business Logic                 │
│   (MatchingService, DonorService, DonationService,           │
│    InventoryService, HospitalService, RequestService)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Parameterized JDBC Calls
┌──────────────────────────────▼──────────────────────────────┐
│                   Data Access Object (DAO)                  │
│   (BloodUnitDAO, DonationDAO, DonorDAO, HospitalDAO,        │
│                         RequestDAO)                         │
└──────────────────────────────┬──────────────────────────────┘
                               │ Oracle JDBC / Connection Pool
┌──────────────────────────────▼──────────────────────────────┐
│                  Relational Database Layer                  │
│    (Oracle Database 19c/21c/23ai / H2 In-Memory for Tests)  │
│  - Tables: DONOR, HOSPITAL, DONATION_RECORD, BLOOD_INVENTORY│
│            EMERGENCY_REQUEST, ALLOCATION_DISPATCH           │
│  - Triggers: trg_check_cooldown (90-day cooldown enforcement│
│  - Procedures: allocate_blood (concurrency-safe allocation) │
│  - Functions: is_blood_compatible (component-aware rules)   │
│  - Views: v_low_inventory_alert (stock monitoring)          │
└─────────────────────────────────────────────────────────────┘
```

---

## Required Software & Versions

| Component | Required Version | Notes |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | Java 21 LTS | Verified with Eclipse Adoptium OpenJDK 21 |
| **Build Tool** | Apache Maven 3.9+ / 3.10+ | Used for compilation and running tests |
| **Database** | Oracle Database (XE, 19c, 21c, 23ai) | Production / target database |
| **Test Database** | H2 Database (Oracle Mode) | Included in Maven test scope for isolated integration testing |
| **JavaFX** | OpenJFX 21 | Bundled via Maven dependencies |

---

## Database Setup Instructions

The database scripts are organized in `database/` in sequential execution order:

```text
database/
├── 00_reset_dev_only.sql   # OPTIONAL / DEV ONLY: Destructive table drops with warnings
├── 01_schema.sql           # Non-destructive table, constraint, and index creation
├── 02_seed.sql             # Safe sample data respecting donor eligibility & expiry
└── 03_logic.sql            # Compatibility function, cooldown trigger & allocation procedure
```

### Safety Policy
- **Never execute destructive scripts against production.**
- `01_schema.sql` contains only safe `CREATE TABLE` and `CREATE INDEX` statements.
- `00_reset_dev_only.sql` is strictly for isolated local development resets when starting fresh.

### Execution via SQL*Plus or Oracle SQL Developer:

Connect as your application schema user (e.g. `bloodbank`):

```bash
# 1. (Optional - Development only) Reset tables if re-initializing:
sqlplus bloodbank/bloodbank@localhost:1521/XE @database/00_reset_dev_only.sql

# 2. Create the schema (tables, constraints, indexes):
sqlplus bloodbank/bloodbank@localhost:1521/XE @database/01_schema.sql

# 3. Load sample seed data:
sqlplus bloodbank/bloodbank@localhost:1521/XE @database/02_seed.sql

# 4. Deploy triggers, stored procedures, and views:
sqlplus bloodbank/bloodbank@localhost:1521/XE @database/03_logic.sql
```

---

## Configuration Instructions

Database credentials and connection URLs are externalized to prevent hardcoding secrets in source code.

Configuration precedence:
1. **JVM System Properties:** `-Ddb.url=... -Ddb.username=... -Ddb.password=...`
2. **Environment Variables:** `DB_URL`, `DB_USERNAME` (or `DB_USER`), `DB_PASSWORD`
3. **Properties File:** `java-app/db.properties` (or on classpath)
4. **Local Fallback:** `jdbc:oracle:thin:@localhost:1521:XE` (`bloodbank`/`bloodbank`)

### Using `db.properties`
Copy the provided template:
```bash
cd java-app
cp db.properties.example db.properties
```
Edit `db.properties`:
```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.username=bloodbank
db.password=your_secure_password
```
*(Note: `db.properties` is git-ignored and will not be committed.)*

---

## Building and Testing

### 1. Run Automated Test Suite (45 Unit & Integration Tests)
The test suite runs in an isolated in-memory Oracle-compatible database environment without requiring an active Oracle instance:

```bash
cd java-app
mvn test
```

Test coverage includes:
- **`BloodCompatibilityTest` (16 tests):** Red-cell compatibility (O- universal red-cell donor, AB+ universal red-cell recipient), conservative exact-match whole-blood compatibility, plasma compatibility (AB universal donor, O universal recipient), platelet matching, alias normalization.
- **`DonorEligibilityTest` (8 tests):** Minimum age (18), 90-day cooldown period, first-time donor eligibility, preceding date rejection, future date rejection, minimum hemoglobin (12.5 g/dL), permanent deferral checks.
- **`RequestStatusLifecycleTest` (7 tests):** Valid transitions (`Pending` -> `Partially_Fulfilled` -> `Fulfilled`), terminal state protection (`Fulfilled` and `Cancelled` cannot be modified or reopened).
- **`AllocationIntegrationTest` (8 tests):** Complete allocation, partial allocation, expired unit exclusion, incompatible group rejection, duplicate allocation prevention, cancelled request rejection, inventory locking.
- **`DaoIntegrationTest` (4 tests):** Hospital, Donor, Donation, Inventory CRUD operations and Foreign-Key constraint enforcement.
- **`FxmlResourceAndWiringTest` (2 tests):** Static verification that all FXML files exist and all `onAction` buttons correctly map to real methods on their respective controllers.

### 2. Compile Application
```bash
cd java-app
mvn clean compile
```

### 3. Launch JavaFX Application
```bash
cd java-app
mvn javafx:run
```

---

## Clinical Transfusion Compatibility Baseline

The system implements component-specific compatibility rules:

### 1. Red-Cell Components (Red Blood Cells, Packed Red Cells)
Based on recipient antibodies against donor red cell surface antigens (A, B, RhD):
- **O-:** Universal cellular donor. Can only receive O-.
- **O+:** Can receive O-, O+.
- **A-:** Can receive O-, A-.
- **A+:** Can receive O-, O+, A-, A+.
- **B-:** Can receive O-, B-.
- **B+:** Can receive O-, O+, B-, B+.
- **AB-:** Can receive O-, A-, B-, AB-.
- **AB+:** Universal cellular recipient. Can receive all groups.

### 2. Acellular / Plasma Components (Fresh Frozen Plasma, Plasma)
Based on donor plasma antibodies against recipient red cells (anti-A, anti-B):
- **AB (AB+, AB-):** Universal plasma donor (plasma has no anti-A or anti-B antibodies).
- **A:** Can donate plasma to A and O recipients.
- **B:** Can donate plasma to B and O recipients.
- **O:** Can donate plasma only to O recipients (plasma contains both anti-A and anti-B).

### 3. Whole Blood
- Conservative policy: require an exact ABO/Rh match because whole blood contains both red cells and donor plasma. The system does not assume that O-negative whole blood is universally compatible; that requires a validated low-titer group O protocol.

### 4. Platelets
- Standard conservative policy: exact ABO/Rh match preferred.

> [!IMPORTANT]
> This academic project establishes a standard baseline. Deployment into a live healthcare environment requires compliance with relevant national transfusion standards (e.g., AABB, WHO, or DGHS guidelines) and validation by a licensed medical director.

---

## Known Limitations & Production Recommendations

1. **Local Oracle Listener:** If Oracle Database is not installed or running locally on port 1521, the application UI opens in offline mode (displaying connection alerts when attempting live queries). Automated tests run independently of local Oracle via in-memory Oracle-mode H2.
2. **Component Shelf-Life Variation:** The default cooldown rule is set to 90 days (standard for whole blood). Apheresis platelet and plasma donors in real-world facilities may be cleared on shorter intervals (e.g., 7–14 days), which can be adjusted via `DonationService.setCooldownDays(...)`.
3. **Connection Pooling:** For high-volume multi-user deployments, upgrading from single JDBC `DriverManager` connections to a connection pool (e.g. HikariCP or Oracle UCP) is recommended.
