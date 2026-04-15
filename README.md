# H3X Licensing Panel

A Spring Boot web panel for buyer accounts, product management and Discord/FiveM license validation.

## Included features

- buyer registration and login
- admin panel for products and license creation
- purchase claim flow using buyer email + purchase reference
- license validation API for Discord bots and FiveM scripts
- audit log entries for important license actions
- Thymeleaf UI styled with Tailwind CSS CDN
- Flyway migrations
- H2 for development and tests
- MariaDB-ready production profile
- API key protection and simple rate limiting for validation requests
- Spring Boot Actuator health endpoints
- Discord OAuth login
- token-based password reset flow (optional mail delivery)
- production reverse proxy setup with Nginx + HTTPS

## Profiles

- `dev` for local development
- `test` for automated tests
- `prod` for deployment

No unsafe production defaults are kept in the base configuration. Choose a profile explicitly.

## Run locally

```powershell
Set-Location "D:\projekte\h3x_licensing"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/login`
- `http://localhost:8080/admin`

## Example validation request

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/licenses/validate" -ContentType "application/json" -Body '{"licenseKey":"your-license-key","productSlug":"fivem-license-core","discordId":"123456789","fivemServerId":"server-01"}'
```

## Build and test

```powershell
Set-Location "D:\projekte\h3x_licensing"
.\mvnw.cmd test
.\mvnw.cmd package -DskipTests
```

## Docker deployment

Build the jar first, then the image:

```powershell
Set-Location "D:\projekte\h3x_licensing"
.\mvnw.cmd package -DskipTests
docker build -t h3x-licensing .
```

Or start with the provided compose stack:

```powershell
Set-Location "D:\projekte\h3x_licensing"
docker compose up --build
```

Before using compose in a real environment, replace all placeholder secrets in `docker-compose.yml`.

For production with HTTPS reverse proxy, use:

```powershell
Set-Location "D:\projekte\h3x_licensing"
Copy-Item .env.example .env
docker compose -f docker-compose.prod.yml --env-file .env up --build -d
```

Nginx certificate files expected in `nginx/certs`:

- `nginx/certs/fullchain.pem`
- `nginx/certs/privkey.pem`

The reverse proxy configuration is in `nginx/conf.d/default.conf`.

## Auth flows

- local login form via email/password
- Discord OAuth via `/oauth2/authorization/discord`
- password reset request via `/forgot-password`
- reset execution via `/reset-password?token=...`

## Security notes

- the validation API requires the `X-API-Key` header
- public registration can be disabled per profile
- production uses secure cookies and disables H2 console
- only health/info actuator endpoints are exposed
- default response data from the validation API is minimized

## Project structure

- `src/main/java/org/h3x_licensing/config` - security and bootstrap configuration
- `src/main/java/org/h3x_licensing/web` - MVC controllers and form flows
- `src/main/java/org/h3x_licensing/api` - REST endpoints for script validation
- `src/main/java/org/h3x_licensing/user` - user domain
- `src/main/java/org/h3x_licensing/product` - product domain
- `src/main/java/org/h3x_licensing/license` - license and audit domain
- `src/main/resources/templates` - Tailwind-based Thymeleaf views
- `src/main/resources/db/migration` - Flyway SQL migrations

## Suggested next steps

- add email verification and password reset
- add webhook integration for Tebex or custom shop systems
- introduce JWT or API key authentication for production script validation
- add rate limiting and IP logging for the validation endpoint
- persist to MariaDB/PostgreSQL in production instead of local H2

