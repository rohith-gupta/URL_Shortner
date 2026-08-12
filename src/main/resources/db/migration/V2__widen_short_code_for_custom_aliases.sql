-- V2: brownfield enhancement — support optional custom aliases on POST /api/urls
-- (see docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases to POST /api/urls").
--
-- V1 hard-coded short_code to exactly 7 characters, which is correct for generated codes but
-- too narrow for custom aliases (4-30 chars). The column is reused as-is (no new alias column
-- — one lookup key for both kinds of short code is what lets redirect/details/analytics work
-- unchanged for either), just widened, with the exact-length CHECK replaced by a range CHECK,
-- plus a new charset CHECK as defense-in-depth behind the application-level validation.
--
-- Never edit V1 to make this change instead — it has already been applied and Flyway
-- checksums it; schema changes after the fact are new, additive migrations.

ALTER TABLE url_mapping
    ALTER COLUMN short_code TYPE VARCHAR(30);

ALTER TABLE url_mapping
    DROP CONSTRAINT chk_url_mapping_short_code_length;

ALTER TABLE url_mapping
    ADD CONSTRAINT chk_url_mapping_short_code_length CHECK (char_length(short_code) BETWEEN 4 AND 30);

ALTER TABLE url_mapping
    ADD CONSTRAINT chk_url_mapping_short_code_charset CHECK (short_code ~ '^[0-9a-zA-Z_-]+$');
