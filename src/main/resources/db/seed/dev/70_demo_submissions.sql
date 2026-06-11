-- Demo submissions — simulate completed assessments for dev/test.
--
-- Alice Rossi:  4/5 correct (missed Q3 — answered '3' instead of '1')
-- Bob Bianchi:  2/5 correct (missed Q1, Q3, Q4) + left Q5 unanswered
-- Carol Verdi:  5/5 correct (perfect score)
-- Dave Neri:    0/5 correct (all wrong answers)
-- Eve Gialli:   3/5 correct (missed Q2, skipped Q4)
--
-- Depends on: 60_demo_test_snapshot.sql (snapshot + question/option snapshots exist)

DO $$
DECLARE
  v_snapshot_id uuid;
  v_user_id     uuid;
  v_sub_id      uuid;
  v_answer_id   uuid;

  -- Question snapshot IDs (ordered by position 1..5)
  v_qs1 uuid; v_qs2 uuid; v_qs3 uuid; v_qs4 uuid; v_qs5 uuid;

  -- Option snapshot IDs per question (ordered by position)
  -- Q1: A=HTTP(correct), B=HighTech, C=Transmission, D=HomeTool
  v_q1_a uuid; v_q1_b uuid; v_q1_c uuid; v_q1_d uuid;
  -- Q2: A=Array, B=Dictionary(correct), C=LinkedList, D=Stack
  v_q2_a uuid; v_q2_b uuid; v_q2_c uuid; v_q2_d uuid;
  -- Q3: A=3, B=1(correct), C=3.33, D=0
  v_q3_a uuid; v_q3_b uuid; v_q3_c uuid; v_q3_d uuid;
  -- Q4: A=GET, B=POST, C=FETCH(correct), D=DELETE
  v_q4_a uuid; v_q4_b uuid; v_q4_c uuid; v_q4_d uuid;
  -- Q5: A=6, B=5(correct), C=0, D=-1
  v_q5_a uuid; v_q5_b uuid; v_q5_c uuid; v_q5_d uuid;

