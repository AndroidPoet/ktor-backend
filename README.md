# Ktor Backend

A Kotlin backend built with Ktor. It demonstrates a practical server-side setup with REST endpoints, WebSocket chat, PostgreSQL persistence, Flyway migrations, Exposed database access, typed HTTP routes, request IDs, health probes, OpenAPI documentation, and tests.

This is not a framework wrapper or a generated sample. The goal is to keep the code understandable while still including the pieces a real backend usually needs.

## What This Project Includes

- REST API for users
- WebSocket chat by room
- Chat message persistence in PostgreSQL
- Chat history endpoint
- Flyway database migrations
- Exposed-based database adapters
- Ktor Resources for type-safe HTTP routes
- Centralized WebSocket route constants
- Problem Details-style error responses
- Request ID propagation with `X-Request-ID`
- Liveness and readiness probes
- OpenAPI JSON and Swagger UI
- Local PostgreSQL via Docker Compose
- Unit and route tests
- Disabled GitHub Actions workflow, ready to re-enable later

## Tech Stack

| Area | Choice |
| --- | --- |
| Language | Kotlin `2.3.0` |
| Server | Ktor `3.5.0` |
| JSON | kotlinx.serialization |
| Database | PostgreSQL |
| Migrations | Flyway |
| Database access | Exposed |
| Connection pool | HikariCP |
| Logging | Logback + Ktor CallLogging |
| Route typing | Ktor Resources |
| Realtime | Ktor WebSockets |
| Build | Gradle Kotlin DSL |
| Dependency injection | Metro `1.1.1` |

## Requirements

- JDK `21`
- Docker, only if you want local PostgreSQL through `compose.yml`

Metro `1.1.1` requires the build to run on Java 21 and Kotlin `2.3.0+`.

## Architecture

The project is a single-module backend organized by feature. This keeps the build simple while still giving each feature clear boundaries.

```text
src/main/kotlin/com/ranbirsingh/ktorbackend
  Application.kt

  common/
    AppRoutes.kt
    Errors.kt
    Observability.kt
    ProblemDetails.kt
    RequestIdPlugin.kt
    ValidationException.kt

  config/
    AppConfig.kt

  db/
    DatabaseFactory.kt

  di/
    AppGraph.kt           Metro dependency graph

  users/
    UserRoutes.kt
    UserService.kt
    UserRepository.kt
    PostgresUserRepository.kt
    User.kt
    UserResponse.kt

  chat/
    ChatRoutes.kt
    ChatRoomHub.kt
    ChatRepository.kt
    PostgresChatRepository.kt
    ChatModels.kt
```

### Layering

Each feature follows the same shape:

```text
Routes -> Service / Hub -> Repository interface -> PostgreSQL adapter
```

Business logic depends on repository interfaces, not on Exposed directly. Exposed stays inside the PostgreSQL adapter classes.

## Why Single Module

This project starts as a single module because there is no independent deployment or ownership boundary yet. Splitting into Gradle modules too early adds build and dependency management overhead without improving the design.

The current package structure still leaves a clean path to split later:

- `users` can become a module
- `chat` can become a module
- `common` can become shared infrastructure
- database adapters can move behind interfaces

## Routing

HTTP routes use Ktor Resources:

```kotlin
@Serializable
@Resource("/api/users")
class UsersRoute {
    @Serializable
    @Resource("{id}")
    data class ById(
        val parent: UsersRoute = UsersRoute(),
        val id: String,
    )
}
```

This avoids scattering path strings across tests and handlers. Tests can call routes using typed resource objects.

WebSocket routes in Ktor are still registered with `webSocket("/path")` in the official API. To avoid scattering strings, this project centralizes the WebSocket route and query parameter names in `AppRoutes`.

## Endpoints

### Health

```text
GET /livez
GET /readyz
```

`/livez` confirms the process is alive.

`/readyz` checks database connectivity with a lightweight `select 1`.

### Users

```text
POST /api/users
GET  /api/users/{id}
```

Create user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "founder@example.com",
    "displayName": "Founder"
  }'
