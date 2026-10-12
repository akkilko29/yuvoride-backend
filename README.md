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

Import these collections separately (File → Import):

- [postman/YuvoRide-Customer.postman_collection.json](postman/YuvoRide-Customer.postman_collection.json)
- [postman/YuvoRide-Driver.postman_collection.json](postman/YuvoRide-Driver.postman_collection.json)

Set `baseUrl` (default `http://localhost:8080`). After **Verify OTP**, `accessToken` and `refreshToken` are stored on the collection. After **Create Ride**, `rideId` is stored.

Driver `/v1/drivers/**` endpoints require a user with role `DRIVER` and a `drivers` profile row. OTP signup creates `CUSTOMER` by default.

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
