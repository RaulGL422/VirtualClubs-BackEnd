-- V9: ON DELETE CASCADE en refresh_tokens y device; timestamps en refresh_tokens
--
-- Problemas que corrige:
--   1. refresh_tokens.user_id no tenía CASCADE → tokens huérfanos al borrar usuario
--   2. device.user_id no tenía CASCADE → dispositivos huérfanos al borrar usuario
--   3. refresh_tokens no tenía created_at ni expires_at → imposible limpiar tokens expirados

-- ─────────────────────────────────────────────────────────────
-- 1. refresh_tokens.user_id → ON DELETE CASCADE
-- ─────────────────────────────────────────────────────────────

ALTER TABLE refresh_tokens
    DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ─────────────────────────────────────────────────────────────
-- 2. device.user_id → ON DELETE CASCADE
-- ─────────────────────────────────────────────────────────────

ALTER TABLE device
    DROP CONSTRAINT IF EXISTS device_user_id_fkey;

ALTER TABLE device
    ADD CONSTRAINT fk_device_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ─────────────────────────────────────────────────────────────
-- 3. refresh_tokens: añadir created_at y expires_at
--    Los tokens existentes reciben valores razonables por defecto
-- ─────────────────────────────────────────────────────────────

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS expires_at  TIMESTAMPTZ NOT NULL DEFAULT (now() + INTERVAL '7 days');

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);
