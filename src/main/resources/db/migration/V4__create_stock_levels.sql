-- ============================================================
-- V4 — Stock Levels
-- FR-10: quantity_on_hand, reserved_quantity, minimum_threshold
--        one record per (branch, item) pair
-- FR-12: low stock = quantity_on_hand <= minimum_threshold
-- FR-13: branch-scoped visibility enforced in service layer
-- ============================================================
CREATE TABLE stock_levels (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    branch_id           BIGINT      NOT NULL,
    item_id             BIGINT      NOT NULL,
    quantity_on_hand    INT         NOT NULL DEFAULT 0,
    reserved_quantity   INT         NOT NULL DEFAULT 0,
    minimum_threshold   INT         NOT NULL DEFAULT 0,
    last_updated        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_stock_branch_item (branch_id, item_id),
    INDEX idx_stock_branch (branch_id),
    INDEX idx_stock_item   (item_id),

    CONSTRAINT fk_stock_branch
        FOREIGN KEY (branch_id) REFERENCES branches (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_stock_item
        FOREIGN KEY (item_id)   REFERENCES items    (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;