# Ktor Backend

Kotlin Ktor backend with PostgreSQL, Flyway migrations, OpenAPI docs, request IDs, health probes, and focused tests.

## Architecture

```text
users/
  UserRoutes.kt          HTTP boundary
  UserService.kt         business rules
  UserRepository.kt      persistence port
  SqlUserRepository.kt   database adapter
```

Migrations are the source of truth. Database access stays behind repository interfaces, so business logic does not depend on persistence details.

## Run

```bash
make db
make dev
```

Or run the app directly:

```bash
./gradlew run
```

## Endpoints

```text
Swagger UI:   http://localhost:8080/openapi
OpenAPI JSON: http://localhost:8080/openapi.json
Liveness:     http://localhost:8080/livez
Readiness:    http://localhost:8080/readyz
```

## Test

```bash
./gradlew build
```
