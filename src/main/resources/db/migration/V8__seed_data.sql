-- ============================================================
-- V8 — Seed Data
-- Creates one ADMIN user so you can log in immediately.
-- Password is:  Admin@1234
-- bcrypt hash generated with cost factor 12.
-- Change this password immediately after first login.
-- ============================================================

-- Default admin user (no branch — admin is global)
INSERT INTO users (full_name, email, password_hash, role, branch_id, active)
VALUES (
    'System Administrator',
    'admin@company.com',
    '$2a$12$9z1Y4Qv9bLqXkPwO2mRtCuRv0eGkMnDvPxWjZl3sNhOqFtUyIeBni',
    'ADMIN',
    NULL,
    TRUE
);

-- Two starter branches so the transfer workflow can be tested immediately
INSERT INTO branches (name, code, location, contact_info, active)
VALUES
    ('Head Office',  'HQ-001', 'Kigali, KG 5 Ave',        '+250 788 000 001', TRUE),
    ('Branch North', 'BR-001', 'Musanze, Northern Province', '+250 788 000 002', TRUE);