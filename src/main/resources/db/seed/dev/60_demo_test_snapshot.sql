-- Publish snapshot for "Programming Basics — Demo" and assign to Demo-2026.
-- Runs after v1.2 migration (snapshot tables exist).
DO $$
DECLARE
  v_test_id uuid;
  v_snapshot_id uuid;
  v_q_id uuid;
  v_qs_id uuid;
BEGIN
  SELECT id INTO v_test_id FROM test WHERE title = 'Programming Basics — Demo';
  IF v_test_id IS NULL THEN
    RAISE NOTICE 'Demo test not found, skipping snapshot';
    RETURN;
  END IF;

  -- Skip if snapshot already exists
  IF EXISTS (SELECT 1 FROM assessment_snapshot WHERE assessment_id = v_test_id) THEN
    RAISE NOTICE 'Snapshot already exists, skipping';
    RETURN;
  END IF;

  v_snapshot_id := gen_random_uuid();
  INSERT INTO assessment_snapshot (id, assessment_id, content_hash, version,
    title, timer_minutes, questions_per_assessment, pts_correct, pts_wrong, published_at)
  VALUES (v_snapshot_id, v_test_id, md5(random()::text || clock_timestamp()::text), 1,
    'Programming Basics — Demo', 30, 5, 1.00, -0.25, NOW());

  FOR v_q_id IN
    SELECT id FROM question WHERE test_id = v_test_id ORDER BY position
  LOOP
    v_qs_id := gen_random_uuid();
    INSERT INTO question_snapshot (id, assessment_snapshot_id, original_question_id,
      type, text, code, position)
    SELECT v_qs_id, v_snapshot_id, v_q_id, type, text, code, position
    FROM question WHERE id = v_q_id;

    INSERT INTO option_snapshot (id, question_snapshot_id, original_option_id,
      text, is_correct, position)
    SELECT gen_random_uuid(), v_qs_id, id, text, is_correct, position
    FROM option WHERE question_id = v_q_id ORDER BY position;
  END LOOP;

  INSERT INTO class_test (class_id, assessment_snapshot_id, activated_at)
  SELECT c.id, v_snapshot_id, NOW() FROM user_class c WHERE c.name = 'Demo-2026';

END $$;
