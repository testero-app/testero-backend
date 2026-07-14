-- Publish the Python Certification assessment and assign it to the Demo-2026 class.
--
-- The content seed (v1.8-001) only creates the assessment *template*. In production a
-- teacher publishes a snapshot and assigns it to a class through the app; locally we do
-- it here, so a freshly seeded database still has something a student can actually sit.
-- This replaces the equivalent step the retired "Programming Basics — Demo" seed used
-- to perform (60_demo_test_snapshot.sql).
--
-- Dev-only: production must not get class assignments from a migration.
DO $$
DECLARE
  v_template_id uuid;
  v_snapshot_id uuid;
  v_question_id uuid;
  v_snap_question_id uuid;
BEGIN
  SELECT id INTO v_template_id
    FROM assessment_template
   WHERE title = 'Python Certification Exam Practice';

  IF v_template_id IS NULL THEN
    RAISE NOTICE 'Python Certification template not found, skipping publish';
    RETURN;
  END IF;

  IF EXISTS (SELECT 1 FROM assessment_snapshot
              WHERE assessment_template_id = v_template_id) THEN
    RAISE NOTICE 'Python Certification already published, skipping';
    RETURN;
  END IF;

  -- ── Snapshot the template ────────────────────────────────────────
  v_snapshot_id := gen_random_uuid();
  INSERT INTO assessment_snapshot (
    id, assessment_template_id, content_hash, title, timer_minutes,
    questions_per_assessment, pts_correct, pts_wrong, difficulty,
    passing_score, type, published_at)
  SELECT
    v_snapshot_id, t.id, md5(random()::text || clock_timestamp()::text), t.title,
    t.timer_minutes, t.questions_per_assessment, t.pts_correct, t.pts_wrong,
    t.difficulty, t.passing_score, t.type, NOW()
  FROM assessment_template t
  WHERE t.id = v_template_id;

  INSERT INTO assessment_snapshot_subject (
    assessment_snapshot_id, subject_id, created_at, updated_at)
  SELECT v_snapshot_id, ats.subject_id, NOW(), NOW()
  FROM assessment_template_subject ats
  WHERE ats.assessment_template_id = v_template_id;

  -- ── Snapshot every question with its options and subject links ───
  FOR v_question_id IN
    SELECT id FROM question_template
     WHERE assessment_template_id = v_template_id
     ORDER BY position
  LOOP
    v_snap_question_id := gen_random_uuid();

    INSERT INTO question_snapshot (
      id, assessment_snapshot_id, original_question_id,
      type, text, code, position, explanation, points, difficulty)
    SELECT v_snap_question_id, v_snapshot_id, q.id,
           q.type, q.text, q.code, q.position, q.explanation, q.points, q.difficulty
    FROM question_template q
    WHERE q.id = v_question_id;

    INSERT INTO option_snapshot (
      id, question_snapshot_id, original_option_id,
      text, is_correct, is_fallback, position)
    SELECT gen_random_uuid(), v_snap_question_id, o.id,
           o.text, o.is_correct, o.is_fallback, o.position
    FROM option_template o
    WHERE o.question_template_id = v_question_id
    ORDER BY o.position;

    INSERT INTO question_snapshot_subject (question_snapshot_id, subject_id, weight)
    SELECT v_snap_question_id, qts.subject_id, qts.weight
    FROM question_template_subject qts
    WHERE qts.question_template_id = v_question_id;
  END LOOP;

  -- ── Assign to the demo class ─────────────────────────────────────
  -- available_from / available_until stay NULL: no window, always open.
  INSERT INTO class_assessment_assignment (class_id, assessment_snapshot_id)
  SELECT c.id, v_snapshot_id
  FROM user_class c
  WHERE c.name = 'Demo-2026';
END $$;
