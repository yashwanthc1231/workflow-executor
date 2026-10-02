# Recruiter Submission Guide

## Project
Workflow Executor — Kotlin + Spring Boot

## Quick Start

### Prerequisites
- JDK 21
- Docker Desktop
- IntelliJ IDEA (recommended)

### Option 1 — IntelliJ IDEA
1. Extract the ZIP.
2. Open the project folder in IntelliJ IDEA.
3. Allow IntelliJ to import/sync the Gradle project.
4. Start PostgreSQL:
   `docker compose up -d postgres`
5. Run the Spring Boot application from IntelliJ.

### Option 2 — Windows command line
1. Open Command Prompt in the project folder.
2. Start PostgreSQL:
   `docker compose up -d postgres`
3. Start the application:
   `gradlew.bat bootRun`

### Run tests
`gradlew.bat test`

### API documentation
After the application starts:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Alternative Swagger UI path: http://localhost:8080/swagger-ui/index.html

## Example workflow

Use POST `/workflows` with an `Idempotency-Key` header and:

```json
{
  "steps": [
    { "id": "a", "type": "task" },
    { "id": "b", "type": "task", "dependsOn": ["a"] },
    { "id": "c", "type": "task", "dependsOn": ["a"] }
  ]
}
```

Then use GET `/workflows/{id}` to inspect execution status.

## What is included
- REST API
- Workflow dependency validation
- Cycle detection
- Workflow/step lifecycle states
- PostgreSQL persistence
- Idempotency key handling
- Asynchronous execution
- Unit tests
- Swagger/OpenAPI
- Docker support
- Architecture and tradeoff documentation

## Submission notes
This is intentionally a focused take-home implementation. Production-scale concerns such as distributed scheduling, durable queues, retries/backoff, cancellation, authentication/authorization, and distributed coordination are documented as future extensions rather than implemented as unnecessary complexity.

## Important
The Gradle wrapper files are included in the final submission package so the project can be built/run with `gradlew.bat` without requiring a separate Gradle installation.
