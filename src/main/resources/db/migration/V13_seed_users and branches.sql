-- ============================================================
-- SEED  Part 1 — Fix  (insert only what is missing)
-- ============================================================
-- Current DB state:
--   Branches : id 1 = Head Office, id 2 = Branch North  (Branch South MISSING)
--   Users    : id 1-5 already exist (admin, ho.admin, manager, staff, accountant)
--
-- This script adds:
--   Branch South  →  will get id 3
--   4 new named users + 1 inactive test user
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. MISSING BRANCH
-- ────────────────────────────────────────────────────────────

INSERT INTO branches (name, code, location, contact_info, active)
VALUES
  ('Branch South', 'BR-002', 'Huye, Southern Province', '+250 788 000 003', TRUE);

-- ────────────────────────────────────────────────────────────
-- 2. MISSING USERS
-- ────────────────────────────────────────────────────────────
-- All passwords: 1234567890
-- branch_id 1 = Head Office | 2 = Branch North | 3 = Branch South

INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES

  -- Head Office staff (dispatches transfers → FR-18)
  ('Carol Ingabire (HO)',
   'staff.ho@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 1, TRUE),

  -- Branch North — named manager + named staff
  -- (manager@company.com id=3 stays; this is the named equivalent)
  ('North Branch Manager',
   'manager.north@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 2, TRUE),

  ('Alice Uwimana (North)',
   'staff.north@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 2, TRUE),

  -- Branch South — manager + staff (branch 3 just inserted above)
  ('South Branch Manager',
   'manager.south@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'MANAGER', 3, TRUE),

  ('Bob Hakizimana (South)',
   'staff.south@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 3, TRUE),

  -- Inactive user — login must be blocked (FR-05)
  ('Deactivated Staff',
   'inactive@company.com',
   '$2b$12$bAsqv1xPU2yppdcQ9LAnbOU29gyEueGs82ZZFod8DnQgDwsvTdcQS',
   'STAFF', 2, FALSE);

-- ============================================================
-- VERIFY  (run immediately after)
-- ============================================================
-- SELECT id, name, code FROM branches ORDER BY id;
-- SELECT id, email, role, branch_id, active FROM users ORDER BY id;
-- ============================================================

-- Expected branches:
--   1  Head Office   HQ-001
--   2  Branch North  BR-001
--   3  Branch South  BR-002

-- Expected users (11 rows):
--   1   admin@company.com           ADMIN        NULL   active
--   2   ho.admin@company.com        HO_ADMIN     1      active
--   3   manager@company.com         MANAGER      2      active
--   4   staff@company.com           STAFF        2      active
--   5   accountant@company.com      ACCOUNTANT   1      active
--   6   staff.ho@company.com        STAFF        1      active
--   7   manager.north@company.com   MANAGER      2      active
--   8   staff.north@company.com     STAFF        2      active
--   9   manager.south@company.com   MANAGER      3      active
--   10  staff.south@company.com     STAFF        3      active
--   11  inactive@company.com        STAFF        2      inactive