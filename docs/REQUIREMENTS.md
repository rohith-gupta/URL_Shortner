# Requirements — AI-Assisted Software Engineering System: URL Shortener

Status: **Draft — normalized from assignment brief.** Backend technology stack (§6 item 1,
partial), initial API scope (§6 item 2), database engine (§6 item 10), short-code generation
strategy (§6 item 5), and schema management/Flyway (§6 item 18) are engineer-approved; all
other open items remain unreviewed. **All 4 approved API endpoints are implemented and
validated end-to-end against real PostgreSQL** — `POST /api/urls`, `GET /{shortCode}`,
`GET /api/urls/{shortCode}`, and `GET /api/urls/{shortCode}/analytics` — completing the
initial greenfield API surface. **The assignment's required brownfield scenario (FR-6) is
also now complete**: optional custom aliases added to `POST /api/urls`, with a
before-any-code impact analysis recorded in `docs/AI_WORKLOG.md`, validated end-to-end against
real PostgreSQL including a real schema migration (`V2`) applied to the already-existing
database. **The assignment's required ambiguous-requirement scenario (FR-10) is also now
complete**: the stakeholder statement "Shortened URLs should expire" was analyzed for
ambiguity and left unresolved/unimplemented until the engineer explicitly answered 10
clarification questions with concrete business rules; only then was optional, lazily-enforced
URL expiration implemented (new `V3` migration, `404` for expired codes, no scheduled jobs),
validated end-to-end against real PostgreSQL. **The assignment's required test-improvement
scenario (FR-7) is also now complete**: a gap analysis of the existing 154-test suite
identified 3 meaningful coverage gaps (recorded in `docs/AI_WORKLOG.md` before any test was
written); the engineer approved one, a real-concurrency test proving the custom-alias
collision guarantee under genuine concurrent writes — implemented, validated, no production
code changed. **The assignment's required documentation-improvement scenario (FR-8) is also
now complete**: a human-facing `README.md` was added after a gap analysis from an
interviewer's perspective. **A submission-readiness audit (2026-08-12) mapped every assignment
requirement/deliverable to project evidence and produced
[`docs/FINAL_ENGINEERING_SUMMARY.md`](FINAL_ENGINEERING_SUMMARY.md)** — see that document for
the concise, interviewer-facing synthesis, and §6 item 19 for the one genuine, still-open gap
that audit surfaced (most documentation, including this file, remains invisible to git).
Remaining project work: formally labeling a well-defined-requirement scenario (FR-9),
resolving the `.gitignore` issue, plus whatever reliability/security items get selected from
§6.
Last updated: 2026-08-12

This document normalizes the source-of-truth assignment brief into engineering-tractable
requirements. It does **not** invent scope beyond the brief. Where the brief is silent or
ambiguous, the item is listed under **Open Questions / Ambiguities** rather than resolved here.

Traceability of how these requirements are acted on (AI-assisted or otherwise) is recorded in
[docs/AI_WORKLOG.md](./AI_WORKLOG.md). Architecture/stack decisions derived from this document
belong in [CLAUDE.md](../CLAUDE.md) once made.

---

## 1. Functional Requirements (FR)

