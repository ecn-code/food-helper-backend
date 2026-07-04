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
- Flyway migrations:
  - `src/main/resources/db/migration/V1__create_products.sql`
  - `src/main/resources/db/migration/V2__create_recipes.sql`
  - `src/main/resources/db/migration/V28__create_stock_movements.sql`
- PostgreSQL/Neon via `SPRING_DATASOURCE_*` environment variables

## Authentication
- `APP_AUTH_REGISTRATION_CODE`: shared code required for user registration, defaults to `foodhelper-invite` for local development
- `APP_AUTH_JWT_SECRET`: signing secret for issued JWTs
- `APP_AUTH_JWT_ISSUER`: JWT issuer, defaults to `foodhelper-api`
- `APP_AUTH_JWT_EXPIRATION_SECONDS`: JWT lifetime, defaults to `3600`

## CORS

- `APP_CORS_ALLOWED_ORIGIN_PATTERNS`: comma-separated browser origin patterns. Local development defaults to `http://localhost:*` and `http://127.0.0.1:*`.
- Production must set this variable to the deployed frontend origin, for example `https://d1234567890.cloudfront.net` or `http://192.168.1.133`; multiple origins may be supplied as a comma-separated list.

## API documentation
The OpenAPI contract is the source of truth for endpoints, parameters, authentication, request and response schemas, validation errors, and examples:

- OpenAPI JSON: `/v3/api-docs`
- Grouped OpenAPI JSON: `/v3/api-docs/{group}`
- Swagger UI: `/swagger`
- Offline static snapshot: `docs/openapi.yaml`

Every new endpoint, and every change to request or response data in an existing endpoint, must be documented in Swagger/OpenAPI within the same change.

Planning creation requests accept a `users` count so stock requirements can be scaled for multi-person weeks.
Stock now exposes a historical ledger at `/api/v1/stock/movements` and a per-product reconciliation view at `/api/v1/products/{productId}/stock/reconciliation`.

## Tests
- Unit tests for service and mapper
- Integration tests with PostgreSQL Testcontainers
- Lambda route integration test with API Gateway event objects
- LocalStack Docker-only smoke test

## Raspberry Pi deployment

For the simplest deployment flow, use:

```bash
./scripts/deploy_to_raspberry.sh
```

That script:

- builds the JAR locally
- copies it to `pi-server`
- installs it in `/opt/foodhelper/foodhelper-api.jar`
- updates `APP_AUTH_REGISTRATION_CODE` on the Raspberry when `DEPLOY_AUTH_REGISTRATION_CODE` is set
- restarts the `foodhelper` systemd service
- checks `http://localhost:8080/actuator/health`

The Raspberry Pi already exposes the app through the `foodhelper` service and PostgreSQL runs locally on the device.

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