BEGIN
  -- Resolve snapshot
  SELECT id INTO v_snapshot_id
  FROM assessment_snapshot WHERE title = 'Programming Basics — Demo' LIMIT 1;

  IF v_snapshot_id IS NULL THEN
    RAISE NOTICE 'Demo snapshot not found, skipping submissions seed';
    RETURN;
  END IF;

  -- Skip if submissions already seeded
  IF EXISTS (SELECT 1 FROM submission WHERE assessment_snapshot_id = v_snapshot_id) THEN
    RAISE NOTICE 'Demo submissions already exist, skipping';
    RETURN;
  END IF;

  -- Resolve question snapshots (by position)
  SELECT id INTO v_qs1 FROM question_snapshot WHERE assessment_snapshot_id = v_snapshot_id AND position = 1;
  SELECT id INTO v_qs2 FROM question_snapshot WHERE assessment_snapshot_id = v_snapshot_id AND position = 2;
  SELECT id INTO v_qs3 FROM question_snapshot WHERE assessment_snapshot_id = v_snapshot_id AND position = 3;
  SELECT id INTO v_qs4 FROM question_snapshot WHERE assessment_snapshot_id = v_snapshot_id AND position = 4;
  SELECT id INTO v_qs5 FROM question_snapshot WHERE assessment_snapshot_id = v_snapshot_id AND position = 5;

  -- Resolve option snapshots per question (by position)
  SELECT id INTO v_q1_a FROM option_snapshot WHERE question_snapshot_id = v_qs1 AND position = 1;
  SELECT id INTO v_q1_b FROM option_snapshot WHERE question_snapshot_id = v_qs1 AND position = 2;
  SELECT id INTO v_q1_c FROM option_snapshot WHERE question_snapshot_id = v_qs1 AND position = 3;
  SELECT id INTO v_q1_d FROM option_snapshot WHERE question_snapshot_id = v_qs1 AND position = 4;

  SELECT id INTO v_q2_a FROM option_snapshot WHERE question_snapshot_id = v_qs2 AND position = 1;
  SELECT id INTO v_q2_b FROM option_snapshot WHERE question_snapshot_id = v_qs2 AND position = 2;
  SELECT id INTO v_q2_c FROM option_snapshot WHERE question_snapshot_id = v_qs2 AND position = 3;
  SELECT id INTO v_q2_d FROM option_snapshot WHERE question_snapshot_id = v_qs2 AND position = 4;

  SELECT id INTO v_q3_a FROM option_snapshot WHERE question_snapshot_id = v_qs3 AND position = 1;
  SELECT id INTO v_q3_b FROM option_snapshot WHERE question_snapshot_id = v_qs3 AND position = 2;
  SELECT id INTO v_q3_c FROM option_snapshot WHERE question_snapshot_id = v_qs3 AND position = 3;
  SELECT id INTO v_q3_d FROM option_snapshot WHERE question_snapshot_id = v_qs3 AND position = 4;

  SELECT id INTO v_q4_a FROM option_snapshot WHERE question_snapshot_id = v_qs4 AND position = 1;
  SELECT id INTO v_q4_b FROM option_snapshot WHERE question_snapshot_id = v_qs4 AND position = 2;
  SELECT id INTO v_q4_c FROM option_snapshot WHERE question_snapshot_id = v_qs4 AND position = 3;
  SELECT id INTO v_q4_d FROM option_snapshot WHERE question_snapshot_id = v_qs4 AND position = 4;

  SELECT id INTO v_q5_a FROM option_snapshot WHERE question_snapshot_id = v_qs5 AND position = 1;
  SELECT id INTO v_q5_b FROM option_snapshot WHERE question_snapshot_id = v_qs5 AND position = 2;
  SELECT id INTO v_q5_c FROM option_snapshot WHERE question_snapshot_id = v_qs5 AND position = 3;
  SELECT id INTO v_q5_d FROM option_snapshot WHERE question_snapshot_id = v_qs5 AND position = 4;

  -- ═══════════════════════════════════════════════════════════════════════════
  -- ALICE ROSSI — 4/5 correct, missed Q3 (answered '3' instead of '1')
  -- Score: 4 * 1.00 + 1 * (-0.25) = 3.75
  -- ═══════════════════════════════════════════════════════════════════════════
  SELECT id INTO v_user_id FROM app_user WHERE username = 'a.rossi';
  v_sub_id := gen_random_uuid();
  INSERT INTO submission (id, user_id, assessment_snapshot_id, started_at, submitted_at, score)
  VALUES (v_sub_id, v_user_id, v_snapshot_id,
    NOW() - INTERVAL '2 days' - INTERVAL '25 minutes',
    NOW() - INTERVAL '2 days',
    3.75);

  -- Q1: correct (selected A — HyperText Transfer Protocol)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs1, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q1_a);

  -- Q2: correct (selected B — Dictionary / Map)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs2, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q2_b);

  -- Q3: WRONG (selected A='3', correct is B='1')
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs3, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q3_a);

  -- Q4: correct (selected C — FETCH)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs4, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q4_c);

  -- Q5: correct (selected B — 5)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs5, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q5_b);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- BOB BIANCHI — 2/5 correct, missed Q1, Q3, Q4, skipped Q5
  -- Score: 2 * 1.00 + 3 * (-0.25) = 1.25
  -- ═══════════════════════════════════════════════════════════════════════════
  SELECT id INTO v_user_id FROM app_user WHERE username = 'b.bianchi';
  v_sub_id := gen_random_uuid();
  INSERT INTO submission (id, user_id, assessment_snapshot_id, started_at, submitted_at, score)
  VALUES (v_sub_id, v_user_id, v_snapshot_id,
    NOW() - INTERVAL '1 day' - INTERVAL '18 minutes',
    NOW() - INTERVAL '1 day',
    1.25);

  -- Q1: WRONG (selected B — High Tech Transfer Process)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs1, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q1_b);

  -- Q2: correct (selected B — Dictionary / Map)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs2, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q2_b);

  -- Q3: WRONG (selected D='0', correct is B='1')
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs3, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q3_d);

  -- Q4: WRONG (selected A — GET, correct is C — FETCH)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs4, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q4_a);

  -- Q5: UNANSWERED (no selection)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs5, 'multiple', '', '', NULL, 0.00);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- CAROL VERDI — 5/5 correct (perfect score)
  -- Score: 5 * 1.00 = 5.00
  -- ═══════════════════════════════════════════════════════════════════════════
  SELECT id INTO v_user_id FROM app_user WHERE username = 'c.verdi';
  v_sub_id := gen_random_uuid();
  INSERT INTO submission (id, user_id, assessment_snapshot_id, started_at, submitted_at, score)
  VALUES (v_sub_id, v_user_id, v_snapshot_id,
    NOW() - INTERVAL '3 days' - INTERVAL '12 minutes',
    NOW() - INTERVAL '3 days',
    5.00);

  -- Q1: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs1, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q1_a);

  -- Q2: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs2, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q2_b);

  -- Q3: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs3, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q3_b);

  -- Q4: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs4, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q4_c);

  -- Q5: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs5, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q5_b);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- DAVE NERI — 0/5 correct (all wrong)
  -- Score: 5 * (-0.25) = -1.25
  -- ═══════════════════════════════════════════════════════════════════════════
  SELECT id INTO v_user_id FROM app_user WHERE username = 'd.neri';
  v_sub_id := gen_random_uuid();
  INSERT INTO submission (id, user_id, assessment_snapshot_id, started_at, submitted_at, score)
  VALUES (v_sub_id, v_user_id, v_snapshot_id,
    NOW() - INTERVAL '1 day' - INTERVAL '8 minutes',
    NOW() - INTERVAL '1 day',
    -1.25);

  -- Q1: WRONG (selected D — Home Tool)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs1, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q1_d);

  -- Q2: WRONG (selected A — Array)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs2, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q2_a);

  -- Q3: WRONG (selected C — 3.33)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs3, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q3_c);

  -- Q4: WRONG (selected B — POST, correct is C — FETCH)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs4, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q4_b);

  -- Q5: WRONG (selected D — -1, correct is B — 5)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs5, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q5_d);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- EVE GIALLI — 3/5 correct, missed Q2 (wrong), skipped Q4
  -- Score: 3 * 1.00 + 1 * (-0.25) = 2.75
  -- ═══════════════════════════════════════════════════════════════════════════
  SELECT id INTO v_user_id FROM app_user WHERE username = 'e.gialli';
  v_sub_id := gen_random_uuid();
  INSERT INTO submission (id, user_id, assessment_snapshot_id, started_at, submitted_at, score)
  VALUES (v_sub_id, v_user_id, v_snapshot_id,
    NOW() - INTERVAL '12 hours' - INTERVAL '20 minutes',
    NOW() - INTERVAL '12 hours',
    2.75);

  -- Q1: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs1, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q1_a);

  -- Q2: WRONG (selected C — Linked List)
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs2, 'multiple', '', '', false, -0.25);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q2_c);

  -- Q3: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs3, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q3_b);

  -- Q4: UNANSWERED
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs4, 'multiple', '', '', NULL, 0.00);

  -- Q5: correct
  v_answer_id := gen_random_uuid();
  INSERT INTO user_answer (id, submission_id, question_snapshot_id, type, text, motivation, is_correct, points_awarded)
  VALUES (v_answer_id, v_sub_id, v_qs5, 'multiple', '', '', true, 1.00);
  INSERT INTO user_answer_selected_option (id, answer_id, option_snapshot_id)
  VALUES (gen_random_uuid(), v_answer_id, v_q5_b);

  RAISE NOTICE 'Demo submissions seeded: Alice 4/5, Bob 2/5, Carol 5/5, Dave 0/5, Eve 3/5';
END $$;
