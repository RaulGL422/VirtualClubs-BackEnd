-- Create device table (one row per unique device+user combination)
CREATE TABLE device (
    id          BIGSERIAL    PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    device_type VARCHAR(255),
    ip_address  VARCHAR(255),
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    CONSTRAINT uq_device_user UNIQUE (device_id, user_id)
);

-- Migrate existing device data from refresh_tokens
INSERT INTO device (device_id, device_name, device_type, ip_address, user_id)
SELECT DISTINCT ON (device_id, user_id)
    device_id, device_name, device_type, ip_address, user_id
FROM refresh_tokens
WHERE device_id IS NOT NULL AND user_id IS NOT NULL
ORDER BY device_id, user_id, id DESC;

-- Add temporary FK column
ALTER TABLE refresh_tokens ADD COLUMN device_fk BIGINT REFERENCES device(id);

-- Link each refresh_token to its device
UPDATE refresh_tokens rt
SET device_fk = d.id
FROM device d
WHERE rt.device_id = d.device_id
  AND rt.user_id = d.user_id;

-- Enforce non-null
ALTER TABLE refresh_tokens ALTER COLUMN device_fk SET NOT NULL;

-- Drop old device columns
ALTER TABLE refresh_tokens DROP COLUMN device_id;
ALTER TABLE refresh_tokens DROP COLUMN device_name;
ALTER TABLE refresh_tokens DROP COLUMN device_type;
ALTER TABLE refresh_tokens DROP COLUMN ip_address;

-- Rename FK column to final name
ALTER TABLE refresh_tokens RENAME COLUMN device_fk TO device_id;

-- Performance index
CREATE INDEX idx_device_user_id ON device(user_id);
