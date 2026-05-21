-- ============================================================
-- V7 — Transfer Costs (Accountant / Finance module)
-- FR-23: attach cost record to a completed transfer
-- FR-24: summary queryable by date range and branch
-- ============================================================
CREATE TABLE transfer_costs (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    transfer_request_id BIGINT          NOT NULL,
    amount              DECIMAL(15, 2)  NOT NULL,
    currency            VARCHAR(10)     NOT NULL DEFAULT 'RWF',
    cost_type           VARCHAR(100)    NOT NULL,   -- e.g. TRANSPORT, HANDLING, INSURANCE
    notes               TEXT,
    recorded_by_id      BIGINT          NOT NULL,
    recorded_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_tc_transfer  (transfer_request_id),
    INDEX idx_tc_recorded_at (recorded_at),

    CONSTRAINT fk_tc_transfer
        FOREIGN KEY (transfer_request_id) REFERENCES transfer_requests (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_tc_recorded_by
        FOREIGN KEY (recorded_by_id) REFERENCES users (id)
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;