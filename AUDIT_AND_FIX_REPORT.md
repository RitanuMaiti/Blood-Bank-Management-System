# Blood Bank Management System — Audit and Fix Report

## Changes made

1. **Conservative whole-blood compatibility:** Java and Oracle PL/SQL now require an exact ABO/Rh match for whole blood. Whole blood contains both donor red cells and donor plasma, so it is not treated as universally compatible O-negative blood without a validated low-titer group O protocol. Red-cell components retain red-cell compatibility rules.
2. **SQL component aliases:** The Oracle allocation procedure now normalizes aliases such as `Plasma` / `Fresh Frozen Plasma` and `Packed Red Cells` / `Red Blood Cells` before selecting inventory, matching the Java service's normalization behavior.
3. **Donation trigger hardening:** The cooldown trigger locks the donor row before checking donation history, reducing the risk of concurrent donations bypassing the 90-day rule. It also rejects donors marked `Permanent_Deferral` at the database layer.
4. **Seed-data consistency:** The FFP sample now belongs to an AB+ donor's own donation. Sample whole-blood and red-cell collection/expiry dates were corrected to use a plausible 42-day refrigerated shelf-life window, and the sample donations respect the cooldown rule as of the sample dates.
5. **Oracle identity sequences:** The seed script advances identity high-water marks after inserting explicit demonstration IDs, so subsequent application-created rows can receive IDs above the seeded rows on the documented Oracle versions.
6. **Reliable request IDs in tests:** `RequestDAO.addAndReturnId` returns the generated request ID. Allocation and status tests no longer infer a newly created request by taking the first row of a sorted list.
7. **Concurrent request-status updates:** `RequestService` uses a conditional update against the status it originally read. A stale status update now fails rather than overwriting a status changed by a concurrent allocation or user action.
8. **Regression coverage and documentation:** Added tests for conservative whole-blood compatibility and stale status updates; updated the README test counts and compatibility policy.

## Important clarification

The Java plasma compatibility rules were rechecked during this audit. Their ABO logic was already correct: AB plasma is the universal ABO plasma donor, and group O recipients can generally receive plasma from any ABO group. The earlier preliminary concern that these rules were reversed was incorrect. The actual compatibility change made in this pass concerns **whole blood**, not plasma.

## Verification performed

- Compiled the modified Java production classes (`MatchingService`, `RequestDAO`, and `RequestService`) with Java 21 successfully.
- Ran 8 standalone Java compatibility smoke checks, including plasma compatibility, red-cell compatibility, conservative whole-blood matching, and component aliases; all 8 passed.
- The full Maven/JUnit suite was **not rerun** in this environment because Maven and the dependency cache are unavailable here. Previous `target/` build artifacts and old test reports were removed so they cannot be mistaken for results from the revised source.
- Oracle SQL scripts were reviewed and edited but **not executed** against an Oracle instance. They must be tested against a disposable development schema before use.

## Safe next steps

From the `java-app` directory on a machine with JDK 21 and Maven installed, run:

```powershell
mvn test
mvn clean compile
```

Only after the tests pass, configure a **disposable development Oracle schema** and run the SQL scripts in order:

1. `database/01_schema.sql`
2. `database/02_seed.sql`
3. `database/03_logic.sql`

Do not run `database/00_reset_dev_only.sql` against any database containing data you need. It is destructive and intended only for an isolated development database.

This is an academic project baseline, not a clinical transfusion decision system. Any real-world use requires validation against local transfusion standards and review by qualified clinical personnel.
