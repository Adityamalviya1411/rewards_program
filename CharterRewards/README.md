# Charter Rewards Program

A Spring Boot REST API that calculates reward points earned by customers based on their purchase transactions.

---

## Rewards Rules

| Spend Range      | Points Earned                              |
|------------------|--------------------------------------------|
| $0 – $50         | 0 points                                   |
| $50.01 – $100    | 1 point per dollar above $50               |
| $100.01+         | 50 points + 2 points per dollar above $100 |

**Example:** A $120 purchase = (2 × $20) + (1 × $50) = **90 points**

---

## Project Structure

```
CharterRewards/
├── src/
│   ├── main/
│   │   ├── java/com/charter/rewards/
│   │   │   ├── RewardsApplication.java          # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── RewardsController.java       # REST endpoints (/api/rewards)
│   │   │   ├── service/
│   │   │   │   └── RewardsService.java          # Points calculation business logic
│   │   │   ├── repository/
│   │   │   │   ├── CustomerRepository.java      # JPA repository for customers
│   │   │   │   └── TransactionRepository.java   # JPA repository for transactions
│   │   │   ├── model/
│   │   │   │   ├── Customer.java                # JPA entity – customers table
│   │   │   │   └── Transaction.java             # JPA entity – transactions table
│   │   │   ├── dto/
│   │   │   │   └── CustomerRewardSummary.java   # API response DTO
│   │   │   └── exception/
│   │   │       ├── CustomerNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java  # Centralised error handling
│   │   └── resources/
│   │       ├── application.properties           # PostgreSQL config (credentials via env vars)
│   │       └── db/
│   │           ├── schema.sql                   # DDL – table definitions (reference only)
│   │           └── data.sql                     # DML – 4 customers, 3 months of transactions
│   └── test/
│       ├── java/com/charter/rewards/
│       │   ├── service/
│       │   │   └── RewardsServiceTest.java      # Unit tests (Mockito) – 20+ test cases
│       │   └── controller/
│       │       └── RewardsControllerIntegrationTest.java  # Integration tests (MockMvc + H2)
│       └── resources/
│           ├── application.properties           # H2 in-memory test config
│           └── db/
│               ├── schema.sql                   # H2-compatible DDL
│               └── data.sql                     # Same seed data for integration tests
├── .gitignore
├── pom.xml
└── README.md
```

---

## Prerequisites

| Tool       | Version |
|------------|---------|
| Java       | 17+     |
| Maven      | 3.8+    |
| PostgreSQL | 14+     |

---

## Database Setup

Create the database in PostgreSQL:

```sql
CREATE DATABASE rewards_db;
```

### Set credentials via environment variables (never hard-code passwords)

**Windows CMD:**
```cmd
set DB_USERNAME=postgres
set DB_PASSWORD=<your_db_password>
```

**Windows PowerShell:**
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="<your_db_password>"
```

**macOS / Linux:**
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=<your_db_password>
```

The schema is managed automatically by Hibernate (`ddl-auto=update`).
Sample data is seeded on startup via `src/main/resources/db/data.sql`.

---

## Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Running Tests

Tests use an H2 in-memory database — no PostgreSQL installation required.

```bash
mvn test
```

---

## REST API

### Get rewards for all customers

```
GET /api/rewards?startDate=2024-01-01&endDate=2024-03-31
```

### Get rewards for a specific customer

```
GET /api/rewards/{customerId}?startDate=2024-01-01&endDate=2024-03-31
```

**Date format:** `yyyy-MM-dd` (ISO 8601)

### Sample Response

```json
{
  "customerId": 1,
  "customerName": "Aditya Malviya",
  "pointsByMonth": {
    "2024-01": 115,
    "2024-02": 255,
    "2024-03": 110
  },
  "totalPoints": 480
}
```

### Error Responses

| Scenario                    | HTTP Status | Example message                          |
|-----------------------------|-------------|------------------------------------------|
| Customer not found          | 404         | Customer not found with id: 999          |
| Start date after end date   | 400         | Start date must not be after end date    |
| Missing required parameter  | 400         | Missing required parameter: startDate    |
| Invalid date format         | 400         | Invalid value for parameter 'startDate'  |

---

## Sample Dataset

Four customers with transactions spread across January–March 2024:

| Customer          | Jan pts | Feb pts | Mar pts | Total |
|-------------------|---------|---------|---------|-------|
| Aditya Malviya    | 115     | 255     | 110     | 480   |
| Shreya Gupta      | 160     | 140     | 200     | 500   |
| Himanshu Sharma   | 550     | 60      | 495     | 1105  |
| Preeyanshu Singh  | 0       | 50      | 100     | 150   |

---

## Implementation Details

### Points Calculation (`RewardsService.calculatePoints`)

```
amount <= $50          → 0 points
$50 < amount <= $100   → (amount - 50) × 1 point
amount > $100          → 50 points + (amount - 100) × 2 points
```

### No Hard-Coded Months

The `buildMonthlyPointsMap` method iterates dynamically from `startDate` to `endDate` using `YearMonth`, pre-populating every month in the range with `0`. This means:
- Any date range works, not just 3 months
- Months with no transactions still appear in the response
- No month strings are ever hard-coded anywhere in the code

### No Hard-Coded Data

All customer and transaction data lives exclusively in SQL scripts:
- `src/main/resources/db/data.sql` — production seed data
- `src/test/resources/db/data.sql` — integration test data

### Security

- Database credentials are read from environment variables `DB_USERNAME` and `DB_PASSWORD`
- No passwords, tokens, or secrets are stored in source code or committed to version control
- `.gitignore` excludes `target/`, `bin/`, `.env`, and IDE files

### Testing Strategy

- **Unit tests** (`RewardsServiceTest`) — 20+ cases using Mockito, covering all spend tiers, monthly aggregation, multi-customer grouping, and all exception/negative scenarios
- **Integration tests** (`RewardsControllerIntegrationTest`) — full HTTP request/response cycle using MockMvc and H2, verifying correct points for all 4 customers across all months, plus all error scenarios
