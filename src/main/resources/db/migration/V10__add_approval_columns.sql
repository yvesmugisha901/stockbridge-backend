ALTER TABLE transfer_requests
  ADD COLUMN IF NOT EXISTS manager_approved_by  BIGINT       NULL,
  ADD COLUMN IF NOT EXISTS manager_approved_at  DATETIME     NULL,
  ADD COLUMN IF NOT EXISTS manager_comments     VARCHAR(500) NULL,
  ADD COLUMN IF NOT EXISTS ho_approved_by       BIGINT       NULL,
  ADD COLUMN IF NOT EXISTS ho_approved_at       DATETIME     NULL,
  ADD COLUMN IF NOT EXISTS ho_comments          VARCHAR(500) NULL;