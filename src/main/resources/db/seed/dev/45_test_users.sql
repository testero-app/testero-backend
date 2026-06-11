-- Test users — password for all: password
INSERT INTO app_user (id, name, username, password_hash, email, created_at) VALUES
  (gen_random_uuid(), 'Test User SW1', 'test_user_sw1', '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW()),
  (gen_random_uuid(), 'Test User SW2', 'test_user_sw2', '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u, app_role r
WHERE u.username IN ('test_user_sw1', 'test_user_sw2')
  AND r.name = 'STUDENT'
ON CONFLICT DO NOTHING;

-- In dev both use Demo-2026 (the only dev class)
INSERT INTO student_profile (id, user_id, class_id)
SELECT gen_random_uuid(), u.id, c.id
FROM app_user u, user_class c
WHERE u.username IN ('test_user_sw1', 'test_user_sw2')
  AND c.name = 'Demo-2026'
ON CONFLICT DO NOTHING;
