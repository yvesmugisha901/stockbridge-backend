-- Flyway migration: seed all system configuration keys
-- Adjust the V4__ prefix to follow your existing migrations.

INSERT INTO system_config (config_key, config_value, description) VALUES

-- Approvals
('APPROVAL_QUANTITY_THRESHOLD',    '50',     'Items that trigger Head Office approval'),
('APPROVAL_VALUE_THRESHOLD',       '500000', 'Transfer value (RWF) that triggers Head Office approval'),
('SINGLE_APPROVER_LIMIT',          '100000', 'Branch Manager can approve transfers below this value alone'),
('APPROVAL_EXPIRY_HOURS',          '48',     'Hours before an unanswered approval is auto-escalated'),
('APPROVAL_REMINDER_HOURS',        '24',     'Hours before a reminder is sent to an inactive approver'),
('REQUIRE_REJECTION_COMMENT',      'true',   'Managers must write a reason when rejecting a transfer'),

-- Transfers
('MAX_TRANSFER_QUANTITY',          '500',    'Maximum items allowed in one transfer request'),
('MIN_TRANSFER_QUANTITY',          '1',      'Minimum items required in one transfer request'),
('TRANSFER_AUTO_COMPLETE_DAYS',    '7',      'Days in transit before a transfer is auto-completed'),
('REQUIRE_JUSTIFICATION',          'true',   'Staff must write a reason before submitting a transfer'),
('STOCK_RESERVATION_ENABLED',      'true',   'Reserve stock at source branch on submission'),
('ALLOW_SAME_BRANCH_TRANSFER',     'false',  'Allow source and destination to be the same branch'),
('CANCEL_WINDOW_MINUTES',          '30',     'Minutes after submission during which staff can self-cancel'),

-- Inventory
('LOW_STOCK_ALERT_ENABLED',              'true',  'Show low stock warnings on dashboards'),
('LOW_STOCK_THRESHOLD_PERCENT',          '20',    'Yellow warning threshold as % of minimum stock'),
('CRITICAL_STOCK_THRESHOLD_PERCENT',     '5',     'Red critical threshold as % of minimum stock'),
('MANUAL_ADJUSTMENT_REQUIRES_REASON',    'true',  'Require reason for manual stock adjustments'),
('NEGATIVE_STOCK_ALLOWED',               'false', 'Allow stock levels to go below zero'),
('STOCK_RECONCILIATION_ENABLED',         'true',  'Allow branch stock reconciliation reports'),
('ITEM_DEACTIVATION_BLOCKS_TRANSFERS',   'true',  'Block transfers for inactive catalogue items'),

-- Finance
('DEFAULT_CURRENCY',               'RWF',   'Currency code used across reports and thresholds'),
('COST_ENTRY_REQUIRED',            'true',  'Accountant must record cost before completing a transfer'),
('FINANCE_REPORT_DATE_RANGE_DAYS', '30',    'Default date range (days) on finance reports'),
('TAX_RATE_PERCENT',               '0',     'Default tax rate applied to transfer cost records'),
('MULTI_CURRENCY_ENABLED',         'false', 'Allow costs to be recorded in non-default currencies'),

-- Security
('SESSION_TIMEOUT_MINUTES',  '1440', 'Idle minutes before automatic sign-out'),
('MAX_LOGIN_ATTEMPTS',        '5',   'Failed login attempts before account lockout'),
('LOCKOUT_DURATION_MINUTES',  '15',  'Minutes an account stays locked after too many failures'),
('REQUIRE_STRONG_PASSWORD',  'true', 'Enforce strong password rules on new passwords'),
('PASSWORD_EXPIRY_DAYS',      '0',   'Days before users are asked to change password (0 = never)'),
('TWO_FACTOR_ENABLED',       'false','Enable two-factor authentication'),

-- Audit
('AUDIT_RETENTION_DAYS',         '365',   'Days to keep audit log entries'),
('AUDIT_LOG_READS',              'false', 'Log when users view sensitive records'),
('AUDIT_EXPORT_ENABLED',         'true',  'Allow admins to download audit log as CSV'),
('AUDIT_MAX_EXPORT_ROWS',        '10000', 'Maximum rows in a single audit log export'),
('AUDIT_INCLUDE_READ_EVENTS',    'false', 'Include view events in audit log exports'),

-- Notifications
('NOTIFY_ON_TRANSFER_SUBMIT', 'true',  'Notify managers when a new transfer request arrives'),
('NOTIFY_ON_APPROVAL',        'true',  'Notify staff when their request is approved or rejected'),
('NOTIFY_ON_LOW_STOCK',       'true',  'Notify managers when branch stock drops below warning level'),
('NOTIFY_DIGEST_ENABLED',     'false', 'Send a daily summary notification to managers'),
('DIGEST_SEND_HOUR',          '8',     'Hour of day (0-23) when daily digest is sent')

ON DUPLICATE KEY UPDATE config_key = config_key;