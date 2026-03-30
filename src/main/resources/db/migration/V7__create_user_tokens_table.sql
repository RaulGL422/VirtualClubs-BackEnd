-- Tokens de un solo uso para verificación de email y reset de contraseña.
-- El token en texto plano NUNCA se almacena — solo su hash SHA-256.
CREATE TABLE user_tokens (
    id         BIGSERIAL                   PRIMARY KEY,
    user_id    BIGINT                      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64)                 NOT NULL UNIQUE,
    type       VARCHAR(32)                 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE    NOT NULL,
    consumed   BOOLEAN                     NOT NULL DEFAULT FALSE,
    meta       VARCHAR(500)
);

-- Índice para la búsqueda por hash (operación más frecuente: validar token)
CREATE INDEX idx_user_tokens_tokenhash ON user_tokens(token_hash);

-- Índice para la limpieza de tokens por usuario y tipo
CREATE INDEX idx_user_tokens_user_type ON user_tokens(user_id, type);