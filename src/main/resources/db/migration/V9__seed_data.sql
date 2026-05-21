-- ============================================================
-- V9 — Seed Role Users
-- ALL users password: 1234567890 (bcrypt cost 12)
-- branch_id 1 = Head Office | branch_id 2 = Branch North
-- INSERT IGNORE skips if email already exists (safe to re-run)
-- ============================================================

INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
('HO Inventory Admin',  'ho.admin@company.com',  '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS', 'HO_ADMIN',   1, TRUE),
('North Branch Manager','manager@company.com',    '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS', 'MANAGER',    2, TRUE),
('Branch Staff Member', 'staff@company.com',      '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS', 'STAFF',      2, TRUE),
('Finance Accountant',  'accountant@company.com', '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS', 'ACCOUNTANT', 1, TRUE);