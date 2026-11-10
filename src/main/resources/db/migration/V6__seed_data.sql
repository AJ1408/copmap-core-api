INSERT INTO stations (id, name, district) VALUES
  (1, 'Central Police Station', 'Patna'),
  (2, 'Civil Lines Police Station', 'Patna');

INSERT INTO users (badge_no, full_name, rank, role, station_id, password_hash, active) VALUES
  ('SUP001', 'Rakesh Sinha', 'DySP', 'SUPERVISOR', 1, '$2a$10$K00zQKTeI5km/Cd15FolM.3A5T2.nE/16iaC8QV7emBIUJVInmnNq', TRUE),
  ('PLN001', 'Anita Verma', 'Inspector', 'PLANNER', 1, '$2a$10$K00zQKTeI5km/Cd15FolM.3A5T2.nE/16iaC8QV7emBIUJVInmnNq', TRUE),
  ('OFF001', 'Amit Kumar', 'Constable', 'OFFICER', 1, '$2a$10$K00zQKTeI5km/Cd15FolM.3A5T2.nE/16iaC8QV7emBIUJVInmnNq', TRUE),
  ('OFF002', 'Sunil Yadav', 'Constable', 'OFFICER', 1, '$2a$10$K00zQKTeI5km/Cd15FolM.3A5T2.nE/16iaC8QV7emBIUJVInmnNq', TRUE),
  ('OFF003', 'Pooja Singh', 'Sub-Inspector', 'OFFICER', 1, '$2a$10$K00zQKTeI5km/Cd15FolM.3A5T2.nE/16iaC8QV7emBIUJVInmnNq', TRUE);

SELECT setval(pg_get_serial_sequence('stations', 'id'), 100);
