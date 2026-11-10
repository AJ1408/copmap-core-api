CREATE TABLE bandobast_operations (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  kind VARCHAR(20),
  station_id BIGINT REFERENCES stations(id),
  status VARCHAR(20),
  starts_at TIMESTAMPTZ,
  ends_at TIMESTAMPTZ,
  commander_id BIGINT REFERENCES users(id)
);

CREATE TABLE bandobast_points (
  id BIGSERIAL PRIMARY KEY,
  operation_id BIGINT REFERENCES bandobast_operations(id),
  name VARCHAR(120),
  lat DOUBLE PRECISION,
  lng DOUBLE PRECISION,
  radius_m INT DEFAULT 100,
  required_strength INT DEFAULT 1
);
