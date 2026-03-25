CREATE TABLE auth_providers (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       REFERENCES users(id),
    provider_name    VARCHAR(255) NOT NULL,
    provider_user_id VARCHAR(255),
    password_hash    VARCHAR(255),
    UNIQUE (provider_name, provider_user_id)
);
