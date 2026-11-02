-- RentWise v0.1 -> v0.2 database migration
--
-- Upgrade the v0.1 users table for authentication and RBAC.
-- This migration is intended to be executed once on a v0.1 database.

USE rentwise;

-- v0.1 built-in demo account has no password and is removed in v0.2.
DELETE FROM users
WHERE username = 'demo_user';

ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(100) NOT NULL,
    ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'LEARNER',
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN created_at TIMESTAMP(6) NULL;

UPDATE users
SET created_at = CURRENT_TIMESTAMP(6)
WHERE created_at IS NULL;

ALTER TABLE users
    MODIFY COLUMN created_at TIMESTAMP(6) NOT NULL;