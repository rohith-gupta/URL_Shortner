# AI Worklog — Traceability Log

This log is the traceability record required by the assignment: every meaningful AI-assisted
engineering activity on this project is recorded here as it happens, in append-only fashion
(newest entry at the bottom). It exists to make visible what AI proposed, what the engineer
did with it, and why — not just the final diff.

Related: [docs/REQUIREMENTS.md](./REQUIREMENTS.md) (requirement PR-6, acceptance criteria "AI
task traceability"), [docs/FINAL_ENGINEERING_SUMMARY.md](./FINAL_ENGINEERING_SUMMARY.md).
(`CLAUDE.md` is also related but is a locally-maintained file, not part of this git
repository — see `docs/REQUIREMENTS.md` §6 item 19.)

## Entry format

Copy this block for every meaningful AI-assisted task:

```
### [YYYY-MM-DD] <Short task title>

- **Task**: What was being worked on.
- **Prompt / Intent**: What was asked of the AI, including constraints and acceptance criteria
  given (per CORE ENGINEERING REQUIREMENTS §4: Intent, Constraints, Acceptance Criteria,
  Technical Context).
- **AI recommendation**: What the AI proposed/produced (summary, or link/reference to the
  diff/artifact).
- **Engineer decision**: Accepted / Modified / Rejected
- **Rationale**: Why that decision was made.
- **Validation performed**: What was actually run/checked (tests, lint, security scan, manual
  review, etc.) and the result.
```

A task doesn't need an entry if it's trivial/mechanical (e.g., fixing a typo). Anything that
touches requirements, architecture, public contracts, data model, security, reliability, or
that an evaluator would reasonably want to see the reasoning behind, gets an entry.

---

## Log

### 2026-08-12 — Establish project foundation docs (CLAUDE.md, REQUIREMENTS.md, AI_WORKLOG.md)

- **Task**: Bootstrap the project's governance documents before any implementation, as
  instructed by the engineer at project kickoff.
- **Prompt / Intent**:
  - Intent: Create exactly three files — `CLAUDE.md`, `docs/REQUIREMENTS.md`,
    `docs/AI_WORKLOG.md` — reflecting the assignment brief as given, with no invented
    architecture, stack, or requirements.
  - Constraints: No application code, no stack/language selection, no scaffolding. Undecided
    items must be marked TBD rather than assumed.
  - Acceptance criteria: `docs/REQUIREMENTS.md` normalizes the brief into functional,
    non-functional, and engineering-process requirements plus deliverables, acceptance
    criteria, and open questions. `docs/AI_WORKLOG.md` establishes the traceability format and
    is usable going forward. `CLAUDE.md` documents current (empty) project state and the
    operating rules, without fabricating commands or architecture that don't exist yet.
  - Technical context: Repository is a brand-new git repo with no commits and no files other
    than `.git`. This is a take-home/interview assignment; the operating model is
    "engineer-led execution accelerated by AI," not autonomous orchestration.
- **AI recommendation**: Drafted all three files as specified: `docs/REQUIREMENTS.md` with
  FR/NFR/process-requirement tables sourced line-by-line from the brief, an acceptance-criteria
  table, and a 17-item open-questions list; `docs/AI_WORKLOG.md` with this entry format and
  first entry; `CLAUDE.md` with project status, operating rules, and TBD placeholders for
  architecture/stack/commands/testing/security.
- **Engineer decision**: *Pending review* — files created per explicit instruction; awaiting
  engineer confirmation that the normalization is accurate and nothing was invented beyond the
  brief. Update this entry to Accepted/Modified/Rejected once reviewed.
- **Rationale**: The assignment explicitly requires these three artifacts to exist before
  other work starts, and requires traceability from the first meaningful AI action onward — so
  the creation of the docs themselves is logged rather than treated as exempt "setup."
- **Validation performed**: Manual cross-check of `docs/REQUIREMENTS.md` content against the
  assignment brief text to confirm no requirement was invented and every brief section is
  represented (traceable via the "Source" column in each requirements table). No automated
  checks applicable yet (no code, no lint/test tooling configured).

### 2026-08-12 — Decision: Backend technology stack

- **Task**: Resolve TBD #1 (Technology stack) from `docs/REQUIREMENTS.md` §6 — select the
  backend language/framework/build tool for the URL Shortener prototype, per Operating Rule 1
  (no major technology decision without options and rationale presented first).
- **Prompt / Intent**: Engineer requested a comparison of 3 realistic backend stack options,
  explicitly requiring Java + Spring Boot as one of them, each evaluated against 14 named
  criteria: production suitability, dev speed for a 2–3 day prototype, API development,
  database integration, unit/integration testing, reliability features, security, performance,
  maintainability, scalability, observability, ease of demonstrating greenfield/brownfield,
  interview defensibility, and risks/trade-offs. Constraint: keep the architecture simple —
  no microservices, Kubernetes, or event streaming without a clear requirement. Instructed to
  recommend one option with rationale and trade-offs, but **not implement anything or record a
  decision** until explicit engineer approval.
- **Options evaluated**:
  - **A — Java 21 + Spring Boot** (Spring Web, Spring Data JPA, Spring Boot Validation, Spring
    Boot Test, Spring Boot Actuator, Maven)
  - **B — Node.js + TypeScript + Express** (Prisma, Jest + Supertest)
  - **C — Python + FastAPI** (SQLModel/SQLAlchemy + Alembic, Pytest + httpx)
- **AI recommendation**: Recommended **Option C (Python + FastAPI)**. Reasoning: the
  assignment is graded on engineering *process* breadth (traceability, greenfield + brownfield
  + ambiguous-requirement scenarios, quality gates) within a tight 2–3 day time-box, making
  development velocity a first-class factor; FastAPI's automatic OpenAPI/schema generation and
  low-friction pytest testing directly satisfy the API/schema-definitions and unit/integration
  test deliverables with minimal extra effort. Noted Option A (Java + Spring Boot) as the most
  conventionally "production-grade"/enterprise-defensible choice, with mature, cohesive
  tooling across validation, testing, security, and observability, but flagged its
  boilerplate/configuration overhead as a risk to the time budget. Noted Option B (Node + TS +
  Express) as a middle ground requiring more manual ecosystem assembly (validation, docs,
  rate limiting each a separate library choice) than either A or C.
- **Engineer decision**: **Accepted Option A — Java 21 + Spring Boot** (differs from the AI
  recommendation).
  - Approved components: Java 21; Spring Boot; Spring Web; Spring Data JPA; Spring Boot
    Validation; Spring Boot Test; Spring Boot Actuator; Maven.
  - **Database technology is explicitly NOT approved** and remains TBD (`docs/REQUIREMENTS.md`
    §6 item 10) — not part of this decision.
- **Engineer rationale**: Java + Spring Boot was selected because it provides strong
  enterprise conventions, mature testing and validation support, clear architectural layering,
  observability capabilities, and a highly defensible production-grade architecture for the
  interview assignment. It also provides a clear structure for demonstrating both greenfield
  development and later brownfield reasoning.
- **Trade-offs (accepted knowingly)**: More initial boilerplate and configuration than FastAPI
  or Express; slightly slower initial development for the 2–3 day prototype window; Spring
  Security and some reliability features can introduce complexity if added unnecessarily. To
  be controlled by keeping the architecture simple and avoiding unnecessary infrastructure.
- **Validation performed**: Architecture review only (three-option comparison presented,
  reviewed, and explicitly approved by the engineer). No application implementation has
  started; no automated checks applicable yet.

### 2026-08-12 — Decision: Core API scope

- **Task**: Resolve TBD #2 (Core API scope) from `docs/REQUIREMENTS.md` §6 item 2 — select the
  initial API surface for the greenfield URL Shortener prototype, per Operating Rule 1.
- **Prompt / Intent**: Engineer requested a comparison of 3 API-scope options — Minimal
  (create + redirect), Core + Analytics (adds retrieve-details + basic analytics), Extended
  (adds custom alias, update, delete, list, richer analytics) — each evaluated against 11
  criteria: alignment with the assignment, satisfaction of "core APIs," ability to demonstrate
  analytics, dev effort within 2–3 days, testing complexity, security considerations,
  maintainability, reliability implications, brownfield opportunity, risk of over-engineering,
  and interview defensibility. Constraint: the brief only names "core APIs, analytics, and
  reliability features" — do not invent unnecessary functionality. Instructed to recommend one
  option, propose its API operations at a conceptual level only (no exact schemas, no DB
  choice, no short-code strategy, no auth), and distinguish required-for-greenfield vs.
  potential-brownfield vs. intentionally-deferred, but not implement anything or record a
  decision until explicit engineer approval.
- **Options evaluated**:
  - **A — Minimal**: create + redirect only.
  - **B — Core + Analytics**: create, redirect, retrieve details, retrieve basic analytics.
  - **C — Extended**: B plus custom aliases, update, delete/deactivate, list, richer analytics.
- **AI recommendation**: Recommended **Option B**. Reasoning: A satisfies "core APIs" but
  entirely omits analytics, a capability the brief names explicitly (weak alignment/
  defensibility). C solves for product scope (aliases, edit, delete, list, rich analytics)
  the brief never asked for, at real cost to the 2–3 day budget, to Operating Rule 8 (simple
  architecture), and to security (forces a premature authz decision for update/delete). B
  satisfies all three named capabilities (core APIs, analytics; reliability is cross-cutting
  regardless of option) without inventing scope, fits the time budget, and — importantly —
  leaves Option C's extra features intact as well-scoped brownfield candidates for later in
  the assignment (FR-6), rather than front-loading everything into greenfield. Proposed 4
  conceptual operations for B: create, redirect, retrieve details, retrieve analytics — each
  with purpose/method/route/expected request-response/validation/failure cases described
  conceptually only.
- **Engineer decision**: **Accepted Option B — Core + Analytics** (matches the AI
  recommendation).
  - Approved initial API scope:
    1. `POST /api/urls` — create a shortened URL.
    2. `GET /{shortCode}` — redirect to the original URL.
    3. `GET /api/urls/{shortCode}` — retrieve short URL details without recording a click.
    4. `GET /api/urls/{shortCode}/analytics` — retrieve basic analytics (click/redirect count
       + minimal metadata).
  - **Explicitly deferred** (candidates for later brownfield/ambiguous-requirement scenarios,
    not decided now): custom aliases; update destination URL; delete/deactivate URL; URL
    expiration; rich analytics (device/referrer/geography); authentication/authorization.
- **Engineer rationale**: These deferred features may be considered later as brownfield or
  ambiguous-requirement scenarios, preserving them as meaningful, well-scoped later work
  rather than building them into the initial greenfield prototype.
- **Validation performed**: Architecture review only (three-option comparison presented,
  reviewed, and explicitly approved by the engineer). No application implementation has
  started; no automated checks applicable yet. Exact request/response schemas, database
  engine, and short-code generation strategy remain separate, not-yet-reviewed decisions.

### 2026-08-12 — Decision: Database technology

- **Task**: Resolve TBD (persistence/database engine, `docs/REQUIREMENTS.md` §6 item 10) —
  select the runtime datastore for the URL Shortener prototype.
- **Prompt / Intent**: Engineer asked for a recommendation between PostgreSQL and H2 for a
  "small but production-defensible interview submission," with 1–2 sentence rationale, then to
  wait for approval before recording or scaffolding.
- **AI recommendation**: PostgreSQL as the runtime datastore (H2 for tests only), run locally
  via Docker Compose. Rationale offered: an in-memory/embedded engine as the "real" runtime
  datastore would undercut the production-defensible bar the assignment is graded on, whereas
  PostgreSQL is a low-effort Docker Compose addition that lets reliability/concurrency behavior
  be demonstrated against a real engine.
- **Engineer decision**: **Accepted** — PostgreSQL is the primary runtime datastore, run
  locally via Docker Compose for easy reviewer setup. H2 may be used only for lightweight
  automated tests where appropriate. Whether a PostgreSQL Testcontainers-based integration test
  is also needed is explicitly left as a later decision, not resolved now.
- **Rationale**: Production-realistic persistence with straightforward Spring Data JPA
  integration and easy Docker-based reviewer setup.
- **Validation performed**: Architecture review only at decision time. Empirical validation
  (application actually connects to Postgres) performed later as part of the scaffolding task
  below — see that entry for what could and could not be verified in this environment.

### 2026-08-12 — Decision: Short-code generation strategy

- **Task**: Resolve TBD (`docs/REQUIREMENTS.md` §6 item 5) — select how short codes are
  generated.
- **Prompt / Intent**: Engineer asked for one simple, prototype-appropriate strategy, with
  1–2 sentence rationale, then to wait for approval before recording or scaffolding.
- **AI recommendation**: Base62-encode an auto-incrementing database ID (sequence/identity
  column → base62 string), generated server-side at creation time. Rationale offered:
  collision-free by construction (no retry loop needed), simple to implement/test, compact
  codes.
- **Engineer decision**: **Rejected / Modified.** The AI recommendation was explicitly
  rejected — sequential IDs encoded as Base62 make short codes predictable and enumerable,
  which is an unacceptable risk for a public-facing URL shortener.
  - **Approved alternative**: generate a random 7-character Base62 code (character set
    `0-9a-zA-Z`) using `java.security.SecureRandom` (not `java.util.Random`); enforce a
    `UNIQUE` constraint on the short-code column in PostgreSQL; on collision, generate another
    code and retry, with a small, bounded number of retries. No distributed ID generators or
    hashing infrastructure.
- **Engineer rationale**: Sequential IDs create predictable short codes and increase
  enumeration risk; random generation with a uniqueness constraint and bounded retry avoids
  that risk without adding unnecessary complexity.
- **Validation performed**: Architecture review only at decision time; this decision is a
  preserved example of an AI recommendation being explicitly rejected and replaced by the
  engineer, per the assignment's traceability requirement. The strategy itself is not yet
  implemented (no `entity`/`service` code exists yet) — implementation and its own validation
  (uniqueness constraint behavior, retry-bound test) are future work.

### 2026-08-12 — Scaffold initial Spring Boot project

- **Task**: Create the initial Spring Boot project skeleton (build config, dependencies,
  package structure, local Postgres via Docker Compose, base application config, Actuator
  health) per the engineer's approved stack (Java 21, Spring Boot, Maven, Spring Web, Spring
  Data JPA, Validation, Actuator, PostgreSQL driver, H2 test dependency, Spring Boot Test,
  OpenAPI/Swagger) and approved API/database/short-code decisions above.
- **Prompt / Intent**: Scaffold only — no URL-shortener functionality (no entities, no
  controllers implementing the approved endpoints). Configure PostgreSQL via environment
  variables; add `docker-compose.yml`; add basic application config and an Actuator health
  endpoint; verify the application builds and starts; run available quality checks; update
  `CLAUDE.md` with the real, discovered build/run/test commands.
