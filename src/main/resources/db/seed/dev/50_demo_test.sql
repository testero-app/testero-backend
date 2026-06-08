-- Demo test — Programming Basics (5 questions)
DO $$
DECLARE
  v_test_id uuid;
  v_subject_id uuid;
  v_q_id uuid;
BEGIN
  IF EXISTS (SELECT 1 FROM test WHERE title = 'Programming Basics — Demo') THEN
    RAISE NOTICE 'Test already exists, skipping';
    RETURN;
  END IF;

  -- Subject
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Programming')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subject_id FROM subject WHERE label = 'Programming';

  -- Test
  v_test_id := gen_random_uuid();
  INSERT INTO test (id, title, date, timer_minutes, total_pool, questions_per_test, pts_correct, pts_wrong)
  VALUES (v_test_id, 'Programming Basics — Demo', '2026-12-31', 30, 5, 5, 1.00, -0.25);

  INSERT INTO test_subject (test_id, subject_id) VALUES (v_test_id, v_subject_id);

  INSERT INTO class_test (class_id, test_id, activated_at)
  SELECT c.id, v_test_id, NOW() FROM user_class c WHERE c.name = 'Demo-2026';

  -- Q1
  v_q_id := gen_random_uuid();
  INSERT INTO question (id, test_id, type, text, code, position)
  VALUES (v_q_id, v_test_id, 'multiple',
    'What does the acronym "HTTP" stand for?', NULL, 1);
  INSERT INTO option (id, question_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, 'HyperText Transfer Protocol', true, false, 1),
    (gen_random_uuid(), v_q_id, 'High Tech Transfer Process', false, false, 2),
    (gen_random_uuid(), v_q_id, 'HyperText Transmission Program', false, false, 3),
    (gen_random_uuid(), v_q_id, 'Home Tool Transfer Protocol', false, false, 4);

  -- Q2
  v_q_id := gen_random_uuid();
  INSERT INTO question (id, test_id, type, text, code, position)
  VALUES (v_q_id, v_test_id, 'multiple',
    'Which data structure uses key-value pairs?', NULL, 2);
  INSERT INTO option (id, question_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, 'Array', false, false, 1),
    (gen_random_uuid(), v_q_id, 'Dictionary / Map', true, false, 2),
    (gen_random_uuid(), v_q_id, 'Linked List', false, false, 3),
    (gen_random_uuid(), v_q_id, 'Stack', false, false, 4);

  -- Q3
  v_q_id := gen_random_uuid();
  INSERT INTO question (id, test_id, type, text, code, position)
  VALUES (v_q_id, v_test_id, 'multiple',
    'What is the output of the following code?',
    E'x = 10\ny = 3\nprint(x % y)', 3);
  INSERT INTO option (id, question_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, '3', false, false, 1),
    (gen_random_uuid(), v_q_id, '1', true, false, 2),
    (gen_random_uuid(), v_q_id, '3.33', false, false, 3),
    (gen_random_uuid(), v_q_id, '0', false, false, 4);

  -- Q4
  v_q_id := gen_random_uuid();
  INSERT INTO question (id, test_id, type, text, code, position)
  VALUES (v_q_id, v_test_id, 'multiple',
    'Which of the following is NOT a valid HTTP method?', NULL, 4);
  INSERT INTO option (id, question_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, 'GET', false, false, 1),
    (gen_random_uuid(), v_q_id, 'POST', false, false, 2),
    (gen_random_uuid(), v_q_id, 'FETCH', true, false, 3),
    (gen_random_uuid(), v_q_id, 'DELETE', false, false, 4);

  -- Q5
  v_q_id := gen_random_uuid();
  INSERT INTO question (id, test_id, type, text, code, position)
  VALUES (v_q_id, v_test_id, 'multiple',
    'In a zero-indexed array of 6 elements, what is the index of the last element?',
    NULL, 5);
  INSERT INTO option (id, question_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, '6', false, false, 1),
    (gen_random_uuid(), v_q_id, '5', true, false, 2),
    (gen_random_uuid(), v_q_id, '0', false, false, 3),
    (gen_random_uuid(), v_q_id, '-1', false, false, 4);

END $$;
