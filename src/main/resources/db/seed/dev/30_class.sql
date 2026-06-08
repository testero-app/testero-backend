-- Demo class + teacher assignment
INSERT INTO user_class (id, name) VALUES
  (gen_random_uuid(), 'Demo-2026')
ON CONFLICT DO NOTHING;

INSERT INTO teacher_class (user_id, class_id)
SELECT u.id, c.id
FROM app_user u, user_class c
WHERE u.username = 'teacher' AND c.name = 'Demo-2026'
ON CONFLICT DO NOTHING;
