# CinePass Entertainment — Microservices Ticket Booking

A Spring Boot microservices reference implementation for movie catalogues, cinema show scheduling, JWT-authenticated ticket booking, Eureka discovery, gateway routing, and concurrency-safe seat allocation.

## Stack and ports

| Component | Port | Responsibility |
|---|---:|---|
| Eureka Server | 8761 | Service registry/discovery |
| API Gateway | 8080 | Single public entry point, JWT validation and routing |
| User Service | 8081 | Signup/signin, BCrypt and JWT issuance |
| Movie Service | 8082 | Movie CRUD |
| Show Service | 8083 | Show CRUD, seat inventory and locked seat reserve/release |
| Booking Service | 8084 | Authenticated booking lifecycle and load-balanced Show Service calls |
| MySQL | 3306 | Four independent service databases |

## Prerequisites

- Java 17 or newer (Java 17 recommended)
- Maven 3.9+
- MySQL 8.0+ for local mode, OR Docker Desktop for Compose mode
- VS Code/IntelliJ (optional)

The previous archive configured Java 25, which is not a safe baseline for typical student machines. This updated project uses Java 17 and Spring Boot 3.3.5 / Spring Cloud 2023.0.3.

## Option A — easiest: Docker Compose

1. Install and start Docker Desktop.
2. In this folder, copy `.env.example` to `.env` and replace `JWT_SECRET` with a unique random value (at least 32 bytes).
3. Run: `docker compose up --build`
4. Open Eureka at http://localhost:8761 and Gateway at http://localhost:8080.
5. Use the requests in `review1-tests.http` in order.
6. Stop with `docker compose down`. Keep database data with `docker compose down`; delete it only with `docker compose down -v`.

Compose starts MySQL, Eureka, all four business services and the Gateway. Database schemas are initialized by `database/setup.sql`.

## Option B — run locally

1. Run `database/setup.sql` in MySQL Workbench as a MySQL administrator. It creates four databases and a `movieapp` user (`MovieApp@123`). Change these demo credentials for any real deployment.
2. Confirm each service's `application.yml` DB settings or set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URL` environment variables.
3. From this root folder run `mvn clean package`.
4. Start in separate terminals, in this order: `mvn -pl eureka-server spring-boot:run`; then user-service, movie-service, show-service, booking-service; finally api-gateway. Or use the `start-*.bat` scripts on Windows.
5. Check service registration at http://localhost:8761.

## API flow

- Public: `POST /api/auth/signup`, `POST /api/auth/signin`
- Protected: `/api/movies/**`, `/api/shows/**`, `/api/bookings/**` through port 8080
- Signin returns `accessToken`; send `Authorization: Bearer <token>` on protected calls.
- The Gateway verifies signature and expiry, then injects `X-Authenticated-User` from the signed JWT subject. Booking Service ignores any client-supplied email and uses this authenticated identity.

### Movie endpoints

`POST /api/movies`, `GET /api/movies`, `GET /api/movies/{id}`, `PUT /api/movies/{id}`, `DELETE /api/movies/{id}`.

Movie JSON: `{"title":"Interstellar","genre":"Sci-Fi","durationMinutes":169,"language":"English","description":"Space exploration"}`.

### Show endpoints

`POST /api/shows`, `GET /api/shows`, `GET /api/shows/{id}`, `PUT /api/shows/{id}`, `DELETE /api/shows/{id}`, `GET /api/shows/{id}/seats`.

Show JSON: `{"movieId":1,"theatre":"CinePass Central","screenName":"Screen 1","startTime":"2030-12-01T18:30:00","seatCapacity":50}`. Show creation provisions S01..S50 (or requested capacity).

### Booking endpoints

`POST /api/bookings` with `{"showId":1,"seatNumber":"S01"}`, `GET /api/bookings`, `GET /api/bookings/{id}`, `DELETE /api/bookings/{id}`.

Booking is owned by the authenticated JWT subject. Cancellation releases the seat and marks the booking CANCELLED.

## Concurrency and consistency design

- Show Service uses a database transaction and `PESSIMISTIC_WRITE` row lock for each `(showId, seatNumber)` reservation.
- The seat table has a unique `(show_id, seat_number)` constraint.
- Booking table has a unique `(show_id, seat_number)` constraint as a second line of defence.
- Booking orchestration reserves the seat before saving the booking; if the booking write fails, it attempts a compensating release.
- This is a small distributed workflow, not a full distributed ACID transaction. For a production-grade system use an outbox/Saga with durable retries, idempotency keys, reservation expiry and reconciliation. The demo compensation is best-effort.

## Load balancing

The Gateway routes to `lb://` service IDs through Eureka. Booking Service uses Spring Cloud LoadBalancer with `http://show-service/...`. Run multiple instances of a service with distinct `SERVER_PORT` values and the same database/Eureka URL to demonstrate distribution. Do not use the same fixed port for two local instances.

## Tests

Run `mvn test` from the root. `review1-tests.http` provides a manual end-to-end sequence, including a duplicate-seat request that should return HTTP 409. To validate actual race behavior, send two simultaneous POST requests for the same show/seat; exactly one should succeed and the other should receive 409.

## Security and demo limitations

- Passwords are BCrypt-hashed; plaintext passwords are never returned.
- JWTs are signed HS256 and expire after two hours.
- Set the same strong `JWT_SECRET` for Gateway and User Service. Never commit a production secret.
- Only expose port 8080 publicly. Internal service ports should be private in a real deployment. Gateway-to-service calls in this educational build are trusted by network boundary; protect internal routes with service-to-service authentication/mTLS in production.
- This archive is an educational baseline. It has not been run in your local environment because the current execution environment has no Maven or Docker installed. Run the verification commands above on your machine and share any error output for correction.
