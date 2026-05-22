-- ─── Drop duplicate columns added by V10 migration ───────────────────────────
-- These were added without _id suffix and clash with the real FK columns
ALTER TABLE transfer_requests
    DROP COLUMN IF EXISTS manager_approved_by,
    DROP COLUMN IF EXISTS manager_approved_at,
    DROP COLUMN IF EXISTS manager_comments,
    DROP COLUMN IF EXISTS ho_approved_by,
    DROP COLUMN IF EXISTS ho_approved_at,
    DROP COLUMN IF EXISTS ho_comments;

-- ─── Add the correct non-FK approval columns ─────────────────────────────────
-- manager_approved_by_id and ho_approved_by_id already exist in the schema
-- We only need the timestamp and comment columns
ALTER TABLE transfer_requests
    ADD COLUMN IF NOT EXISTS manager_approved_at  DATETIME     NULL,
    ADD COLUMN IF NOT EXISTS manager_comments     VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS ho_approved_at       DATETIME     NULL,
    ADD COLUMN IF NOT EXISTS ho_comments          VARCHAR(500) NULL;