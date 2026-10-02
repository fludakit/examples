CREATE TABLE IF NOT EXISTS engineers (
    id BIGINT PRIMARY KEY,
    dev_name VARCHAR(255)
);

INSERT INTO engineers VALUES (1, 'Duke Jakarta') ON CONFLICT DO NOTHING;
INSERT INTO engineers VALUES (2, 'Arquillian Glassfish') ON CONFLICT DO NOTHING;
