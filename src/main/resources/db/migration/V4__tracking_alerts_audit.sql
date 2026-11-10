CREATE TABLE location_pings (
  id BIGSERIAL PRIMARY KEY,
  officer_id BIGINT NOT NULL,
  patrol_id BIGINT NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lng DOUBLE PRECISION NOT NULL,
  accuracy_m REAL,
  speed_mps REAL,
  recorded_at TIMESTAMPTZ NOT NULL,
  received_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_ping_patrol_time ON location_pings(patrol_id, recorded_at);
CREATE UNIQUE INDEX uq_ping_officer_patrol_time ON location_pings(officer_id, patrol_id, recorded_at);

CREATE TABLE alerts (
  id BIGSERIAL PRIMARY KEY,
  type VARCHAR(30) NOT NULL,
  severity VARCHAR(10),
  operation_type VARCHAR(20),
  operation_id BIGINT,
  officer_id BIGINT,
  message TEXT,
  lat DOUBLE PRECISION,
  lng DOUBLE PRECISION,
  status VARCHAR(15) DEFAULT 'OPEN',
  acked_by BIGINT,
  acked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  actor_id BIGINT,
  action VARCHAR(60) NOT NULL,
  entity_type VARCHAR(30),
  entity_id BIGINT,
  details JSONB,
  created_at TIMESTAMPTZ DEFAULT now()
);
