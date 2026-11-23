# LedgerCore

A Java 21 / Spring Boot 4 banking REST API portfolio project.

## Current functionality

- Create and retrieve bank accounts
- Deposit and withdraw funds
- Transfer funds between accounts
- Reject invalid amounts, insufficient funds, and missing accounts
- JUnit service tests and a health endpoint

**MVP limitation:** Accounts are stored in memory and are reset when the application restarts. This is a learning/demo backend, **not** a production banking system. PostgreSQL, authentication, audit logging, durable transactions, and integration tests are future work.

## Requirements

Java 21. The repository includes a Maven wrapper.

## Run locally

On Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The application listens on `http://localhost:8080`.

## API endpoints

| Method | Endpoint | JSON body | Result |
| --- | --- | --- | --- |
| GET | `/api/health` | — | Health text |
| POST | `/api/accounts` | `{"ownerName":"Alex"}` | 201 + account |
| GET | `/api/accounts/1` | — | 200 + account, or 404 |
| POST | `/api/accounts/1/deposits` | `{"amount":100.00}` | Updated account |
| POST | `/api/accounts/1/withdrawals` | `{"amount":25.00}` | Updated account |
| POST | `/api/accounts/transfers` | `{"fromAccountId":1,"toAccountId":2,"amount":20.00}` | 204 |

Amounts must be positive with no more than two decimal places. Insufficient funds return 409; invalid input returns 400.

## Example

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/accounts -ContentType application/json -Body '{"ownerName":"Alex"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/accounts/1/deposits -ContentType application/json -Body '{"amount":100.00}'
Invoke-RestMethod -Uri http://localhost:8080/api/accounts/1
```

## Design

`controller/` defines HTTP routes and request DTOs; `service/` contains account operations; `model/` holds account data. Service methods are synchronized for atomic in-memory balance updates within a single process. No database is currently configured.

## Testing

Run `.\mvnw.cmd test` from PowerShell. The service tests cover account creation, retrieval, deposits, withdrawals, transfers, and invalid operations.

## Roadmap

- PostgreSQL persistence and database transactions
- Request validation and structured error responses
- API integration tests
- Authentication, audit trail, Docker and CI
