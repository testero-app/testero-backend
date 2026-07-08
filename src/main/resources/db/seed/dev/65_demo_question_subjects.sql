-- Link demo questions to the "Programming" subject and copy to snapshot.
-- Runs after v1.4 (question_template_subject table exists) and after 60_demo_test_snapshot.sql.
DO $$
DECLARE
  v_subject_id uuid;
  v_q record;
BEGIN
  SELECT id INTO v_subject_id FROM subject WHERE label = 'Programming';
  IF v_subject_id IS NULL THEN
    RAISE NOTICE 'Subject "Programming" not found, skipping';
    RETURN;
  END IF;

  -- Link all template questions to the subject
  INSERT INTO question_template_subject (question_template_id, subject_id, weight)
  SELECT q.id, v_subject_id, 1.00
  FROM question_template q
  JOIN assessment_template t ON q.assessment_template_id = t.id
  WHERE t.title = 'Programming Basics — Demo'
  ON CONFLICT DO NOTHING;

  -- Copy links to snapshot questions
  INSERT INTO question_snapshot_subject (question_snapshot_id, subject_id, weight)
  SELECT qs.id, v_subject_id, 1.00
  FROM question_snapshot qs
  JOIN assessment_snapshot asnap ON qs.assessment_snapshot_id = asnap.id
  JOIN assessment_template t ON asnap.assessment_template_id = t.id
  WHERE t.title = 'Programming Basics — Demo'
  ON CONFLICT DO NOTHING;

  -- Create topic and link subject
  INSERT INTO topic (id, title, abbreviation, position, enabled)
  VALUES (gen_random_uuid(), 'Programming Basics', 'PB', 1, true)
  ON CONFLICT DO NOTHING;

  INSERT INTO topic_subject (topic_id, subject_id, position)
  SELECT t.id, v_subject_id, 1
  FROM topic t
  WHERE t.title = 'Programming Basics'
  ON CONFLICT DO NOTHING;

END $$;
