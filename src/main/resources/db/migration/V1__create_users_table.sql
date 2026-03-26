CREATE TABLE users (
    id                BIGSERIAL    PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    name              VARCHAR(255),
    surname           VARCHAR(255),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ
);
