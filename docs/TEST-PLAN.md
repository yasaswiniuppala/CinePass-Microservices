# Integration and acceptance test plan

1. Start all services; confirm all five clients (Gateway + four business services) appear in Eureka.
2. Signup with valid email/password; duplicate signup returns 409.
3. Signin; incorrect password returns 401.
4. Call protected movie/show/booking route without token: expect 401.
5. Call each protected route with valid token: expect normal response.
6. Movie CRUD: create, list, get, update, delete; invalid title/duration returns 400.
7. Show CRUD: create future show and seat inventory; list/get/update/delete. A show with reserved seats cannot be deleted.
8. Booking: reserve an available seat; response contains authenticated email.
9. Duplicate seat booking: expect 409.
10. Ownership: a second account cannot read/cancel the first account's booking.
11. Cancel: seat becomes available and booking status becomes CANCELLED.
12. Concurrency: issue two concurrent booking requests for the same show/seat; exactly one should return 201, the other 409.
13. Load balancing: start two instances of a service with distinct ports and same DB/Eureka; inspect logs or add request tracing to confirm both receive traffic.
14. Failure handling: stop Show Service and verify Booking Service returns a controlled 502 rather than creating a false booking.

Automated Maven test execution: `mvn test`. End-to-end requests are in `review1-tests.http`.
