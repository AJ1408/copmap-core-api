CREATE TABLE assignments (
  id BIGSERIAL PRIMARY KEY,
  officer_id BIGINT NOT NULL REFERENCES users(id),
  operation_type VARCHAR(20) NOT NULL,
  operation_id BIGINT NOT NULL,
  point_id BIGINT,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL,
  started_at TIMESTAMPTZ,
  ended_at TIMESTAMPTZ
);

CREATE INDEX idx_assign_officer_time ON assignments(officer_id, starts_at, ends_at);
