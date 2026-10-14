# Aube Eats

A warm editorial ordering storefront with a React/TypeScript client, Spring Boot API, and Socket.IO order-status relay.

## Quick Preview

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`. The menu and cart work from bundled preview data; guest checkout creates a clearly labeled preview order. Register/login and saved orders use the Spring API when it is running.

## Full Stack

Docker and Docker Compose are required. Copy `.env.example` to `.env`, replace the database passwords and webhook secret, and set a fresh base64-encoded JWT key (at least 32 random bytes). Then run:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Open `http://localhost:8081`. The API is also available at `http://localhost:8080`; Swagger UI is at `/swagger-ui.html`. Flyway creates the schema and seeds the menu at first startup.

For local Spring development, run MySQL and Redis separately, then set the variables in `.env` and use `mvn spring-boot:run` from `backend`. Run the realtime service with `npm ci` and `npm run dev` from `node-service`.

## Checks

```powershell
npm run lint
npm run build
Push-Location node-service; npm ci; npm run check; Pop-Location
```

Backend tests run in CI with Java 17 and Maven. The MySQL migration test uses Testcontainers and is skipped when Docker is unavailable.

## Services

- `src/`: responsive storefront, R3F bagel scene, cart, account dialog, API client, and order confirmation.
- `backend/`: layered Spring Boot API, JWT/refresh auth, JPA entities, Flyway migrations, menu/order/admin endpoints, and OpenAPI.
- `node-service/`: Express health/webhook endpoints and JWT-authenticated Socket.IO order rooms.
- `docker-compose.yml`: frontend, API, realtime service, MySQL, and Redis.

Razorpay credentials are configuration placeholders. Signed-in checkout currently supports cash on delivery only; online payment is rejected until Razorpay order creation and signature verification are implemented for your merchant account. Guest checkout is a preview and does not create a saved kitchen order.