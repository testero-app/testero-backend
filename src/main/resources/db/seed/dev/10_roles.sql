-- Roles
INSERT INTO app_role (id, name) VALUES
  (gen_random_uuid(), 'TEACHER'),
  (gen_random_uuid(), 'STUDENT'),
  (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;
