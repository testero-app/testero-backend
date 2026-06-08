-- Demo admin — username: admin | password: password
INSERT INTO app_user (id, name, username, password_hash, email, created_at) VALUES
  (gen_random_uuid(), 'Admin', 'admin',
   '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u, app_role r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
