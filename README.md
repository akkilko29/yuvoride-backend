# YuvoRide Backend

Production REST API for the YuvoRide ride-booking apps (customer and driver).

## Stack

- Java 17
- Spring Boot 4.1
- Spring Web, Spring Data JPA, Spring Security
- MySQL
- JWT access tokens + hashed refresh tokens
- OpenAPI / Swagger

## Run

1. Create MySQL database `yuvoride` (or rely on `createDatabaseIfNotExist=true` if the user has permission).
2. Copy `.env.example` and export the variables in your shell or IDE run configuration. Spring Boot does not load `.env` by itself.
3. Start:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

## Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database (`test` profile).

## Swagger

- UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Postman

Re-import these files in Postman (File → Import). If you already imported an older copy, delete it first or choose Replace.

- [postman/YuvoRide-Customer.postman_collection.json](postman/YuvoRide-Customer.postman_collection.json) — `/v1/auth`, `/v1/users`, `/v1/vehicles`, `/v1/rides`
- [postman/YuvoRide-Driver.postman_collection.json](postman/YuvoRide-Driver.postman_collection.json) — `/auth/driver/*`, `/driver/*`, `/auth/refresh`, `/auth/logout`, plus legacy `/v1/drivers` and `/v1/rides/{id}/enroute|arrive|start|complete`
- [postman/YuvoRide-Admin.postman_collection.json](postman/YuvoRide-Admin.postman_collection.json) — `/v1/admin/**` (`ROLE_ADMIN`)

Set `baseUrl` (default `http://localhost:8080`).

Driver login is email OTP on `POST /auth/driver/login` then `POST /auth/driver/verify-otp` (saves `token` as `accessToken`). Going online requires admin KYC `VERIFIED` and account `ACTIVE`.

## Required environment variables

| Variable | Purpose |
| --- | --- |
| `DB_HOST` | MySQL host (default `localhost`) |
| `DB_PORT` | MySQL port (default `3306`) |
| `DB_NAME` | Database name (default `yuvoride`) |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Long random secret for access tokens |
| `GOOGLE_CLIENT_ID` | Required for Google login |
| `GOOGLE_MAPS_API_KEY` | Required for distance/duration/fare estimates |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |

## Notes

- OTP is hashed at rest and never returned in API responses. The default SMS gateway is a mock that can be replaced via `SmsGateway`.
- Ride estimates and booking require Google Directions (`GOOGLE_MAPS_API_KEY`). The backend does not invent production distances or fares.
- Pricing lives on `vehicle_types` in MySQL, not in Java constants.
