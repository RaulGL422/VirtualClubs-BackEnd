CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    device_id   VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    ip_address  VARCHAR(255),
    device_type VARCHAR(255),
    user_id     BIGINT       REFERENCES users(id)
);
