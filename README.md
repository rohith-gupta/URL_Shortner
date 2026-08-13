# URL Shortener

A URL-shortening service (Java 21 + Spring Boot + PostgreSQL) built as an AI-assisted software
engineering exercise. The code is the artifact you can run; the more important artifact is the
*process* behind it — requirement analysis, architecture decisions with recorded rationale,
disciplined AI-assisted execution, and full traceability of every AI-assisted change. That
process is documented in full in [`CLAUDE.md`](CLAUDE.md) and
[`docs/AI_WORKLOG.md`](docs/AI_WORKLOG.md) — this file is just the fast path to running it and
knowing where to look next.

## What's implemented

- `POST /api/urls` — create a short URL, with an optional custom alias and an optional
  expiration timestamp
- `GET /{shortCode}` — redirect to the original URL (302), tracking a click count
- `GET /api/urls/{shortCode}` — read-only details for a short URL
- `GET /api/urls/{shortCode}/analytics` — read-only click-count analytics
- A Flyway-managed PostgreSQL schema, atomic click-count increment under concurrency, bounded
  short-code collision retry, and 155 automated tests (unit, slice, full-stack, and real
  multi-threaded concurrency tests)

Also complete: the assignment's required brownfield scenario (custom aliases added to an
existing endpoint), ambiguous-requirement scenario (URL expiration, resolved from a
deliberately underspecified stakeholder statement), and test-improvement scenario. Full status,
what's still open, and what's explicitly out of scope are in `CLAUDE.md`'s "Current status" and
"Key constraints" sections.

## Quickstart

Requires Docker and JDK 21. No local Maven install needed — the project ships its own wrapper.

```bash
# 1. Start PostgreSQL (host port 5433 — see docker-compose.yml for why not 5432)
docker compose up -d

# 2. Run the app (connects to Postgres automatically; see CLAUDE.md if JAVA_HOME needs fixing)
./mvnw spring-boot:run
```

Then, from another terminal:

```bash
# Create a short URL
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com/some/long/path"}'
# -> 201, { "shortCode": "...", "shortUrl": "http://localhost:8080/...", ... }

# Follow it
curl -i http://localhost:8080/<shortCode>          # 302 redirect

# Read it back without redirecting
curl http://localhost:8080/api/urls/<shortCode>            # details
curl http://localhost:8080/api/urls/<shortCode>/analytics  # click count
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`. Health check:
`http://localhost:8080/actuator/health`.

To run the test suite (uses an in-memory H2 database — no Docker needed):

```bash
./mvnw test
```

## Where to go next

| Looking for... | See |
|---|---|
| Full architecture, API contracts, database schema, coding conventions, testing strategy | [`CLAUDE.md`](CLAUDE.md) |
| Normalized requirements, acceptance criteria, open questions | [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) |
| The AI-assisted-process traceability log — every decision, option considered, rationale, and validation result | [`docs/AI_WORKLOG.md`](docs/AI_WORKLOG.md) |
| Known limitations and what's explicitly out of scope (auth, rate limiting, SSRF protection, etc.) | `CLAUDE.md`, "Security approach" and "Reliability approach" sections |

This is a take-home/interview prototype, not a production deployment — see the documents above
for exactly which production-hardening concerns were deliberately deferred, and why.
