-- ============================================================
-- V14 — Seed Items Catalogue  (safe to re-run — INSERT IGNORE)
-- 17 active items + 1 inactive across 5 categories
-- Columns: name, code, description, category,
--          unit_of_measure, unit_price, active
-- unit_price in RWF
-- ============================================================

INSERT IGNORE INTO items
  (name, code, description, category, unit_of_measure, unit_price, active)
VALUES

  -- ── OFFICE SUPPLIES ────────────────────────────────────────
  ('A4 Paper (Ream)',
   'OFF-001', '500 sheets per ream, 80gsm white printing paper',
   'OFFICE_SUPPLIES', 'REAM', 3500.00, TRUE),

  ('Ballpoint Pen (Box)',
   'OFF-002', 'Box of 12 blue ballpoint pens',
   'OFFICE_SUPPLIES', 'BOX', 1800.00, TRUE),

  ('Stapler',
   'OFF-003', 'Standard desktop stapler, accepts 24/6 staples',
   'OFFICE_SUPPLIES', 'PIECE', 4500.00, TRUE),

  ('Staple Pins (Box)',
   'OFF-004', 'Box of 1000 standard 24/6 staple pins',
   'OFFICE_SUPPLIES', 'BOX', 800.00, TRUE),

  ('Sticky Notes Pack',
   'OFF-005', '100 sheets per pad, 76x76mm yellow sticky notes',
   'OFFICE_SUPPLIES', 'PACK', 1200.00, TRUE),

  ('Manila Folder',
   'OFF-006', 'A4 size manila folder for document filing',
   'OFFICE_SUPPLIES', 'PIECE', 300.00, TRUE),

  -- ── IT EQUIPMENT ───────────────────────────────────────────
  ('USB Drive 32GB',
   'IT-001', 'USB 3.0 flash drive, 32GB storage capacity',
   'IT_EQUIPMENT', 'PIECE', 8500.00, TRUE),

  ('Ethernet Cable 5m',
   'IT-002', 'Cat6 UTP patch cable, 5 metres',
   'IT_EQUIPMENT', 'PIECE', 3200.00, TRUE),

  ('Wireless Mouse',
   'IT-003', '2.4GHz wireless optical mouse with USB receiver',
   'IT_EQUIPMENT', 'PIECE', 12000.00, TRUE),

  ('HDMI Cable 2m',
   'IT-004', 'High-speed HDMI 1.4 cable, 2 metres',
   'IT_EQUIPMENT', 'PIECE', 5500.00, TRUE),

  ('Laptop Stand',
   'IT-005', 'Adjustable aluminium laptop stand, fits 11-17 inch laptops',
   'IT_EQUIPMENT', 'PIECE', 35000.00, TRUE),

  -- ── CLEANING SUPPLIES ──────────────────────────────────────
  ('Hand Sanitizer 500ml',
   'CLN-001', '70% alcohol-based hand sanitizer, pump dispenser',
   'CLEANING_SUPPLIES', 'BOTTLE', 2800.00, TRUE),

  ('Disinfectant Spray 750ml',
   'CLN-002', 'Multi-surface disinfectant spray',
   'CLEANING_SUPPLIES', 'BOTTLE', 3500.00, TRUE),

  ('Microfiber Cloth Pack',
   'CLN-003', 'Pack of 5 microfiber cleaning cloths',
   'CLEANING_SUPPLIES', 'PACK', 2000.00, TRUE),

  -- ── FURNITURE & FIXTURES ───────────────────────────────────
  ('Whiteboard Marker Set',
   'FUR-001', 'Set of 4 assorted colour dry-erase markers',
   'FURNITURE_FIXTURES', 'SET', 2500.00, TRUE),

  ('Extension Cord 5m',
   'FUR-002', '4-socket extension cord with surge protection, 5 metres',
   'FURNITURE_FIXTURES', 'PIECE', 9500.00, TRUE),

  -- ── STATIONERY ─────────────────────────────────────────────
  ('Correction Fluid',
   'STA-001', 'White correction fluid, fast drying',
   'STATIONERY', 'PIECE', 700.00, TRUE),

  -- ── OBSOLETE — tests FR-09 item deactivation ───────────────
  ('Legacy Fax Paper Roll',
   'OFF-OBS', 'Thermal fax paper roll — obsolete, kept for records',
   'OFFICE_SUPPLIES', 'ROLL', 1500.00, FALSE);

-- ============================================================
-- VERIFY
-- SELECT id, code, name, category, unit_price, active
-- FROM items ORDER BY id;
-- Expected: 18 rows, last row (OFF-OBS) active = 0
-- ============================================================
