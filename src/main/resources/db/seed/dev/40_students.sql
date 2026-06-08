-- Demo students — password for all: password
INSERT INTO app_user (id, name, username, password_hash, email, created_at) VALUES
  (gen_random_uuid(), 'Alice Rossi',  'a.rossi',   '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW()),
  (gen_random_uuid(), 'Bob Bianchi',  'b.bianchi', '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW()),
  (gen_random_uuid(), 'Carol Verdi',  'c.verdi',   '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW()),
  (gen_random_uuid(), 'Dave Neri',    'd.neri',     '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW()),
  (gen_random_uuid(), 'Eve Gialli',   'e.gialli',  '$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', NULL, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO app_user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u, app_role r
WHERE u.username IN ('a.rossi', 'b.bianchi', 'c.verdi', 'd.neri', 'e.gialli')
  AND r.name = 'STUDENT'
ON CONFLICT DO NOTHING;

INSERT INTO student_profile (id, user_id, class_id)
SELECT gen_random_uuid(), u.id, c.id
FROM app_user u, user_class c
WHERE u.username IN ('a.rossi', 'b.bianchi', 'c.verdi', 'd.neri', 'e.gialli')
  AND c.name = 'Demo-2026'
ON CONFLICT DO NOTHING;
