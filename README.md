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
- `GET /api/v1/supermarkets`
- `GET /api/v1/supermarkets/{id}`
- `POST /api/v1/supermarkets`
- `PUT /api/v1/supermarkets/{id}`
- `DELETE /api/v1/supermarkets/{id}`
- `GET /api/v1/recipes?page=&size=`
- `GET /api/v1/recipes/stats`
- `POST /api/v1/recipes`
- `PUT /api/v1/recipes/{id}`
- `DELETE /api/v1/recipes/{id}`
- `POST /api/v1/recipes/{id}/derived-product`
- `POST /api/v1/planning`
- `GET /api/v1/planning/{id}`
- `PUT /api/v1/planning/{id}/days`
- `POST /api/v1/planning/{id}/menu`
- `GET /api/v1/planning/day-parts`
- `POST /api/v1/planning/day-parts`
- `PUT /api/v1/planning/day-parts/{id}`
- `GET /api/v1/menus/{id}`
- `GET /api/v1/menus/{id}/used-stock`
- `GET /api/v1/menus/{id}/shopping-list?supermarketId=`
- `POST /api/v1/menus/{id}/close`
- `GET /api/v1/menus/{id}/stats`
- `GET /api/v1/nutritional-rules`
- `PUT /api/v1/nutritional-rules`
- `POST /api/v1/products/{productId}/stock`
- `GET /api/v1/products/{productId}/stock`
- `GET /api/v1/stock`
- `POST /api/v1/stock/{stockEntryId}/add`
- `POST /api/v1/stock/{stockEntryId}/remove`
- `GET /api/v1/health`

`Product` nutritional values are stored per 100 grams.

`Products` can be assigned to zero or more supermarkets. The supermarket catalog uses case-insensitive unique names, and an assigned supermarket cannot be deleted.

`Recipe` nutritional values are calculated from assigned ingredient grams and kept synchronized with the derived product created through `POST /api/v1/recipes/{id}/derived-product`.

`Planning` covers a flexible inclusive date range of up to 16 days and may contain only the days planned so far. Each day selects reusable day parts with ordered products. Product grams can be supplied explicitly or inferred from `gramsPerUnit * units`, and nutritional totals are calculated for each product, section, day, and planning period. A day cannot repeat the same day part.

`Menus` are created from planning. Creating a menu freezes the snapshot, captures nutritional totals and covered cost, consumes matching stock entries in FIFO order by expiration date, deletes entries that reach zero, and stores missing quantities as a shopping list. The shopping-list endpoint returns every missing product or filters them by `supermarketId`; reading it never changes stock or the saved menu. After its end date, closing the menu persists period and month stats and blocks further planning edits.

`Nutritional rules` store optional daily minimums and maximums for calories, carbohydrates, proteins, and fats. Planning and menu responses include each nutrient's average per planned day and whether it is below, within, or above the configured range.

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
- `supermarkets`
- `recipes`
- `stock`
- `planning`
- `menus`
- `nutritional-rules`

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
