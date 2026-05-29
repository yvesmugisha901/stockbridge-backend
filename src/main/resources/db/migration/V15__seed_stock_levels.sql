-- ============================================================
-- V15 — Seed Stock Levels  (safe to re-run — INSERT IGNORE)
-- 5 branches × 18 items = 90 rows
-- Designed to exercise:
--   • Low stock alerts  (quantity_on_hand < minimum_threshold)
--   • Quantity approval threshold (>50 units)
--   • Value approval threshold    (>500,000 RWF)
--   • Zero stock at branch        (forces transfer request)
--   • Inactive item stock at HO   (physical stock still exists)
-- ============================================================
-- branch_id: 1=Head Office  2=Branch North  3=Branch South
--            4=Branch East  5=Branch West
-- item_id  : 1=A4 Paper  2=Pens  3=Stapler  4=Staple Pins
--            5=Sticky Notes  6=Manila Folder  7=USB Drive
--            8=Ethernet Cable  9=Wireless Mouse  10=HDMI Cable
--            11=Laptop Stand  12=Hand Sanitizer  13=Disinfectant
--            14=Microfiber Cloth  15=Whiteboard Marker
--            16=Extension Cord  17=Correction Fluid
--            18=Legacy Fax Paper (inactive)
-- ============================================================

INSERT IGNORE INTO stock_levels
  (branch_id, item_id, quantity_on_hand, reserved_quantity, minimum_threshold)
VALUES

-- ============================================================
-- BRANCH 1 — HEAD OFFICE (warehouse / dispatch hub)
-- ============================================================
  (1,  1, 500, 0, 100),   -- A4 Paper         : well stocked
  (1,  2, 300, 0,  50),   -- Pens
  (1,  3,  80, 0,  20),   -- Stapler
  (1,  4, 400, 0,  80),   -- Staple Pins
  (1,  5, 200, 0,  40),   -- Sticky Notes
  (1,  6, 600, 0, 100),   -- Manila Folder
  (1,  7, 150, 0,  30),   -- USB Drive
  (1,  8, 120, 0,  20),   -- Ethernet Cable
  (1,  9,  60, 0,  10),   -- Wireless Mouse
  (1, 10,  90, 0,  15),   -- HDMI Cable
  (1, 11,  40, 0,   5),   -- Laptop Stand     : high value item
  (1, 12, 180, 0,  30),   -- Hand Sanitizer
  (1, 13, 160, 0,  30),   -- Disinfectant Spray
  (1, 14, 200, 0,  40),   -- Microfiber Cloth
  (1, 15, 100, 0,  20),   -- Whiteboard Marker
  (1, 16,  70, 0,  10),   -- Extension Cord
  (1, 17, 250, 0,  50),   -- Correction Fluid
  (1, 18,  12, 0,   0),   -- Legacy Fax Paper  : inactive item, physical stock remains

-- ============================================================
-- BRANCH 2 — BRANCH NORTH (established branch, medium stock)
-- ============================================================
  (2,  1,  80, 0,  50),   -- A4 Paper         : above threshold
  (2,  2,  60, 0,  30),   -- Pens
  (2,  3,  10, 0,  10),   -- Stapler          : AT threshold — low stock alert
  (2,  4,  45, 0,  30),   -- Staple Pins
  (2,  5,  30, 0,  20),   -- Sticky Notes
  (2,  6,  90, 0,  40),   -- Manila Folder
  (2,  7,  20, 0,  10),   -- USB Drive
  (2,  8,  15, 0,  10),   -- Ethernet Cable
  (2,  9,   8, 0,   5),   -- Wireless Mouse
  (2, 10,  12, 0,   8),   -- HDMI Cable
  (2, 11,   3, 0,   2),   -- Laptop Stand
  (2, 12,  25, 0,  20),   -- Hand Sanitizer
  (2, 13,  18, 0,  15),   -- Disinfectant Spray
  (2, 14,  30, 0,  20),   -- Microfiber Cloth
  (2, 15,  14, 0,  10),   -- Whiteboard Marker
  (2, 16,   8, 0,   5),   -- Extension Cord
  (2, 17,  40, 0,  20),   -- Correction Fluid
  (2, 18,   0, 0,   0),   -- Legacy Fax Paper  : none at branch

