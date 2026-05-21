-- ============================================================
-- V1 — Branches
-- FR-06: name, location, code, contact information
-- FR-07: active flag for deactivation
-- ============================================================
CREATE TABLE branches (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)    NOT NULL,
    code          VARCHAR(20)     NOT NULL,
    location      VARCHAR(255),
    contact_info  VARCHAR(255),
    active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_branches_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;