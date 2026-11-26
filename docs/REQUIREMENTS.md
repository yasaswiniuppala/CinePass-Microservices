# Problem analysis and requirements specification

## Problem statement
CinePass must support multiple cinemas/screens and scheduled showtimes while surviving traffic bursts during high-demand releases. The critical correctness requirement is that a seat for one show can be confirmed for at most one customer.

## Functional requirements
1. Users can register and sign in.
2. Passwords are stored as BCrypt hashes.
3. Sign-in issues a signed, expiring JWT.
4. Movie administrators/authorized callers can create, read, update and delete movie records (the educational gateway currently authenticates users but does not distinguish admin roles).
5. Shows can be created, listed, retrieved, updated and deleted, with seat inventory generated per show.
6. A seat can be reserved only while available.
7. A booking is associated with the identity in the verified JWT, not an email supplied by the client.
8. Users can list, retrieve and cancel their own bookings.
9. Cancelling a confirmed booking releases its seat.
10. Services register with Eureka; Gateway and Booking use service discovery/load balancing.

## Non-functional requirements
- **Consistency:** database row locking and uniqueness of `(show_id, seat_number)` in the seat inventory prevent concurrent successful reservations of one seat.
- **Security:** BCrypt, JWT signature/expiry verification, protected Gateway routes, and ownership checks for booking reads/cancellations.
- **Scalability:** stateless services and service discovery allow multiple instances behind load-balanced calls.
- **Maintainability:** bounded services with separate databases and API contracts.
- **Observability:** actuator health/info endpoints.

## Service ownership
| Service | Owns | Database |
|---|---|---|
| User | credentials and user role | movie_user_db |
| Movie | movie metadata | movie_movie_db |
| Show | show schedule and seat inventory | movie_show_db |
| Booking | booking records and status | movie_booking_db |

## Acceptance criteria
The manual test sequence in `review1-tests.http` and the automated repository tests must pass. The race test must result in one success and one conflict for two simultaneous requests for the same seat. Eureka must show all service instances, and protected routes must reject missing/invalid JWTs.

## Explicit limitations
This is a student demonstration baseline. Role-based admin authorization, Movie-Service validation of referenced movie IDs, full overlapping-show duration validation, durable Saga/outbox retries, idempotency keys, distributed tracing, and production secrets management are recommended extensions and are not claimed as fully implemented.
