# DTI concepts demonstrated in CinePass

- **Distributed systems:** independently deployable services communicate over HTTP.
- **Service discovery:** Eureka registers active service instances and supports dynamic resolution.
- **API Gateway pattern:** one entry point centralizes routing and JWT checks.
- **Stateless authentication:** signed, expiring JWT bearer tokens avoid server-side session state.
- **Database per service:** each domain owns its persistence schema.
- **Concurrency control:** pessimistic row locking serializes competing reservations for one seat.
- **Data integrity:** unique constraints backstop application-level duplicate checks.
- **Load balancing:** Spring Cloud LoadBalancer distributes service-to-service calls across discovered instances.
- **Saga/compensation concept:** failed booking persistence triggers a compensating seat release; a durable outbox/retry design is a production extension.
- **Resilience and observability:** health endpoints, controlled downstream errors and explicit test scenarios support operations.

## Innovation / business value
CinePass separates catalogue, schedule, identity and booking concerns so they can scale independently. During blockbuster releases, the booking path can be replicated separately, while seat ownership remains serialized at the Show Service database row.
