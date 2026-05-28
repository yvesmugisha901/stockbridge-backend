
CREATE TABLE system_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description  VARCHAR(500)
);



INSERT INTO system_config (config_key, config_value, description) VALUES
('APPROVAL_QUANTITY_THRESHOLD', '50',    'Transfers above this quantity require HO approval'),
('APPROVAL_VALUE_THRESHOLD',    '500000','Transfers above this value (RWF) require HO approval'),
('DEFAULT_CURRENCY',            'RWF',   'Default currency for cost recording'),
('MAX_TRANSFER_QUANTITY',       '1000',  'Maximum quantity allowed per transfer request'),
('LOW_STOCK_ALERT_ENABLED',     'true',  'Enable low stock threshold alerts on dashboards');