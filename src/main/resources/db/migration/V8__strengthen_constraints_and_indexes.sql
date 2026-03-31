-- V8: Fortalecer integridad referencial e índices de rendimiento
--
-- Problemas que corrige:
--   1. roles.role_name era nullable y sin unicidad → duplicados posibles
--   2. auth_providers.user_id era nullable → provider huérfano posible
--   3. FKs de auth_providers y user_roles sin CASCADE → usuario borrado dejaba huérfanos
--   4. refresh_tokens sin índice compuesto (user_id, revoked) → slow queries en findByUserAndRevokedFalse
--   5. refresh_tokens sin índice en device_id FK → slow JOINs con device

-- ─────────────────────────────────────────────────────────────
-- 1. roles.role_name — NOT NULL + UNIQUE
-- ─────────────────────────────────────────────────────────────

-- Eliminar posibles duplicados dejando solo el de menor id
DELETE FROM roles r1
USING roles r2
WHERE r1.id > r2.id
  AND r1.role_name = r2.role_name;

-- Reasignar referencias a roles duplicados eliminados
-- (user_roles con role_id apuntando a un role borrado ya no existen tras el DELETE)

ALTER TABLE roles ALTER COLUMN role_name SET NOT NULL;
ALTER TABLE roles ADD CONSTRAINT uq_roles_role_name UNIQUE (role_name);

-- ─────────────────────────────────────────────────────────────
-- 2. auth_providers.user_id — NOT NULL + ON DELETE CASCADE
-- ─────────────────────────────────────────────────────────────

ALTER TABLE auth_providers ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE auth_providers
    DROP CONSTRAINT IF EXISTS auth_providers_user_id_fkey;

ALTER TABLE auth_providers
    ADD CONSTRAINT fk_auth_providers_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ─────────────────────────────────────────────────────────────
-- 3. user_roles.user_id — ON DELETE CASCADE
-- ─────────────────────────────────────────────────────────────

ALTER TABLE user_roles
    DROP CONSTRAINT IF EXISTS user_roles_user_id_fkey;

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ─────────────────────────────────────────────────────────────
-- 4. refresh_tokens — índice compuesto para findByUserAndRevokedFalse
-- ─────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_revoked
    ON refresh_tokens(user_id, revoked);

-- ─────────────────────────────────────────────────────────────
-- 5. refresh_tokens — índice en FK device_id
-- ─────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_device_id
    ON refresh_tokens(device_id);