# URL Shortener

A URL-shortening service (Java 21 + Spring Boot + PostgreSQL) built as an AI-assisted software
engineering exercise. The code is the artifact you can run; the more important artifact is the
*process* behind it — requirement analysis, architecture decisions with recorded rationale,
disciplined AI-assisted execution, and full traceability of every AI-assisted change. That
process is documented in full in [`docs/AI_WORKLOG.md`](docs/AI_WORKLOG.md) (chronological
traceability log), [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) (normalized requirements),
and [`docs/FINAL_ENGINEERING_SUMMARY.md`](docs/FINAL_ENGINEERING_SUMMARY.md) (a concise
synthesis) — this file is just the fast path to running it and knowing where to look next.

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
what's still open, and what's explicitly out of scope are in
[`docs/FINAL_ENGINEERING_SUMMARY.md`](docs/FINAL_ENGINEERING_SUMMARY.md).

## Quickstart

Requires Docker and JDK 21. No local Maven install needed — the project ships its own wrapper.

```bash
# 1. Start PostgreSQL (host port 5433 — see docker-compose.yml for why not 5432)
docker compose up -d

# 2. Run the app (connects to Postgres automatically)
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

> **Windows/Git Bash note**: if `mvnw`/`./mvnw` fails complaining about `JAVA_HOME`, it's
> usually pointing at `...\bin\java.exe` instead of the JDK home directory. Fix per-shell:
> `export JAVA_HOME="/c/Program Files/<your JDK install>"` (PowerShell:
> `$env:JAVA_HOME = "C:\Program Files\<your JDK install>"`) before running `mvnw`.

To run the test suite (uses an in-memory H2 database — no Docker needed):

```bash
./mvnw test
```

## Where to go next

| Looking for... | See |
|---|---|
| Architecture overview, decisions and rationale, all required scenarios, risks/trade-offs, known limitations | [`docs/FINAL_ENGINEERING_SUMMARY.md`](docs/FINAL_ENGINEERING_SUMMARY.md) — start here |
| Normalized requirements, acceptance criteria, open questions | [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) |
| The AI-assisted-process traceability log — every decision, option considered, rationale, and validation result | [`docs/AI_WORKLOG.md`](docs/AI_WORKLOG.md) |

This is a take-home/interview prototype, not a production deployment — see
`docs/FINAL_ENGINEERING_SUMMARY.md` for exactly which production-hardening concerns were
deliberately deferred, and why.

> A fourth document, `CLAUDE.md`, is maintained locally as day-to-day AI-agent operating
> instructions and the most granular architecture/convention detail. By deliberate decision it
> is not included in this repository — everything needed to evaluate this submission is in the
> three documents above.