| ID | Requirement | Source | Status |
|----|-------------|--------|--------|
| FR-1 | The system must provide core API(s) to shorten a long URL into a short URL. | SCENARIO | **Implemented & validated** 2026-08-12 — `POST /api/urls` |
| FR-2 | The system must provide core API(s) to resolve/redirect a short URL to its original long URL. | SCENARIO ("Core APIs") | **Implemented & validated** 2026-08-12 — `GET /{shortCode}`, 302 redirect |
| FR-3 | The system must provide analytics on shortened URLs (usage is tracked and retrievable). | SCENARIO ("Analytics") | **Implemented & validated** 2026-08-12 — `GET /api/urls/{shortCode}/analytics` returns `clickCount`, tracked and retrievable end-to-end against real PostgreSQL. Deliberately basic by explicit scope decision: no referrer/device/geography breakdown (see §6 item 3). |
| FR-4 | The system must implement reliability features (concrete mechanisms TBD — see Open Questions). | SCENARIO ("Reliability features") | Partially — bounded short-code collision retry and atomic click-count increment (concurrency-tested against real threads) implemented & tested; other mechanisms not started |
| FR-5 | The project must include a greenfield development scenario (new feature/service built from scratch). | SCENARIO | **Complete for the initial API surface** — all 4 approved endpoints (`POST /api/urls`, `GET /{shortCode}`, `GET /api/urls/{shortCode}`, `GET /api/urls/{shortCode}/analytics`) implemented and validated 2026-08-12 |
| FR-6 | The project must include a brownfield scenario: enhancement, refactor, or bug fix against existing code. | SCENARIO | **Implemented & validated** 2026-08-12 — optional custom aliases added to `POST /api/urls`; full impact analysis recorded before implementation (`docs/AI_WORKLOG.md`); real schema migration (`V2`) applied to the pre-existing database; all pre-existing endpoints/tests confirmed unaffected |
| FR-7 | The project must include a test-improvement scenario (adding/strengthening tests on existing functionality). | SCENARIO | **Implemented & validated** 2026-08-12 — gap analysis identified 3 meaningful coverage gaps in existing functionality (recorded in `docs/AI_WORKLOG.md` before any test was written); engineer approved one (real-concurrency test proving the custom-alias collision guarantee under genuine concurrent writes, not just a mocked race); implemented as `UrlAliasCollisionConcurrencyTest`, validated with repeated runs to rule out flakiness (4/4 green) plus a full-suite regression pass (155/155). No production code changed. |
| FR-8 | The project must include a documentation-improvement scenario. | SCENARIO | **Implemented** 2026-08-12 — gap analysis identified 3 meaningful documentation gaps from an interviewer's perspective (recorded in `docs/AI_WORKLOG.md` before anything was written); engineer approved one (a human-facing `README.md`, since none existed); implemented, cross-checked against `docker-compose.yml`/`CLAUDE.md` for accuracy. **Caveat**: the new `README.md` is itself caught by the still-unresolved `.gitignore` `*.md` issue (§6 item 19) — written and correct on disk, not yet visible via git. |
| FR-9 | The project must include at least one well-defined requirement handled end-to-end. | SCENARIO | Not started |
| FR-10 | The project must include at least one ambiguous requirement, with the ambiguity explicitly identified and resolved/normalized before implementation. | SCENARIO | **Implemented & validated** 2026-08-12 — stakeholder statement "Shortened URLs should expire" analyzed for ambiguity *before* any code/schema change (10 clarification questions, 3 options, all recorded in `docs/AI_WORKLOG.md`); engineer resolved all 10 with explicit business rules; only then implemented: optional `expiresAt` on `POST /api/urls`, lazy request-time enforcement via a centralized `UrlMapping.isExpired`/`UrlService.findActiveMapping`, `404` (not `410`) for expired codes — indistinguishable from unknown codes by design — new `V3` migration, no scheduled jobs. Validated end-to-end against real PostgreSQL, including pre-existing rows confirmed unaffected (`expires_at = NULL`, no retroactive expiry). |

## 2. Non-Functional Requirements (NFR)

The brief names these quality dimensions explicitly (via Quality Gates and Evaluation
Priorities) without quantitative targets. Categories are firm; numeric thresholds are TBD and
must be proposed and reviewed before being treated as requirements.

| ID | Requirement | Source |
|----|-------------|--------|
| NFR-1 | Design must be modular, testable, reliable, secure, and scalable. | EVALUATION PRIORITIES |
| NFR-2 | Code must be production-quality, clean, and maintainable. | ENGINEERING OUTPUT |
| NFR-3 | Changes must pass quality gates: analysis, linting, tests, security, performance. | CORE ENGINEERING REQUIREMENTS §4 |
| NFR-4 | The system must demonstrate a defined reliability approach (specific mechanisms TBD). | SCENARIO, CLAUDE.md REQUIREMENT |
| NFR-5 | The system must demonstrate a defined security approach (specific mechanisms TBD). | Quality Gates, CLAUDE.md REQUIREMENT |
| NFR-6 | The system must be runnable end-to-end as a prototype (setup instructions provided). | FINAL DELIVERABLES |

