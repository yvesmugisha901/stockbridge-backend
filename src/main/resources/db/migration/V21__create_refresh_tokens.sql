-- V{NEXT}__create_refresh_tokens.sql
-- Name this file with the next Flyway version in your db/migration folder,
-- e.g. V5__create_refresh_tokens.sql (check your existing migrations for the right number)

CREATE TABLE refresh_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    revoked    TINYINT(1)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_rt_token   ON refresh_tokens (token);
CREATE INDEX idx_rt_user_id ON refresh_tokens (user_id);