# Remindly

**Self-hosted automation engine for things you don't want to remember.**

Remindly is a Spring Boot service that turns recurring chores and one-off tasks into scheduled
push notifications, delivered through [ntfy](https://ntfy.sh/). Fire off an ad-hoc reminder from
your phone via a Shortcut, snooze it if the timing's off, or let Remindly watch your inbox and
auto-schedule reminders from incoming emails (e.g. garbage collection notices). The app itself
runs entirely on your own infrastructure — no reminder app or backend required — and ntfy can
point at the free public `ntfy.sh` service or your own self-hosted ntfy instance.

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
- .env file with configuration (see below)

## Modules

- `core` - Shared domain, persistence, ntfy client, scheduler, and reminder guards.
- `adhoc` - Creates one-off reminders through `POST /v1/adhoc`.
- `garbage_collection` - Imports garbage collection schedules and converts them into reminders.
- `snooze` - Snooze functionality for reminders
- `gmail` - Fetches gmail email content based on templates and creates reminder for each.


## Configuration

The app reads environment variables from `.env` at startup.

| Env variable | Property | Description | Default |
|---|---|---|---|
| `APP_EXTERNAL_BASE_URL` | `external-base-url` | Base URL of the application, used for building callback URLs. | - |
| `NTFY_SERVER_URL` | `ntfy-server` | URL of the ntfy server. | https://ntfy.sh |
| `NTFY_ERRORS_TOPIC` | `reminders.topic` | Topic for error notifications. | - |
| `NTFY_ERRORS_TOPIC_ENABLED` | `reminders.enabled` | Whether the error notifications reminder is enabled. | true |
| `NTFY_GARBAGE_COLLECTION_TOPIC` | `reminders.topic` | Topic for garbage collection notifications. | - |
| `NTFY_GARBAGE_COLLECTION_TOPIC_ENABLED` | `reminders.enabled` | Whether the garbage collection notifications reminder is enabled. | false |
| `NTFY_ADHOC_TOPIC` | `reminders.topic` | Topic for adhoc notifications. | - |
| `NTFY_ADHOC_TOPIC_ENABLED` | `reminders.enabled` | Whether the adhoc notifications reminder is enabled. | true |
| `GMAIL_CREDENTIALS_FILE` | `credentials-file` | Path to the Google OAuth client credentials JSON. | /secrets/google-credentials.json |
| `GMAIL_TOKEN_DIRECTORY` | `token-directory` | Directory where the OAuth access/refresh tokens are stored after authorization. | /app/data/gmail |
| `GMAIL_AUTH_CALLBACK_BASE_URL` | `auth-callback-url` | Base URL used to build the OAuth redirect/callback URL. | http://localhost:9999 |

**Some of env variables are prefilled with default values, and some of the are optional** and only needed if you want to enable the corresponding feature.
(See the [Gmail integration](#gmail-integration) section for details.)

Reminder config is stored in the `app.reminders` list, which is resolved from the environment variables above. The:

```yaml
app:
   external-base-url: ${APP_EXTERNAL_BASE_URL}
   ntfy-server: ${NTFY_SERVER_URL}
   reminders:
      - id: errors
        topic: ${NTFY_ERRORS_TOPIC}
        enabled: ${NTFY_ERRORS_TOPIC_ENABLED}
      - id: garbage-collection
        topic: ${NTFY_GARBAGE_COLLECTION_TOPIC}
        enabled: ${NTFY_GARBAGE_COLLECTION_TOPIC_ENABLED}
      - id: adhoc
        topic: ${NTFY_ADHOC_TOPIC}
        enabled: ${NTFY_ADHOC_TOPIC_ENABLED}
   gmail:
      credentials-file: ${GMAIL_CREDENTIALS_FILE}
      token-directory: ${GMAIL_TOKEN_DIRECTORY}
      auth-callback-url: ${GMAIL_AUTH_CALLBACK_BASE_URL}
```

Reminder topic configuration is also exposed through Spring Actuator at `GET /actuator/info` under:

```json
{
  "app": {
    "reminders": [
      {
        "id": "errors",
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

The `gmail` submodule reads structured data from Gmail messages using the Gmail API.

The integration is template-based: every supported email format is described by a YAML template under
`src/main/resources/gmail-templates/*.yml`. At startup, the app loads all templates from that directory,
resolves environment variable placeholders in them, and uses the templates to search and process matching
Gmail messages.

**Currently only table-based email content is supported.** This means a Gmail template can process an email
when the relevant data is available in an HTML table. The template defines how to find that table, which
columns should be read, and how the extracted values should be interpreted by the matching processor.

**It is optional and only activates when a valid credentials file setup is provided.**

Setup:

1. Create an OAuth client ID (Web) in Google Cloud Console with the Gmail API enabled, download the credentials JSON, and place it at the path referenced by `GMAIL_CREDENTIALS_FILE` (see `secrets/google-credentials.json.sample` for the expected shape).
2. Add authorized url in Google Cloud Console.
3. Start the app, then open `GET /v1/gmail/setup"` in a browser to begin the OAuth consent flow.
4. After granting access, Google redirects to `/v1/gmail/setup/callback`, which exchanges the authorization code for tokens and stores them under `GMAIL_TOKEN_DIRECTORY`.
5. Once authorized, `GmailMessageScheduler` periodically polls the Gmail inbox for new schedule attachments.

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
