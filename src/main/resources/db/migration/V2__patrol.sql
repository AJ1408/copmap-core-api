CREATE TABLE patrols (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  type VARCHAR(20) NOT NULL,
  station_id BIGINT NOT NULL REFERENCES stations(id),
  status VARCHAR(20) NOT NULL,
  shift_start TIMESTAMPTZ NOT NULL,
  shift_end TIMESTAMPTZ NOT NULL,
  geofence_radius_m INT DEFAULT 200,
  created_by BIGINT NOT NULL REFERENCES users(id),
  closed_by BIGINT REFERENCES users(id),
  close_remarks TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE patrol_waypoints (
  id BIGSERIAL PRIMARY KEY,
  patrol_id BIGINT REFERENCES patrols(id) ON DELETE CASCADE,
  seq INT NOT NULL,
  name VARCHAR(120),
  lat DOUBLE PRECISION NOT NULL,
  lng DOUBLE PRECISION NOT NULL
);
