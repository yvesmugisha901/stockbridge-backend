-- ============================================================
-- V3 — Items (master catalogue)
-- FR-09: name, code, category, unit of measure
-- FR-15: unit_price used to calculate transfer total value
-- ============================================================
CREATE TABLE items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    code            VARCHAR(30)     NOT NULL,
    description     TEXT,
    category        VARCHAR(100)    NOT NULL,
    unit_of_measure VARCHAR(50)     NOT NULL,
    unit_price      DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_items_code (code),
    INDEX idx_items_category (category),
    INDEX idx_items_active   (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;