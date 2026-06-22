-- Flyway migration: seed all system configuration keys
-- Adjust version prefix to follow your existing migrations.

INSERT INTO system_config (config_key, config_value, description) VALUES

-- Approval thresholds
('APPROVAL_QUANTITY_THRESHOLD',   '50',    'Transfers at or above this quantity require Head Office second-level approval'),
('APPROVAL_VALUE_THRESHOLD',      '500000','Transfers at or above this RWF value require Head Office second-level approval'),
('SINGLE_APPROVER_LIMIT',         '100000','Below this value, Branch Manager approval alone is sufficient'),
('APPROVAL_EXPIRY_HOURS',         '48',    'Pending approval auto-escalates after this many hours'),

-- Transfer rules
('MAX_TRANSFER_QUANTITY',         '500',   'Hard ceiling on units per transfer request'),
('MIN_TRANSFER_QUANTITY',         '1',     'Requests below this unit count are rejected at submission'),
('TRANSFER_AUTO_COMPLETE_DAYS',   '7',     'IN_TRANSIT transfers older than this are auto-completed'),
('REQUIRE_JUSTIFICATION',         'true',  'Staff must supply written reason before submitting a transfer'),
('STOCK_RESERVATION_ENABLED',     'true',  'Locks source stock on submission, before approval'),
('ALLOW_SAME_BRANCH_TRANSFER',    'false', 'Permits source and destination to be the same branch'),

-- Inventory & stock alerts
('LOW_STOCK_ALERT_ENABLED',            'true', 'Show alerts when stock falls below minimum threshold'),
('LOW_STOCK_THRESHOLD_PERCENT',        '20',   'Stock flagged low when on-hand drops to this % of minimum'),
('CRITICAL_STOCK_THRESHOLD_PERCENT',   '5',    'Stock flagged critical when on-hand drops to this % of minimum'),
('MANUAL_ADJUSTMENT_REQUIRES_REASON',  'true', 'Admins must provide reason for manual stock changes'),
('NEGATIVE_STOCK_ALLOWED',             'false','Allow stock levels to go below zero'),

-- Finance & reporting
('DEFAULT_CURRENCY',               'RWF',  'Currency code used in cost recording and reports'),
('COST_ENTRY_REQUIRED',            'true', 'Accountants must record a cost before marking a transfer Completed'),
('FINANCE_REPORT_DATE_RANGE_DAYS', '30',   'Default date range pre-selected on finance summary report'),

-- Security & audit
('SESSION_TIMEOUT_MINUTES',  '1440', 'Frontend logs users out after this many inactive minutes'),
('AUDIT_RETENTION_DAYS',     '365',  'Audit entries older than this are purged by nightly job'),
('MAX_LOGIN_ATTEMPTS',       '5',    'Account locked after this many consecutive failed logins'),
('LOCKOUT_DURATION_MINUTES', '15',   'How long an account stays locked after too many failed attempts'),
('REQUIRE_STRONG_PASSWORD',  'true', 'Passwords must contain upper, lower, number, and special character')

ON DUPLICATE KEY UPDATE config_key = config_key;