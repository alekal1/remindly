# Remindly

Remindly is a Spring Boot service that notifies user abount any kind of reminder with ntfy application.

The current implementation uses **garbage collection** as the main example of how the app connects ntfy notifications
with recurring reminders.

## Tech stack

- Java 25
- Spring Boot
- PostgreSQL
- Liquibase
- ntfy [Learn more about ntfy](https://ntfy.sh/)

## Requirements

- Java 25
- Docker and Docker Compose
- an ntfy topic for garbage collection reminders
- an ntfy topic for error notifications

## Configuration

The app reads environment variables from `.env` at startup.

Required variables:

- `NTFY_GARBAGE_COLLECTION_TOPIC`
- `NTFY_ERRORS_TOPIC`

Database defaults:

- host: `localhost`
- port: `54321`
- database: `remindlydb`
- user/password: `remindly` / `remindly`

## Run locally

1. Start PostgreSQL:

   ```bash
   docker compose up -d
   ```

2. Run the application:

   ```bash
   ./gradlew bootRun
   ```

   On Windows:

   ```powershell
   .\gradlew.bat bootRun
   ```

## Reminder behavior

Reminders are processed automatically every second. When an event becomes due, the app sends the matching ntfy notification and marks the event as sent.
