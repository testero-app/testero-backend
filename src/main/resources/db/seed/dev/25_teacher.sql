-- Demo teacher — username: teacher | password: password
INSERT INTO app_user (id, name, username, password_hash, email, created_at) VALUES
  (gen_random_uuid(), 'Demo Teacher', 'teacher',
   '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u, app_role r
WHERE u.username = 'teacher' AND r.name = 'TEACHER'
ON CONFLICT DO NOTHING;