-- ============================================================
-- BRANCH 3 — BRANCH SOUTH (medium stock, a few lows)
-- ============================================================
  (3,  1,  60, 0,  50),   -- A4 Paper
  (3,  2,  20, 0,  30),   -- Pens             : BELOW threshold → low stock alert
  (3,  3,  15, 0,  10),   -- Stapler
  (3,  4,  25, 0,  30),   -- Staple Pins      : BELOW threshold
  (3,  5,  22, 0,  20),   -- Sticky Notes
  (3,  6,  55, 0,  40),   -- Manila Folder
  (3,  7,  12, 0,  10),   -- USB Drive
  (3,  8,   9, 0,  10),   -- Ethernet Cable   : BELOW threshold
  (3,  9,   6, 0,   5),   -- Wireless Mouse
  (3, 10,  10, 0,   8),   -- HDMI Cable
  (3, 11,   2, 0,   2),   -- Laptop Stand     : AT threshold
  (3, 12,  28, 0,  20),   -- Hand Sanitizer
  (3, 13,  20, 0,  15),   -- Disinfectant Spray
  (3, 14,  18, 0,  20),   -- Microfiber Cloth : BELOW threshold
  (3, 15,  12, 0,  10),   -- Whiteboard Marker
  (3, 16,   6, 0,   5),   -- Extension Cord
  (3, 17,  35, 0,  20),   -- Correction Fluid
  (3, 18,   0, 0,   0),   -- Legacy Fax Paper

-- ============================================================
-- BRANCH 4 — BRANCH EAST (newer branch, lower stock)
-- ============================================================
  (4,  1,  40, 0,  50),   -- A4 Paper         : BELOW threshold
  (4,  2,  15, 0,  30),   -- Pens             : BELOW threshold
  (4,  3,   5, 0,  10),   -- Stapler          : BELOW threshold
  (4,  4,  20, 0,  30),   -- Staple Pins      : BELOW threshold
  (4,  5,   8, 0,  20),   -- Sticky Notes     : BELOW threshold
  (4,  6,  30, 0,  40),   -- Manila Folder    : BELOW threshold
  (4,  7,   5, 0,  10),   -- USB Drive        : BELOW threshold
  (4,  8,   6, 0,  10),   -- Ethernet Cable   : BELOW threshold
  (4,  9,   0, 0,   5),   -- Wireless Mouse   : ZERO stock
  (4, 10,   4, 0,   8),   -- HDMI Cable       : BELOW threshold
  (4, 11,   0, 0,   2),   -- Laptop Stand     : ZERO stock
  (4, 12,  10, 0,  20),   -- Hand Sanitizer   : BELOW threshold
  (4, 13,  12, 0,  15),   -- Disinfectant     : BELOW threshold
  (4, 14,  10, 0,  20),   -- Microfiber Cloth : BELOW threshold
  (4, 15,   6, 0,  10),   -- Whiteboard Marker: BELOW threshold
  (4, 16,   3, 0,   5),   -- Extension Cord   : BELOW threshold
  (4, 17,  15, 0,  20),   -- Correction Fluid : BELOW threshold
  (4, 18,   0, 0,   0),   -- Legacy Fax Paper

-- ============================================================
-- BRANCH 5 — BRANCH WEST (newest branch, minimal stock)
-- ============================================================
  (5,  1,  30, 0,  50),   -- A4 Paper         : BELOW threshold
  (5,  2,  10, 0,  30),   -- Pens             : BELOW threshold
  (5,  3,   3, 0,  10),   -- Stapler          : BELOW threshold
  (5,  4,  15, 0,  30),   -- Staple Pins      : BELOW threshold
  (5,  5,   5, 0,  20),   -- Sticky Notes     : BELOW threshold
  (5,  6,  20, 0,  40),   -- Manila Folder    : BELOW threshold
  (5,  7,   3, 0,  10),   -- USB Drive        : BELOW threshold
  (5,  8,   4, 0,  10),   -- Ethernet Cable   : BELOW threshold
  (5,  9,   0, 0,   5),   -- Wireless Mouse   : ZERO stock
  (5, 10,   3, 0,   8),   -- HDMI Cable       : BELOW threshold
  (5, 11,   0, 0,   2),   -- Laptop Stand     : ZERO stock
  (5, 12,   8, 0,  20),   -- Hand Sanitizer   : BELOW threshold
  (5, 13,   9, 0,  15),   -- Disinfectant     : BELOW threshold
  (5, 14,   7, 0,  20),   -- Microfiber Cloth : BELOW threshold
  (5, 15,   4, 0,  10),   -- Whiteboard Marker: BELOW threshold
  (5, 16,   2, 0,   5),   -- Extension Cord   : BELOW threshold
  (5, 17,  10, 0,  20),   -- Correction Fluid : BELOW threshold
  (5, 18,   0, 0,   0);   -- Legacy Fax Paper

-- ============================================================
-- VERIFY
-- SELECT b.name, i.name, sl.quantity_on_hand, sl.minimum_threshold,
--        CASE WHEN sl.quantity_on_hand < sl.minimum_threshold
--             THEN 'LOW' ELSE 'OK' END AS status
-- FROM stock_levels sl
-- JOIN branches b ON b.id = sl.branch_id
-- JOIN items    i ON i.id = sl.item_id
-- ORDER BY b.id, i.id;
-- Expected: 90 rows
-- ============================================================
