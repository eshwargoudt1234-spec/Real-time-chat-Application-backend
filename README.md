# Pulse Chat – BACKEND (Spring Boot + WebSocket + MySQL)

REST API + WebSocket only. The web pages live in the separate **chat-frontend** project.

## Run
1. Start MySQL.
2. Edit `src/main/resources/application.properties`: set `spring.datasource.password`
   (and `server.port` if 8080 is busy, e.g. `server.port=8081`).
3. In a normal Command Prompt / terminal, inside this folder (the one with `pom.xml`):
   ```
   mvn spring-boot:run
   ```
4. Check http://localhost:8080 → you should see `{"status":"UP",...}`.

Demo users (created on first start): alice, bob, charlie – password `Password@123`.

## Allowing your frontend (CORS)
`app.cors.allowed-origins` lists the frontend addresses that may call this backend.
Defaults: Live Server (5500), `npx serve` (3000), python (8000), 9090.
When you deploy the frontend, add its URL, e.g.
`app.cors.allowed-origins=https://my-chat.netlify.app`
(or set the environment variable `APP_CORS_ALLOWED_ORIGINS` on your host).

## Endpoints
POST /api/auth/register · POST /api/auth/login · GET /api/users · GET /api/users/me · GET /api/users/{id}
GET /api/messages/conversations · GET /api/messages/{userId} · PUT /api/messages/{userId}/read
WebSocket (SockJS/STOMP): /ws → send /app/chat, /app/typing → subscribe /user/queue/*, /topic/status

## Deploy (Docker)
A `Dockerfile` is included. Environment variables to set on the host:
SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, APP_JWT_SECRET, APP_CORS_ALLOWED_ORIGINS
