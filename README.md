# Scam Report API

A Spring Boot backend for reporting suspicious phone-number activity. Reports from distinct
users contribute to automatically generated warnings. The API supports public warning lookup
and synchronization, user-submitted disputes, moderator review, configurable warning rules,
and administrator-managed impersonation categories.

The project is a layered modular monolith: controllers handle HTTP, services implement business
rules and transaction boundaries, repositories access PostgreSQL, and JPA entities represent
the persisted domain. Flyway manages schema changes.

## Requirements

- Java 25
- Docker with Docker Compose (local PostgreSQL and Testcontainers tests)
- Gradle Wrapper (included)

## Run locally

Create local configuration and secrets:

```shell
cp .env.example .env
```

Set a unique `POSTGRES_PASSWORD` and generate a Base64 JWT secret of at least 256 bits:

```shell
openssl rand -base64 32
```

Start PostgreSQL and the backend:

```shell
docker compose up -d postgres
./gradlew bootRun
```

Flyway applies migrations automatically at startup. The local database defaults to port 5432;
database name, username, password, and port are configurable in `.env`.

## API documentation

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The API is versioned under `/api/v1`. Public endpoints include active category lookup,
active warning lookup, and warning synchronization. Reporting and disputes require a JWT.
Administrative endpoints require `ADMIN`; dispute review requires `MODERATOR` or `ADMIN`.

## Authentication and a basic flow

Registering always creates a `USER` account:

```shell
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"person@example.com","password":"StrongPassword123!"}'
```

Login at `POST /api/v1/auth/login`; use the returned `accessToken` as
`Authorization: Bearer <token>` on protected endpoints. Access tokens expire after 15 minutes.

Submit an authenticated report with an international phone number, an active category code,
incident date, and communication channel:

```json
{
  "phoneNumber": "+372 5123 4567",
  "categoryCode": "POLICE",
  "incidentDate": "2026-11-24",
  "communicationChannel": "PHONE_CALL",
  "description": "Optional plain-text details"
}
```

The initial warning threshold is two distinct reporting users per phone number. The default
inactivity expiration is 60 days. Admins can update both settings; changing either triggers
recalculation as documented in Swagger.

To grant a local development account the `ADMIN` role, register it first and update its role in
the local PostgreSQL database. Never expose this operation through public self-registration.

## Warning synchronization

Call `GET /api/v1/sync/warnings` for an initial snapshot of active warnings. Store the returned
`syncTime`. For subsequent calls, pass it as `updatedAfter`, for example:

```text
GET /api/v1/sync/warnings?updatedAfter=2026-11-25T08:30:00Z
```

Incremental results include changed warning states, including inactive states, so clients can
update their local copy. Use the server-provided cursor, not the device clock.

## Tests

Run the complete test suite:

```shell
./gradlew test
```

Unit tests cover isolated business logic. Integration and end-to-end tests use PostgreSQL 16
through Testcontainers and require Docker. The end-to-end test exercises registration, login,
report submission by two users, warning creation, public lookup, and initial synchronization.
A second end-to-end test exercises dispute submission, moderator acceptance, warning withdrawal,
and incremental synchronization of the withdrawn state.

## Logging

Log4j2 writes to standard output and `logs/application.log`. The active file rolls daily or at
10 MB; compressed archives under `logs/archive` are retained for 14 days. Configure `APP_LOG_DIR`
to change the directory and `LOG_LEVEL` to change the root level. The local `logs/` directory is
ignored by Git. Sensitive credentials, tokens, report descriptions, and dispute reasons are not
written to application logs.

## Main endpoints

| Purpose | Endpoint | Access |
|---|---|---|
| Register / login / current account | `/api/v1/auth/*` | Register/login public; current account requires JWT |
| List active categories | `GET /api/v1/categories` | Public |
| Create report | `POST /api/v1/reports` | JWT |
| List active warnings | `GET /api/v1/warnings` | Public |
| Synchronize warnings | `GET /api/v1/sync/warnings` | Public |
| Submit a dispute | `POST /api/v1/warnings/{warningId}/disputes` | JWT |
| Review disputes | `/api/v1/moderation/disputes` | MODERATOR or ADMIN |
| Manage categories and warning settings | `/api/v1/admin/**` | ADMIN |

Warnings are risk information based on user-submitted reports, not proof of wrongdoing. The
absence of a warning does not guarantee that a number is safe, and caller ID can be spoofed.
