# Final Engineering Summary — URL Shortener

Concise, interviewer-facing summary of what was built and how. Full detail, options
considered, and every AI-assisted decision with its rationale live in
[`docs/AI_WORKLOG.md`](AI_WORKLOG.md) (chronological traceability log) and
[`docs/REQUIREMENTS.md`](REQUIREMENTS.md) (normalized requirements and acceptance criteria) —
this document synthesizes both rather than duplicating them. (`CLAUDE.md`, the day-to-day
AI-agent-facing operating instructions and most granular architecture detail, is maintained
locally but deliberately not included in this repository — see "Documentation scope" under
Assumptions and limitations.)

## Project objective

Not just a working URL shortener, but a demonstration of an AI-assisted software engineering
*process*: requirement understanding, task decomposition, disciplined AI-assisted execution,
quality gates, and full traceability — across a greenfield build, a brownfield change, a
well-defined requirement, a deliberately ambiguous requirement, a test-improvement pass, and a
documentation-improvement pass. Development approach throughout: engineer-led execution
accelerated by AI, not autonomous
orchestration — the engineer reviewed and approved every architectural decision and every
scenario before implementation began.

## Architecture and technology decisions

| Decision | Outcome | Notable process detail |
|---|---|---|
| Backend | Java 21 + Spring Boot (Web, Data JPA, Validation, Test, Actuator), Maven | AI recommended Python/FastAPI first; engineer overrode with an explicit constraint, recorded as such |
| Database | PostgreSQL 17 (Docker Compose, host port 5433 — a local port collision, not a design choice), H2 in-memory for tests | — |
| Schema management | Flyway-owned migrations (`V1`–`V3`); Hibernate `ddl-auto: validate` only | Engineer directive, not an AI-recommend-then-approve decision |
| Short-code generation | Random 7-char Base62 via `SecureRandom`, DB `UNIQUE` constraint, bounded retry on collision | AI's first recommendation (Base62 of a sequential ID) was **rejected** by the engineer as enumerable/predictable — the project's clearest reject-and-redirect example |
| API scope | 4 endpoints: create, redirect, details, analytics | Chosen from 3 presented options |

Full option comparisons and rationale for each: `docs/AI_WORKLOG.md`, "Decision: ..." entries.

## Greenfield scenario

Built from nothing: `POST /api/urls` (create, with request validation and a uniform error
shape), `GET /{shortCode}` (302 redirect with atomic click-count increment),
`GET /api/urls/{shortCode}` (read-only details), `GET /api/urls/{shortCode}/analytics`
(read-only click-count analytics) — real controller/service/repository/entity layers, a real
Flyway `V1` migration, OpenAPI/Swagger documentation. Each of the 4 endpoints was its own
vertical slice with its own `docs/AI_WORKLOG.md` entry and its own real-PostgreSQL validation
pass, not one undifferentiated build. One real bug found and fixed along the way: unmatched
routes were falling through to a generic exception handler and returning `500` instead of
`404` — fixed with an explicit `NoResourceFoundException` handler (see "AI-assisted
engineering approach" below).

## Brownfield scenario

Enhancement against the greenfield code above: `POST /api/urls` gained an optional
`customAlias`, used verbatim instead of a generated code. A full impact analysis (affected
modules, schema, validation, error handling, tests, risks) was written and reviewed **before**
any code changed — it correctly predicted that widening the short-code route pattern would
invalidate several existing "malformed code" test examples, which were then fixed proactively
as part of the same change instead of being discovered later by a failing build. Required a
real, additive `V2` schema migration (`short_code` widened, new `CHECK` constraints) applied to
the *already-existing* database, not a fresh one. Collision handling deliberately differs from
the generated-code path: a colliding alias returns `409` immediately with no retry, since
retrying would silently substitute a short code other than the one the caller asked for.

## Ambiguous-requirement scenario

Stakeholder statement: *"Shortened URLs should expire."* Deliberately treated as ambiguous
first — a dedicated analysis-only pass identified 10 unanswered business-rule questions
(trigger, scope, representation, validation, effect on existing rows, affected endpoints,
status code, enforcement mechanism, data retention) and presented 3 implementation options,
**with zero code or schema changes**, explicitly waiting for the engineer's resolution rather
than assuming reasonable defaults. The engineer then answered all 10 explicitly; only at that
point was it implemented: an optional, absolute-UTC `expiresAt` on `POST /api/urls`, lazy
request-time enforcement only (no scheduled jobs), and — the most consequential single design
decision — expired short codes return `404`, identical in shape to an unknown code, so a caller
can never tell "never existed" from "existed but expired." A new `V3` migration; zero
retroactive expiry of the 6 pre-existing rows in the validation database; no physical deletion,
ever.

## Well-defined requirement scenario

`POST /api/urls` — the original create-endpoint slice — is formally designated as this
project's demonstration of a well-defined requirement handled end-to-end, the deliberate
counterpart to the ambiguous-requirement scenario above. Its original engineer specification
left nothing open to interpretation: exact entity fields and constraints, the migration, the
already-approved short-code strategy, precise request-validation rules, a specified minimum
test list, explicit exclusions (redirect, details, analytics, auth, expiration, aliases,
caching, rate limiting), and a required real-PostgreSQL validation pass — all stated up front,
with zero clarification questions needed before implementation could begin. It was decomposed
into eight independently verifiable units (persistence layer, short-code generation,
request/response contracts, business logic, HTTP layer, error handling, API documentation,
verification), implemented exactly to that specification, tested at every layer, and validated
end to end against real PostgreSQL (Flyway migration applied, a real HTTP request persisted and
read back via `psql`, error cases and health/OpenAPI endpoints all confirmed). Full task
decomposition, acceptance criteria, and validation record: `docs/AI_WORKLOG.md`, "Close FR-9
(well-defined requirement scenario)."

## Test-improvement and documentation-improvement scenarios

Both followed the same discipline: analyze first, get explicit approval, implement second.

- **Test-improvement (FR-7)**: reviewed the (then-)154-test suite and identified 3 genuine
  coverage gaps — an untested generic-exception `500` fallback, custom-alias collision handling
  proven only against a mocked repository, and a repository-level schema test that never
  round-tripped the newest column. Engineer approved one: a real-concurrency test proving the
  custom-alias `409`/no-duplicate-grant guarantee under genuine concurrent writes, modeled
  directly on this project's own click-count concurrency test (whose history is the reason this
  gap mattered — see below). Zero production code changed; the new test was re-run 4 times in
  isolation specifically to rule out flakiness before being considered done.
