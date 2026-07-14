-- Remove the legacy "Programming Basics — Demo" seed data.
--
-- The demo assessment (seeded across 50/60/65/70 in v1.0 → v1.7) is
-- superseded by the Python Certification content seeded in v1.8-001.
--
-- Those original seed files are deliberately left untouched: they belong to
-- changesets that have already been applied, and editing them would change
-- their Liquibase checksums and break startup on every existing database.
-- We delete the data here instead, in a new changeset.
--
-- Table names below are the post-v1.7 ones (test → assessment_template,
-- question → question_template, class_test → class_assessment_assignment, …).
DO $$
DECLARE
  v_template_id uuid;
  v_subject_id  uuid;
BEGIN
  SELECT id INTO v_template_id
    FROM assessment_template
   WHERE title = 'Programming Basics — Demo';

  IF v_template_id IS NULL THEN
    RAISE NOTICE 'Demo assessment not present, skipping';
  ELSE
    -- Submissions on the demo snapshots.
    -- user_answer and user_answer_selected_option cascade from submission.
    DELETE FROM submission
     WHERE assessment_snapshot_id IN (
       SELECT id FROM assessment_snapshot
        WHERE assessment_template_id = v_template_id);

    -- Class assignments pointing at the demo snapshots.
    DELETE FROM class_assessment_assignment
     WHERE assessment_snapshot_id IN (
       SELECT id FROM assessment_snapshot
        WHERE assessment_template_id = v_template_id);

    -- Published snapshots. question_snapshot, option_snapshot,
    -- question_snapshot_subject and assessment_snapshot_subject all cascade.
    DELETE FROM assessment_snapshot
     WHERE assessment_template_id = v_template_id;

    -- Draft questions. option_template and question_template_subject cascade.
    DELETE FROM question_template
     WHERE assessment_template_id = v_template_id;

    DELETE FROM assessment_template_subject
     WHERE assessment_template_id = v_template_id;

    DELETE FROM assessment_template
     WHERE id = v_template_id;
  END IF;

  -- Demo topic "Programming Basics" and its subject links.
  DELETE FROM topic_subject
   WHERE topic_id IN (SELECT id FROM topic WHERE title = 'Programming Basics');
  DELETE FROM topic
   WHERE title = 'Programming Basics';

  -- Demo subject "Programming", but only once nothing references it anymore.
  SELECT id INTO v_subject_id FROM subject WHERE label = 'Programming';
  IF v_subject_id IS NOT NULL
     AND NOT EXISTS (SELECT 1 FROM assessment_template_subject WHERE subject_id = v_subject_id)
     AND NOT EXISTS (SELECT 1 FROM question_template_subject   WHERE subject_id = v_subject_id)
     AND NOT EXISTS (SELECT 1 FROM question_snapshot_subject   WHERE subject_id = v_subject_id)
     AND NOT EXISTS (SELECT 1 FROM assessment_snapshot_subject WHERE subject_id = v_subject_id)
     AND NOT EXISTS (SELECT 1 FROM topic_subject               WHERE subject_id = v_subject_id)
  THEN
    DELETE FROM subject WHERE id = v_subject_id;
  END IF;
END $$;
