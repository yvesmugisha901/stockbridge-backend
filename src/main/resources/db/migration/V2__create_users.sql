-- ============================================================
-- V2 — Users
-- FR-01: name, email, password, role, branch assignment
-- FR-04: password stored as bcrypt hash
-- FR-05: active flag for deactivation without deletion
-- Roles: STAFF | MANAGER | HO_ADMIN | ACCOUNTANT | ADMIN
-- ============================================================
CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(150)    NOT NULL,
    email         VARCHAR(150)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    role          ENUM(
                    'STAFF',
                    'MANAGER',
                    'HO_ADMIN',
                    'ACCOUNTANT',
                    'ADMIN'
                  )               NOT NULL,
    branch_id     BIGINT,
    active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    CONSTRAINT fk_users_branch
        FOREIGN KEY (branch_id) REFERENCES branches (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;