- **Documentation-improvement (FR-8)**: reviewed the project from an interviewer's perspective
  and identified 3 gaps — no human-facing `README.md`, no consolidated engineering summary
  (this document), and a `.gitignore` rule silently hiding every doc from git. Engineer approved
  one: a short `README.md` with a copy-pasteable quickstart, written to link out to the deeper
  docs rather than duplicate them.

## AI-assisted engineering approach

- **Implementation**: every endpoint, migration, and DTO across all scenarios above.
- **Debugging**: the `NoResourceFoundException`/500-vs-404 bug (greenfield); a `Transaction
  RequiredException` discovered while building the click-count concurrency test, revealing that
  Spring Data's `@Modifying` queries don't get an implicit transaction; a self-inflicted
  `CHECK`-constraint test-methodology error during manual expiration validation, correctly
  rejected by the constraint doing its job, not a product bug.
- **Refactoring**: introducing `UrlService.findActiveMapping` as the single centralized
  expiration/not-found check, refactoring three existing read methods
  (`resolveAndRecordRedirect`, `getUrlDetails`, `getUrlAnalytics`) off direct
  `repository.findByShortCode` calls onto it, so a future read path can't reimplement (or
  forget) the expiration check.
- **Testing**: 155 tests across unit/slice/full-stack/real-concurrency layers, including the
  FR-7 scenario above.
- **Documentation**: `CLAUDE.md`, `docs/REQUIREMENTS.md`, and `docs/AI_WORKLOG.md` updated in
  the same change as every architecture-affecting decision, plus the FR-8 scenario above and
  this document.

## Engineer approval and traceability

Every meaningful AI-assisted task in this project is logged in `docs/AI_WORKLOG.md` with the
task/intent, what was recommended, the engineer's decision, rationale, and validation performed
— **including rejected suggestions**, not just accepted ones (the backend-stack recommendation
and the short-code-strategy recommendation were both explicitly rejected and overridden; both
are recorded as such, not omitted). Every scenario above followed the same shape: AI proposes
(often with an explicit options-and-rationale comparison) → engineer decides → AI implements →
AI validates → results reported honestly. Nothing in this project was auto-approved or
implemented without an explicit engineer decision in `docs/AI_WORKLOG.md`.

## Validation and quality gates

- **Automated**: `./mvnw clean test` — 155 tests (unit, `@WebMvcTest`/`@DataJpaTest` slices,
  full-stack `@SpringBootTest`+`MockMvc`, and two real-multithreaded-concurrency tests) — green
  as of this document. `./mvnw clean package` confirmed buildable.
