INSERT INTO roles (role_name) VALUES ('USER')  ON CONFLICT (role_name) DO NOTHING;
INSERT INTO roles (role_name) VALUES ('ADMIN') ON CONFLICT (role_name) DO NOTHING;
