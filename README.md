# Reservas

Booking platform for resources such as courts or rooms. A REST API built as a **hexagonal modular
monolith** with Java and Spring Boot, with the central rule enforced where it cannot be bypassed:
**a resource can never be booked twice for overlapping time ranges**, not even under concurrent requests.

This is a portfolio project. It is built step by step, with small pull requests, to practice a
professional stack end to end and to be able to explain every decision. See
[Working with Claude Code](#working-with-claude-code) for how AI is used in the process.

## Status

| Phase | Scope | State |
|---|---|---|
| 1 | Domain, overlap rule, PostgreSQL + Flyway, REST API, OpenAPI | Done |
| 2 | React frontend, CI with coverage and quality gates, API and end-to-end tests | Next |
| 3 | Authentication (Spring Security + JWT) and real-time updates (WebSocket) | Planned |
| 4 | Events (SQS/SNS), a notifications service, deployment on AWS | Planned |

## Tech stack

- **Backend:** Java 25, Spring Boot 4.1, Spring Web, Spring Data JPA (Hibernate), Bean Validation
- **Database:** PostgreSQL 16, schema managed with Flyway
- **API contract:** OpenAPI 3 generated with springdoc, Swagger UI included
- **Tests:** JUnit, Mockito, Testcontainers (real PostgreSQL), MockMvc
- **Build and local environment:** Maven, Docker Compose

## Architecture

Dependencies point inwards. The domain knows nothing about Spring, JPA or HTTP.

```
 HTTP --> infrastructure/rest --> application/usecase --> domain
                                         |
                                         | depends on an interface (port)
                                         v
                                  application/port
                                         ^
                                         | implements
 PostgreSQL <-- infrastructure/persistence
```

```
backend/src/main/java/com/rubenlazaro/reservas/
  domain/                  Booking, Resource, TimeRange and the domain exceptions
                           (no framework annotations of any kind)
  application/
    port/                  BookingRepository, ResourceRepository: interfaces owned by the core
    usecase/               CreateBooking, CancelBooking, ... one class and one execute() each
  infrastructure/
    rest/                  Controllers, request/response records, ApiExceptionHandler
    persistence/           JPA entities and the adapters that implement the ports
    config/                Wiring of the use cases as beans, OpenAPI metadata
```

The JPA entities live in `infrastructure` and are mapped to the domain model, so the domain stays
a plain Java model.

## Design decisions

- **The database guarantees the overlap rule.** The use case checks for conflicts to return a
  friendly error, but two simultaneous requests can both pass that check. What actually holds the
  invariant is a PostgreSQL exclusion constraint (`EXCLUDE USING gist` over the resource and the
  time range). A concurrency test fires eight threads at the same slot, ten times, and expects
  exactly one winner every time.
- **Concurrent inserts can end in a deadlock, not only in a constraint violation.** The same test
  showed that PostgreSQL may abort one of two overlapping inserts with `deadlock detected`. The
  persistence adapter translates both outcomes into the same domain exception, which the API
  returns as `409 Conflict`.
- **Time ranges are half-open, `[start, end)`.** A booking can start exactly when another one ends.
  Instants are stored as `TIMESTAMPTZ`, so there is no time zone ambiguity.
- **The SQL conflict query and the domain rule are tested against each other.** A grid of cases
  runs both the query and `Booking.conflictsWith` and requires the same answer.
- **Validation in two layers.** Bean Validation checks the shape of the input (missing fields).
  Business rules, such as `end` after `start` or the maximum name length, live in the domain.
- **Use cases are plain classes**, not Spring components. They are wired as beans in one
  configuration class, so the application layer does not import the framework.
- **No transaction around the use cases.** Each repository call is atomic and the database
  constraint protects the invariant. This is revisited in phase 4, when publishing events will
  need a transactional outbox.
- **Errors follow RFC 9457** (`application/problem+json`). Validation failures include the list of
  offending fields.
- **Error messages have a fixed English locale**, so the same request gets the same body on any
  machine. This was found by running the application: messages came out in Spanish on a machine
  with a Spanish JVM.
- **`userId` is sent by the client for now.** It is documented as provisional in the OpenAPI
  schema and will come from the access token once authentication exists (phase 3).

## Getting started

Requirements: JDK 25, Maven, and Docker.

```bash
# from the repository root: start PostgreSQL
docker compose up -d

# from backend/: run the API (Flyway applies the migrations on startup)
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI document: <http://localhost:8080/v3/api-docs>

The database credentials in `application.yaml` are for local development only. Production
configuration is still to be done (see [Known limitations](#known-limitations)).

## API

| Method and path | Result |
|---|---|
| `POST /api/resources` | `201`; `400` for a blank, missing or too long name; `409` if the name exists |
| `GET /api/resources` | `200` with all resources |
| `POST /api/resources/{resourceId}/bookings` | `201` with a `Location` header; `400` for invalid input; `404` unknown resource; `409` overlap |
| `GET /api/resources/{resourceId}/bookings` | `200` ordered by start; `404` unknown resource |
| `GET /api/bookings/{id}` | `200` or `404` |
| `DELETE /api/bookings/{id}` | `204` or `404`; the slot is free again |

```bash
curl -X POST localhost:8080/api/resources \
  -H 'Content-Type: application/json' -d '{"name":"Court 1"}'

curl -X POST localhost:8080/api/resources/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"userId":7,"start":"2026-01-01T10:00:00Z","end":"2026-01-01T11:00:00Z"}'
```

Booking the same court for an overlapping range answers `409` with a problem detail:

```json
{
  "detail": "Resource 1 is already booked between 2026-01-01T10:30:00Z and 2026-01-01T11:30:00Z",
  "instance": "/api/resources/1/bookings",
  "status": 409,
  "title": "Conflict"
}
```

## Tests

Run them from `backend/`:

```bash
mvn test
```

**Docker must be running.** Most of the tests start a real PostgreSQL 16 in a container with
Testcontainers, and they fail at startup if Docker is not available.

| Kind | What it covers | Needs Docker |
|---|---|---|
| Domain | `TimeRange`, `Booking`, `Resource`, plain JUnit | No |
| Use cases | Business flow with the ports mocked with Mockito | No |
| Persistence | Adapters against PostgreSQL, constraint translation, the SQL-versus-domain grid | Yes |
| Concurrency | Many threads booking the same slot | Yes |
| API | Controllers through MockMvc on the full application: status codes, problem details, OpenAPI | Yes |

Why a real PostgreSQL and not an in-memory database such as H2: the overlap guarantee is an
exclusion constraint over `tstzrange`, which H2 does not support, so the tests would pass against a
database that does not behave like production. The tests also never touch the development
database: they use their own throwaway container.

Planned: separate unit and integration tests into different Maven phases so the fast ones run
without Docker, then add coverage (JaCoCo), API tests with Newman and end-to-end tests with
Playwright (phase 2).

## Working with Claude Code

The project is developed with [Claude Code](https://claude.com/claude-code) as an AI pair
programmer, under a few rules written in [`CLAUDE.md`](CLAUDE.md): propose a plan before changing
files, explain the reasoning behind each decision, never invent versions or APIs, and keep the
architecture rules (for example, no framework annotations in `domain/`).

How it is used in practice:

- **Test-first.** Tests are written before the code and run to confirm they fail for the right
  reason. This caught tests that passed for the wrong reason: two API tests expected a `404`,
  which an unknown route also returns, so they were tightened to require the problem detail body
  before implementing.
- **Tests as a way to find bugs.** The deadlock under concurrent bookings was found by the
  concurrency test, not predicted. The locale issue was found by starting the real application and
  calling it with `curl`, then pinned with a regression test.
- **Review and trade-offs.** Before writing code, each step starts from a plan with alternatives
  and their cost. One earlier recommendation (a transactional decorator around the use cases) was
  reversed once it was clear the database constraint already protected the invariant.
- **Small, reviewable changes.** One branch and one pull request per step, with Conventional
  Commits. Commits made with Claude Code carry a `Co-Authored-By` trailer.
- **A human decides.** Scope, architecture and every commit, pull request and merge are approved
  explicitly. The goal is to be able to explain each decision, not only to have the code.

## Known limitations

- **No authentication yet.** Anyone who can reach the API can create resources and cancel any
  booking. It is not meant to be exposed publicly before phase 3.
- **Configuration is for local development.** Credentials are in `application.yaml`; a production
  profile with secrets from environment variables is needed before any deployment. Swagger UI is
  enabled in every environment.
- **No CORS configuration**, because there is no browser client yet.
- **No CI yet**: the tests run locally. A GitHub Actions pipeline with quality gates is the first
  step of phase 2.
