# LedgerCore

A small banking REST API built with Java 21, Spring Boot 4, Spring Data JPA and PostgreSQL. This is a learning project, not a real banking platform.

## What it does

- Create accounts and look up balances
- Deposit and withdraw money
- Transfer money between two accounts
- Save accounts and transaction history in PostgreSQL
- View a single account's deposits, withdrawals and transfers
- Reject invalid amounts, missing accounts and insufficient funds
- Lock account rows during balance changes and update balances and history together in a database transaction

## Get started

You need Java 21 and PostgreSQL running locally.

1. Create a PostgreSQL database named `ledgercore`.
2. Create a PostgreSQL login role named `ledgercore_app` with a password. Grant it access to the database and permission to create tables in the `public` schema.
3. Set the password in an environment variable **in your terminal**, not in source code.

On Windows PowerShell:

```powershell
cd C:\Ledger\ledgercore\ledgercore
$env:LEDGERCORE_DB_PASSWORD = Read-Host "Database password" -MaskInput
.\mvnw.cmd spring-boot:run
```

If you cloned the repository elsewhere, run the commands from that repository's root directory instead.

The application uses `jdbc:postgresql://localhost:5432/ledgercore` by default. Override `LEDGERCORE_DB_URL` and `LEDGERCORE_DB_USER` if needed. The server listens on `http://localhost:8080`.

For this demo, Hibernate updates the database schema on startup (`ddl-auto=update`). Production systems should use versioned database migrations instead.

## API

| Method | Route | Request body |
| --- | --- | --- |
| GET | `/api/health` | — |
| POST | `/api/accounts` | `{"ownerName":"Alex"}` |
| GET | `/api/accounts/1` | — |
| POST | `/api/accounts/1/deposits` | `{"amount":100.00}` |
| POST | `/api/accounts/1/withdrawals` | `{"amount":25.00}` |
| POST | `/api/accounts/transfers` | `{"fromAccountId":1,"toAccountId":2,"amount":20.00}` |
| GET | `/api/accounts/1/transactions` | — |

Amounts must be positive with no more than two decimal places. Insufficient funds return HTTP 409; invalid amounts return HTTP 400. Account lookups return HTTP 404 when the account does not exist.

Transaction history records the operation type, amount, time and the source/destination account IDs where applicable. A transfer appears in both accounts' histories.

## API documentation

An OpenAPI 3 specification is available at [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml). When the app is running, retrieve it from `http://localhost:8080/openapi.yaml`. You can import the file into an OpenAPI viewer such as Swagger Editor. A built-in Swagger UI is not installed.

## Try it in PowerShell

Keep the server running in one terminal and use a second terminal:

```powershell
$account = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/accounts -ContentType application/json -Body '{"ownerName":"Alex"}'
$id = $account.id
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/accounts/$id/deposits" -ContentType application/json -Body '{"amount":100.00}'
Invoke-RestMethod -Uri "http://localhost:8080/api/accounts/$id/transactions"
```

Restart the server and retrieve the same account to check persistence.

## Tests

Run `./mvnw clean test` (macOS/Linux) or `.\mvnw.cmd clean test` (Windows). Tests use an isolated **H2 in-memory database**, so PostgreSQL credentials are not needed to run them.

GitHub Actions also runs the Maven tests on pushes to `main` and on pull requests. Integration tests exercise account and transaction repositories against H2 and check that rejected transfers do not change balances or create transaction records. These are not PostgreSQL-specific integration tests.

## How it's organized

- `controller/`: REST endpoints
- `service/`: business rules, transaction boundaries and row locking
- `model/`: JPA entities
- `repository/`: database access

## Known limitations

This project has **no authentication or authorization**. Anyone with network access to the API can operate on any account. It is not suitable for real funds or deployment as a public banking service. Future improvements include request validation, API integration tests, versioned migrations, structured error responses and authentication.
