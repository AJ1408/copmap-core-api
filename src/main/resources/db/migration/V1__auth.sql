CREATE TABLE stations (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  district VARCHAR(80)
);

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  badge_no VARCHAR(30) UNIQUE NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  rank VARCHAR(40),
  role VARCHAR(20) NOT NULL,
  station_id BIGINT REFERENCES stations(id),
  password_hash VARCHAR(100) NOT NULL,
  active BOOLEAN DEFAULT TRUE
);
