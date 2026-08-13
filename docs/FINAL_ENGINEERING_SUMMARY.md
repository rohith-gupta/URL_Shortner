# Final Engineering Summary — URL Shortener

Concise, interviewer-facing summary of what was built and how. Full detail, options
considered, and every AI-assisted decision with its rationale live in
[`CLAUDE.md`](../CLAUDE.md) (architecture/conventions) and
[`docs/AI_WORKLOG.md`](AI_WORKLOG.md) (chronological traceability log) — this document
synthesizes them rather than duplicating them.

## Project objective

Not just a working URL shortener, but a demonstration of an AI-assisted software engineering
*process*: requirement understanding, task decomposition, disciplined AI-assisted execution,
quality gates, and full traceability — across a greenfield build, a brownfield change, a
deliberately ambiguous requirement, a test-improvement pass, and a documentation-improvement
pass. Development approach throughout: engineer-led execution accelerated by AI, not autonomous
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
- **`.gitignore`'s blanket `*.md` rule** — the single largest realized risk in this project's
  process, not just a documented candidate one: it silently kept every documentation file
  (including this one) out of git for most of the project's history. Partially fixed (`README.md`
  is now tracked); `CLAUDE.md` and everything under `docs/` — including this file — remain
  git-ignored as of this writing. See Limitations below; this is the one item that could
  materially affect whether a reviewer cloning the repo sees this document at all.

## Assumptions and limitations

**Explicitly out of scope, not silently missing** (see `CLAUDE.md`'s Security/Reliability
sections for the full narrative):
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
- The `.gitignore` issue above — needs an explicit engineer decision on how to handle it.
- FR-9 (a dedicated, explicitly-labeled "well-defined requirement" scenario) has no standalone
  artifact of its own; every other scenario in this project (all 4 greenfield endpoints, the
  brownfield alias work) *was* engineer-specified in full, unambiguous detail up front and
  executed as such, but none was formally called out as *the* FR-9 demonstration.
- Whether a Testcontainers-based PostgreSQL integration test is needed alongside the current
  H2-based automated suite remains an open, deferred decision.

## Final implementation status

All 4 approved API endpoints implemented and validated end-to-end against real PostgreSQL.
Required scenarios complete: greenfield, brownfield (FR-6), ambiguous-requirement (FR-10),
test-improvement (FR-7), documentation-improvement (FR-8). 155 automated tests passing,
`./mvnw clean package` green. Remaining, genuinely open items: FR-9's standalone labeling, the
`.gitignore` documentation-visibility issue, and the reliability/security mechanisms named above
as explicitly deferred pending a future options-and-rationale review.