- **Manual, against real PostgreSQL** (not just H2): re-run after every schema-affecting
  change — migration applies cleanly against the *already-existing* database, pre-existing rows
  confirmed unaffected, each new/changed endpoint exercised via `curl`, Actuator health, OpenAPI
  JSON, and Swagger UI all confirmed reachable.
- **No lint/static-analysis tool is configured** — an explicitly named open item, not silently
  skipped (see Limitations below).

## Risks and trade-offs

- **302, not 301, for redirects** — deliberately, because a `301` would be cached indefinitely
  by browsers/proxies, silently undercounting clicks on repeat visits. Trades a small amount of
  redirect performance for correct analytics.
- **Bounded (5-attempt) collision retry, not unbounded** — fails loudly with a `500` rather than
  looping forever if something is systemically wrong; at 62⁷ (~3.5 trillion) possible codes a
  real collision is already vanishingly unlikely, so this bound exists purely as a safety net.
- **Custom aliases never retry on collision; generated codes always do** — a deliberate
  asymmetry: the caller chose the alias, so silently substituting a different one on conflict
  would be wrong; the caller didn't choose a generated code, so retrying is safe.
- **Expired codes return `404`, not `410`** — trades a slightly less semantically precise status
  code for closing an enumeration side-channel (a caller can't distinguish "never existed" from
  "existed but expired").
- **`.gitignore`'s blanket `*.md` rule initially hid every documentation file from git** — a
  realized risk during this project's history, not just a theoretical one: it silently kept
  every documentation file out of git for most of the project's history before being caught.
  Now resolved by explicit engineer decision: `README.md`, `docs/REQUIREMENTS.md`,
  `docs/AI_WORKLOG.md`, and this file are all tracked and committed. `CLAUDE.md` remains
  excluded — a deliberate scope decision made *after* this trade-off was explicitly surfaced
  and considered, not an oversight. See "Documentation scope" below.

## Assumptions and limitations

**Documentation scope**: this repository intentionally does not include `CLAUDE.md`. That file
is maintained locally as Claude Code's own operating instructions plus the most granular
architecture/convention detail (coding conventions, field-by-field API/schema spec,
package-by-package rationale); by explicit engineer decision — made after the trade-off above
was surfaced — it stays a local working document rather than a submitted artifact. Everything
an interviewer needs to evaluate this submission (architecture overview, decisions and
rationale, scenario evidence, testing approach, risks, limitations) is in this document,
`README.md`, `docs/REQUIREMENTS.md`, and `docs/AI_WORKLOG.md` — all of which **are** committed
and visible in a fresh clone.

**Explicitly out of scope, not silently missing**:
- No authentication/authorization — any caller can create or read any short URL.
- No SSRF/private-network protection — `http`/`https` scheme validation is the entire extent of
  URL validation; `http://localhost/...` or link-local/metadata-endpoint targets are accepted.
- No rate limiting, no caching layer, no idempotency keys.
- No scheduled/background jobs of any kind (expiration enforcement is lazy/request-time only,
  by deliberate design, not as a shortcut).
- No update/delete/deactivate API, no listing API, no richer analytics (referrer, device,
  geography, unique-vs-total clicks) — `clickCount` only, by explicit scope decision.
- No numeric performance/reliability SLAs were assumed or targeted.
- No lint/static-analysis tool is wired in yet.

**Genuinely unresolved, not a scope decision**:
- Whether a Testcontainers-based PostgreSQL integration test is needed alongside the current
  H2-based automated suite remains an open, deferred decision.
- Reliability/security mechanisms beyond what's implemented (rate limiting, caching,
  idempotency, authN/authZ, SSRF protection) remain unselected — never required to be resolved,
  only transparently tracked as candidates pending a future options-and-rationale review.

## Final implementation status

All 4 approved API endpoints implemented and validated end-to-end against real PostgreSQL.
**All required scenarios complete**: greenfield, brownfield (FR-6), well-defined requirement
(FR-9), ambiguous-requirement (FR-10), test-improvement (FR-7), documentation-improvement
(FR-8) — every functional requirement, FR-1 through FR-10, is now implemented and validated.
155 automated tests passing, `./mvnw clean package` green. Documentation visibility resolved:
`README.md`, `docs/REQUIREMENTS.md`, `docs/AI_WORKLOG.md`, and this file are committed and
visible in a fresh clone; `CLAUDE.md` is deliberately kept local-only (see "Documentation
scope" above). Remaining, genuinely open items are all non-blocking, explicitly-deferred
candidates rather than incomplete requirements: the Testcontainers question and unselected
reliability/security mechanisms named above.
