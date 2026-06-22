-- Flyway migration: seed all system configuration keys
-- Place at: src/main/resources/db/migration/V4__seed_system_config.sql
-- (adjust version number to follow your existing migrations)

INSERT INTO system_config (config_key, config_value, description) VALUES

-- Approval thresholds (FR-15)
('APPROVAL_QUANTITY_THRESHOLD', '50',
 'Transfers at or above this quantity require HO_ADMIN second-level approval'),

('APPROVAL_VALUE_THRESHOLD', '500000',
 'Transfers at or above this RWF value require HO_ADMIN second-level approval'),

-- Transfer rules (FR-14)
('MAX_TRANSFER_QUANTITY', '500',
 'Hard ceiling on quantity per transfer request — submission blocked above this'),

('TRANSFER_AUTO_COMPLETE_DAYS', '7',
 'IN_TRANSIT transfers older than this many days are automatically marked COMPLETED'),

('REQUIRE_JUSTIFICATION', 'true',
 'Forces the requester to enter a justification before submitting a transfer request'),

('STOCK_RESERVATION_ENABLED', 'true',
 'Reduces available stock at source branch when a transfer is submitted, not just approved'),

-- Finance & alerts (FR-12, FR-23)
('DEFAULT_CURRENCY', 'RWF',
 'Currency code used across cost recording and finance reports'),

('LOW_STOCK_ALERT_ENABLED', 'true',
 'Show dashboard alerts when branch stock falls below minimum threshold'),

-- Security & audit (NFR-02, NFR-10)
('SESSION_TIMEOUT_MINUTES', '1440',
 'Informational: JWT effectively unused after this many minutes of inactivity'),

('AUDIT_RETENTION_DAYS', '365',
 'Audit log entries older than this many days are purged by a nightly scheduled job')

ON DUPLICATE KEY UPDATE config_key = config_key;  -- idempotent re-run