```

Example response:

```json
{
  "id": "b7bd6b9f-09b8-48e9-a6a9-63f9c8c522b8",
  "email": "founder@example.com",
  "displayName": "Founder",
  "createdAt": "2026-05-26T17:30:00.123"
}
```

Find user:

```bash
curl http://localhost:8080/api/users/b7bd6b9f-09b8-48e9-a6a9-63f9c8c522b8
```

### Chat

```text
WS  /ws/chat/{roomId}?sender={name}
GET /api/chat/rooms/{roomId}/messages
```

Connect to a room:

```text
ws://localhost:8080/ws/chat/general?sender=ranbir
```

Send a plain text WebSocket frame:

```text
hello
```

The server stores the message and broadcasts this JSON shape to active clients in the same room:

```json
{
  "id": "f4f234b2-3f22-4182-9bb8-9ebf924eb768",
  "roomId": "general",
  "sender": "ranbir",
  "text": "hello",
  "sentAt": "2026-05-26T17:30:00.123"
}
```

Fetch recent room history:

```bash
curl http://localhost:8080/api/chat/rooms/general/messages
```

Chat behavior:

- incoming WebSocket frames are plain text
- outgoing WebSocket frames are serialized `ChatMessage` JSON
- messages are persisted before broadcast
- active WebSocket sessions are held in memory
- message history is read from PostgreSQL
- room IDs and sender names are simple strings for now

## Error Responses

Errors use a Problem Details-style JSON shape:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "code": "validation_failed",
  "errors": {
    "email": "Email must be valid"
  }
}
```

Common error codes:

```text
validation_failed
bad_request
duplicate_user_email
user_not_found
internal_error
```

## Request IDs

Every response includes an `X-Request-ID` header.

If a client sends one, the server keeps it:

```bash
curl http://localhost:8080/livez -H "X-Request-ID: demo-123"
```

If the client does not send one, the server generates a UUID. The request ID is also placed into the logging MDC as `request.id`.

## Database

Migrations live in:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__create_users.sql
V2__create_chat_messages.sql
```

Current tables:

```text
users
chat_messages
```

Flyway runs during application startup before routes are used.

## Configuration

Configuration comes from environment variables:

| Variable | Default |
| --- | --- |
| `PORT` | `8080` |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/app` |
| `DATABASE_USER` | `app` |
| `DATABASE_PASSWORD` | `app` |
| `DATABASE_POOL_SIZE` | `10` |

Example:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/app \
DATABASE_USER=app \
DATABASE_PASSWORD=app \
DATABASE_POOL_SIZE=10 \
./gradlew run
```

## Local Development

Check Java:

```bash
java -version
```

This project expects Java 21. If multiple JDKs are installed on macOS:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

Start PostgreSQL:

```bash
docker compose up -d
```

Run the server:

```bash
./gradlew run
```

Or use the Makefile:

```bash
make db
make dev
```

Useful commands:

```bash
make test
make build
make openapi
```

## OpenAPI

Swagger UI:

```text
http://localhost:8080/openapi
```

OpenAPI JSON:

```text
http://localhost:8080/openapi.json
```

The OpenAPI document is checked in:

```text
src/main/resources/openapi/openapi.json
```

The WebSocket endpoint is documented in OpenAPI for discoverability, but WebSocket protocol details still live in this README because OpenAPI is primarily HTTP-focused.

## Testing

Run tests:

```bash
./gradlew test
```

Run the full build:

```bash
./gradlew clean build
```

Current test coverage includes:

- user service behavior
- user HTTP route behavior
- chat WebSocket broadcast
- chat history route
- chat message persistence boundary using a fake repository

The route tests use Ktor's `testApplication` API.

## CI

GitHub Actions is disabled for now. The workflow file is parked at:

```text
.github/workflows-disabled/ci.yml
```

To re-enable CI:

```bash
mkdir -p .github/workflows
mv .github/workflows-disabled/ci.yml .github/workflows/ci.yml
```

## Current Limits

The project is intentionally small. A few things are not implemented yet:

- authentication
- authorization
- rate limiting
- pagination on chat history
- durable WebSocket session tracking
- multi-node chat fanout
- database-backed room membership
- production deployment manifests

For a single-node backend, the current chat setup is enough to develop against. For multiple app instances, chat fanout should move through Redis Pub/Sub, PostgreSQL `LISTEN/NOTIFY`, Kafka, or another shared messaging layer.

## Good Next Steps

Reasonable next changes:

- add authentication
- add pagination to chat history
- add request/response examples to OpenAPI
- add integration tests with Testcontainers
- re-enable CI
- add rate limiting around WebSocket connections
- add a deployment profile
