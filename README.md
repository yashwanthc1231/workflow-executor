# Interview-Ready Small Workflow Executor

This is an expanded version of the Small Workflow Executor take-home project.

The assignment asks for a service capable of executing a simple workflow definition with dependencies and explicitly says the tasks may be mocked/simple; it also says a general-purpose workflow engine is not required.

## Stack

- Kotlin
- Spring Boot
- Spring Data JPA
- PostgreSQL
- OpenAPI / Swagger UI
- JDK 21
- Docker Compose

## API

### POST /workflows

Requires:

`Idempotency-Key: <unique-key>`

Example:

```json
{
  "steps": [
    {"id":"a","type":"task"},
    {"id":"b","type":"task","dependsOn":["a"]},
    {"id":"c","type":"task","dependsOn":["a"]}
  ]
}
```

Returns `202 Accepted`.

### GET /workflows/{id}

Returns workflow status and step status.

## Why these design choices?

### PostgreSQL
Workflow state survives application restarts, unlike the simplest in-memory implementation.

### Idempotency
The Idempotency-Key prevents accidental duplicate workflow creation when a client retries the same POST.

### DAG validation
Dependencies form a directed graph. Cycle detection prevents workflows that can never reach completion.

### Async execution
Workflow submission returns quickly. Execution happens in a bounded executor, allowing the API to remain responsive.

### Explicit lifecycle
Workflow status:
QUEUED -> RUNNING -> SUCCEEDED/FAILED

Step status:
PENDING -> RUNNING -> SUCCEEDED/FAILED

### Failure behavior
A failing task causes the workflow to fail. The task records its attempt count and error.

## Deliberate scope

This is still intentionally smaller than a production workflow platform.

Not implemented:

- Kafka/RabbitMQ durable task queue
- True distributed workers
- Exponential backoff retries
- Task timeouts/cancellation
- Authentication/authorization
- Full event history
- Metrics backend
- Dead-letter queue
- Multi-node coordination

## What I would build next

1. Move task dispatch to a durable queue.
2. Add retry policy with exponential backoff and maximum attempts.
3. Execute independent steps concurrently with bounded workers.
4. Add optimistic locking/versioning for concurrent state updates.
5. Add workflow cancellation and timeouts.
6. Add structured logs, metrics and tracing.
7. Add integration tests with Testcontainers.
8. Add authentication and tenant isolation.

## Run locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the application:

```bash
./gradlew bootRun
```

Swagger:

`http://localhost:8080/swagger-ui.html`

## Example curl

```bash
curl -X POST http://localhost:8080/workflows   -H "Content-Type: application/json"   -H "Idempotency-Key: demo-001"   -d '{
    "steps": [
      {"id":"a","type":"task"},
      {"id":"b","type":"task","dependsOn":["a"]},
      {"id":"c","type":"task","dependsOn":["a"]}
    ]
  }'
```

Then query:

```bash
curl http://localhost:8080/workflows/<workflow-id>
```

To demonstrate failure:

```json
{
  "steps": [
    {"id":"a","type":"fail"}
  ]
}
```

## Interview talking points

Be ready to explain:

- Why POST returns 202
- Why workflow execution is asynchronous
- Why PostgreSQL is used
- How idempotency works
- Why DAG/cycle validation is required
- What happens after a task failure
- How independent steps could be parallelized
- How retries should work
- How the design changes with multiple application instances
- How to recover a RUNNING workflow after a process crash
- How you would introduce Kafka or another durable queue
