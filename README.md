# Remindly

**Self-hosted automation engine for things you don't want to remember.**

Remindly is a Spring Boot service that turns recurring chores and one-off tasks into scheduled
push notifications, delivered through [ntfy](https://ntfy.sh/). Fire off an ad-hoc reminder from
your phone via a Shortcut, snooze it if the timing's off, or let Remindly watch your inbox and
auto-schedule reminders from incoming emails (e.g. garbage collection notices) — all running on
your own infrastructure, with no third-party reminder app required.

## Tech stack

- Java 25
- Spring Boot
- PostgreSQL
- Liquibase
- ntfy: https://ntfy.sh/
- caddy: https://caddyserver.com/ (optional reverse proxy with automatic HTTPS)
- sslip.io: https://sslip.io/ (optional free wildcard DNS service for HTTPS)

## Requirements

- Java 25
- Docker and Docker Compose
- ntfy topics for the reminder types

## Modules

- `core` - shared domain, persistence, ntfy client, scheduler, and reminder guards.
- `adhoc` - creates one-off reminders through `POST /v1/adhoc`.
- `garbage_collection` - imports garbage collection schedules and converts them into reminders. Contains two submodules:
  - `garbage` - parses garbage collection schedule PDFs and creates reminder events.
  - `gmail` - fetches the schedule PDF as a Gmail attachment via the Gmail API and hands it off to `garbage` for processing.
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

## Gmail integration

**Note:** this integration is very specific to one waste collection provider's notification email.
`GmailHtmlExtractor` parses the HTML body of the message looking for a table with the exact Estonian column headers
`Jäätmeliik` (waste type) and `Tühjendamise kuupäev` (collection date).
Any other email format/structure will not be recognized and will simply produce no schedule.

The `gmail` submodule reads garbage collection schedules from Gmail messages using the Gmail API. 

**It is optional and only activates when a valid credentials file is configured.**

Configuration (`app.gmail`):

| Env variable | Property | Description |
|---|---|---|
| `GMAIL_CREDENTIALS_FILE` | `credentials-file` | Path to the Google OAuth client credentials JSON (see `secrets/google-credentials.json.sample`). |
| `GMAIL_TOKEN_DIRECTORY` | `token-directory` | Directory where the OAuth access/refresh tokens are stored after authorization. |
| `GMAIL_SENDER` | `sender` | Email address whose messages are scanned for schedules. |
| `GMAIL_AUTH_CALLBACK_BASE_URL` | `auth-callback-url` | Base URL used to build the OAuth redirect/callback URL. |

Setup:

1. Create an OAuth client ID (Web) in Google Cloud Console with the Gmail API enabled, download the credentials JSON, and place it at the path referenced by `GMAIL_CREDENTIALS_FILE` (see `secrets/google-credentials.json.sample` for the expected shape).
2. Add authorized url in Google Cloud Console.
3. Start the app, then open `GET /v1/garbage-collection/setup/gmail` in a browser to begin the OAuth consent flow.
4. After granting access, Google redirects to `/v1/garbage-collection/setup/gmail/callback`, which exchanges the authorization code for tokens and stores them under `GMAIL_TOKEN_DIRECTORY`.
5. Once authorized, `GmailMessageScheduler` periodically polls the Gmail inbox for new schedule attachments.

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
2. Set the URL to `http://<APP_DOMAIN>/v1/adhoc`. (`http://<PC_IP_ADDRESS>:<APP_PORT>/v1/adhoc` if running locally in same LAN)
3. Set the method to `POST`.
4. Set the request body to `JSON`.
5. Send a payload like this:

```json
{
  "message": "Take out the trash",
  "scheduledAt": "2026-08-24T21:00:00"
}
```

## Reverse proxy / HTTPS

The `caddy` service in `docker-compose.caddy.yml` provides automatic HTTPS via Let's Encrypt and
proxies traffic to the app. The app itself is no longer published on a host port (`expose: 8080`
only) — Caddy is the sole entry point on ports 80/443.

1. Point a DNS name at your server's public IP. If you don't own a domain, you can use a free
   wildcard DNS service like [sslip.io](https://sslip.io) (e.g. `remindly.<ip-with-dashes>.sslip.io`).
2. Set the hostname in `Caddyfile` to match.
3. Ensure ports 80 and 443 are open (cloud firewall/security list **and** OS firewall, e.g.
   `iptables`/`firewalld` on Oracle Cloud instances).
4. Update `APP_EXTERNAL_BASE_URL` and `GMAIL_AUTH_CALLBACK_BASE_URL` in your `.env` file to the
   `https://` version of that hostname.
5. Start everything, including Caddy:

```bash
docker compose -f docker-compose.caddy.yml --env-file secrets/.env up -d
```

Caddy automatically requests and renews a Let's Encrypt certificate for the configured hostname
on first startup (requires ports 80/443 reachable from the internet for the ACME challenge).
