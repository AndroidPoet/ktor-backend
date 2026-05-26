# Ktor Backend

A small Kotlin backend built with Ktor. It includes REST endpoints, WebSocket chat, PostgreSQL persistence, Flyway migrations, Exposed SQL access, request IDs, health probes, OpenAPI documentation, and tests.

The project is intentionally single-module. The code is organized by feature package so it stays easy to split later if there is a real reason to do that.

## Stack

- Kotlin `2.2.21`
- Ktor `3.5.0`
- PostgreSQL `17` for local development
- Flyway for schema migrations
- Exposed for database access
- kotlinx.serialization for JSON
- Ktor Resources for type-safe HTTP routes
- Ktor WebSockets for chat
- Logback + Ktor CallLogging
- Gradle wrapper

## Project Shape

```text
src/main/kotlin/com/ranbirsingh/ktorbackend
  Application.kt

  common/
    AppRoutes.kt          typed route declarations and route constants
    Errors.kt             StatusPages error mapping
    Observability.kt      request logging and request IDs
    ProblemDetails.kt     API error shape
    RequestIdPlugin.kt

  config/
    AppConfig.kt          environment-backed config

  db/
    DatabaseFactory.kt    Hikari, Flyway, Exposed database setup

  users/
    UserRoutes.kt         user HTTP routes
    UserService.kt        user business logic
    UserRepository.kt     persistence boundary
    PostgresUserRepository.kt  Exposed adapter

  chat/
    ChatRoutes.kt         chat REST + WebSocket routes
    ChatRoomHub.kt        active WebSocket session coordination
    ChatRepository.kt     persistence boundary
    PostgresChatRepository.kt  Exposed adapter
```

## Route Model

HTTP routes use Ktor Resources where practical:

- `UsersRoute`
- `UsersRoute.ById`
- `ChatMessagesRoute`

The WebSocket route is centralized in `AppRoutes.ChatWebSocket` because Ktor's official WebSocket API uses `webSocket("/path")`. The path and query names are still defined once in `AppRoutes`.

## Endpoints

```text
GET  /livez
GET  /readyz

POST /api/users
GET  /api/users/{id}

GET  /api/chat/rooms/{roomId}/messages
WS   /ws/chat/{roomId}?sender={name}

GET  /openapi
GET  /openapi.json
```

## Chat Protocol

Connect to a room:

```text
ws://localhost:8080/ws/chat/general?sender=ranbir
```

Send a plain text WebSocket frame:

```text
hello
```

The server broadcasts a JSON message to all active sessions in the room:

```json
{
  "id": "b7bd6b9f-09b8-48e9-a6a9-63f9c8c522b8",
  "roomId": "general",
  "sender": "ranbir",
  "text": "hello",
  "sentAt": "2026-05-26T17:30:00.123"
}
```

Messages are persisted in PostgreSQL. The history endpoint returns the latest messages for a room:

```bash
curl http://localhost:8080/api/chat/rooms/general/messages
```

## Local Development

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

The server listens on port `8080` by default.

## Configuration

Configuration is read from environment variables:

```text
PORT                 default: 8080
DATABASE_URL         default: jdbc:postgresql://localhost:5432/app
DATABASE_USER        default: app
DATABASE_PASSWORD    default: app
DATABASE_POOL_SIZE   default: 10
```

Example:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/app \
DATABASE_USER=app \
DATABASE_PASSWORD=app \
./gradlew run
```

## Database

Migrations live in:

```text
src/main/resources/db/migration
```

Current schema:

- `users`
- `chat_messages`

Flyway runs on startup before the application accepts traffic.

## API Documentation

Swagger UI:

```text
http://localhost:8080/openapi
```

OpenAPI JSON:

```text
http://localhost:8080/openapi.json
```

The OpenAPI file is checked in at:

```text
src/main/resources/openapi/openapi.json
```

## Testing

Run all tests:

```bash
./gradlew test
```

Run build + tests:

```bash
./gradlew clean build
```

The tests cover:

- user service behavior
- user HTTP routes
- chat WebSocket broadcast
- chat history route
- chat persistence boundary through a fake repository

## Repository Status

GitHub Actions workflow files are intentionally disabled for now under:

```text
.github/workflows-disabled
```

Move them back to `.github/workflows` when CI should run.
