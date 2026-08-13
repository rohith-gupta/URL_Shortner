-- V3: ambiguous-requirement scenario — optional expiration for short URLs (see
-- docs/AI_WORKLOG.md, "Shortened URLs should expire").
--
-- Nullable: NULL means "never expires" — the default. Existing rows get NULL automatically
-- (no backfill needed) and remain valid indefinitely, per the engineer's explicit decision not
-- to retroactively expire anything. Never edit V1/V2 to make this change instead.
--
-- The CHECK below is defense-in-depth only (mirrors the pattern already used in V1/V2): it
-- guards against expires_at ever being set at-or-before created_at, which would be nonsensical
-- regardless of how it got there. It deliberately does NOT try to enforce "expires_at must be
-- in the future" here — that's a point-in-time rule relative to wall-clock "now" at insert
-- time, not a static invariant a row-level CHECK can safely express (a CHECK referencing
-- now() would make previously-valid rows start failing future unrelated UPDATEs once time
-- passes them). That rule is enforced at the application layer (@Future on the request DTO).

ALTER TABLE url_mapping
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE NULL;

ALTER TABLE url_mapping
    ADD CONSTRAINT chk_url_mapping_expires_at_after_created CHECK (expires_at IS NULL OR expires_at > created_at);
