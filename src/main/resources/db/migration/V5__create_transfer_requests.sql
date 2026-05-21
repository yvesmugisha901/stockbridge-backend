-- ============================================================
-- V5 — Transfer Requests
-- FR-14: source branch, destination branch, item, quantity, justification
-- FR-15: total_value and requires_ho_approval for tier routing
-- FR-21: per-level approval columns (user + timestamp + comments)
-- Status lifecycle:
--   PENDING → MANAGER_APPROVED → HO_APPROVED
--           → IN_TRANSIT → COMPLETED
--           → REJECTED | CANCELLED  (terminal)
-- ============================================================
CREATE TABLE transfer_requests (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,

    -- Core request fields
    source_branch_id      BIGINT          NOT NULL,
    destination_branch_id BIGINT          NOT NULL,
    item_id               BIGINT          NOT NULL,
    quantity              INT             NOT NULL,
    total_value           DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    justification         TEXT,
    requires_ho_approval  BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Workflow status
    status                ENUM(
                            'PENDING',
                            'MANAGER_APPROVED',
                            'HO_APPROVED',
                            'IN_TRANSIT',
                            'COMPLETED',
                            'REJECTED',
                            'CANCELLED'
                          )               NOT NULL DEFAULT 'PENDING',

    -- Requester
    requested_by_id       BIGINT          NOT NULL,
    requested_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Manager approval (Level 1)
    manager_approved_by_id BIGINT,
    manager_approved_at    DATETIME,
    manager_comments       TEXT,

    -- HO approval (Level 2)
    ho_approved_by_id     BIGINT,
    ho_approved_at        DATETIME,
    ho_comments           TEXT,

    -- Logistics timestamps
    dispatched_at         DATETIME,
    received_at           DATETIME,

    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_tr_status              (status),
    INDEX idx_tr_source_branch       (source_branch_id),
    INDEX idx_tr_destination_branch  (destination_branch_id),
    INDEX idx_tr_item                (item_id),
    INDEX idx_tr_requested_by        (requested_by_id),
    INDEX idx_tr_requested_at        (requested_at),

    CONSTRAINT fk_tr_source_branch
        FOREIGN KEY (source_branch_id)       REFERENCES branches (id) ON UPDATE CASCADE,
    CONSTRAINT fk_tr_destination_branch
        FOREIGN KEY (destination_branch_id)  REFERENCES branches (id) ON UPDATE CASCADE,
    CONSTRAINT fk_tr_item
        FOREIGN KEY (item_id)                REFERENCES items    (id) ON UPDATE CASCADE,
    CONSTRAINT fk_tr_requested_by
        FOREIGN KEY (requested_by_id)        REFERENCES users    (id) ON UPDATE CASCADE,
    CONSTRAINT fk_tr_manager_approved_by
        FOREIGN KEY (manager_approved_by_id) REFERENCES users    (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_tr_ho_approved_by
        FOREIGN KEY (ho_approved_by_id)      REFERENCES users    (id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;