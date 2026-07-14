# Study Deck

Backend API for a Knowt-style flashcard app with folders, decks, flashcards, Learn, Matching, Practice Test, and FSRS spaced repetition.

## Requirements

- Java 17+
- Docker Desktop
- Maven wrapper from this repo

## Run MySQL

```powershell
docker compose up -d
```

This starts MySQL on `localhost:3306` and creates the `study_deck` database.

## Run The API

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/study_deck"
$env:DB_USER="root"
$env:DB_PASSWORD=""
.\mvnw.cmd spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Run The Frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

The frontend proxies `/api` requests to the Spring Boot API at `http://localhost:8080`.

## Run Tests

```powershell
.\mvnw.cmd test
```

Frontend tests:

```powershell
cd frontend
npm test
```

## Stop MySQL

```powershell
docker compose down
```

Delete MySQL data too:

```powershell
docker compose down -v
```
