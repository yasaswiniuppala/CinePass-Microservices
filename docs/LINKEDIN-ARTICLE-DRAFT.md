# Building CinePass: A Concurrency-Aware Microservices Ticket Booking Platform

For our CinePass Entertainment project, we designed a microservices-based cinema ticket booking platform to address a familiar challenge: blockbuster releases can create sudden traffic spikes, and a seat must never be sold to two customers.

The solution separates the platform into User, Movie, Show and Booking services. Eureka provides service discovery, while a Spring Cloud API Gateway offers a centralized entry point. User authentication uses BCrypt password hashing and short-lived signed JWT access tokens.

The key engineering challenge is concurrency. The Show Service owns seat inventory and reserves a seat inside a database transaction using a pessimistic row lock. A unique constraint on show and seat provides an additional integrity guarantee. The Booking Service obtains the authenticated identity from the verified token rather than trusting an email address supplied by the client.

The project demonstrates important DTI concepts: distributed systems, independent service boundaries, API Gateway, service discovery, load balancing, stateless authentication, database-per-service, concurrency control and compensating actions. We also prepared an integration test plan for duplicate bookings, access control, cancellation and simultaneous seat requests.

A key lesson is that distributed workflows are not automatically atomic. Our educational implementation uses a compensating seat release if booking persistence fails. A production deployment should strengthen this with a durable Saga/outbox, retries, reservation expiry, idempotency keys, monitoring and service-to-service authentication.

CinePass illustrates how thoughtful service boundaries and database-level concurrency controls can protect customer experience under high demand.

#Microservices #SpringBoot #DistributedSystems #JWT #Eureka #SoftwareEngineering #DTI

*Before publishing, add your team's actual implementation screenshots, measured test results, and team acknowledgements. Do not claim performance numbers that were not measured.*