- **AI actions and notable implementation decisions** (surfaced here because they were not
  pre-approved line items and a reviewer should be able to see them):
  - Generated the base project via Spring Initializr (`start.spring.io`) with groupId
    `com.urlshortener`, artifactId `url-shortener`, Java 21, dependencies: web, data-jpa,
    validation, actuator, postgresql, h2.
  - **Spring Boot version correction**: Initializr's metadata advertised `4.1.0.RELEASE` as
    the current default, and generated a `pom.xml` with that exact parent version — but that
    coordinate does not exist on Maven Central (confirmed via direct repository lookup); the
    real resolvable coordinate is `4.1.0` (no `.RELEASE` suffix, consistent with Spring Boot's
    versioning since the 2.x line). Corrected `pom.xml` to `4.1.0`, the latest version that
    both start.spring.io accepts and Maven Central can actually resolve. Flagging this because
    it silently would have failed the build otherwise, not because it changes the approved
    stack (still Spring Boot, still Java 21).
  - **OpenAPI/Swagger dependency**: `springdoc-openapi-starter-webmvc-ui` is not offered by
    Spring Initializr, so it was added manually. Its latest published version (2.8.6) predates
    Spring Boot 4 / Spring Framework 7 (no newer major line exists on Maven Central yet), which
    was a real compatibility risk. Verified empirically (see Validation below) rather than
    assumed — it works: `/v3/api-docs` and `/swagger-ui/index.html` both serve correctly at
    runtime.
  - **Dual datasource config**: `src/main/resources/application.yml` configures PostgreSQL via
    env vars (`SPRING_DATASOURCE_URL` etc., defaulting to values matching
    `docker-compose.yml`); `src/test/resources/application.yml` configures H2 in-memory,
    consistent with the approved database decision ("H2 may be used only for lightweight
    automated tests"). This lets `./mvnw test` run without Docker.
  - **Schema/migration strategy deliberately not decided**: set `spring.jpa.hibernate.ddl-auto:
    none` (no-op) in the runtime config rather than picking Hibernate auto-DDL or a migration
    tool (Flyway/Liquibase) by default, since that choice hasn't been through an
    options-and-rationale review. Added as a new open question in `docs/REQUIREMENTS.md` §6
    (item 18) rather than deciding it implicitly.
  - `spring.jpa.open-in-view: false` set in both configs — a standard, low-risk REST API
    default (avoids lazy-loading outside the service layer), not a scope decision.
  - Created `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `config`
    packages under `com.urlshortener`, each with a `package-info.java` documenting its role
    only — no business logic, no entities, no endpoints implementing the approved API scope.
  - `docker-compose.yml` added for local PostgreSQL 17 (alpine image), with defaults matching
    `application.yml` and a healthcheck.
- **Engineer decision**: Pending review — scaffolding created per explicit instruction;
  awaiting engineer confirmation, in particular of the Spring Boot version correction and the
  springdoc compatibility call, both discovered during execution rather than pre-approved.
- **Rationale**: The literal instructed version string was unresolvable; substituting the
  nearest actually-resolvable version preserves the approved stack decision (Java 21 + Spring
  Boot + Maven) while making the build possible at all. Deferring schema/migration strategy
  keeps to Operating Rule 1 (no architecture decision without options-and-rationale review)
  rather than picking one implicitly via a config default.
- **Validation performed**:
  - `./mvnw clean compile` — succeeded.
  - `./mvnw test` — 1 test run (`UrlShortenerApplicationTests#contextLoads`), 0 failures, 0
    errors, against H2 (test config). Confirms the Spring context — including Data JPA,
    Validation, Actuator, and springdoc auto-configuration together — starts cleanly.
  - `./mvnw clean package` — succeeded, produced
    `target/url-shortener-0.0.1-SNAPSHOT.jar`.
  - Manual runtime smoke test: ran the packaged app with H2 (env-var override) on a free port,
    confirmed `GET /actuator/health` → `200 {"status":"UP",...}`, `GET /v3/api-docs` → valid
    OpenAPI 3.1 JSON, `GET /swagger-ui/index.html` → `200`. Process was then stopped.
  - **Not verified**: a real connection to PostgreSQL. `docker-compose.yml` was written and
    `docker compose up -d` was attempted, but the Docker Desktop daemon is not running in this
    environment (`docker info` fails to reach the engine) — this is an environment limitation,
    not a code issue, and is called out explicitly rather than silently skipped. Recommended
    follow-up: engineer runs `docker compose up -d` locally (with Docker Desktop running) and
    `./mvnw spring-boot:run`, then confirms `/actuator/health` reports the datasource healthy
    against real Postgres.
  - No lint/static-analysis tool is configured yet (not a decided item) — flagged as an open
    gap in `docs/REQUIREMENTS.md`, not silently skipped.

### 2026-08-12 — Verify PostgreSQL runtime configuration end-to-end

- **Task**: With Docker Desktop now running, verify (not just write) the PostgreSQL runtime
  path left unverified in the previous scaffolding entry: container up, app connects to
  Postgres (not H2), `/actuator/health` UP, OpenAPI/Swagger still load, tests pass, build
  succeeds.
- **Prompt / Intent**: Engineer instruction, scoped explicitly to verification only — no
  URL-shortener implementation. "If anything fails, diagnose and fix only the configuration
  issue, then rerun validation." Record meaningful results/issues here; update `CLAUDE.md`
  only if an actual command/config detail changed.
- **What happened**:
  1. `docker ps` showed `url-shortener-postgres` already up and reporting `healthy` (started
     shortly before this task, via `docker-compose.yml`'s defaults).
  2. First app start against Postgres (default `application.yml`, no H2 override) **failed**:
     `org.postgresql.util.PSQLException: FATAL: password authentication failed for user
     "urlshortener"`, even though the configured username/password matched the container's own
     `POSTGRES_USER`/`POSTGRES_PASSWORD` env vars.
  3. **Diagnosis**: `docker exec ... psql -U urlshortener -d urlshortener` from *inside* the
     container succeeded with the same credentials — proving the container itself was fine and
     the failure was specific to connections from the host. Recreating the container from a
     clean volume (`docker compose down -v && docker compose up -d`) did not fix it, ruling out
     a stale-volume/stale-credentials theory. Checked `Get-NetTCPConnection -LocalPort 5432`
     (PowerShell) and found **two** processes bound to port 5432: Docker's backend, and a
     separate **native (non-Docker) `postgres.exe` process already running on this Windows
     machine**. The native service was silently intercepting host connections to
     `localhost:5432` — same host, same port, different server, hence "wrong password" for a
     user that only exists in the container's database.
  4. **Fix applied** (config-only, does not touch the pre-existing native Postgres install):
     remapped the container's host port from `5432:5432` to `5433:5432` in
     `docker-compose.yml`, and updated the default `SPRING_DATASOURCE_URL` in
     `src/main/resources/application.yml` from `jdbc:postgresql://localhost:5432/urlshortener`
     to `...5433/urlshortener`. Recreated the container on the new port and reran the app.
  5. Re-verified end to end (see Validation below) — all green.
- **Engineer decision**: Pending review — this is a diagnosed environment/config fix applied
  within the explicit "diagnose and fix only the configuration issue" instruction, not a new
  architecture decision. Flagging the port number specifically for engineer awareness since
  it's a discovered fact about this machine, not a design choice.
- **Rationale**: A port remap is the narrowest fix that resolves the collision without touching
  or stopping a pre-existing service on the host that this project doesn't own and has no
  visibility into what else depends on it.
- **Validation performed**:
  1. **Container status**: `docker ps` → `url-shortener-postgres`, image `postgres:17-alpine`,
     `Up ... (healthy)`, `0.0.0.0:5433->5432/tcp`.
  2. **App startup against Postgres**: `./mvnw spring-boot:run` (default config, `SERVER_PORT`
     overridden to 8081 only because port 8080 was already occupied by an unrelated process on
     this machine) → started cleanly. Log confirms
     `Database JDBC URL [jdbc:postgresql://localhost:5433/urlshortener]`,
     `Database dialect: PostgreSQLDialect`, `HikariPool-1 - Added connection
     org.postgresql.jdbc.PgConnection@...`, `Started UrlShortenerApplication in 4.741 seconds`.
  3. **Health**: `GET /actuator/health` → `200 {"status":"UP","groups":["liveness","readiness"]}`.
  4. **OpenAPI/Swagger**: `GET /v3/api-docs` → valid OpenAPI 3.1 JSON; `GET
     /swagger-ui/index.html` → `200`.
  5. **Tests**: `./mvnw test` → 1 run, 0 failures, 0 errors (against H2, per test config —
     unaffected by the Postgres port change).
  6. **Build**: `./mvnw clean package` → success, `target/url-shortener-0.0.1-SNAPSHOT.jar`
     produced.
  7. App process stopped after verification; no leftover Java processes. Postgres container
     left running (healthy) as the normal local-dev state.
- **Follow-up note, not yet actioned**: the native Postgres service on this machine was left
  untouched (out of scope — it's not this project's to stop/reconfigure). If a *different*
  environment doesn't have that native service, the default port 5432 would also work; 5433 is
  simply the safer default going forward for this machine and is now documented in
  `docker-compose.yml`.

### 2026-08-12 — Decision: Schema management (Flyway)

- **Task**: Record an additional database decision — schema management approach — ahead of the
  first vertical slice implementation.
- **Prompt / Intent**: Engineer directive (not an AI-recommendation-and-approval this time):
  use Flyway for migrations, Hibernate `ddl-auto=validate`, no Hibernate auto-create/update of
  the production schema, add the Flyway dependency and an initial V1 migration.
- **AI recommendation**: N/A — the engineer specified the decision directly rather than asking
  for options to be compared. Recorded here for traceability/completeness, not because AI
  proposed and the engineer chose between alternatives.
- **Engineer decision**: **Directed** — Flyway owns schema migrations
  (`src/main/resources/db/migration`); Hibernate `ddl-auto: validate` in the runtime config
  (never `create`/`update`/`create-drop` against the real database); Flyway dependency and a
  V1 migration to be added as part of this task.
- **Engineer rationale**: Keeps database schema changes explicit, versioned, reproducible, and
  easy for an interviewer to review, without adding significant complexity.
- **Validation performed**: Deferred to the implementation task below — a schema/entity
  mismatch under `ddl-auto=validate` would fail application startup immediately, which is
  itself the validation mechanism for this decision. See "Implement POST /api/urls" for the
  actual migration content and confirmation it applied cleanly against real PostgreSQL.

### 2026-08-12 — Implement POST /api/urls (first vertical slice)

- **Task**: Implement the create-short-URL functionality only — the first of the 4 approved
  API endpoints (`docs/AI_WORKLOG.md`, "Decision: Core API scope") — end to end: entity,
  Flyway migration, short-code generator, request/response DTOs, service layer, controller,
  centralized error handling, tests, OpenAPI visibility.
- **Prompt / Intent**: Detailed engineer specification covering: entity fields and constraints
  (id, originalUrl, shortCode unique/non-null/7 chars, createdAt, clickCount defaulting to 0);
  a PostgreSQL-targeted V1 Flyway migration; the already-approved short-code strategy
  (SecureRandom Base62, no sequential IDs, bounded collision retry, DB uniqueness as the real
  guarantee); a validated request DTO (required/non-blank/syntactically-valid/http-https-only,
  clean 400 on failure); a response DTO independent of the entity; business logic in the
  service layer with the controller kept thin; centralized exception handling with no leaked
  internals; a specified minimum test list; OpenAPI visibility for the new endpoint. Explicitly
  **not** in scope: redirect, details, analytics, auth, expiration, aliases, caching, rate
  limiting. Required real end-to-end validation against PostgreSQL (Docker Compose up, Flyway
  migration applied, real HTTP request, row verified in the database) in addition to automated
  tests.
- **AI-generated implementation**:
  - `entity/UrlMapping.java` — id (`IDENTITY`), originalUrl (2048), shortCode (7, unique),
    createdAt (set in the constructor), clickCount (defaults to 0).
  - `db/migration/V1__create_url_mapping_table.sql` — matches the entity exactly (column
    lengths Hibernate's `validate` mode actually checks), plus a `CHECK (char_length(short_code)
    = 7)` constraint the entity mapping can't itself express, and no separate index on
    `short_code` (the `UNIQUE` constraint already provides one).
  - `service/ShortCodeGenerator.java` — `SecureRandom`, 7-char Base62, zero Spring/persistence
    dependencies (trivially unit-testable).
  - `dto/CreateUrlRequest.java` / `CreateUrlResponse.java` — Java records; a custom
    `dto/validation/HttpUrl` Bean Validation constraint (+ `HttpUrlValidator`) restricting to
    syntactically-valid `http`/`https` URLs only.
  - `service/UrlService.java` — bounded retry (5 attempts) across both a pre-check
    (`existsByShortCode`) and an actual `DataIntegrityViolationException` from the database
    constraint; **not** `@Transactional` — see decision below.
  - `controller/UrlController.java` — `POST /api/urls`, `201` + `Location` header + response
    DTO; OpenAPI annotations.
  - `exception/GlobalExceptionHandler.java` + `ErrorResponse` + `ShortCodeGenerationException`
    — uniform JSON error shape, no stack traces, handles validation failures, malformed JSON,
    and the (practically unreachable) exhausted-retry case.
  - `config/OpenApiConfig.java` — API title/description/version for Swagger UI.
  - 75 tests total across 7 classes (unit, `@DataJpaTest`, `@WebMvcTest`, full-stack
    `@SpringBootTest`) — see `CLAUDE.md` Testing strategy for the full breakdown.
- **Engineer-approved constraints honored**: no redirect/details/analytics endpoints; no auth;
  no expiration/aliases/caching/rate limiting; SecureRandom (not `java.util.Random`); no
  sequential IDs; DB uniqueness as the final collision guarantee with a small bounded retry;
  entity never returned directly from the controller; controller kept thin.
- **Important implementation decisions not dictated line-by-line by the spec** (surfaced for
  engineer visibility, none are scope changes):
  - **Not `@Transactional` on `UrlService.createShortUrl`** — wrapping the whole bounded-retry
    loop in one transaction would let a unique-constraint violation from PostgreSQL mark that
    transaction rollback-only ("current transaction is aborted..."), breaking retry: the
    *next* attempt would fail too, even with a fresh, non-colliding code. Each repository call
    getting its own transaction (Spring Data JPA's default when the caller isn't transactional)
    avoids this. Documented as a code comment on the class, not just here.
  - **Custom `@HttpUrl` validator, not Hibernate Validator's built-in `@URL`** — the built-in
    constraint doesn't support restricting to multiple specific schemes (only a single fixed
    `protocol` attribute), so it couldn't express "http or https only" directly. Placed under
    `dto/validation/` (a subpackage of the approved `dto` package) rather than inventing a new
    top-level package outside `controller/service/repository/entity/dto/exception/config`.
  - **Location header points at `/api/urls/{shortCode}`** (the future GET-details endpoint),
    not the public redirect URL `/{shortCode}` — those are different resources with different
    semantics; only the former is "the resource this POST created" in REST terms. Neither
    endpoint exists yet, but the URI itself is still meaningful.
  - **`app.base-url` config property** (default `http://localhost:8080`, override via
    `APP_BASE_URL`) to build the response's `shortUrl` field — not previously specified;
    a minimal, necessary addition to produce a complete response, kept simple (static config,
    not derived from request host/port).
- **Failures encountered and fixes applied** (Spring Boot 4.1-specific; none were architecture
  changes):
  1. `@WebMvcTest`, `@DataJpaTest`, `TestRestTemplate` all failed to compile at their Boot-3-era
     import paths — Boot 4.1 moved them (`org.springframework.boot.webmvc.test.autoconfigure`,
     `org.springframework.boot.data.jpa.test.autoconfigure`,
     `org.springframework.boot.resttestclient`, respectively). Fixed by updating imports after
     inspecting the actual jars on the classpath rather than guessing.
  2. `TestRestTemplate` then failed to autowire (`No qualifying bean`) — Boot 4.1 requires an
     explicit `@AutoConfigureTestRestTemplate` now; it's no longer auto-registered for
     `RANDOM_PORT` tests. Adding the annotation surfaced a *second*, deeper problem:
     `NoClassDefFoundError: RestTemplateBuilder` — the autoconfiguration needs a module we
     didn't otherwise depend on. Rather than add a dependency purely for test plumbing, rewrote
     `CreateShortUrlIntegrationTest` to use `@SpringBootTest` + `@AutoConfigureMockMvc` +
     `MockMvc` instead — proves the same thing (real service/repository/H2 over real HTTP
     handling) without the extra dependency.
  3. **Flyway never ran** (no Flyway log lines, straight to Hibernate's "missing table
     `url_mapping`" schema-validation failure) despite `flyway-core` +
     `flyway-database-postgresql` being on the classpath and `spring.flyway.enabled: true` set.
     Root cause: Boot 4 moved Flyway's actual autoconfiguration into a separate
     `spring-boot-flyway` module that `flyway-core` alone doesn't pull in — only
     `spring-boot-starter-flyway` does. Fixed by depending on the starter instead of
     `flyway-core` directly (kept `flyway-database-postgresql` since Postgres support isn't
     bundled by either).
  4. Port collisions (8080 occupied by an unrelated process; the existing 5433 Postgres remap
     from the prior verification task) — already-known environment facts, not new problems;
     worked around the same way as before (alternate port for ad hoc runs).
- **Engineer decision**: Pending review — implementation completed per the detailed spec and
  fully validated (see below); awaiting engineer confirmation, in particular of the four
  judgment calls listed above that weren't explicitly dictated.
- **Validation performed**:
  - `./mvnw clean test` → **75 tests, 0 failures, 0 errors** across `ShortCodeGeneratorTest`
    (42), `HttpUrlValidatorTest` (15), `UrlServiceTest` (5), `UrlMappingRepositoryTest` (3),
    `UrlControllerTest` (5), `CreateShortUrlIntegrationTest` (4),
    `UrlShortenerApplicationTests` (1) — against H2, Flyway disabled for tests (see
    `CLAUDE.md` Testing strategy for why).
  - `./mvnw clean package` → success, jar produced.
  - **Real PostgreSQL, end to end**: confirmed `url-shortener-postgres` container healthy
    (`docker ps`); started the app against the real datasource (no H2 override); log confirmed
    Flyway ran ("Successfully validated 1 migration", created `flyway_schema_history`) *before*
    Hibernate's `validate` check, and the app started cleanly (proving entity ⇄ schema
    agreement). `POST /api/urls` with a real HTTP request → `201`, short code `UAKdtZB`
    (matches `[0-9a-zA-Z]{7}`), `Location: .../api/urls/UAKdtZB`. Confirmed via
    `docker exec ... psql`: the row is actually persisted
    (`SELECT * FROM url_mapping` returned it, `count(*) = 1`), and `\d url_mapping` shows the
    live schema matches the migration exactly (types, `NOT NULL`, `UNIQUE`, `CHECK`).
    Blank `originalUrl` → `400` with a field error; `ftp://...` → `400` (scheme rejected).
    `/actuator/health` → `200 UP`; `/v3/api-docs` includes the new `/api/urls` path;
    `/swagger-ui/index.html` → `200`. App process stopped afterward; no leftover Java
    processes; Postgres container left running.
- **Known limitations surfaced, not fixed (out of the approved scope for this slice)**: no
  SSRF/private-network protection beyond scheme allow-listing; no auth; no rate limiting — see
  `CLAUDE.md` Security approach for the explicit list.

### 2026-08-12 — Implement GET /{shortCode} (second vertical slice)

- **Task / Intent**: Implement the redirect endpoint only — the second of the 4 approved API
  endpoints — end to end: route, service resolve+increment logic, repository additions,
  centralized 404 handling, tests, real-PostgreSQL validation.
- **Engineer constraints honored**: route constrained to exactly 7 Base62 characters where
  practical; 302 (not 301), explicitly because 301's cacheability would let browsers/proxies
  bypass the service — and its click counting — on repeat visits; entity never returned in the
  response body; unknown code → 404 via the existing centralized error format, no internal
  details leaked; malformed code → also a normal 404, not a validation-style error that would
  hint at the short-code format; click-count increment must be safe under concurrency, using
  the simplest production-defensible atomic DB approach (not Redis/distributed locks/queues);
  controller kept thin, service does resolve/not-found/increment/return-URL, with transactional
  boundaries explicit and explained; repository gets only the two methods actually needed.
  Explicitly not built: details endpoint, analytics endpoint, expiration, aliases, auth,
  caching, rate limiting.
- **AI implementation approach**:
  - `repository/UrlMappingRepository`: added `findByShortCode` (plain derived query) and
    `incrementClickCount` — a `@Modifying` `@Query("UPDATE UrlMapping u SET u.clickCount =
    u.clickCount + 1 WHERE u.shortCode = :shortCode")`, i.e. the increment happens as one
    atomic database statement, never as read-in-Java-then-write-back.
  - `service/UrlService#resolveAndRecordRedirect`: looks up the mapping (throws
    `ShortCodeNotFoundException` if absent), calls the atomic increment, returns the original
    URL. `@Transactional` — see "why" below.
  - `controller/RedirectController` (new, separate from `UrlController` — different resource,
    different semantics): `@GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")`, returns
    `ResponseEntity<Void>` with status `302` and a `Location` header, empty body.
  - `exception/ShortCodeNotFoundException` + a new `GlobalExceptionHandler` case → `404`.
- **Why HTTP 302 was selected**: per the engineer's explicit instruction — 301 is treated as
  permanently cacheable by browsers and intermediate proxies, so after the first redirect a
  client could resolve the short URL locally forever, never hitting this service again on
  subsequent clicks. That would silently undercount (or entirely stop counting) redirects for
  any client that cached a 301. 302 signals "temporary," so clients keep asking the service
  each time, which is what makes click counting meaningful at all.
- **Click-count concurrency approach**: a single atomic `UPDATE ... SET click_count =
  click_count + 1 WHERE short_code = ?`. This is safe under concurrent redirects because the
  database serializes the update at the row level — there is no "read current value in the
  application, compute new value, write it back" step for two concurrent requests to race on,
  which is exactly the lost-update failure mode the engineer flagged to avoid. No Redis,
  distributed locks, or message queue — the atomicity guarantee comes entirely from the
  database doing a single `x = x + 1` statement, which is the simplest mechanism that's still
  genuinely correct under concurrency, not a simplification that happens to usually work.
  `@Transactional` on `resolveAndRecordRedirect` exists for a different reason than the
  atomicity itself (Spring Data's `@Modifying` queries require an active transaction to run at
  all — confirmed empirically, see Problems below) and is documented in code as such, so it
  isn't mistaken for "the thing that makes the counter safe" later.
- **Problems encountered and fixes applied** (all real bugs caught by tests, not just Boot-4
  API-location trivia this time):
  1. **False 500s for malformed short codes.** A request that matches no controller route at
     all (e.g. a 5-character path) falls through to Spring's static-resource handling, which
     throws `NoResourceFoundException`. The existing `GlobalExceptionHandler`'s catch-all
     `@ExceptionHandler(Exception.class)` was catching that too, turning every "no route
     matched" case into an incorrect `500` instead of `404`. Root-caused by adding logging to
     the catch-all handler (it had none — also fixed, since a client-facing generic message
     with no server-side trace is a real diagnosability gap on its own) and reading the
     resulting stack trace. Fixed with an explicit
     `@ExceptionHandler(NoResourceFoundException.class)` → `404`, which Spring dispatches to in
     preference to the generic catch-all automatically. This is a genuine, general-purpose fix
     to shared exception handling, not something scoped only to the redirect endpoint.
  2. **Stale cached `clickCount` after a bulk update, within the same persistence context.** A
     `@Modifying` bulk `UPDATE` bypasses Hibernate's first-level cache; an entity already loaded
     earlier in the same transaction (e.g. by `findByShortCode` just before the increment) would
     keep showing its old in-memory `clickCount` on a later read in that same transaction, even
     though the database row is correct. Caught by reasoning through the JPA semantics before it
     could bite a test silently; fixed with `@Modifying(clearAutomatically = true)`.
  3. **Concurrency test initially "passed" for the wrong reason, then failed loudly once fixed
     to actually check.** The first version of `UrlMappingClickCountConcurrencyTest` used
     `executor.submit(...)` without ever calling `.get()` on the resulting `Future`s — so when
     every worker thread's call actually threw `TransactionRequiredException` (see #4), the
     exceptions were silently swallowed and the assertion just failed with "expected 20, was 0,"
     which looked like a lost-update bug rather than the real cause. Fixed the test to collect
     and `.get()` every `Future`, surfacing the real exception.
  4. **`TransactionRequiredException: No active transaction for update or delete query`** when
     calling `repository.incrementClickCount(...)` directly from worker threads with no
     surrounding transaction. This disproved an assumption made while writing the first version
     of the concurrency test — that Spring Data's repository proxy auto-supplies a transaction
     for any repository call with none active. It doesn't, for `@Modifying` queries specifically;
     Spring Data requires the *caller* to provide one. Fixed by having the concurrency test call
     the real `UrlService.resolveAndRecordRedirect` (already `@Transactional`) instead of the
     repository directly — which is also more representative of real redirect traffic anyway.
- **Engineer decision**: Pending review — implementation completed per the detailed spec and
  fully validated (see below); awaiting engineer confirmation, in particular of the
  `NoResourceFoundException` fix (a change to shared, not redirect-only, exception handling)
  and the `@Transactional` placement rationale.
- **Validation performed**:
  - `./mvnw clean test` → **90 tests, 0 failures, 0 errors** (was 75; +15 across
    `UrlServiceTest` (+2), `UrlMappingRepositoryTest` (+3),
    `UrlMappingClickCountConcurrencyTest` (new, 1), `RedirectControllerTest` (new, 5),
    `RedirectIntegrationTest` (new, 4) — against H2. `CreateShortUrlIntegrationTest` and
    `UrlControllerTest` (the create-slice tests) rerun unchanged and still pass, confirming
    `POST /api/urls` behavior is unaffected.
  - `./mvnw clean package` → success, jar produced.
  - **Real PostgreSQL, end to end** (Docker Compose already up from the prior session; app
    started against the real datasource, `SERVER_PORT` overridden only because 8080 was
    occupied by an unrelated process on this machine):
    1. `POST /api/urls` with `{"originalUrl":"https://example.com/redirect-slice-target"}` →
       `201`, short code `sbXyBZ7`.
    2. `docker exec ... psql`: `click_count` for `sbXyBZ7` = `0` before any redirect.
    3. `GET /sbXyBZ7` → `302`, `Location: https://example.com/redirect-slice-target`.
    4. `psql`: `click_count` = `1`.
    5. `GET /sbXyBZ7` again → `302`; `psql`: `click_count` = `2`.
    6. `GET /zzzzzzz` (well-formed, unknown) → `404`,
       `{"status":404,"error":"Not Found","message":"No short URL found for code
       'zzzzzzz'",...}`.
    7. `GET /short` (malformed — too short, doesn't match the route) → `404`, same centralized
       `ErrorResponse` shape (`"message":"Resource not found"`), confirming the
       `NoResourceFoundException` fix.
    8. `/actuator/health` → `200 UP`; `/v3/api-docs` includes both `/api/urls` and
       `/{shortCode}`; `/swagger-ui/index.html` → `200`.
    9. App process stopped afterward; no leftover Java processes; Postgres container left
       running (healthy).
- **Known limitations, unchanged from the prior slice**: no SSRF/private-network protection, no
  auth, no rate limiting — see `CLAUDE.md` Security approach.

### 2026-08-12 — Implement GET /api/urls/{shortCode} (third vertical slice)

- **Task / Intent**: Implement the read-only URL-details endpoint only — the third of the 4
  approved API endpoints. Given the same short-code format rules already established, reusing
  the existing repository lookup and error handling rather than adding anything new.
- **Engineer constraints honored**: 7-character Base62 route constraint reused verbatim
  (matches `RedirectController`'s pattern); response is a dedicated DTO (`originalUrl`,
  `shortCode`, `shortUrl`, `createdAt`) — no `clickCount` (that's the not-yet-built analytics
  endpoint's job) and never the JPA entity; the endpoint must not redirect, increment
  `clickCount`, or modify the record in any way; unknown code → `404` via the existing
  `ShortCodeNotFoundException`/`GlobalExceptionHandler` path; malformed code → `404` the same
  way malformed codes already do for the redirect endpoint (no new special-casing needed, since
  the route-constraint + `NoResourceFoundException` handling from the prior slice already
  covers this generically); controller kept thin; service does lookup + not-found + map-to-DTO,
  with `@Transactional(readOnly = true)` and its rationale documented; `findByShortCode` reused
  as-is, no new/duplicate repository method added.
- **AI implementation approach**:
  - `dto/UrlDetailsResponse` (new record) — `originalUrl`, `shortCode`, `shortUrl`,
    `createdAt`. No `clickCount` field at all, not just omitted from serialization — the type
    itself can't carry it, so there's no accidental-exposure risk later.
  - `service/UrlService#getUrlDetails` (new method) — `@Transactional(readOnly = true)`, calls
    the existing `findByShortCode`, throws the existing `ShortCodeNotFoundException` if absent,
    maps to `UrlDetailsResponse`. No repository changes needed.
  - `controller/UrlController#getUrlDetails` (new method, same controller as `POST
    /api/urls` — same resource family, `/api/urls/...`) — `@GetMapping("/{shortCode:
    [0-9a-zA-Z]{7}}")`, returns the DTO directly (plain `200`, no special headers needed here
    unlike create's `Location` or redirect's `302`+`Location`).
  - Small refactor while adding this: extracted `UrlService#buildShortUrl` (was inlined in
    `createShortUrl`'s response-building) so both `createShortUrl` and `getUrlDetails` build
    the `shortUrl` field the same way instead of two copies that could drift.
- **Read-only behavior**: enforced at three levels, not just asserted — (1) the service method
  only calls `findByShortCode`, never `incrementClickCount` or `save`; (2)
  `@Transactional(readOnly = true)` documents the intent so a future accidental write would look
  wrong at a glance; (3) `UrlDetailsIntegrationTest` proves it end-to-end against a real
  database: details call → count still `0` → a real redirect → count `1` → details call again →
  count still `1`, not `2`.
- **Problems encountered**: none — this slice reused enough already-established groundwork
  (route-constraint pattern, `ShortCodeNotFoundException`, `NoResourceFoundException` handling,
  `MockMvc`-based full-stack tests) that it compiled and passed on the first attempt, including
  the real-PostgreSQL validation. Worth noting precisely because it's a contrast to the previous
  slice: the fixes made there (the `NoResourceFoundException` handler in particular) paid off
  immediately here rather than needing to be rediscovered.
- **Engineer decision**: Pending review — implementation completed per the detailed spec and
  fully validated (see below).
- **Validation performed**:
  - `./mvnw clean test` → **99 tests, 0 failures, 0 errors** (was 90; +9 across `UrlServiceTest`
    (+2: details success, details-not-found-does-not-increment), `UrlControllerTest` (+3:
    details 200/404/malformed), `UrlDetailsIntegrationTest` (new, 4)) — against H2.
    `CreateShortUrlIntegrationTest` and `RedirectIntegrationTest` rerun unchanged and still
    pass, confirming create and redirect are unaffected.
  - `./mvnw clean package` → success, jar produced.
  - **Real PostgreSQL, end to end** (Docker Compose already up from the prior session; app
    started against the real datasource, `SERVER_PORT` overridden only because 8080 was
    occupied by an unrelated process on this machine):
    1. `POST /api/urls` with `{"originalUrl":"https://example.com/details-slice-target"}` →
       `201`, short code `TgZDSi8`.
    2. `GET /api/urls/TgZDSi8` → `200`, body matches exactly what was created; `psql`:
       `click_count` = `0` (unaffected by the details call).
    3. `GET /TgZDSi8` (redirect, once) → `302`; `psql`: `click_count` = `1`.
    4. `GET /api/urls/TgZDSi8` again → `200`; `psql`: `click_count` still `1` — the second
       details call did not increment it.
    5. `GET /api/urls/zzzzzzz` (well-formed, unknown) → `404`,
       `{"status":404,"error":"Not Found","message":"No short URL found for code
       'zzzzzzz'",...}`.
    6. `GET /api/urls/short` (malformed) → `404`, `{"message":"Resource not found",...}` — same
       shape, confirming the generic `NoResourceFoundException` handling from the prior slice
       covers this route too without any endpoint-specific work.
    7. `/actuator/health` → `200 UP`; `/v3/api-docs` includes all three implemented paths
       (`/api/urls`, `/{shortCode}`, `/api/urls/{shortCode}`); `/swagger-ui/index.html` → `200`.
    8. App process stopped afterward; no leftover Java processes; Postgres container left
       running (healthy).
- **Known limitations, unchanged from prior slices**: no SSRF/private-network protection, no
  auth, no rate limiting — see `CLAUDE.md` Security approach. Analytics endpoint (`GET
  /api/urls/{shortCode}/analytics`) remains the one not-yet-implemented endpoint of the
  approved API scope.

### 2026-08-12 — Implement GET /api/urls/{shortCode}/analytics (fourth and final greenfield API slice)

- **Task / Intent**: Implement the basic click-count analytics endpoint — the fourth and last
  of the 4 approved API endpoints, closing out the initial greenfield API surface.
- **Engineer-approved scope**: `clickCount` plus `shortCode`/`shortUrl`/`originalUrl`/
  `createdAt` — the counter already maintained on `UrlMapping` by the redirect endpoint,
  nothing computed or stored beyond it. Explicitly excluded: referrer tracking, device/browser
  tracking, geographic analytics, and anything else beyond the one counter. 7-character Base62
  short-code rules reused unchanged. Read-only: must not redirect, increment `clickCount`,
  modify the mapping, or create any kind of analytics "event" record. `404` via the existing
  `ShortCodeNotFoundException` path for unknown codes, and via the existing
  `NoResourceFoundException` handling (from the redirect slice) for malformed ones — no new
  error-handling code needed for either case. Controller kept thin; service does
  lookup + not-found + map-to-DTO with `@Transactional(readOnly = true)`; `findByShortCode`
  reused as-is, no new/duplicate repository method.
- **Implementation approach**:
  - `dto/UrlAnalyticsResponse` (new record) — `shortCode`, `shortUrl`, `originalUrl`,
    `clickCount`, `createdAt`. No richer fields exist on the type at all, so there's nothing to
    accidentally wire up later without a deliberate decision.
  - `service/UrlService#getUrlAnalytics` (new method) — `@Transactional(readOnly = true)`,
    calls the existing `findByShortCode`, throws the existing `ShortCodeNotFoundException` if
    absent, maps straight from `UrlMapping.getClickCount()` (no computation, no aggregation).
  - `controller/UrlController#getUrlAnalytics` (new method, same controller, same resource
    family) — `@GetMapping("/{shortCode:[0-9a-zA-Z]{7}}/analytics")`.
  - Also fixed a now-stale code comment on `createShortUrl` that still said "the (future) GET
    /api/urls/{shortCode} resource" — that endpoint has existed since the prior slice.
- **Decision to keep analytics intentionally basic**: this was an explicit engineer instruction
  for this task, not an AI judgment call — recorded here because the assignment brief itself
  only says "Analytics" with no detail, and the brief's own scenario section separately lists
  "Ambiguous-requirement" and "brownfield" as required *later* project phases. Treating
  referrer/device/geography as deliberately-deferred candidates for those later phases (rather
  than building them now) keeps this slice's scope matched to what was actually approved, and
  preserves genuinely useful, well-motivated material for the brownfield/ambiguous-requirement
  work the assignment still requires (`docs/REQUIREMENTS.md` FR-6, FR-10) rather than
  front-loading it here.
- **Problems encountered**: none. Same as the details slice — this one compiled and passed on
  the first attempt, including the real-PostgreSQL run, entirely by reusing groundwork already
  laid (route-constraint pattern, `ShortCodeNotFoundException`, `NoResourceFoundException`
  handling, `@Transactional(readOnly = true)` convention, `MockMvc`-based full-stack tests).
- **Tests performed**:
  - `./mvnw clean test` → **108 tests, 0 failures, 0 errors** (was 99; +9 across
    `UrlServiceTest` (+2: analytics success without incrementing, analytics-not-found),
    `UrlControllerTest` (+3: analytics 200/404/malformed), `UrlAnalyticsIntegrationTest` (new,
    4)) — against H2. `CreateShortUrlIntegrationTest`, `RedirectIntegrationTest`, and
    `UrlDetailsIntegrationTest` rerun unchanged and still pass, confirming create, redirect,
    and details are unaffected.
  - `UrlAnalyticsIntegrationTest` is the test that actually matters here: new URL → `clickCount
    = 0` → real redirect → `1` → real redirect → `2` → calling analytics again alone → still
    `2` → calling details alone → still `2`. That sequence is what proves the read-only
    guarantee and the tracking behavior together, not just one or the other.
  - `./mvnw clean package` → success, jar produced.
- **Real PostgreSQL, end to end** (Docker Compose already up from the prior session; app
  started against the real datasource, `SERVER_PORT` overridden only because 8080 was occupied
  by an unrelated process on this machine) — followed the engineer's lettered scenario exactly:
  1. `POST /api/urls` with `{"originalUrl":"https://example.com/analytics-slice-target"}` →
     `201`, short code `lK041tL`.
  2. `GET /api/urls/lK041tL/analytics` → `200`, `clickCount: 0`.
  3. `GET /lK041tL` (redirect) → `302`.
  4. `GET /api/urls/lK041tL/analytics` → `clickCount: 1`.
  5. `GET /lK041tL` (redirect again) → `302`.
  6. `GET /api/urls/lK041tL/analytics` → `clickCount: 2`.
  7. `GET /api/urls/lK041tL/analytics` again, no redirect in between → still `clickCount: 2`.
  8. `GET /api/urls/lK041tL` (details) → `200`; re-checked analytics afterward → still
     `clickCount: 2`.
  9. `docker exec ... psql`: `SELECT short_code, click_count FROM url_mapping WHERE
     short_code='lK041tL'` → `click_count = 2`, confirmed directly in the database, not just
     through the API.
  10. `GET /api/urls/zzzzzzz/analytics` (unknown) → `404`,
      `{"status":404,"error":"Not Found","message":"No short URL found for code
      'zzzzzzz'",...}`.
  11. `/actuator/health` → `200 UP`; `/v3/api-docs` confirmed to list all 4 paths (`/api/urls`,
      `/{shortCode}`, `/api/urls/{shortCode}`, `/api/urls/{shortCode}/analytics`);
      `/swagger-ui/index.html` → `200`.
  12. App process stopped afterward; no leftover Java processes; Postgres container left
      running (healthy).
- **Engineer decision**: Pending review — implementation completed per the detailed spec and
  fully validated (see above).
- **Scope status**: this completes the approved API scope (`docs/REQUIREMENTS.md` §6 item 2)
  — all 4 endpoints implemented and validated. Richer analytics, custom aliases, update/delete,
  expiration, auth, caching, and rate limiting remain explicitly deferred, not started; no
  brownfield work has begun automatically per the engineer's explicit instruction to wait for
  approval.

---

## Brownfield scenario

### 2026-08-12 — Brownfield: add optional custom aliases to POST /api/urls

- **Task / Intent**: This is the assignment's required **brownfield** scenario (FR-6) —
  enhance the existing, already-shipped `POST /api/urls` to accept an optional `customAlias`,
  used as the short code verbatim instead of a randomly generated one, while every existing
  endpoint and existing-client behavior keeps working unmodified.
- **Original system behavior** (before this change): `POST /api/urls` accepts only
  `{"originalUrl": "..."}`; the short code is always a random `SecureRandom`-generated
  7-character Base62 string (`ShortCodeGenerator`); all 3 `GET` routes constrain the short-code
  path variable to exactly `[0-9a-zA-Z]{7}`; the `short_code` column is `VARCHAR(7)` with a
  `CHECK (char_length(short_code) = 7)` constraint (`V1__create_url_mapping_table.sql`);
  collisions on the generated code retry up to 5 times against a fresh random candidate.

#### Impact analysis (performed before any code change)

- **Existing modules/files impacted**:
  - `dto/CreateUrlRequest.java` — new optional field.
  - `dto/CreateUrlResponse.java` — no field changes, but its example/description implicitly
    assumed a generated code; doc-only touch-up.
  - `service/UrlService.java` — `createShortUrl` gains a parameter and branches into two paths.
  - `service/ShortCodeGenerator.java` — gains a shared route-matching pattern constant (no
    change to what it generates).
  - `controller/UrlController.java` — POST handler passes the new field through; both GET route
    patterns (`/{shortCode}`, `/{shortCode}/analytics`) must widen.
  - `controller/RedirectController.java` — its GET route pattern must widen identically.
  - `entity/UrlMapping.java` — `short_code` column-length annotation must match the new schema.
  - `src/main/resources/db/migration/` — needs a new `V2` migration (V1 is immutable once
    applied — Flyway checksums it; it must never be edited after the fact).
  - `exception/` — needs one new exception type + one new `GlobalExceptionHandler` case (409).
  - Five existing test files touch short-code route-matching assumptions that this change
    invalidates (see "Tests affected" below) — this is the single biggest hidden-cost item in
    this analysis.
- **Existing API behavior that must remain backward compatible**: `POST /api/urls` with only
  `originalUrl` must produce byte-for-byte the same kind of response as before (random
  7-character code, same fields). `GET /{shortCode}`, `GET /api/urls/{shortCode}`, and
  `GET /api/urls/{shortCode}/analytics` must keep working unchanged for every
  already-generated 7-character code, and must start working identically for custom aliases
  too, via the same three routes (no new endpoints).
- **Database/schema impact — real, not avoidable.** The instruction to "prefer reusing the
  existing `short_code` column, document if unnecessary" was evaluated and a schema change
  **is** necessary: `V1`'s `CHECK (char_length(short_code) = 7)` hard-codes the old fixed
  length and would reject every custom alias outright (aliases are 4–30 chars) with a raw
  constraint-violation error, not a clean 400/409. Conclusion: reuse the column (no new
  `alias` column — one lookup key for both kinds of short code, which is exactly what makes
  redirect/details/analytics work unchanged for both), but widen it: `VARCHAR(7)` →
  `VARCHAR(30)`, and replace the exact-length `CHECK` with a range `CHECK (char_length(...)
  BETWEEN 4 AND 30)` plus a new charset `CHECK (short_code ~ '^[0-9a-zA-Z_-]+$')` for
  defense-in-depth consistent with how V1 already used a DB-level CHECK as a second line of
  defense behind application validation. `entity/UrlMapping`'s `@Column(length = 7)` must be
  updated to `30` in the same change — Hibernate's `ddl-auto=validate` checks this against the
  real schema at startup and will fail fast (by design) if they drift.
- **Validation impact**: new Bean Validation constraint needed on `customAlias`: 4–30 chars,
  `[0-9a-zA-Z_-]` only, optional (null = not supplied, use random generation). A single
  `@Pattern(regexp = "^[0-9a-zA-Z_-]{4,30}$")` covers length, charset, and blank-rejection in
  one constraint (an empty or space-containing string simply fails the regex) — no new custom
  annotation class needed, unlike `@HttpUrl` which needed one because no built-in constraint
  could express "one of several schemes."
- **Short-code generation impact**: none to the generator itself — `ShortCodeGenerator` still
  always produces exactly a 7-character Base62 string when asked, unchanged. The impact is
  entirely in *when* it gets called: the service must skip calling it altogether when a custom
  alias is supplied, and — critically — must **not** retry with a random code if the alias
  collides. Retrying-on-collision is correct for a code *we* chose (already the existing
  behavior) but would be wrong for a code the *user* explicitly chose: silently substituting a
  different value would violate "use the custom alias as the short code" and hand the caller a
  short URL they never asked for. These need genuinely different collision-handling semantics,
  not a shared retry loop.
- **Error-handling impact**: needs one new exception (`ShortCodeAlreadyExistsException` → 409,
  named around "short code" rather than "alias" since the column — and now the conflict — is
  shared between generated codes and aliases) and one new `GlobalExceptionHandler` case,
  following the exact pattern already established for `ShortCodeNotFoundException` (404):
  same `ErrorResponse` shape, no DB constraint name or SQL state ever in the message.
- **Tests that may be affected — the main hidden regression risk in this change.** Widening
  the route pattern from exactly-7-alphanumeric to 4–30-alphanumeric-plus-hyphen-underscore
  invalidates the *premise* of five existing "malformed short code" tests, because their
  example inputs are no longer malformed under the new rules:
  - `"short"` (5 chars) — was too short for the old fixed-7 rule; is **≥ the new minimum of 4**,
    so it now matches the route.
  - `"toolongcode123"` (14 chars) — was too long for fixed-7; is **within the new max of 30**,
    so it now matches.
  - `"abc-123"` (7 chars, contains a hyphen) — was rejected because the old pattern had no
    hyphen; hyphen is **now explicitly allowed** (that's the whole point of supporting
    aliases), so it now matches.

  All three would flip from "route doesn't match → 404 via `NoResourceFoundException`" to
  "route matches, service reports not-found → 404 via `ShortCodeNotFoundException`" — same
  final status code by coincidence, *except* in the `@WebMvcTest` slices where the service is
  mocked: an unstubbed mock call would return `null`, and the controller would then NPE on
  `URI.create(null)`, turning an expected `404` into an actual `500`. This is a real,
  concrete regression, not a hypothetical — confirmed by re-deriving each example against the
  new pattern before writing any code. Affected files: `RedirectControllerTest`,
  `UrlControllerTest` (both the details and analytics malformed-code tests),
  `RedirectIntegrationTest`, `UrlDetailsIntegrationTest`, `UrlAnalyticsIntegrationTest`. Plan:
  replace the example literals with inputs that are still genuinely malformed under the *new*
  rules (e.g. `"abc"` for too-short — now < 4; a 31-character string for too-long; `"abc.de"`
  for an invalid character — `.` is still outside `[0-9a-zA-Z_-]`), not delete the tests —
  the underlying "malformed route → 404" behavior is still real and still needs coverage,
  just with correct examples.
- **Risks and regression possibilities** (full list, including accepted/unfixed ones):
  1. Schema/entity length drift (above) — mitigated by making the entity and migration changes
     in the same commit and letting `ddl-auto=validate` fail startup if they ever disagree.
  2. Five pre-existing tests silently asserting the wrong thing after the route widens (above)
     — mitigated by fixing all five before considering this change done, not just adding new
     tests alongside stale ones.
  3. Accidentally substituting a random code for a colliding custom alias — mitigated by
     giving the two code paths (`createWithGeneratedCode` / `createWithCustomAlias`)
     genuinely different collision handling, not a shared retry loop.
  4. **Accepted, not fixed**: the widened root-level route (`/{shortCode}`, single path
     segment, 4–30 chars, alphanumeric+hyphen+underscore) can now syntactically match some
     single-segment system-ish paths it couldn't before — e.g. a bare `GET /swagger-ui`
     (10 chars, letters+hyphen) or a bare `GET /actuator` (9 chars, letters) would now be
     routed to `RedirectController` instead of falling through to static/actuator handling,
     because Spring dispatches by pattern match regardless of what the segment "looks like."
     In practice this only matters for *bare* single-segment hits — real Swagger UI/Actuator
     traffic goes to multi-segment paths (`/swagger-ui/index.html`, `/actuator/health`, both
     already verified working in every prior slice) which this single-segment pattern was
     never able to match anyway. No reserved-word list was added to guard against the bare
     case: it wasn't asked for, the practical exposure is low (nobody's tooling requests bare
     `/swagger-ui`), and adding one would be scope creep against an explicit "do not add"
     list. Verified empirically during real-Postgres validation below rather than left as
     pure theory.
  5. **Confirmed no change needed**: `resolveAndRecordRedirect`, `getUrlDetails`, and
     `getUrlAnalytics` all treat `shortCode` as an opaque lookup key already — none of the
     three needed a single line changed for alias support, only the *route patterns* in front
     of them. This is called out as a positive finding, not just a risk list — it's the
     payoff of `short_code` having been designed as one column/one concept from the start
     rather than something that needed retrofitting.
  6. H2 (test profile) schema is generated from the entity mapping, not from the Flyway
     migration (Flyway is disabled for tests — see `CLAUDE.md` Testing strategy) — so the new
     DB-level length-range and charset `CHECK` constraints exist only in the real PostgreSQL
     schema, not in the automated H2 suite, exactly the same shape of gap V1's exact-length
     `CHECK` already had. The application-level `@Pattern` validation is what the automated
     suite actually exercises for invalid input; the DB constraints are defense-in-depth,
     proven only by the manual real-PostgreSQL validation step, not new to this change.
- **Engineer-approved implementation approach**: reuse `short_code` (no new column); widen
  length + replace the CHECK via a new `V2` Flyway migration; single `@Pattern` constraint on
  a new optional `customAlias` request field; `UrlService.createShortUrl` gains a parameter
  and branches to either the existing (unchanged) generated-code retry loop or a new
  single-attempt custom-alias path that returns `409` on any collision, no retry; widen all
  three route patterns via one shared constant so they can't drift from each other; fix the
  five pre-existing tests whose examples are invalidated by the widened route before adding
  new ones.

#### Implementation, validation, and outcome (after the code change)

- **Changes made**:
  - `src/main/resources/db/migration/V2__widen_short_code_for_custom_aliases.sql` (new) —
    widens `short_code` to `VARCHAR(30)`, replaces the exact-length `CHECK` with a
    `BETWEEN 4 AND 30` range check, adds a new charset `CHECK` (`~ '^[0-9a-zA-Z_-]+$'`). `V1`
    untouched, as required.
  - `entity/UrlMapping.java` — `@Column(length = 7)` → `length = 30`, matching V2; class/field
    Javadoc updated to explain the column is shared between generated codes and aliases.
  - `service/ShortCodeGenerator.java` — added `public static final String ROUTE_PATTERN =
    "[0-9a-zA-Z_-]{4,30}"`, one definition shared by all three `GET` routes.
  - `exception/ShortCodeAlreadyExistsException.java` (new) → `409`, plus a new
    `GlobalExceptionHandler` case, following the exact pattern already used for
    `ShortCodeNotFoundException` (404): same `ErrorResponse` shape, no DB detail in the message.
  - `dto/CreateUrlRequest.java` — new optional `customAlias` field,
    `@Pattern(regexp = "^[0-9a-zA-Z_-]{4,30}$")`; `null` (omitted) is valid by the constraint's
    own semantics, preserving "not supplied" as a first-class, unpenalized case.
  - `service/UrlService.java` — `createShortUrl` gained a `customAlias` parameter and now
    branches to `createWithGeneratedCode` (the original loop, extracted unchanged) or
    `createWithCustomAlias` (new, single-attempt, `409` on any collision, no retry).
  - `controller/UrlController.java` — passes `request.customAlias()` through; both `GET` route
    patterns now reference `ShortCodeGenerator.ROUTE_PATTERN`; `@Operation`/`@ApiResponses`
    docs updated (409 added, alias mentioned).
  - `controller/RedirectController.java` — route pattern likewise now references
    `ShortCodeGenerator.ROUTE_PATTERN`; docs updated to say "short code" generically rather
    than assuming a generated one.
  - `dto/CreateUrlResponse.java` — one description string updated for accuracy (shortCode may
    now be an alias); no field/behavior change.
  - Five pre-existing test files had their "malformed short code" example literals corrected
    (see below) — this is a genuine fix to tests whose premise the route change invalidated,
    not new test additions.
  - New tests: `CustomAliasIntegrationTest` (full-stack, 8 tests), plus additions to
    `UrlServiceTest` (+3), `UrlControllerTest` (+7), `ShortCodeGeneratorTest` (+3).
- **Regression risks — realized vs. accepted, checked against what actually happened**:
  - *Realized and fixed*: the five stale "malformed code" test examples (predicted in the
    analysis) did in fact need correcting — confirmed by re-deriving each one against the new
    pattern before touching test code, then fixing all five before writing anything new.
  - *Accepted risk, empirically checked, not fixed*: a bare `GET /swagger-ui` (no trailing
    path) does now route to `RedirectController` and returns our `404` ("No short URL found
    for code 'swagger-ui'") instead of whatever it did before — confirmed by direct `curl`
    during validation (see below). A bare `GET /actuator`, by contrast, turned out **not** to
    be affected — it still returns Actuator's own HATEOAS link document, `200` — Spring
    apparently favors Actuator's own explicit endpoint mapping over the generic `{shortCode}`
    pattern for that path specifically. Neither of the two *real* usage paths this project
    actually relies on (`/swagger-ui/index.html`, `/actuator/health`) is affected either way —
    both still return `200` exactly as in every prior slice. No reserved-word list was added;
    this was flagged as an accepted, out-of-scope risk in the analysis and the empirical check
    confirms it stays contained to a bare path nothing in this project's real usage exercises.
  - *Confirmed, as predicted*: `resolveAndRecordRedirect`, `getUrlDetails`, and
    `getUrlAnalytics` needed zero changes — only the route patterns in front of them widened.
- **Bugs discovered**: none new during implementation — the impact analysis's predictions
  (schema/entity length mismatch risk, the five stale test examples) were exactly right and
  were addressed as part of the planned implementation rather than discovered via a failing
  build afterward. This is a direct contrast to earlier slices in this project (e.g. the
  `NoResourceFoundException` 500-instead-of-404 bug found while building the redirect
  endpoint) — the payoff of doing the impact analysis before writing code instead of after.
- **Fixes**: none needed beyond what was already planned in the impact analysis.
- **Tests performed**:
  - `./mvnw clean test` → **129 tests, 0 failures, 0 errors** on the first full run (was 108;
    +21: `CustomAliasIntegrationTest` new, 8; `UrlServiceTest` +3; `UrlControllerTest` +7;
    `ShortCodeGeneratorTest` +3) — against H2. All four pre-existing full-stack integration
    test files (`CreateShortUrlIntegrationTest`, `RedirectIntegrationTest`,
    `UrlDetailsIntegrationTest`, `UrlAnalyticsIntegrationTest`) rerun with only their
    malformed-code literals corrected, otherwise untouched, and still pass — this is the real
    evidence of backward compatibility, not just a claim.
  - `./mvnw clean package` → success, jar produced.
- **Real PostgreSQL, end to end** (existing `url-shortener-postgres` container from prior
  sessions — already had `V1` applied and existing data from earlier slices; `SERVER_PORT`
  overridden only because 8080 was occupied by an unrelated process on this machine):
  1. Startup log confirmed Flyway found the database already at schema version 1 and applied
     **only** `V2` ("Migrating schema \"public\" to version \"2 - widen short code for custom
     aliases\"... now at version v2") — exactly the real brownfield scenario: evolving a
     schema that already has data and history, not a fresh database.
  2. `POST /api/urls` with `{"originalUrl":"https://example.com/products","customAlias":
     "products"}` → `201`, `shortCode: "products"`, `shortUrl` ends in `/products`.
  3. `GET /products` → `302`, `Location: https://example.com/products`.
  4. `GET /api/urls/products` → `200`, fields match exactly.
  5. `GET /api/urls/products/analytics` → `200`, `clickCount: 1` (reflecting the one redirect
     above).
  6. `POST /api/urls` with the same `customAlias: "products"` again → `409`,
     `{"status":409,"error":"Conflict","message":"Short code 'products' is already in use",...}`
     — no DB constraint name or SQL state in the message.
  7. `POST /api/urls` with only `originalUrl` (no alias) → `201`, `shortCode: "fxVPkP4"` —
     random 7-character generation confirmed unchanged.
  8. `docker exec ... psql`: `SELECT short_code, click_count, length(short_code) FROM
     url_mapping WHERE short_code IN ('products','fxVPkP4')` → `products` (len 8, clicks 1),
     `fxVPkP4` (len 7, clicks 0) — both coexist correctly in the same widened column.
     `\d url_mapping` confirmed live schema: `short_code character varying(30)`, both new
     `CHECK` constraints present with the exact expressions from the migration.
  9. Alias-validation `400`s confirmed live: too-short (`"abc"`), space (`"has space"`), slash
     (`"has/slash"`) — all rejected with `fieldErrors[0].field = "customAlias"`.
  10. Bare-route risk check (see "Regression risks" above): `GET /swagger-ui` → `404` (our
      error body); `GET /actuator` → `200` (Actuator's own response, unaffected); the real
      paths `/swagger-ui/index.html` and `/actuator/health` → `200` in both cases, unaffected.
  11. `/v3/api-docs` confirmed to list all 4 endpoints; `/swagger-ui/index.html` → `200`.
  12. App process stopped afterward; no leftover Java processes; Postgres container left
      running (healthy).
- **Engineer decision**: Pending review — brownfield enhancement completed per the
  engineer-approved requirements, impact analysis performed and recorded before any code
  change, and fully validated (see above).
- **Scope status**: this is the assignment's required brownfield scenario (FR-6), now
  complete. No URL expiration, authentication, rate limiting, caching, rich analytics, or
  update/delete APIs were added — none were in scope for this change. The
  ambiguous-requirement scenario has not been started automatically, per the engineer's
  explicit instruction to wait for approval.

---

## Ambiguous-requirement scenario

### 2026-08-12 — Ambiguity analysis: "Shortened URLs should expire" (FR-10) — analysis only, no implementation

- **Task / Intent**: This is the assignment's required **ambiguous-requirement** scenario
  (FR-10). The engineer supplied a stakeholder requirement verbatim — *"Shortened URLs should
  expire"* — and explicitly instructed: treat it as ambiguous, analyze without assuming
  answers, do not modify application code or the database schema, do not resolve the ambiguity
  unilaterally, present clarification questions and options, and wait for engineer decisions
  before proceeding. No code or schema was touched in this entry — analysis only, exactly as
  instructed.
- **Approach**: Read the current `UrlMapping` entity and `UrlService` (all three read paths:
  `resolveAndRecordRedirect`, `getUrlDetails`, `getUrlAnalytics`) fresh before analyzing, so the
  impact assessment below is grounded in the actual current code, not assumed. Then reasoned
  through the requirement across every axis the engineer asked for, deliberately choosing *not*
  to pick answers even where a reasonable-sounding default was available — e.g. did not assume
  "time-based" is correct without flagging it as an assumption to confirm, did not assume 404
  vs. 410, did not assume opt-in vs. mandatory.
- **Ambiguities identified**: "expire" alone is compatible with at least 4 different mechanisms
  (soft/still-queryable, hard-deleted, soft-inactive-but-retained, or a hybrid where redirect
  blocks but details/analytics don't); the trigger is unstated (time/usage/manual — the
  requirement *reads* as time-based but that's an assumption); the scope is unstated (mandatory
  on every URL, opt-in per URL like `customAlias` was, or a system-wide default with override).
- **Missing business rules identified**: default/allowed expiration duration; behavior for a
  past-dated expiration supplied at creation; whether click count increments on a request
  against an already-expired link (existing pattern only increments on success — needs an
  explicit rule for expiry too); whether generated codes and custom aliases follow the same
  expiry rules; exact boundary semantics at the expiry instant.
- **Existing modules/APIs/data model assessed as potentially affected**:
  `entity/UrlMapping.java` (likely needs a new nullable field under most interpretations — none
  exists today); a new `V3` Flyway migration would be needed for any DB-backed approach (**not
  created**); `dto/CreateUrlRequest.java` (possible new optional field, mirroring the
  `customAlias` precedent); all three response DTOs (`CreateUrlResponse`,
  `UrlDetailsResponse`, `UrlAnalyticsResponse`); critically, **all three read paths in
  `UrlService`** — `resolveAndRecordRedirect`, `getUrlDetails`, `getUrlAnalytics` — not just the
  redirect path, which is a wider blast radius than the alias brownfield change (that one only
  touched creation); possibly a new exception type depending on the 404-vs-410 decision;
  `RedirectController`/`UrlController` behavior (no new routes needed under any interpretation
  considered, since update/delete/reactivate endpoints are out of scope regardless).
- **Backward compatibility concerns identified**: every row created across all four prior
  greenfield slices and the brownfield slice has no expiration concept; a nullable-column,
  `NULL`-means-never-expires default would preserve them automatically (mirroring the
  `customAlias` `NULL`-means-omitted precedent) — but a system-wide default TTL instead would
  make existing links start expiring where they never would have, which is a behavioral break
  requiring explicit sign-off, not a technical detail.
- **Failure scenarios identified**: redirect against an expired-but-present code; creation with
  a past-dated expiration; the exact-boundary race at the expiry instant; the gap between
  "logically expired" and "physically purged" if any cleanup job exists (on-read check must be
  authoritative regardless); clock-based correctness in general.
- **Security/reliability considerations identified**: the most significant one —
  distinguishing "expired" from "never existed" via different status codes (`410` vs `404`)
  would reintroduce exactly the enumeration risk this project explicitly designed against when
  choosing `SecureRandom` short-code generation over sequential/predictable codes (see
  "Decision: Short-code generation strategy"); flagged as a genuine trade-off, not a
  formality. Also: reliability risk of a background-job-based enforcement approach (new
  scheduler infra, silent-failure mode) vs. lazy on-read enforcement (simpler, and would need
  to be authoritative regardless of whatever else exists); no caching exists anywhere in this
  project so cache-invalidation is a non-issue; whether "expire" implies eventual data
  deletion/scrubbing (privacy-adjacent) was flagged as unaddressed by the requirement, not
  assumed either way.
- **Testing implications identified**: tests need deterministic control over "now" (real-time
  waiting would be slow/flaky) — likely via constructing entities with an already-past
  expiration directly (consistent with existing test patterns) or a clock abstraction injected
  into the service, which is itself a design decision, not just a test detail; new scenarios
  once resolved would span all three read paths, not just one, meaning wider regression review
  than the alias change needed.
- **Implementation options presented (not decided)**:
  - **Option A** — system-wide fixed TTL computed from `createdAt`, zero schema change.
  - **Option B** — optional per-URL expiration set at creation (new nullable column,
    `NULL` = never expires), directly mirroring the `customAlias` pattern.
  - **Option C** — hybrid: system-wide default, per-request override.
  - AI recommendation offered *as a lean, explicitly not a decision*: Option B + lazy on-read
    enforcement only (no background job) + reuse `404`/`ShortCodeNotFoundException` for expired
    links rather than introduce `410`, on the grounds of smallest surface area, strongest
    backward compatibility, closest fit to established project patterns, and avoiding the
    enumeration side-channel. Presented to the engineer for accept/modify/reject — not applied.
- **Clarification questions posed to the engineer** (all **TBD**, none answered yet):
  1. Trigger: time-based / usage-based / manual? — **TBD**
  2. Scope: opt-in per-URL / mandatory / system-wide default with override? — **TBD**
  3. Default duration and who sets it? — **TBD**
  4. On expiry: row stays queryable / gets deleted / marked inactive-but-retained? — **TBD**
  5. Which endpoints are affected: redirect only, or also details/analytics? — **TBD**
  6. Status code for expired-on-redirect: `404` or `410`? — **TBD**
  7. Enforcement: lazy on-read only, or also a background purge job? — **TBD**
  8. Backward compatibility: do existing rows become permanent, or retroactively get a default
     expiry from their original `createdAt`? — **TBD**
  9. Past-dated expiration at creation: reject (`400`) or accept as dead-on-arrival? — **TBD**
  10. Data retention: does expiry ever imply deleting/scrubbing the original URL? — **TBD**
- **Engineer decision (at analysis time)**: Pending — this entry deliberately resolved
  nothing at the time it was written. No code, no schema, no ambiguity resolution. **AI did
  not implement any part of the ambiguous requirement until the engineer explicitly resolved
  the business rules below** — see the resolution recorded immediately following.
- **Validation performed (at analysis time)**: N/A — no code or schema changed; analysis only,
  per explicit instruction. The two files read to ground the analysis
  (`entity/UrlMapping.java`, `service/UrlService.java`) were read-only, not modified.

### 2026-08-12 — Resolution and implementation: "Shortened URLs should expire"

- **Original ambiguous stakeholder statement** (preserved verbatim, as instructed):
  *"Shortened URLs should expire."*
- **Clarification questions identified by AI**: the 10 questions listed in the analysis entry
  immediately above — trigger, scope, duration, on-expiry row fate, affected endpoints, status
  code, enforcement mechanism, backward compatibility, past-dated-expiry handling, data
  retention.
- **Options considered**: Options A/B/C for scope/mechanism, as listed above, plus the AI's
  non-binding lean (Option B + lazy enforcement + reused `404`) — all recorded above.

#### Engineer decisions for each ambiguity, with rationale

| # | Question | Engineer decision | Rationale (as given) |
|---|---|---|---|
| 1 | Trigger | Time-based, automatic | (confirms the AI's assumed-but-flagged reading) |
| 2 | Scope | Optional per-URL at creation (`expiresAt` field); omitted = never expires | Preserves backward compatibility |
| 3 | Representation | Absolute timestamp (`Instant`/UTC), not a TTL duration | Explicit, unambiguous, no "duration relative to what" question |
| 4 | Validation | Must be a valid timestamp, strictly in the future at creation; past/current → `400` | — |
| 5 | Existing rows | All remain valid indefinitely; `expires_at = NULL`; no retroactive expiry | Explicit non-negotiable: "Do not retroactively expire existing URLs" |
| 6 | Database | New nullable `expires_at` (`TIMESTAMP WITH TIME ZONE`) via new Flyway migration; V1/V2 untouched | Matches the `customAlias` reuse-a-column precedent |
| 7 | Enforcement | Lazy request-time checking only; explicitly no scheduled jobs/background cleanup/queues/cron | "Intentionally kept simple for the interview prototype" |
| 8 | Affected endpoints | `GET /{shortCode}`, `GET /api/urls/{shortCode}`, `GET /api/urls/{shortCode}/analytics` — all three | Consistency: an expired URL is unavailable everywhere, not just at redirect |
| 9 | Status code | `404`, not `410` | "Keeps behavior consistent with unknown short codes, avoids exposing whether an expired resource previously existed, and reuses the existing error-handling model" — matches the AI's own enumeration-risk analysis exactly |
| 10 | Data retention | No physical deletion; `originalUrl` never scrubbed; expiry only blocks API access | Explicit non-negotiable |

Also resolved implicitly by the engineer's implementation-design instructions: custom aliases
and `expiresAt` must compose freely (all four combinations — neither, either, or both fields —
must work); expiration logic must be centralized, not duplicated per controller; a testable
time abstraction (`java.time.Clock`) should be used if it materially improves the design
without adding unnecessary complexity.

#### Impact analysis (brief, performed immediately before code changes, per instruction)

`entity/UrlMapping.java` (new nullable field + additive constructor overload + a centralized
`isExpired(Instant)` domain method), a new `V3` Flyway migration (`V1`/`V2` untouched),
`dto/CreateUrlRequest.java` (new optional `expiresAt`, `@Future`-validated — Jakarta's built-in
constraint already expresses exactly "null is fine, must be strictly after now," no custom
validator needed), `dto/CreateUrlResponse.java` (new `expiresAt` field — `UrlDetailsResponse`/
`UrlAnalyticsResponse` deliberately left unchanged, consistent with their existing
minimal-field precedent, e.g. `clickCount` was likewise kept out of the details response),
`service/UrlService.java` (new `Clock` dependency; `createShortUrl` gains an `expiresAt` param;
one new private `findActiveMapping` helper reused by all three read paths — the actual
"centralize, don't duplicate" mechanism), a new `config/ClockConfig` bean. **No repository
changes** (still just `findByShortCode`), **no new exception type** (reuses
`ShortCodeNotFoundException` verbatim — required by decision #9's own rationale: same message,
same status, no distinguishing signal), **no `GlobalExceptionHandler` changes** (existing
validation handlers already cover it).

#### Implementation

- `V3__add_expires_at_to_url_mapping.sql` — adds nullable `expires_at TIMESTAMP WITH TIME
  ZONE`, plus a defense-in-depth `CHECK (expires_at IS NULL OR expires_at > created_at)`
  (mirroring the CHECK-constraint pattern already used in `V1`/`V2`). Deliberately does **not**
  attempt a `CHECK` against `now()` for the "must be future at creation" rule — a row-level
  CHECK referencing a volatile function like `now()` would make previously-valid rows start
  failing on any *unrelated* future `UPDATE` once real time passes their `expires_at`; that
  rule is correctly an application-layer concern (`@Future`), not a database one.
- `entity/UrlMapping.java` — new nullable `expiresAt` field; **additive** 3-arg constructor
  (`originalUrl, shortCode, expiresAt`) with the existing 2-arg constructor now delegating to
  it with `null` — every existing `new UrlMapping(url, code)` call site across the whole test
  suite kept compiling unchanged, zero call-site churn from this alone. New
  `isExpired(Instant now)` domain method: `expiresAt != null && !now.isBefore(expiresAt)` —
  i.e. expired *at or after* the expiry instant, not only strictly after (an "expires at 6pm"
  reading, not "valid through 6pm"); this specific boundary wasn't pinned down by the engineer,
  so it's called out here explicitly as an engineering-judgment call within the resolved
  design, not silently assumed.
- `config/ClockConfig.java` (new) — `Clock.systemUTC()` bean.
- `service/UrlService.java` — constructor now takes a `Clock`; `createShortUrl` takes a third
  `expiresAt` parameter, threaded through both `createWithGeneratedCode` and
  `createWithCustomAlias` unchanged otherwise; new private `findActiveMapping(shortCode)` —
  look up or throw `ShortCodeNotFoundException`, then check `isExpired(clock.instant())` and
  throw the *same* exception type if so — used by `resolveAndRecordRedirect`, `getUrlDetails`,
  and `getUrlAnalytics`, replacing their previous inline `findByShortCode(...).orElseThrow(...)`
  calls. This is the literal realization of "keep expiration logic centralized rather than
  duplicating timestamp comparisons across controllers" — centralized even one level deeper
  than asked, across the three *service* methods, not just controllers.
- `dto/CreateUrlRequest.java` — new optional `expiresAt` (`Instant`, `@Future`).
- `dto/CreateUrlResponse.java` — new `expiresAt` field, populated from the saved entity.
- `controller/UrlController.java` — passes `request.expiresAt()` through; OpenAPI
  descriptions updated on all three affected endpoints (create mentions the new field and its
  `400` case; details/analytics/redirect note expired codes now also produce `404`).
- Existing behavior preserved by construction, not by a special case: creating a URL that
  happens to reuse an *expired* code's exact short code (were that ever attempted) still hits
  the ordinary `UNIQUE` constraint / `existsByShortCode` check — expired rows are never
  deleted, so their codes are never reclaimable, which is a direct, unforced consequence of
  decision #10 (no physical deletion), not new logic.

#### Tests added

24 new tests (130 → **154 total**, all passing on the first full run):
- `entity/UrlMappingTest` (new, 4) — pure, mock-free tests of `isExpired` covering null,
  future, past, and the exact-boundary ("expires at now") cases — this is the one place the
  precise boundary is deterministically testable, since both sides of the comparison are
  explicit `Instant` literals, no clock involved.
- `service/UrlServiceTest` (+7) — create with `expiresAt` (persisted and returned correctly),
  create without it (response `expiresAt` is `null`), create with alias + `expiresAt`
  together; non-expired and expired cases for each of the three read paths, including an
  explicit assertion that an expired redirect attempt does **not** call
  `incrementClickCount`. The whole test class now constructs `UrlService` with a
  `Clock.fixed(...)` instead of a real clock — not just for the new expiration tests, every
  test in the file is now deterministic with respect to time.
- `controller/UrlControllerTest` (+3) — future `expiresAt` → `201` with the field echoed back;
  past `expiresAt` → `400`; current/non-future `expiresAt` → `400`.
- `UrlExpirationIntegrationTest` (new, full-stack, 9) — the real evidence: create with future
  expiry → persists correctly → works across all three `GET` endpoints; past/current expiry →
  `400`; alias + expiry together; create without expiry → still generates a random code, DB
  row's `expires_at` is `null`; an expired mapping (constructed directly via the repository,
  since a past `expiresAt` can never pass creation's own `@Future` check) → all three endpoints
  return `404`, and click count is confirmed **unchanged** by the failed attempt; a direct
  byte-for-byte comparison proving the expired-code and unknown-code `404` bodies are
  identical in shape (the actual proof of the no-enumeration-signal decision); a `NULL`
  `expiresAt` mapping works regardless of time; the existing create endpoint still works
  alongside the new field.
- Avoided sleep-based tests throughout, per instruction — either exact `Instant` literals
  (unit tests), a `Clock.fixed` service instance (service unit tests), or directly-constructed
  already-past-or-future entities via the repository (integration tests, mirroring the
  established pattern from every prior slice).

#### Problems/fixes

One minor issue, self-inflicted during **manual** real-PostgreSQL validation (not a bug in
the implementation): attempting to hand-craft an expired row via
`UPDATE ... SET expires_at = now() - interval '1 hour'` was correctly **rejected** by the new
`chk_url_mapping_expires_at_after_created` defense-in-depth constraint, because that row's
`created_at` was itself only moments old — `now() - 1 hour` was earlier than `created_at`,
violating the (correct) invariant. Not a defect: the constraint did exactly its job. Fixed the
*test methodology*, not the code — re-issued the `UPDATE` as `expires_at = created_at +
interval '1 second'` (safely after `created_at`, still comfortably in the past relative to
real "now" a few seconds later), which succeeded and produced the intended expired row for
validation. No application code or migration was touched to work around this.

#### Validation results

- `./mvnw clean test` → **154 tests, 0 failures, 0 errors**, first full run. All prior
  full-stack integration test files (create, redirect, details, analytics, custom-alias)
  rerun completely unchanged and still pass.
- `./mvnw clean package` → success, jar produced.
- **Real PostgreSQL, end to end** (existing `url-shortener-postgres` container, already at
  schema v2 with 6 pre-existing rows from prior sessions; `SERVER_PORT` overridden only
  because 8080 was occupied by an unrelated process on this machine):
  1. Startup log: Flyway found the database at v2 and applied only `V3` ("Migrating schema
     \"public\" to version \"3 - add expires at to url mapping\"... now at version v3").
  2. All 6 pre-existing rows confirmed with `expires_at` = empty/`NULL` — zero retroactive
     expiry, exactly as decided.
  3. `POST /api/urls` with `expiresAt: "2099-01-01T00:00:00Z"` → `201`, response includes
     `expiresAt`; `psql` confirms `expires_at = 2099-01-01 00:00:00+00` persisted exactly.
  4. Non-expired redirect → `302` to the correct URL.
  5. `POST` with a past `expiresAt` → `400`, `fieldErrors[0].field = "expiresAt"`.
  6. Manually aged the created row's `expires_at` into the past directly in PostgreSQL (see
     "Problems/fixes" above for the one hiccup in doing this correctly) — then confirmed
     `GET /{shortCode}`, `GET /api/urls/{shortCode}`, and `GET /api/urls/{shortCode}/analytics`
     **all three** return `404` with the identical centralized `ErrorResponse` shape and
     message text (`"No short URL found for code '...'"`) — indistinguishable from a
     never-existed code, exactly per decision #9. `click_count` confirmed unchanged by the
     failed redirect attempt (stayed at `2`, its value from before expiry).
  7. A pre-existing row from an earlier session (`products`, no `expires_at`) still redirects
     correctly — confirms existing non-expiring URLs are entirely unaffected.
  8. `/actuator/health` → `200 UP`; `/v3/api-docs` includes `expiresAt`; `/swagger-ui/index.html`
     → `200`.
  9. App process stopped afterward; no leftover Java processes; Postgres container left
     running (healthy).
- **Regression**: full suite green, all pre-existing integration tests untouched and passing,
  every prior slice's real-PostgreSQL-verified endpoint (create, redirect, details, analytics,
  custom alias) re-confirmed working in this same validation pass.
- **Scope status**: this is the assignment's required ambiguous-requirement scenario (FR-10),
  now complete — analysis, engineer resolution, and implementation all recorded. No
  scheduled/background cleanup, expiration-update APIs, notifications, authentication, caching,
  or rate limiting were added — none were in scope for this change.

### 2026-08-12 — Test-improvement scenario (FR-7): gap analysis — analysis only, no implementation

- **Task / prompt intent**: engineer asked for a review of the existing automated test suite
  (154 tests as of the expiration slice) to identify 3 meaningful gaps in coverage of
  *existing* functionality — explicitly **no new features, no production-code changes** — with
  for each gap: what's under-tested, the regression risk, and what tests should be added; plus
  one recommended small-but-valuable FR-7 test-improvement scenario. Explicitly instructed to
  record the analysis here and wait for approval before implementing anything.
- **Method**: read every test class against every production class it's supposed to exercise
  (`GlobalExceptionHandler`, `UrlService`, `UrlMappingRepository`, `UrlController`,
  `CreateUrlRequest`, `UrlMapping`) and grepped the whole test tree for patterns that would
  indicate coverage of specific behaviors (concurrency primitives, malformed-body handling,
  generic-exception handling, boundary lengths) to find call sites that exist in `main/` with
  zero corresponding assertion anywhere in `test/`. No code was modified — read-only.

#### Gap 1 — `GlobalExceptionHandler`'s generic `Exception` catch-all (`handleUnexpected`) has zero test coverage

- **What's under-tested**: every other handler in `GlobalExceptionHandler`
  (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`,
  `ShortCodeGenerationException`, `ShortCodeNotFoundException`,
  `ShortCodeAlreadyExistsException`, `NoResourceFoundException`) is exercised, directly or
  indirectly, by an existing controller-slice or full-stack test. The final `@ExceptionHandler(
  Exception.class)` fallback — the one that returns a generic `500` with a fixed
  `"An unexpected error occurred."` message and logs the real exception server-side via
  `log.error(...)` — is never triggered by any test in the suite. Grepped the whole test tree
  for `RuntimeException`/`"An unexpected error occurred"`/`unexpected`: no matches.
- **Regression risk**: this exact code path already caused one real, shipped bug earlier in the
  project — `NoResourceFoundException` was originally being swallowed by this same catch-all
  and silently turned into an incorrect `500` instead of `404` (see the "Bug found and fixed"
  entry above / `CLAUDE.md`'s API design section). The specific `NoResourceFoundException` case
  is now fixed and covered, but the catch-all *itself* — its status code, its message shape
  (must never leak `ex.getMessage()` or a stack trace, per the Security approach section of
  `CLAUDE.md`), and the fact that it actually logs server-side — has no regression protection.
  A future change (e.g. someone "simplifying" the handler, or an unrelated refactor that
  accidentally changes which exception types route through it) could silently start leaking
  internal exception details to a client, return the wrong status code, or drop the
  server-side logging, and nothing in the suite would catch it.
- **Tests that should be added**: a slice-level test (likely added to `UrlControllerTest` or a
  small new `GlobalExceptionHandlerTest`) that forces the mocked service to throw an
  unexpected, undeclared `RuntimeException` and asserts: `500` status, the exact generic
  message (not the real exception's message), an empty `fieldErrors` array, and the standard
  `ErrorResponse` shape (`timestamp`/`status`/`error`/`message`/`path`). Verifying the
  server-side `log.error(...)` call itself is a secondary, lower-priority addition (e.g. via a
  captured `Logger`/`ListAppender`) — the response-body/status assertion is the primary,
  higher-value piece.

#### Gap 2 — Custom-alias collision handling is proven only against a mocked repository, never under real concurrent writes

- **What's under-tested**: `UrlServiceTest.createShortUrl_customAliasRaceLostToDatabase_
  throwsConflict_doesNotRetry` simulates a collision race by stubbing
  `repository.save(...)` to throw `DataIntegrityViolationException` on demand — a Mockito
  simulation of what a race *might* look like, not a real race. The generated-code path has the
  exact same kind of race, and *that* one is additionally proven under real concurrent
  conditions elsewhere in the suite (see below) — but no equivalent exists for the custom-alias
  path. Grepped the whole test tree for concurrency primitives (`Thread`, `ExecutorService`,
  `CountDownLatch`, `Future<`): the only match anywhere in `src/test` is
  `UrlMappingClickCountConcurrencyTest`, which covers the atomic click-count increment, not
  alias-collision handling.
- **Regression risk**: this project has direct, documented precedent for exactly this failure
  mode. The click-count concurrency test's own history (see the "Bug found and fixed" —
  actually "Problem Solving" narrative for `GET /{shortCode}`, `CLAUDE.md`'s Reliability
  section) records that an earlier version of that same kind of test "passed for the wrong
  reason" (it silently swallowed exceptions) until it was fixed to genuinely wait on every
  thread — at which point it surfaced a real, previously-undetected bug: `@Modifying` queries
  don't get an implicit transaction. There is no reason to assume custom-alias creation is
  immune to an analogous surprise (e.g. real connection-pool/transaction timing under the JPA
  `save()` + `UNIQUE`-constraint-violation path behaving subtly differently from what Mockito's
  synchronous stub simulates). Today, the "colliding alias → `409`, no retry, no double-grant
  of the same alias to two callers" guarantee — a public-facing correctness property directly
  called out as a deliberate design decision in `CLAUDE.md` — is unverified under real
  concurrency.
- **Tests that should be added**: a real-concurrency test, structurally modeled on
  `UrlMappingClickCountConcurrencyTest` (same package, same `@SpringBootTest`-not-`@DataJpaTest`
  reasoning documented in that class's Javadoc), that fires N threads (e.g. 20) all calling
  `UrlService.createShortUrl(differentUrl, "sameAlias", null)` for the *same* `customAlias`
  concurrently, then asserts: exactly one thread's call succeeds (returns a `201`-equivalent
  response for `"sameAlias"`), every other thread's call throws
  `ShortCodeAlreadyExistsException`, and the repository ends up with exactly one row for that
  alias — proving the `409`/no-retry/no-duplicate-grant guarantee under real conditions, not
  just a mocked approximation of one.

#### Gap 3 — The repository-level schema/mapping test never round-trips `expires_at`, despite that being its own stated purpose

- **What's under-tested**: `UrlMappingRepositoryTest`'s own class Javadoc states its purpose is
  in part schema/mapping validation — *"if `UrlMapping`'s column mappings didn't match the
  schema Hibernate generates here, these tests would fail at context startup, not just at
  assertion time."* Its `savesAndReadsBackAllFields` test predates the `expires_at` column
  (added by `V3` in the expiration slice) and was never extended to cover it: it asserts `id`,
  `originalUrl`, `shortCode`, `createdAt`, `clickCount`, but not `expiresAt` — neither the
  `NULL` case (the common case for every pre-existing and most new rows) nor a populated case.
  `expiresAt` round-tripping is currently only exercised indirectly: through `UrlServiceTest`
  (which mocks the repository entirely — it never touches the real column mapping) and through
  `UrlExpirationIntegrationTest` (a full-stack `MockMvc` test, which does exercise the real
  mapping but only as an incidental side effect of an HTTP-level test, not as a targeted
  mapping check, and doesn't test the `NULL` round-trip explicitly at the repository layer).
- **Regression risk**: this is precisely the layer this test class exists to protect, and it's
  the newest column in the schema. A future mapping mistake specific to `expiresAt` — wrong
  `@Column` name, wrong nullability, a timezone-handling regression in how H2 vs. Hibernate
  serializes `Instant` for that specific column — could plausibly still pass Hibernate's
  `ddl-auto: validate` startup check (which only checks structural compatibility: type/
  nullability/length, not round-trip value correctness) while silently corrupting the value on
  read-back, and nothing at this layer would catch it. The full-stack integration test would
  likely still catch a gross failure, but a subtler one (e.g. precision loss) is exactly the
  kind of thing a dedicated, minimal repository-level round-trip test is best positioned to
  catch quickly and in isolation from the rest of the HTTP stack.
- **Tests that should be added**: extend `savesAndReadsBackAllFields` (or add a sibling test)
  to assert `getExpiresAt()` is `null` after saving a mapping constructed via the 2-arg
  constructor, and add a new test that saves a mapping with a specific non-null `Instant`
  `expiresAt` and asserts the value read back via `findByShortCode` is `.isEqualTo(...)` the
  original — a direct, minimal round-trip check at exactly the layer this test file already
  exists to cover.

#### Recommended FR-7 test-improvement scenario

**Recommend Gap 2** (real-concurrency test for custom-alias collision) as the one scenario to
implement for FR-7, over Gaps 1 and 3, for these reasons:
- **Highest regression risk of the three** — it's the only one of the three protecting a
  public-facing correctness guarantee (no two callers can ever be silently granted the same
  alias) rather than an internal-error-shape or persistence-mapping detail.
- **Direct, on-point precedent already in this codebase** — `UrlMappingClickCountConcurrencyTest`
  is proof, in this exact project, that a mocked test protecting a "the database handles this
  race safely" claim is not equivalent to proving it, and that the gap between the two has
  already produced one real, previously-hidden bug here. Applying the identical technique to
  the one other place in the codebase making a similar "the database's `UNIQUE` constraint plus
  application-level handling makes this race-safe" claim is a natural, well-precedented next
  step, not a new pattern to justify from scratch.
- **Small and well-scoped** — one new test class, modeled directly on an existing one, no
  production code changes, no new dependencies, no new test infrastructure.

Gaps 1 and 3 remain valid, recorded findings and are reasonable candidates for a later pass
(e.g. alongside FR-8 documentation work or a future test-improvement iteration) but are not the
recommendation for this scenario.

- **Engineer decision (at analysis time)**: Pending — this entry deliberately implemented
  nothing at the time it was written. **Engineer decision: Accepted, Gap 2 only** — the
  recommended real-concurrency alias-collision test, exactly as proposed; Gaps 1 and 3 not
  actioned in this pass, left recorded above as candidates for a later iteration.
- **Validation performed (at analysis time)**: N/A — analysis only, per explicit instruction.
  All files read to ground this analysis (`GlobalExceptionHandler.java`, `UrlServiceTest.java`,
  `UrlMappingRepositoryTest.java`, `UrlMappingClickCountConcurrencyTest.java`,
  `UrlController.java`, `CreateUrlRequest.java`, `UrlMapping.java`, and a full-tree grep for
  concurrency primitives and specific error-message strings) were read-only, not modified.

### 2026-08-12 — Test-improvement scenario (FR-7): implement Gap 2 (real-concurrency alias-collision test)

- **Task / prompt intent**: engineer approved Gap 2 only from the analysis above — implement a
  real-concurrency test proving the custom-alias collision guarantee (colliding alias → `409`,
  no retry, no double-grant of the same alias to two callers) under genuine concurrent writes
  against the real H2 database and its `UNIQUE` constraint, not just a mocked simulation of a
  single race. No production code changes; test-only, per FR-7's own definition.
- **Implementation**: new
  `src/test/java/com/urlshortener/repository/UrlAliasCollisionConcurrencyTest.java`,
  structurally modeled on the existing `UrlMappingClickCountConcurrencyTest` (same package,
  same `@SpringBootTest`-not-`@DataJpaTest` reasoning — `@DataJpaTest` binds each test method to
  a single thread-local transaction, which would prevent worker threads from racing against
  each other or the real database at all). 20 threads, released together via a
  `CountDownLatch` pair (`allReady`/`go`) to maximize actual overlap, each call
  `UrlService.createShortUrl(uniqueUrl, "raceAlias", null)` for the **same** alias
  concurrently. Each worker catches its own `ShortCodeAlreadyExistsException` internally
  (the expected outcome for every losing thread) and returns `Optional.empty()`; any other,
  unexpected exception is left to propagate and surface via `Future.get()`'s
  `ExecutionException` — deliberately not swallowed, for the same reason
  `UrlMappingClickCountConcurrencyTest`'s Javadoc documents (a `submit()`-and-forget test can
  pass for the wrong reason). Assertions: exactly one of the 20 calls succeeds; the winning
  response's `shortCode` is the alias; the repository has exactly one row for the alias, and
  its `originalUrl` matches the winning caller's, not some other racer's.
- **Design decisions**:
  - Went through `UrlService.createShortUrl`, not the repository directly — the same
    reasoning as the click-count test: it's what actually runs under concurrent production
    traffic, and `createShortUrl` is deliberately *not* `@Transactional` (see its own Javadoc),
    so unlike the redirect path there's no `TransactionRequiredException` risk to route around
    here; each `save`/`existsByShortCode` call already gets its own repository-managed
    transaction.
  - Losing threads' `ShortCodeAlreadyExistsException` is caught *inside* each worker task, not
    asserted via a bare `assertThatThrownBy` per thread — with 20 concurrent callables, the
    losers' exceptions need to be individually inspected and counted, not just "expected
    somewhere," so each task normalizes its own outcome to `Optional<CreateUrlResponse>` and
    the test aggregates afterward.
  - The DB's `UNIQUE` constraint already makes a duplicate row physically impossible, so
    "exactly one row for the alias" is arguably implied by "exactly one success + 19
    `ShortCodeAlreadyExistsException`s" — the explicit repository assertion is kept anyway as a
    direct, low-cost confirmation that the winning row is actually the *correct* one (matches
    the winning caller's `originalUrl`), not just that a row exists.
- **Tests added**: 1 new test, `UrlAliasCollisionConcurrencyTest
  .concurrentCreatesForSameAlias_exactlyOneWins_restAreRejectedWithConflict` (154 → **155
  total**).
- **Problems/fixes**: none — passed on the first run.
- **Validation performed**:
  - New test run in isolation (`-Dtest=UrlAliasCollisionConcurrencyTest`): pass, ~9-14s per run
    (dominated by the 20-thread pool warm-up/`@SpringBootTest` context, not flakiness).
  - Re-run 3 additional times in isolation specifically to rule out concurrency-test flakiness
    before considering this done: **4/4 runs green**, no intermittent failures.
  - Full suite (`./mvnw clean test`): **155 tests, 0 failures, 0 errors** — every pre-existing
    test, including `UrlMappingClickCountConcurrencyTest`, still passes unchanged alongside the
    new test.
  - No production code was touched; `./mvnw clean package` not re-run separately since no
    `main/` source changed (test-only addition — nothing new to package differently).
- **Regression**: none — this is a strictly additive test file; nothing else in the suite was
  modified.
- **Scope status**: this satisfies FR-7 (test-improvement scenario) for this project. Gaps 1
  (generic-exception-handler coverage) and 3 (repository-level `expires_at` round-trip) remain
  valid, recorded-but-not-implemented findings from the gap analysis above, available as
  candidates for a future iteration if the engineer wants a second test-improvement pass.

### 2026-08-12 — Documentation-improvement scenario (FR-8): gap analysis — analysis only, no implementation

- **Task / prompt intent**: engineer asked for a review of the project's documentation from the
  perspective of an interviewer trying to understand and run it, identifying 3 meaningful
  documentation gaps — for each, what's missing/unclear, why it matters, what should be
  improved — plus one recommended small documentation-improvement scenario. Explicitly
  instructed: no application-code changes, no documentation written yet, record the analysis
  here and wait for approval.
- **Method**: inspected the repo root and `docs/` for what a first-time human reader would
  actually encounter (`ls`, search for a `README*`), checked `git ls-files` and `git status
  --ignored` to see what's *actually visible* to someone who receives this project via git
  rather than this exact local filesystem, checked `git log` for what's been committed so far,
  and searched for any existing summary/overview document. No code was modified — read-only.

#### Gap 1 — `.gitignore`'s `*.md` rule makes every documentation file invisible to git (confirmed again, with harder evidence than before)

- **What's missing/unclear**: `git ls-files | grep '\.md$'` returns **nothing** — zero markdown
  files are tracked. `git status --ignored` shows `CLAUDE.md` and `docs/` both marked `!!`
  (untracked *and* ignored). `git log` shows **5 real commits** already exist, spanning the
  full progression documented in these very files (initial setup, details endpoint, analytics
  endpoint, custom-alias brownfield, URL-expiration ambiguous-requirement) — and not one of
  them includes a single line of documentation. This was flagged twice already in this
  conversation (after the brownfield and expiration slices) but never acted on by either party.
- **Why it matters**: an interviewer's most likely path to this project is a `git clone` or a
  GitHub link, not access to this exact local working directory. If received via git today,
  they'd see a working URL shortener with real migrations and a real test suite and **zero**
  explanation of any of it — no `CLAUDE.md`, no `docs/REQUIREMENTS.md`, no
  `docs/AI_WORKLOG.md`. That's not a hypothetical risk; it's confirmed, current, reproducible
  fact about this repository right now. Given the assignment's stated objective is to
  demonstrate an AI-assisted engineering *process* on top of the code (not just the code
  itself), this single misconfiguration is capable of hiding the majority of what's actually
  being evaluated.
- **What should be improved**: this is a documentation-*delivery* gap, not a content gap —
  narrow the `.gitignore` rule (remove the blanket `*.md` line, or scope it to whatever
  IntelliJ-generated file it was originally meant to catch) and commit the three docs. Distinct
  in kind from Gaps 2 and 3 below (no new prose to write), and requires a git commit, which per
  standing practice in this project needs the engineer's explicit go-ahead rather than being
  done silently as a side effect of something else.

#### Gap 2 — No human-facing entry point: no `README.md` exists, and `CLAUDE.md` isn't shaped to serve as one

- **What's missing/unclear**: there is no `README.md` anywhere in the repo (not even one
  excluded by `.gitignore` — it simply doesn't exist; `.gitignore` only lists `HELP.md` by
  name plus the blanket `*.md`). GitHub/GitLab/Bitbucket all render `README.md` on a repo's
  landing page automatically; nothing here fills that role. `CLAUDE.md` is the closest analog,
  but its own opening line states its actual purpose: *"This file provides guidance to Claude
  Code... when working with code in this repository"* — it's written in second person to an AI
  coding agent, opens with an "Operating rules (binding for all work in this repo)" section
  and internal process directives, and has grown to ~755 lines covering everything down to
  Spring Boot 4.1 package-relocation trivia. A human has to read past the AI-operating-rules
  framing before reaching anything answering "what is this and how do I run it."
- **Why it matters**: the assignment's own Final Deliverables require the system be "runnable
  end-to-end as a prototype" with setup instructions (NFR-6). Those instructions do exist (the
  Commands table in `CLAUDE.md`), but they're buried in a ~755-line document not shaped for
  that discovery, and not the file any git host would surface first. An interviewer's first two
  minutes with this repo currently have no dedicated, human-first landing point telling them
  what this is, what's implemented, and how to run it.
- **What should be improved**: a short, human-oriented `README.md` at the repo root — what the
  project is (a couple of sentences), current implemented scope at a glance, a minimal
  quickstart (Docker Compose + `mvnw` in the fewest steps), and links out to `CLAUDE.md` /
  `docs/REQUIREMENTS.md` / `docs/AI_WORKLOG.md` for anyone who wants full engineering detail.
  Should point to those files, not duplicate their content — avoiding a second copy of the
  Commands table or architecture decisions that would drift out of sync with `CLAUDE.md` over
  time.

#### Gap 3 — No "Final Engineering Summary" deliverable exists anywhere

- **What's missing/unclear**: `docs/REQUIREMENTS.md`'s own deliverables checklist and
  acceptance-criteria table both explicitly list a **"Final Engineering Summary — plan and
  rationale, artifacts produced, risks, trade-offs, validation, assumptions, limitations"** as
  a required deliverable, still unchecked (`- [ ] Final Engineering Summary`). Searched the
  whole repo for anything resembling one (`*summary*`, case-insensitive): no matches.
- **Why it matters**: `docs/AI_WORKLOG.md` is now ~1500 lines — exactly what it's meant to be,
  a chronological, append-only traceability log — but not something an interviewer would read
  start-to-finish to get the big picture. Without a consolidated summary, evaluating the
  *outcome* of this exercise (not just re-tracing the whole process) requires reconstructing
  from scratch: what was built, which trade-offs were made and why, what's honestly still
  missing (security, reliability, auth — all explicitly deferred), and how to verify any of it
  — by reading all three documents cover to cover and cross-referencing them. That reconstruction
  is exactly the work this named deliverable exists to do once, on the interviewer's behalf.
- **What should be improved**: a new `docs/FINAL_SUMMARY.md` — plan and rationale (what was
  asked, how it was approached), artifacts produced (endpoints, schema, test suite, with
  numbers), key decisions and their rationale (short, with pointers into `AI_WORKLOG.md` for
  full detail rather than duplicating it), the required scenarios' outcomes (greenfield,
  brownfield, ambiguous-requirement, test-improvement), and an honest, named list of
  limitations/explicitly-out-of-scope items (no auth, no SSRF protection, no rate limiting —
  already documented piecemeal in `CLAUDE.md`'s Security/Reliability sections, but not
  gathered in one interviewer-facing place). This is a standalone required deliverable in its
  own right (`docs/REQUIREMENTS.md`), not exclusively an FR-8 artifact — worth noting it will
  need to exist regardless of which FR-8 scenario is chosen below.

#### Recommended FR-8 documentation-improvement scenario

**Recommend Gap 2** (add a human-facing `README.md`) as the one scenario to implement for
FR-8, over Gaps 1 and 3, for these reasons:
- **Purely additive content, cleanly scoped** — one new, reasonably short file, no risk of
  contradicting or duplicating existing docs if written to link out rather than restate, and
  no dependency on a git-hygiene decision the engineer hasn't yet made.
- **Directly answers the stated brief** — "an interviewer trying to understand and run it" is
  almost exactly what a good `README.md` exists to solve, and doing it well is a small,
  self-contained, easily-reviewed unit of work in the spirit of FR-8.
- **Distinct from Gap 3** — the Final Engineering Summary is a separately-required deliverable
  regardless of this scenario's outcome (see above), so recommending it *as* the FR-8 scenario
  would conflate a demonstration exercise with an obligation that exists either way; better to
  keep them visibly separate.

**Gap 1 is flagged as the most urgent of the three, independent of which scenario is chosen** —
a great `README.md` that git itself hides is not meaningfully better than no `README.md`, so
whichever gets built here, the `.gitignore` fix (and committing the docs) is worth doing
alongside or immediately after, once the engineer decides how they want it handled. Gap 3
remains a valid, recorded, not-yet-built deliverable, tracked separately from this decision.

- **Engineer decision (at analysis time)**: Pending — this entry deliberately implemented
  nothing at the time it was written. **Engineer decision: Accepted, Gap 2 only** — the
  recommended `README.md`, exactly as proposed; Gaps 1 and 3 not actioned in this pass, left
  recorded above as candidates for later.
- **Validation performed (at analysis time)**: N/A — analysis only, per explicit instruction.
  All inspection was read-only (`ls`, `git ls-files`, `git status --ignored`, `git log`, a
  repo-wide filename search for `*summary*`); nothing was modified.

### 2026-08-12 — Documentation-improvement scenario (FR-8): implement Gap 2 (human-facing README.md)

- **Task / prompt intent**: engineer approved Gap 2 only from the analysis above — add a short,
  human-oriented `README.md` at the repo root for an interviewer trying to understand and run
  the project, without duplicating `CLAUDE.md`'s content. No application-code changes.
- **Implementation**: new `README.md` at the repo root. Contents: a short framing paragraph
  (the code is the artifact you can run; the AI-assisted *process* behind it, documented in
  `CLAUDE.md`/`docs/AI_WORKLOG.md`, is the more important one — matching this project's stated
  objective rather than presenting it as an ordinary URL shortener); a bullet summary of what's
  implemented (4 endpoints, custom aliases, expiration, 155 tests) and which required scenarios
  are complete (greenfield, brownfield, ambiguous-requirement, test-improvement); a minimal
  copy-pasteable quickstart (`docker compose up -d` → `./mvnw spring-boot:run` → a
  create/redirect/details/analytics `curl` walkthrough) plus Swagger UI and health-check URLs;
  a "run the tests" one-liner; and a table pointing to `CLAUDE.md`, `docs/REQUIREMENTS.md`, and
  `docs/AI_WORKLOG.md` for full detail, explicitly including a pointer to where known
  limitations/out-of-scope items are documented, rather than restating them here.
- **Design decisions**:
  - Deliberately kept short (~90 lines) and link-forward rather than self-contained — the gap
    analysis's own concern about Gap 2 was discoverability, not missing content (the content,
    e.g. the Commands table, already exists in `CLAUDE.md`); duplicating it here would just
    create a second copy to keep in sync on every future change, which this project has
    consistently avoided elsewhere (see `CLAUDE.md`'s own "Shared response-building logic"
    and "Route patterns shared" conventions — same principle applied to docs, not code).
  - Quickstart commands cross-checked directly against `docker-compose.yml` and `CLAUDE.md`'s
    Commands table (host port `5433`, default app port `8080`, no `SERVER_PORT` override
    baked in since that was only needed on this dev machine when 8080 happened to be
    occupied by an unrelated process) rather than re-derived from memory.
- **Problems/fixes / important honest finding**: confirmed via `git status --short --ignored`
  immediately after creating the file that **`README.md` is itself caught by the same
  `.gitignore` `*.md` rule identified as Gap 1** (`!! README.md`) — exactly the failure mode
  Gap 1's analysis predicted ("a great `README.md` that git itself hides is not meaningfully
  better than no `README.md`"). Not fixed here, since the engineer approved Gap 2 only and
  Gap 1 (the `.gitignore` rule + committing docs) was explicitly left as a separate, pending
  decision — but surfaced immediately and prominently rather than silently left for the
  engineer to discover later. The file exists and is correct on disk; it is currently **not**
  visible to anyone who clones this repository via git.
- **Tests added**: none — documentation-only change, no test suite impact.
- **Validation performed**: read the finished file back for accuracy against
  `docker-compose.yml` and `CLAUDE.md`; confirmed no application code, test code, or existing
  documentation file was modified. Did not re-run `./mvnw test` (no source changed) or attempt
  to "run" the quickstart end-to-end in this pass, since it's the same sequence already
  validated multiple times earlier in this project's history (see the "Verify PostgreSQL
  runtime configuration end-to-end" and subsequent slice entries).
- **Regression**: none — strictly additive, single new file.
- **Scope status**: this satisfies FR-8 (documentation-improvement scenario) for this project,
  with the important caveat above. Gap 1 (`.gitignore`) remains the single highest-leverage
  unresolved item across both the FR-7 and FR-8 work in this project — it now affects a second,
  freshly-created deliverable, not just the three original docs. Gap 3 (Final Engineering
  Summary) remains a separately-tracked, still-required deliverable, not built in this pass.

### 2026-08-12 — Submission-readiness audit + Final Engineering Summary

- **Task / prompt intent**: engineer requested a final submission-readiness audit against the
  assignment brief — confirm clear demonstration of every required scenario/capability
  (greenfield, brownfield, ambiguous-requirement, test-improvement, documentation-improvement,
  well-defined-requirement handling, AI-assisted implementation/debugging/refactoring/testing/
  documentation, engineer approval and traceability, reliability/security consideration, setup
  instructions, architecture overview, testing approach, risks/trade-offs/assumptions/
  limitations), mapping each to existing project evidence and identifying only genuine
  remaining gaps (not inventing new scope). Also requested `docs/FINAL_ENGINEERING_SUMMARY.md`
  — concise and interviewer-friendly — covering objective, architecture/tech decisions, the
  three required scenarios, test/documentation improvements, the AI-assisted approach,
  validation/quality gates, risks/trade-offs, assumptions/limitations, and final status. No
  application-code changes.
- **Method**: reread `docs/REQUIREMENTS.md` end to end (all FR/NFR/PR tables, the deliverables
  checklist, acceptance criteria, and all 19 items in §6) and `CLAUDE.md`'s section headers and
  key sections (Current status, Security approach, Reliability approach, Key
  assumptions/constraints) against the actual repository state: `git status --ignored` and
  `git log` (to check what's really committed, not just what the docs claim), a fresh
  `./mvnw clean test` run (155/155, confirmed green before writing anything), and a check of
  `.gitignore`'s current content (discovered the engineer had already partially addressed Gap 1
  from the FR-8 analysis — see Findings below).
- **Findings — requirements fully satisfied**: FR-1 through FR-8 and FR-10 all confirmed
  implemented and validated with direct evidence (endpoint implementations, migrations, test
  files, and their corresponding `docs/AI_WORKLOG.md` entries); PR-1 through PR-18
  (engineering-process requirements) all confirmed satisfied by this project's own consistent
  pattern (options-before-decisions, impact-analysis-before-brownfield-code,
  analysis-before-ambiguity-resolution, analysis-before-test/doc changes, `docs/AI_WORKLOG.md`
  entries preserving rejected AI suggestions alongside accepted ones); NFR-1/2/3/6 satisfied;
  NFR-4/5 satisfied at the "defined approach, honestly scoped" level the brief asks for (not
  full production hardening, which was never in scope).
- **Findings — genuine remaining gaps** (not previously fully closed out):
  1. **`.gitignore`'s blanket `*.md` rule** — discovered, while grounding this audit in real
     repository state rather than trusting the docs' own prior claims, that the engineer had
     *already* partially fixed this since the last FR-8 entry: `git log` shows a new commit
     ("Add Project documentation") adding a `!README.md` exception and committing `README.md`
     (confirmed tracked via `git ls-files`). **`CLAUDE.md` and everything under `docs/`
     — including the two files this very audit is updating, and the new
     `FINAL_ENGINEERING_SUMMARY.md`** — remain untracked and ignored (confirmed via
     `git status --ignored` immediately after creating the new summary file). This is now the
     single highest-priority genuine gap in the project: `README.md`'s own links to `CLAUDE.md`
     and `docs/` point at files invisible in a fresh clone.
  2. **FR-9 (well-defined requirement) has no standalone, explicitly-labeled scenario.** Every
     other scenario in this project was engineer-specified in full, unambiguous detail up front
     and executed as such (all 4 greenfield endpoints, the brownfield alias work) — arguably
     already satisfying the underlying capability — but none was ever formally called out as
     *the* FR-9 demonstration. Recorded as an open item at the time (`docs/REQUIREMENTS.md` §6
     item 13); still open now. A labeling/documentation gap, not evidence of an actual
     capability gap.
  3. No other genuine gaps found. Specifically checked and confirmed **not** gaps: a
     Testcontainers-based PostgreSQL integration test (explicitly deferred by engineer
     decision, not a silent omission — §6 item 10); no lint/static-analysis tool (explicitly
     named as an open, undecided item throughout, not hidden); reliability/security mechanisms
     beyond what's implemented (explicitly named as deferred, with rationale, in `CLAUDE.md`'s
     Security/Reliability sections — this is exactly what NFR-4/NFR-5 ask for: a *defined*
     approach, honestly scoped, not full implementation).
- **Documentation updated as part of this audit** (beyond creating the new summary): fixed
  several items in `docs/REQUIREMENTS.md` that had gone stale relative to the actual repo state
  discovered above — several deliverables-checklist items (`Runnable end-to-end prototype`,
  `Architecture overview`, `Testing approach`, `Limitations`, `Trade-offs`,
  `Supporting documentation`, `Final Engineering Summary`) were still unchecked despite the
  underlying evidence already existing (in `CLAUDE.md` and, now, the new summary) — corrected
  to `[x]` with pointers, each with an honest caveat where one applies. §6 item 19 rewritten
  from "OPEN, not yet resolved" to "PARTIALLY RESOLVED" to reflect the `README.md` fix found
  above, while making the *remaining* scope (CLAUDE.md, all of `docs/`) unambiguous. `CLAUDE.md`
  "Current status" updated with the audit outcome and a directory-structure entry for the new
  file.
- **Implementation**: new `docs/FINAL_ENGINEERING_SUMMARY.md` — project objective, an
  architecture/tech-decisions table, one concise section per required scenario (greenfield,
  brownfield, ambiguous-requirement, test-improvement + documentation-improvement), an
  AI-assisted-engineering-approach section mapping implementation/debugging/refactoring/
  testing/documentation each to concrete evidence, engineer-approval/traceability, validation
  and quality gates, risks and trade-offs (including the `.gitignore` issue, named plainly as
  "the single largest realized risk in this project's process"), assumptions/limitations split
  into "explicitly out of scope" vs. "genuinely unresolved," and a final status summary.
  Written to link into `CLAUDE.md`/`docs/AI_WORKLOG.md` for full detail rather than duplicate
  either, consistent with how `README.md` was written for the same reason in the FR-8 pass.
- **Tests added**: none — audit and documentation only, no test-suite impact.
- **Validation performed**: `./mvnw clean test` re-run before and mentally cross-checked
  against after this pass — 155/155, unaffected (no production or test code touched).
  `git status --ignored` re-run immediately after creating
  `docs/FINAL_ENGINEERING_SUMMARY.md` to confirm, rather than assume, its own git-visibility
  status — confirmed ignored, consistent with the rest of `docs/`, and reported as such rather
  than silently left for the engineer to discover.
- **Problems/fixes**: none in code. The stale-checklist items found in `docs/REQUIREMENTS.md`
  (see above) weren't "bugs" so much as documentation debt from moving quickly through FR-6
  through FR-8 without a dedicated reconciliation pass — this audit *is* that reconciliation
  pass.
- **Regression**: none — no application or test code changed.
- **Engineer decision**: N/A for this entry — an audit-and-synthesis task performed as
  instructed, not a proposal awaiting a separate accept/reject decision. The two genuine gaps
  found are reported to the engineer for a decision on next steps, not resolved here.

### 2026-08-12 — Resolve `.gitignore` documentation-visibility gap; deliberately exclude CLAUDE.md

- **Task / prompt intent**: following the submission-readiness audit above, the engineer
  progressively narrowed `.gitignore`'s blanket `*.md` rule directly (outside this session,
  confirmed by inspecting `.gitignore` and `git log`/`git status` when asked "can you check
  now" / "fixed the docs folder but ignoring the claude.md file"): first a `!README.md`
  exception (already recorded in the FR-8 implementation entry above), then
  `!docs/REQUIREMENTS.md` and `!docs/AI_WORKLOG.md`, then generalized to `!docs/` and
  `!docs/*.md` (confirmed via `git check-ignore -v` covering the newer
  `docs/FINAL_ENGINEERING_SUMMARY.md` too, created after the two specific per-file exceptions).
  `CLAUDE.md` itself was left with no exception, on purpose. Asked to confirm the state was
  understood correctly and to make the engineer's intent explicit before treating it as final:
  presented three options (include `CLAUDE.md` too; keep it excluded and update every
  cross-reference; leave everything as-is) via a direct question. **Engineer decision: keep
  `CLAUDE.md` excluded, update the docs.**
- **Rationale (as given/inferred and confirmed by the decision)**: `CLAUDE.md` is Claude Code's
  own AI-agent-facing operating-instructions file and the most granular level of architecture/
  convention/API-field detail in the project — not shaped as an interviewer-facing artifact
  even where its content overlaps with one. Keeping it local-only while ensuring every
  interviewer-relevant fact it contained is also captured in `docs/FINAL_ENGINEERING_SUMMARY.md`,
  `docs/REQUIREMENTS.md`, or `docs/AI_WORKLOG.md` (all of which **are** committed) preserves the
  submission's completeness without shipping an AI-tool-internal file as if it were a
  deliverable.
- **Implementation**: updated every place that pointed to `CLAUDE.md` as if it were a
  submission-visible file, so no committed document contains a dead link:
  - `README.md` — intro paragraph and "Where to go next" table now point to
    `docs/FINAL_ENGINEERING_SUMMARY.md`/`docs/REQUIREMENTS.md`/`docs/AI_WORKLOG.md` instead;
    added an explicit note that `CLAUDE.md` is a local-only file, not part of the repository;
    inlined the `JAVA_HOME` troubleshooting tip that had only existed in `CLAUDE.md` before, so
    the quickstart stays self-sufficient without that file.
  - `docs/FINAL_ENGINEERING_SUMMARY.md` — intro paragraph, the `.gitignore` risk/trade-off
    bullet, and the assumptions/limitations section all rewritten: the `.gitignore` issue is no
    longer framed as an open risk (it's resolved), and a new "Documentation scope" subsection
    explains the `CLAUDE.md` decision plainly, listing exactly which four documents *are*
    committed and sufficient on their own.
  - `docs/REQUIREMENTS.md` — top status paragraph, the deliverables checklist (`Architecture
    overview`, `Setup instructions`, `Testing approach`, `Runnable end-to-end prototype`, the
    named-process-artifacts list's `CLAUDE.md` line), and §6 item 19 all rewritten from
    "OPEN"/"caveat: still git-ignored" framing to "RESOLVED, by explicit engineer decision,"
    with the `CLAUDE.md` exclusion stated as a considered choice rather than a leftover gap.
  - `CLAUDE.md` itself — still updated (it remains a living document by its own stated rules,
    used locally/for AI-agent continuity going forward) — "Current status" section rewritten to
    record the final resolution and an explicit reminder for future sessions: keep this file
    current, but anything an outside reviewer needs must *also* land in one of the three
    committed docs, since this file itself won't be visible to them.
  - `docs/AI_WORKLOG.md` (this file) — **not** retroactively edited for its ~39 pre-existing
    historical mentions of `CLAUDE.md`; those describe what was true/done at each point in the
    project's history and stay as written, consistent with this log's append-only convention.
    Only this new entry documents the final decision.
- **Tests added**: none — documentation/configuration only.
- **Validation performed**: `git check-ignore -v` re-run against `CLAUDE.md`,
  `docs/REQUIREMENTS.md`, `docs/AI_WORKLOG.md`, `docs/FINAL_ENGINEERING_SUMMARY.md`, and
  `README.md` to confirm the exact current state before writing anything (rather than trusting
  the prior entry's now-stale claims) — `CLAUDE.md` matched the blanket `*.md` rule (ignored);
  the other four matched a negating rule (not ignored). Re-read every edited document afterward
  to confirm no remaining reference treats `CLAUDE.md` as available in a fresh clone.
  `./mvnw test` not re-run — no application or test code touched by this entry.
- **Regression**: none.
- **Scope status**: the `.gitignore` item first flagged during the brownfield slice is now
  closed, with an explicit, documented engineer decision rather than left ambiguous. This
  project's documentation set, as committed, is now internally consistent: every link in
  `README.md`, `docs/REQUIREMENTS.md`, `docs/AI_WORKLOG.md`, and
  `docs/FINAL_ENGINEERING_SUMMARY.md` resolves to a file that is actually present in a fresh
  clone of this repository.

### 2026-08-12 — Close FR-9 (well-defined requirement scenario): formalize POST /api/urls as the demonstration

- **Task / prompt intent**: engineer directed closing the last open scenario, FR-9 ("the
  project must include at least one well-defined requirement handled end-to-end"), by formally
  designating the already-completed `POST /api/urls` create-endpoint slice as that
  demonstration — no re-implementation, no new features, no application-code changes; a
  retroactive but honest documentation pass over evidence that already exists, brought together
  and cross-referenced the way FR-6/FR-7/FR-8/FR-10 already are. Requested structure: the
  original clear requirement, task decomposition, acceptance criteria, implementation, tests,
  PostgreSQL validation, final result.
- **Why this slice qualifies, specifically**: `docs/REQUIREMENTS.md` §6 item 13 had left this
  open since the ambiguous-requirement work — noting that every other scenario in this project
  *was* engineer-specified in full, unambiguous detail up front, but none had been formally
  labeled as *the* FR-9 demonstration. `POST /api/urls` is the clearest candidate: the entire
  original engineer prompt (reproduced below) specified every field, constraint, validation
  rule, layering decision, and test expectation before a single line of code was written — in
  direct, deliberate contrast to FR-10's "Shortened URLs should expire," which was handed over
  *without* those specifics and required 10 clarification questions before implementation could
  even be planned. The two scenarios are each other's clearest illustration of "well-defined"
  vs. "ambiguous" in this project, which is exactly what FR-9/FR-10 together ask a submission to
  demonstrate.

#### The original clear requirement (as given, 2026-08-12, reproduced from the "Implement POST /api/urls" entry above)

A single, complete, unambiguous engineer specification, with no open questions posed back and
none needed: entity fields and constraints (`id`, `originalUrl`, `shortCode` unique/non-null/7
characters, `createdAt`, `clickCount` defaulting to `0`); a PostgreSQL-targeted `V1` Flyway
migration; the already-approved short-code strategy (`SecureRandom` Base62, no sequential IDs,
bounded collision retry, DB uniqueness as the real guarantee); a validated request DTO
(required/non-blank/syntactically-valid/`http`/`https`-only, clean `400` on failure); a
response DTO independent of the entity; business logic in the service layer with the controller
kept thin; centralized exception handling with no leaked internals; a specified minimum test
list; OpenAPI visibility for the new endpoint. Explicitly out of scope, stated up front:
redirect, details, analytics, auth, expiration, aliases, caching, rate limiting. Real
end-to-end validation against PostgreSQL was required in addition to automated tests. Nothing
in this specification was silent or open to interpretation — every acceptance criterion below
traces directly back to an explicit sentence in the original prompt.

#### Task decomposition

The specification was executed as eight sequenced, independently verifiable units — the same
decomposition already visible in the original implementation entry's file list, formalized here
as the decomposition it always was:

1. Persistence layer — `UrlMapping` entity + `V1__create_url_mapping_table.sql`, kept in exact
   sync (column lengths, nullability, the `CHECK (char_length(short_code) = 7)` constraint
   Hibernate's mapping can't itself express).
2. Short-code generation — `ShortCodeGenerator`, isolated with zero Spring/persistence
   dependencies specifically so it's trivially unit-testable on its own.
3. Request/response contracts — `CreateUrlRequest`/`CreateUrlResponse` records, plus a custom
   `@HttpUrl` Bean Validation constraint (the built-in `@URL` can't restrict to multiple
   specific schemes).
4. Business logic — `UrlService.createShortUrl`: bounded retry (5 attempts) across both a
   pre-check and the real `DataIntegrityViolationException` from the DB constraint; explicitly
   *not* `@Transactional`, for the documented reason (a single wrapping transaction would make
   PostgreSQL abort retries after the first collision).
5. HTTP layer — `UrlController`: thin, `201` + `Location` header (pointing at the
   future GET-details resource, not the public redirect URL — different resource, different
   REST semantics) + response body.
6. Error handling — `GlobalExceptionHandler` + `ErrorResponse` +
   `ShortCodeGenerationException`: one uniform JSON shape, no stack traces, covers validation
   failures, malformed JSON, and the (practically unreachable) exhausted-retry case.
7. API documentation — `OpenApiConfig`, Swagger/OpenAPI visibility for the new endpoint.
8. Verification — the test suite (unit/slice/full-stack) plus a dedicated real-PostgreSQL
   end-to-end validation pass, both required by the original spec, neither treated as optional.

#### Acceptance criteria

Derived directly from the original requirement, with no criterion added beyond what was asked:

- `originalUrl`: required, non-blank, ≤2048 characters, syntactically valid, `http`/`https`
  scheme only → `400` with a field-level error otherwise, never a stack trace.
- `shortCode`: exactly 7 characters, Base62 (`0-9a-zA-Z`), generated via `SecureRandom`; DB
  `UNIQUE` constraint is the real collision guarantee; bounded retry (5 attempts) on collision,
  loud `500` failure (not an infinite loop) if exhausted.
- Success: `201 Created`, `Location` header pointing at `/api/urls/{shortCode}`, response body
  containing `originalUrl`/`shortCode`/`shortUrl`/`createdAt` — never the persistence entity
  itself.
- All errors: uniform `ErrorResponse` JSON shape via centralized handling, no leaked exception
  internals.
- Endpoint visible in OpenAPI/Swagger.
- Out of scope for this slice, and correctly absent: redirect, details, analytics, auth,
  expiration, aliases, caching, rate limiting.
- Validated end-to-end against a real PostgreSQL database, not solely against the H2-backed
  automated suite.

#### Implementation

No code changed by this entry — implementation is the existing, unmodified production code
from the original slice: `entity/UrlMapping.java`, `db/migration/V1__create_url_mapping_table
.sql`, `service/ShortCodeGenerator.java`, `dto/CreateUrlRequest.java` /
`dto/CreateUrlResponse.java` / `dto/validation/HttpUrl.java` + `HttpUrlValidator.java`,
`service/UrlService.java` (the `createShortUrl`/`createWithGeneratedCode` path specifically —
now also shared with the later custom-alias and expiration additions, which extended rather
than altered this original contract), `controller/UrlController.java`,
`exception/GlobalExceptionHandler.java` + `ErrorResponse` + `ShortCodeGenerationException`,
`config/OpenApiConfig.java`. Full file-by-file detail already recorded in the "Implement
POST /api/urls (first vertical slice)" entry above — not duplicated here.

#### Tests

75 tests at the time of original implementation, across 7 classes exercising exactly this
slice's acceptance criteria: `ShortCodeGeneratorTest` (42), `HttpUrlValidatorTest` (15),
`UrlServiceTest` (5, create-path only at the time), `UrlMappingRepositoryTest` (3),
`UrlControllerTest` (5), `CreateShortUrlIntegrationTest` (4), `UrlShortenerApplicationTests`
(1). These classes have since grown (custom aliases, `expiresAt`, the alias-collision
concurrency test) as later scenarios extended the same create path, but the original
create-only acceptance criteria above remain fully covered within today's 155-test suite —
confirmed by rerunning it as part of this entry's validation (see below), not assumed.

#### PostgreSQL validation

Performed at the time of original implementation (`url-shortener-postgres` container via
Docker Compose): confirmed Flyway ran and validated cleanly against the real datastore before
Hibernate's `validate` check ("Successfully validated 1 migration," `flyway_schema_history`
created); a real `POST /api/urls` HTTP request returned `201` with a 7-character Base62
`shortCode` and the correct `Location` header; `psql` confirmed the row was actually persisted
(`SELECT * FROM url_mapping`, `count(*) = 1`) and that the live schema matched the migration
exactly (types, `NOT NULL`, `UNIQUE`, `CHECK`); blank `originalUrl` → `400`; `ftp://...` → `400`
(scheme rejected); `/actuator/health` → `200 UP`; `/v3/api-docs` included the new path;
`/swagger-ui/index.html` → `200`. Full narrative in the original entry above.

#### Final result

FR-9 is satisfied by `POST /api/urls`: a requirement specified completely and unambiguously up
front, decomposed into eight independently verifiable units, implemented exactly to the given
acceptance criteria (no scope drift in either direction), tested at every layer, and validated
end to end against a real PostgreSQL database — with zero clarification questions needed before
implementation could begin, the defining contrast against FR-10's ambiguous-requirement
scenario. `docs/REQUIREMENTS.md` §6 item 13 is now fully resolved (previously "PARTIALLY
RESOLVED," pending exactly this labeling decision).

- **Tests added**: none — no code changed; this entry documents existing, already-passing
  tests.
- **Validation performed**: `./mvnw clean test` re-run as part of this entry —
  **155 tests, 0 failures, 0 errors** — confirming the create-path acceptance criteria above
  remain covered by the current suite, not just the historical 75-test snapshot. No
  `./mvnw clean package` or real-PostgreSQL re-run performed for this entry specifically (no
  code changed; the original entry's real-PostgreSQL validation stands as the evidence for this
  slice, and no later scenario touching the same create path — custom aliases, expiration —
  reported a regression when it re-validated against real PostgreSQL at the time).
- **Regression**: none — documentation-only entry, zero application or test code touched.
- **Engineer decision**: **Accepted** — `POST /api/urls` formally designated as the FR-9
  demonstration, closing the last open required scenario in `docs/REQUIREMENTS.md`.
