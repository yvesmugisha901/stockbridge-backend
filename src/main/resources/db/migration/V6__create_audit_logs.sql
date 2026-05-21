-- ============================================================
-- V6 — Audit Log
-- FR-21 / NFR-10: every status change logged with actor + timestamp
-- Immutable — no updates or deletes permitted on this table
-- ============================================================
CREATE TABLE audit_logs (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    action        VARCHAR(100)    NOT NULL,   -- e.g. TRANSFER_CREATED, MANAGER_APPROVED
    entity_type   VARCHAR(100)    NOT NULL,   -- e.g. TransferRequest, StockLevel
    entity_id     BIGINT          NOT NULL,
    performed_by  VARCHAR(150)    NOT NULL,   -- email of the acting user
    details       TEXT,
    performed_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_audit_entity     (entity_type, entity_id),
    INDEX idx_audit_performed_by (performed_by),
    INDEX idx_audit_performed_at (performed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;