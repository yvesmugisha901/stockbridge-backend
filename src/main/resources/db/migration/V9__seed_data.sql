-- ============================================================
-- V9 — Seed Role Users
-- Creates one user per role for development and testing.
--
-- ALL users share the same password:  1234567890
-- bcrypt hash, cost factor 12.
--
-- branch_id 1 = Head Office  (HQ-001)
-- branch_id 2 = Branch North (BR-001)
-- ============================================================

-- Head Office / Inventory Admin
INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES (
    'HO Inventory Admin',
    'ho.admin@company.com',
    '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
    'HO_ADMIN',
    1,
    TRUE
);

-- Branch Manager
INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES (
    'North Branch Manager',
    'manager@company.com',
    '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
    'MANAGER',
    2,
    TRUE
);

-- Branch Staff
INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES (
    'Branch Staff Member',
    'staff@company.com',
    '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
    'STAFF',
    2,
    TRUE
);

-- Accountant
INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES (
    'Finance Accountant',
    'accountant@company.com',
    '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
    'ACCOUNTANT',
    1,
    TRUE
);