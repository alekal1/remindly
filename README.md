# Remindly

Remindly is a Spring Boot service for scheduling reminders and delivering them through ntfy.

## Tech stack

- Java 25
- Spring Boot
- PostgreSQL
- Liquibase
- ntfy: https://ntfy.sh/

## Requirements

- Java 25
- Docker and Docker Compose
- ntfy topics for the reminder types

## Modules

- `core` - shared domain, persistence, ntfy client, scheduler, and reminder guards.
- `adhoc` - creates one-off reminders through `POST /v1/adhoc`.
- `garbage_collection` - imports garbage collection schedules from PDF and converts them into reminders.
- `snooze` - Snooze functionality for reminders

## Configuration

The app reads environment variables from `.env` at startup.

By default, the ntfy server is `https://ntfy.sh`. You can override this by setting the `NTFY_SERVER` environment variable.

NTFY topics are only needed for the reminder types that are enabled in `app.reminders`:

- `NTFY_ERRORS_TOPIC`
- `NTFY_GARBAGE_COLLECTION_TOPIC`
- `NTFY_ADHOC_TOPIC`

**It is highly recommended to keep the `errors` reminder enabled, because app exceptions are reported through this channel.**

Example reminder config:

```yaml
app:
  reminders:
    - name: errors
      topic: ${NTFY_ERRORS_TOPIC}
      enabled: true
    - name: garbage-collection
      topic: ${NTFY_GARBAGE_COLLECTION_TOPIC}
      enabled: true
    - name: adhoc
      topic: ${NTFY_ADHOC_TOPIC}
      enabled: true
```

Reminder configuration is also exposed through Spring Actuator at `GET /actuator/info` under:

```json
{
  "app": {
    "reminders": [
      {
        "name": "errors",
        "topic": "..."
      }
    ]
  }
}
```

Database defaults:

- host: `localhost`
- port: `54321`
- database: `remindlydb`
- user/password: `remindly` / `remindly`

## Run locally

1. Start PostgreSQL:

```bash
docker compose up -d postgres-remindly
```

2. Run the application:

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

## Run with Docker

```bash
docker-compose --env-file secrets/.env up -d
```

## iPhone Shortcuts

Tested only with the PC and iPhone connected to the same LAN.

On Windows, open the application port if the phone cannot reach the PC over LAN:

```powershell
New-NetFirewallRule -DisplayName "Allow Docker TCP <APP-PORT>" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort <APP-PORT> `
  -Action Allow
```

Use the Shortcuts action **Get Contents of URL**:

1. Add a new shortcut and choose **Get Contents of URL**.
2. Set the URL to `http://<PC-IP>:<APP-PORT>/v1/adhoc`.
3. Set the method to `POST`.
4. Set the request body to `JSON`.
5. Send a payload like this:

```json
{
  "message": "Take out the trash",
  "scheduledAt": "2026-08-24T21:00:00"
}
```

## Reminder behavior

Reminders are processed automatically every minute.

When an event becomes due, the app sends the matching ntfy notification and marks the event as sent.
