# UML and design diagrams

## Use-case diagram
```mermaid
flowchart LR
  Customer((Customer)) --> UC1[Register / Sign in]
  Customer --> UC2[Browse Movies]
  Customer --> UC3[Browse Shows and Seats]
  Customer --> UC4[Book Seat]
  Customer --> UC5[View My Bookings]
  Customer --> UC6[Cancel Booking]
  Admin((Cinema Admin)) --> UC7[Manage Movies]
  Admin --> UC8[Manage Shows]
  UC4 --> JWT[JWT validation at API Gateway]
  UC5 --> JWT
  UC6 --> JWT
```

## Booking sequence
```mermaid
sequenceDiagram
  actor Customer
  participant Gateway
  participant Booking
  participant Show
  participant DB as Show Database
  participant BDB as Booking Database
  Customer->>Gateway: POST booking + Bearer JWT
  Gateway->>Gateway: Verify signature and expiry
  Gateway->>Booking: Forward request + authenticated subject
  Booking->>Show: Reserve show/seat (Eureka LB)
  Show->>DB: Begin transaction + pessimistic row lock
  Show->>DB: Check availability and set reserved=true
  Show-->>Booking: 200 reserved / 409 unavailable
  Booking->>BDB: Persist booking for JWT subject
  alt booking persisted
    Booking-->>Gateway: 201 Created
    Gateway-->>Customer: Booking confirmation
  else booking persistence failed
    Booking->>Show: Compensating release request
    Booking-->>Gateway: Error
  end
```

## Domain class diagram
```mermaid
classDiagram
  class User {Long id; String email; String passwordHash; String role}
  class Movie {Long id; String title; String genre; int durationMinutes; String language}
  class MovieShow {Long id; Long movieId; String theatre; String screenName; LocalDateTime startTime; int seatCapacity}
  class Seat {Long id; Long showId; String seatNumber; boolean reserved}
  class Booking {Long id; Long showId; String seatNumber; String email; String status; Instant bookedAt}
  MovieShow "1" --> "1..*" Seat : contains
  User "1" --> "0..*" Booking : owns by email subject
  Movie "1" --> "0..*" MovieShow : referenced by movieId
```

## Deployment architecture
See `ARCHITECTURE.md` for the deployment component diagram and service/database ownership.
