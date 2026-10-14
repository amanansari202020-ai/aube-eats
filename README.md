# Aube Eats

> Good things take their time.

[![CI](https://github.com/amanansari202020-ai/aube-eats/actions/workflows/ci.yml/badge.svg)](https://github.com/amanansari202020-ai/aube-eats/actions/workflows/ci.yml)

Aube Eats is a full-stack cafe ordering experience built around a warm, editorial storefront and a practical order-management backend. Browse a bagel-and-coffee menu, build a cart, and follow the order flow through a React and TypeScript UI, Spring Boot API, and real-time status service.

## What It Includes

- Responsive storefront with menu search, category filters, vegetarian filter, and price filter
- Cart, coupon preview, and checkout flows
- Account registration and login, saved profile addresses, and order history
- Menu, order, customer, and coupon administration with a dashboard
- Reviews for menu items purchased and delivered
- Real-time order status updates over Socket.IO
- REST API documentation through Swagger UI
- Docker Compose stack with MySQL, Redis, API, and realtime services

## Technology

| Area | Stack |
| --- | --- |
| Frontend | React, TypeScript, Vite, React Three Fiber, GSAP |
| API | Java 17, Spring Boot, Spring Security, Spring Data JPA |
| Realtime | Node.js, Express, Socket.IO |
| Data | MySQL, Redis, Flyway |
| Quality | GitHub Actions, Maven, JUnit, Testcontainers, oxlint |

## Run the Storefront Preview

The preview runs without the backend. The menu and cart use bundled data, and guest checkout creates a preview order rather than saving an order to the database.

Requirements: Node.js 24 recommended.

```bash
npm ci
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

## Run the Full Stack

Requirements: Docker Desktop with Docker Compose.

1. Create a local environment file:

	```powershell
	Copy-Item .env.example .env
	```

2. Edit `.env`. Replace the sample database passwords and webhook secret. Set `JWT_SECRET_BASE64` to a fresh Base64-encoded secret containing at least 32 random bytes. Use different values for each secret, and do not commit `.env`.

	To generate a JWT secret in Windows PowerShell:

	```powershell
	$bytes = New-Object byte[] 32
	$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
	$rng.GetBytes($bytes)
	[Convert]::ToBase64String($bytes)
	$rng.Dispose()
	```

	Copy the output into `JWT_SECRET_BASE64` in `.env`. Generate a separate random value for `ORDER_WEBHOOK_SECRET`. `ADMIN_EMAIL` and `ADMIN_PASSWORD` are optional; when configured, the bootstrap password must be 12 to 72 characters.

3. Build and start the services:

	```bash
	docker compose up --build
	```

Open the app at [http://localhost:8081](http://localhost:8081). The API is at [http://localhost:8080](http://localhost:8080), with Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). Flyway creates the schema and seeds the initial menu on first startup.

Stop the stack with `Ctrl+C`, or run `docker compose down` from another terminal. Named database volumes are retained when you use `docker compose down`.

## Development Checks

Run these from the repository root:

```bash
npm ci
npm run lint
npm run build
```

Check the realtime service:

```bash
cd node-service
npm ci
npm run check
```

Run backend tests with Java 17 and Maven:

```bash
cd backend
mvn -B verify
```

The Flyway migration test uses Testcontainers and requires Docker. It is skipped when Docker is unavailable. GitHub Actions runs the frontend, realtime, and backend checks on pushes and pull requests.

## Project Layout

```text
src/           React storefront and shared client code
backend/       Spring Boot API, persistence, migrations, and tests
node-service/  Express health/webhook service and Socket.IO server
public/        Brand assets
docker-compose.yml
```

## Current Limitations

- Guest checkout is a preview and does not create a persisted order.
- Signed-in checkout currently supports cash on delivery. Online payment is rejected until Razorpay order creation and signature verification are implemented.
- Razorpay environment variables are placeholders; no merchant credentials are included.