-- ============================================================
-- V13 — Seed Branches & Users  (safe to re-run — INSERT IGNORE)
-- Branches : Head Office, North, South, East, West
-- Users    : full role spread across all branches
-- Password : 1234567890  (bcrypt cost 12, $2b$ prefix)
-- ============================================================

-- ------------------------------------------------------------
-- 1. BRANCHES
-- ------------------------------------------------------------
INSERT IGNORE INTO branches (name, code, location, contact_info, active) VALUES
  ('Branch South', 'BR-002', 'Huye, Southern Province',      '+250 788 000 003', TRUE),
  ('Branch East',  'BR-003', 'Rwamagana, Eastern Province',  '+250 788 000 004', TRUE),
  ('Branch West',  'BR-004', 'Rubavu, Western Province',     '+250 788 000 005', TRUE);

-- ------------------------------------------------------------
-- 2. USERS
-- ------------------------------------------------------------
-- Head Office
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('Carol Ingabire (HO)',
   'staff.ho@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 1, TRUE);

-- Branch North
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('North Branch Manager',
   'manager.north@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 2, TRUE),

  ('Alice Uwimana (North)',
   'staff.north@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 2, TRUE);

-- Branch South  (branch_id 3)
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('South Branch Manager',
   'manager.south@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 3, TRUE),

  ('Bob Hakizimana (South)',
   'staff.south@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 3, TRUE);

-- Branch East  (branch_id 4)
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('East Branch Manager',
   'manager.east@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 4, TRUE),

  ('David Niyonzima (East)',
   'staff.east@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 4, TRUE);

-- Branch West  (branch_id 5)
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('West Branch Manager',
   'manager.west@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 5, TRUE),

  ('Eve Mutoni (West)',
   'staff.west@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 5, TRUE);

-- Inactive user — login must be blocked (FR-05)
INSERT IGNORE INTO users (full_name, email, password_hash, role, branch_id, active) VALUES
  ('Deactivated Staff',
   'inactive@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 2, FALSE);

-- ============================================================
-- VERIFY
-- SELECT id, name, code FROM branches ORDER BY id;
-- SELECT id, email, role, branch_id, active FROM users ORDER BY id;
-- Expected: 5 branches, 15 users (ids 1-15), id=11 inactive
-- ============================================================
