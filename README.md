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
- PostgreSQL/Neon via `SPRING_DATASOURCE_*` environment variables

## Authentication
- `APP_AUTH_REGISTRATION_CODE`: shared code required by `POST /api/v1/auth/register`, defaults to `foodhelper-invite` for local development
- `APP_AUTH_JWT_SECRET`: signing secret for issued JWTs
- `APP_AUTH_JWT_ISSUER`: JWT issuer, defaults to `foodhelper-api`
- `APP_AUTH_JWT_EXPIRATION_SECONDS`: JWT lifetime, defaults to `3600`

## API
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/products`
- `GET /api/v1/products/stats`
- `PUT /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`
- `GET /api/v1/products?page=&size=`
- `GET /api/v1/recipes?page=&size=`
- `GET /api/v1/recipes/stats`
- `POST /api/v1/recipes`
- `PUT /api/v1/recipes/{id}`
- `DELETE /api/v1/recipes/{id}`
- `POST /api/v1/recipes/{id}/derived-product`
- `POST /api/v1/proposed-week-menus`
- `GET /api/v1/proposed-week-menus/{id}`
- `PUT /api/v1/proposed-week-menus/{id}/days`
- `POST /api/v1/proposed-week-menus/{id}/publish`
- `GET /api/v1/established-week-menus/{id}`
- `GET /api/v1/established-week-menus/{id}/used-stock`
- `GET /api/v1/established-week-menus/{id}/shopping-list`
- `GET /api/v1/proposed-week-menu-day-parts`
- `POST /api/v1/proposed-week-menu-day-parts`
- `PUT /api/v1/proposed-week-menu-day-parts/{id}`
- `POST /api/v1/products/{productId}/stock`
- `GET /api/v1/products/{productId}/stock`
- `GET /api/v1/stock`
- `POST /api/v1/stock/{stockEntryId}/add`
- `POST /api/v1/stock/{stockEntryId}/remove`
- `GET /api/v1/health`

`Product` nutritional values are stored per 100 grams.

`Recipe` nutritional values are calculated from assigned ingredient grams and kept synchronized with the derived product created through `POST /api/v1/recipes/{id}/derived-product`.

`Proposed week menus` are draft weekly plans with a start date, an end date, optional planned days, and selected reusable day parts for each planned day. Day parts are configured separately with a name, description, and sort order. The inclusive date range must span at most 8 calendar days so a Friday-to-Friday or Monday-to-Monday menu is valid. Product grams can be supplied explicitly or inferred from `gramsPerUnit * units`, and nutritional totals are calculated for each product, section, day, and proposed menu. A day cannot repeat the same day part.

`Established week menus` are created by publishing a proposed week menu. Publication freezes the menu snapshot, captures the nutritional totals and covered cost, consumes matching stock entries in FIFO order by expiration date, deletes any stock entry that reaches zero, and stores the missing quantities as a shopping list. The consumed stock can be queried later through the established week endpoints.

`Stock` is stored as independent stock entries linked to products. Each entry keeps a positive quantity, a required entry date, and an optional expiration date. When a removal leaves a stock entry at zero, that entry is deleted.

## Swagger / OpenAPI
- OpenAPI JSON: `/v3/api-docs`
- Grouped OpenAPI JSON: `/v3/api-docs/{group}`
- Swagger UI: `/swagger`
- Offline static snapshot: `docs/openapi.yaml`

Available OpenAPI groups:
- `auth`
- `health`
- `media`
- `products`
- `recipes`
- `stock`
- `proposed-week-menus`
- `established-week-menus`

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