## 3. Engineering-Process Requirements (PR)

These govern *how* the work is done, independent of the shipped product, and are binding for
every session on this project.

| ID | Requirement | Source |
|----|-------------|--------|
| PR-1 | Interpret intent, identify ambiguity, and normalize requirements before implementing. | CORE REQ §1 |
| PR-2 | Decompose high-level requirements into actionable tasks with dependencies and sequencing. | CORE REQ §2 |
| PR-3 | For brownfield work, identify impacted modules/services/APIs/data flows before changing them. | CORE REQ §3 |
| PR-4 | Every meaningful AI task must be framed with: Intent, Constraints, Acceptance Criteria, Technical Context. | CORE REQ §4 |
| PR-5 | Use disciplined prompting and iterative refinement (not one-shot autonomous generation). | CORE REQ §4 |
| PR-6 | Maintain traceability of AI-generated output, engineer-edited output, rejected AI suggestions, and rationale for important decisions, in `docs/AI_WORKLOG.md`. | CORE REQ §4, TRACEABILITY REQUIREMENT |
| PR-7 | Apply quality gates (analysis, linting, tests, security, performance) to changes. | CORE REQ §4 |
| PR-8 | High-impact changes require explicit human/engineer approval before execution. | CORE REQ §4, §7 |
| PR-9 | The engineer retains ownership of correctness, maintainability, and production readiness — AI assists, does not decide. | CORE REQ §4, §7, Development approach statement |
| PR-10 | No major architectural or technology decision is made without first presenting options and rationale to the engineer. | OPERATING RULE 1 |
| PR-11 | Implementation does not start until requirements and architecture have been reviewed. | OPERATING RULE 2 |
| PR-12 | No assignment requirements are invented. | OPERATING RULE 3 |
| PR-13 | Engineer ownership is kept explicit throughout artifacts and process. | OPERATING RULE 4 |
| PR-14 | Before a major change: state Intent, Files/modules affected, Acceptance criteria, Risks, Validation approach. | OPERATING RULE 5 |
| PR-15 | After a change: run the appropriate quality checks. | OPERATING RULE 6 |
| PR-16 | Never silently ignore a failed test, lint error, security concern, or requirement. | OPERATING RULE 7 |
| PR-17 | Prefer simple, production-quality architecture over unnecessary complexity. | OPERATING RULE 8 |
| PR-18 | `CLAUDE.md` is a living document, updated whenever architecture, stack, structure, commands, API design, schema, conventions, testing strategy, security/reliability approach, key decisions, assumptions, or constraints change. | CLAUDE.md REQUIREMENT |

## 4. Deliverables

Per **FINAL DELIVERABLES** in the brief:

- [x] Runnable end-to-end prototype — validated repeatedly against real PostgreSQL via Docker Compose (see `docs/AI_WORKLOG.md` validation notes on every scenario); **caveat**: only reproducible by someone with this exact local checkout right now, see §6 item 19
- [x] Architecture overview (components, tools, execution approach, control flow, key architectural decisions) — `CLAUDE.md`'s "Architecture / Tech stack", "API design", and "Database / schema design" sections; synthesized for a first-time reader in `docs/FINAL_ENGINEERING_SUMMARY.md`
- [x] Greenfield scenario (artifact/evidence) — 4 vertical slices, `POST /api/urls` through `GET /api/urls/{shortCode}/analytics`, each with its own `docs/AI_WORKLOG.md` entry and real-PostgreSQL validation
- [x] Brownfield scenario (artifact/evidence) — optional custom aliases on `POST /api/urls`; impact analysis recorded *before* implementation, real `V2` schema migration applied to the pre-existing database, all pre-existing tests/endpoints confirmed unaffected (`docs/AI_WORKLOG.md`)
- [x] Ambiguous-requirement scenario (artifact/evidence) — "Shortened URLs should expire," analyzed for ambiguity *before* any code (10 clarification questions, no code/schema change) then implemented only after the engineer resolved all 10 with explicit business rules (`docs/AI_WORKLOG.md`)
- [x] Setup instructions — `README.md` quickstart (Docker Compose + `mvnw`, plus a `curl` walkthrough); `README.md` itself is now tracked in git (fixed since first written — see §6 item 19), but the deeper docs it links to (`CLAUDE.md`, `docs/REQUIREMENTS.md`, `docs/AI_WORKLOG.md`, this file's own directory) are still git-ignored
- [x] Testing approach (documented) — `CLAUDE.md`'s "Testing strategy" section; summarized in `docs/FINAL_ENGINEERING_SUMMARY.md`
- [x] Limitations (documented) — `CLAUDE.md`'s "Security approach"/"Reliability approach" sections document them piecemeal as each arose; gathered in one place in `docs/FINAL_ENGINEERING_SUMMARY.md`
- [x] Trade-offs (documented) — scattered across individual `docs/AI_WORKLOG.md` decision entries; gathered in one place in `docs/FINAL_ENGINEERING_SUMMARY.md`
- [x] API/schema definitions — all 4 approved endpoints documented (OpenAPI/Swagger + `CLAUDE.md`): `POST /api/urls` (incl. optional `customAlias`), `GET /{shortCode}`, `GET /api/urls/{shortCode}`, `GET /api/urls/{shortCode}/analytics`
- [x] Unit tests — 155 tests total across generator/validator/entity/service/repository (see `CLAUDE.md` Testing strategy for the full breakdown)
- [x] Integration tests — `@DataJpaTest`, `@WebMvcTest`, and full-stack `@SpringBootTest` suites for all 4 endpoints (including custom-alias and expiration behavior), plus two real-concurrency tests (click-count increment, custom-alias collision)
- [x] Supporting documentation — OpenAPI/Swagger UI, `README.md`, `CLAUDE.md`, this file
- [x] Final Engineering Summary — [`docs/FINAL_ENGINEERING_SUMMARY.md`](FINAL_ENGINEERING_SUMMARY.md); **caveat**: currently git-ignored along with the rest of `docs/`, see §6 item 19

Plus the process artifacts required explicitly by name:

- [x] `CLAUDE.md` (created 2026-08-12, living document)
- [x] `docs/REQUIREMENTS.md` (this file)
- [x] `docs/AI_WORKLOG.md` (created 2026-08-12, traceability log)

## 5. Acceptance Criteria

| Deliverable / Requirement | Acceptance Criteria |
|---|---|
| Prototype | Runs end-to-end locally by following the setup instructions in the repo docs, with no undocumented manual steps. |
| Core APIs (FR-1, FR-2) | **Fully met.** All 4 approved endpoints — `POST /api/urls`, `GET /{shortCode}`, `GET /api/urls/{shortCode}`, `GET /api/urls/{shortCode}/analytics` — implemented and validated end-to-end (real PostgreSQL, real HTTP, 155 automated tests). |
| Brownfield scenario (FR-6) | **Met.** Optional custom aliases added to the existing `POST /api/urls`; impact analysis (impacted modules, schema, validation, error-handling, tests, risks) recorded in `docs/AI_WORKLOG.md` *before* any code change; real `V2` Flyway migration applied to the already-existing PostgreSQL database (not a fresh one); all pre-existing endpoints, tests, and generated-code behavior confirmed unaffected by rerunning the full suite plus a dedicated real-PostgreSQL end-to-end pass. |
| Test-improvement scenario (FR-7) | **Met.** Existing 154-test suite reviewed for coverage gaps in existing functionality (not new features); 3 gaps identified and recorded in `docs/AI_WORKLOG.md` *before* any test was written, each with what's under-tested/regression risk/tests-needed; engineer approved one (real-concurrency proof of the custom-alias collision guarantee); implemented with zero production-code changes, validated via repeated isolated runs (4/4 green, ruling out flakiness) and a full-suite regression pass (155/155). |
| Analytics (FR-3) | **Met (basic scope).** `click_count` is correctly and atomically incremented on every redirect (concurrency-tested) and retrievable via `GET /api/urls/{shortCode}/analytics`, validated end-to-end including direct verification against PostgreSQL. Richer analytics (referrer/device/geography) explicitly deferred (§6 item 3) — not a gap, a scope decision. |
| Reliability (FR-4) | **Partially met.** Two concrete mechanisms implemented with documented failure-mode reasoning: bounded short-code collision retry (`docs/AI_WORKLOG.md`, "Implement POST /api/urls"), and atomic click-count increment avoiding lost updates under concurrency (`docs/AI_WORKLOG.md`, "Implement GET /{shortCode}"). Other candidate mechanisms (rate limiting, caching, idempotency) not started. |
| AI task traceability (PR-6) | Every meaningful AI-assisted task has a corresponding `docs/AI_WORKLOG.md` entry with Task, Prompt/Intent, AI recommendation, Engineer decision (Accepted/Modified/Rejected), Rationale, Validation performed. |
| CLAUDE.md currency (PR-18) | No PR/change lands that alters architecture, stack, structure, commands, API/schema, conventions, testing, security/reliability approach, or key assumptions without a corresponding `CLAUDE.md` update in the same change. |
| Quality gates (PR-7, NFR-3) | Each change lists which of analysis/lint/tests/security/performance were run and their results (pass, or documented known-failure with rationale) — never silently skipped. |
| Ambiguity handling (FR-10) | **Met.** "Shortened URLs should expire" shown going from ambiguous (10 clarification questions, 3 options, zero code/schema changes) → explicit engineer normalization (10 documented business-rule decisions with rationale) → implementation (optional `expiresAt`, lazy enforcement, `404` semantics, new `V3` migration), validated end-to-end against real PostgreSQL — full record in `docs/AI_WORKLOG.md`. |
| Final Engineering Summary | A single document exists covering plan/rationale, artifacts, risks, trade-offs, validation, assumptions, and limitations, consistent with the rest of the docs. |

## 6. Open Questions / Ambiguities

These are gaps or open decisions in the assignment brief itself. Per Operating Rules 1–3, none
are resolved unilaterally — each needs an options-plus-rationale discussion with the engineer
before or during the relevant work, and the resolution should be logged in
`docs/AI_WORKLOG.md` and reflected in `CLAUDE.md`. Items marked **RESOLVED**/**PARTIALLY
RESOLVED** are kept here for audit trail; the full options considered, AI recommendation, and
engineer rationale live in `docs/AI_WORKLOG.md`.

1. **Technology stack — PARTIALLY RESOLVED (2026-08-12).** Language, runtime, framework, and build tool are approved: **Java 21 + Spring Boot** (Spring Web, Spring Data JPA, Spring Boot Validation, Spring Boot Test, Spring Boot Actuator), **Maven**. See `docs/AI_WORKLOG.md` entry "Decision: Backend technology stack" for the three options compared, the AI recommendation, and the engineer's rationale for choosing differently. **The datastore is explicitly not approved** and remains open — tracked as item 10 below.
2. **"Core APIs" scope — RESOLVED (2026-08-12); 3 of 4 endpoints implemented (2026-08-12).** Initial scope approved as Option B (Core + Analytics): create (`POST /api/urls` — **implemented & validated**), redirect (`GET /{shortCode}` — **implemented & validated**), retrieve details (`GET /api/urls/{shortCode}` — **implemented & validated**, read-only, deliberately excludes `clickCount`), retrieve analytics (`GET /api/urls/{shortCode}/analytics` — not started). Custom aliases, update, delete, and listing are explicitly deferred (see items 6–7 below and `docs/AI_WORKLOG.md` entry "Decision: Core API scope"). Exact request/response schema for the one remaining endpoint is still open.
3. **Analytics scope — RESOLVED (2026-08-12); implemented & validated (2026-08-12).** Basic scope approved and now built: `clickCount` (plus `shortCode`/`shortUrl`/`originalUrl`/`createdAt`) via `GET /api/urls/{shortCode}/analytics`, confirmed end-to-end against real PostgreSQL. Richer signals (referrer, geography, device/user-agent, unique vs. total clicks) remain explicitly deferred as a brownfield/ambiguous-requirement candidate — deliberately not started, not a gap.
4. **"Reliability features" scope — PARTIALLY RESOLVED.** Two mechanisms implemented as natural consequences of other work (not a full options review of the category): short-code collision handling (bounded retry against the `UNIQUE` constraint) and atomic click-count increment (single `UPDATE ... SET x = x + 1`, concurrency-tested against 20 real threads — see `docs/AI_WORKLOG.md`, "Implement GET /{shortCode}"). Remaining candidates not yet evaluated: rate limiting, caching, retry/backoff for external calls, graceful degradation.
5. **Short-code generation strategy — RESOLVED (2026-08-12).** Random 7-character Base62 code (`0-9a-zA-Z`) generated with `java.security.SecureRandom`; `UNIQUE` constraint on the short-code column in PostgreSQL; on collision, regenerate and retry with a small, bounded retry count. An AI-recommended alternative (Base62 encoding of an auto-incrementing ID) was explicitly rejected as predictable/enumerable — see `docs/AI_WORKLOG.md` entry "Decision: Short-code generation strategy" for the full AI-recommendation-vs-engineer-decision record.
6. **Custom alias / vanity URL support — RESOLVED (2026-08-12).** Explicitly deferred from the initial greenfield scope; flagged as a candidate for a later brownfield/ambiguous-requirement scenario (`docs/AI_WORKLOG.md` entry "Decision: Core API scope").
7. **URL expiration / TTL — RESOLVED and IMPLEMENTED (2026-08-12).** Initially deferred from greenfield scope, then used as the assignment's ambiguous-requirement scenario (FR-10): stakeholder statement "Shortened URLs should expire" analyzed for ambiguity (10 clarification questions, no code/schema change) before the engineer resolved all 10 with explicit business rules — optional per-URL `expiresAt` (absolute UTC timestamp, must be strictly future at creation, `400` otherwise), lazy request-time enforcement only (no scheduled jobs), `404` for expired codes (indistinguishable from unknown codes, not `410`), no retroactive expiry of existing rows, no physical deletion. Implemented and validated end-to-end against real PostgreSQL — see `docs/AI_WORKLOG.md` entries "Ambiguity analysis: 'Shortened URLs should expire'" and "Resolution and implementation".
8. **AuthN/AuthZ — PARTIALLY RESOLVED (2026-08-12).** Explicitly excluded from the initial greenfield scope (no login, no ownership model, no protected endpoints). Whether/how it's added later (e.g., to scope analytics access or enable update/delete) remains open and is flagged as a candidate ambiguous-requirement or brownfield scenario, not decided now.
9. **Rate limiting specifics** — if selected as a reliability feature, thresholds/policy are undefined.
10. **Persistence model / database engine — RESOLVED (2026-08-12), connectivity verified (2026-08-12).** PostgreSQL is the primary runtime datastore, run locally via Docker Compose (`docker-compose.yml`, PostgreSQL 17, host port **5433** — remapped from the default 5432 due to a pre-existing native Postgres service on the dev machine; see `docs/AI_WORKLOG.md` entry "Verify PostgreSQL runtime configuration end-to-end"). H2 is used only for lightweight automated tests (`src/test/resources/application.yml`). See `docs/AI_WORKLOG.md` entry "Decision: Database technology". **Still open**: whether a PostgreSQL Testcontainers-based integration test is also needed (deferred by the engineer) — see also item 18 (schema/migration strategy).
11. **Deployment target** — brief requires a "runnable end-to-end prototype" but does not specify local-only, containerized, or cloud-deployed. Assumed local-runnable-by-default unless stated otherwise; to be confirmed.
12. **Brownfield baseline — RESOLVED (2026-08-12).** Confirmed by the engineer's own instruction to proceed: the brownfield scenario (FR-6) was enacted on `POST /api/urls`, the greenfield code produced earlier in this same project — greenfield-then-brownfield sequencing within the assignment, not a pre-existing external codebase. See `docs/AI_WORKLOG.md`, "Brownfield: add optional custom aliases to POST /api/urls".
13. **Well-defined vs. ambiguous requirement scenarios (FR-9, FR-10) — PARTIALLY RESOLVED (2026-08-12).** FR-10 (ambiguous) is now satisfied by a purpose-built, clearly-labeled scenario: "Shortened URLs should expire," analyzed and resolved per items above. Whether FR-9 (well-defined requirement) needs its own similarly purpose-built scenario, or whether it's sufficient to point at an already-completed slice with an unambiguous spec (e.g., the brownfield custom-alias work, or any of the four core endpoints, all of which were engineer-specified in full detail up front) remains open — still needs confirmation.
14. **"High-impact change" definition (PR-8)** — no threshold given for what qualifies as high-impact (e.g., schema changes, public API contract changes, security-relevant changes, deletions). Needs an explicit working definition so approval gating is applied consistently.
15. **Quantitative NFR targets** — no numeric SLAs/SLOs given for performance (latency, throughput) or reliability (uptime, error budget). If quantified targets are wanted for the prototype, they need to be proposed and approved rather than assumed.
16. **CI/CD** — the brief requires quality gates be applied to changes but does not require a CI pipeline explicitly. Unclear whether gates are expected to run locally only, or wired into CI as part of the deliverable.
17. **Test framework/tooling — PARTIALLY RESOLVED (2026-08-12).** Spring Boot Test (JUnit 5, Mockito) approved as part of the stack decision (item 1). Specific conventions (e.g., whether Testcontainers is used for integration tests against a real database) remain open and depend on the database decision (item 10). No specific coverage target given beyond "unit tests" and "integration tests" being required deliverables.
18. **Schema/migration strategy — RESOLVED (2026-08-12).** Flyway owns the schema (`src/main/resources/db/migration`); Hibernate `ddl-auto` is `validate` only — never creates/alters the production schema, and startup fails fast on any entity/schema mismatch. This was an engineer directive (not an AI-recommendation-then-approval — recorded as such in `docs/AI_WORKLOG.md`, "Decision: Schema management (Flyway)"). V1 migration created and validated against real PostgreSQL the same day.
19. **`.gitignore`'s blanket `*.md` rule hides most documentation files from git — PARTIALLY RESOLVED, still OPEN for the rest.** `README.md` is now fixed: the engineer added a `!README.md` exception under the `.gitignore`'s IntelliJ IDEA section and committed it (`git log`: "Add Project documentation") — confirmed via `git ls-files` that it's tracked. **`CLAUDE.md` and everything under `docs/`** (`REQUIREMENTS.md`, `AI_WORKLOG.md`, and the new `FINAL_ENGINEERING_SUMMARY.md`) **remain untracked and ignored** — confirmed via `git status --ignored` as of the submission-readiness audit. This means `README.md`'s own links to those files (and this file's cross-references throughout) point at files that don't exist in a fresh clone of this repository as it stands. Still the single highest-leverage unresolved item in the project — flagged after the brownfield slice, the expiration slice, the FR-8 gap analysis, and again here; needs an explicit engineer decision (narrow the rule further, then commit `CLAUDE.md` and `docs/`) before this project can be considered submission-ready.

---

*This document is normative for scope. Do not begin implementation of any item marked TBD/open
above without first resolving it via the options-and-rationale process required by the
Operating Rules, and recording the resolution in `docs/AI_WORKLOG.md`.*
