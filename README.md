# foodhelper-api

Spring Boot 4 + Spring Cloud Function project prepared for dual execution:
- Local REST API
- AWS Lambda (API Gateway-style event routing)

## Maven coordinates
- `groupId`: `com.eliascanalesnieto`
- `artifactId`: `foodhelper-api`

## Runtime profiles
- `local`: regular REST app
- `lambda`: Lambda-oriented mode (`web-application-type=none`)
- `test-integration`: integration tests with Testcontainers

## Lambda handler
Use:
- `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest`

Function definition:
- `productHttpHandler`

## Database
- Flyway migration: `src/main/resources/db/migration/V1__create_products.sql`
- PostgreSQL/Neon via `SPRING_DATASOURCE_*` environment variables

## API
- `POST /api/v1/products`
- `PUT /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`
- `GET /api/v1/health`

## Swagger / OpenAPI
- OpenAPI JSON: `/v3/api-docs`
- Swagger UI: `/swagger`

Every new endpoint, and every change to request or response data in an existing endpoint, must be documented in Swagger/OpenAPI within the same change.

## Tests
- Unit tests for service and mapper
- Integration tests with PostgreSQL Testcontainers
- Lambda route integration test with API Gateway event objects
- LocalStack Docker-only smoke test

## Docker host
When running against a remote Docker host, create an SSH tunnel to the Docker socket before starting the app or the tests:

```bash
ssh -NL <port>:/var/run/docker.sock <host_user>@<host_ip>
```

Use the same `<port>` value in your Docker client or environment configuration so Testcontainers and any Docker-based smoke tests can reach the remote daemon.

Run:
```bash
mvn verify
```
