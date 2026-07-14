-- Python Certification Exam Practice — seed data
-- 23 subjects, 3 topics (hierarchy), 1 assessment template, 67 questions, 268 options
-- Runs after v1.7 (all renames applied). Uses final table names.
DO $$
DECLARE
  v_at_id uuid;  -- assessment template
  v_q_id  uuid;  -- question template (reused per question)

  -- Subject IDs (23)
  v_s01 uuid; v_s02 uuid; v_s03 uuid; v_s04 uuid; v_s05 uuid;
  v_s06 uuid; v_s07 uuid; v_s08 uuid; v_s09 uuid; v_s10 uuid;
  v_s11 uuid; v_s12 uuid; v_s13 uuid; v_s14 uuid; v_s15 uuid;
  v_s16 uuid; v_s17 uuid; v_s18 uuid; v_s19 uuid; v_s20 uuid;
  v_s21 uuid; v_s22 uuid; v_s23 uuid;

  -- Topic IDs (3-level hierarchy)
  v_t_root   uuid;  -- Programmazione Software
  v_t_python uuid;  -- Python
  v_t_fond   uuid;  -- Fondamenti
BEGIN
  -- Guard: skip if already seeded
  IF EXISTS (SELECT 1 FROM assessment_template WHERE title = 'Python Certification Exam Practice') THEN
    RAISE NOTICE 'Python Certification Exam Practice already exists, skipping';
    RETURN;
  END IF;

  -- ═══════════════════════════════════════════════════════════════════════════
  -- SUBJECTS (23)
  -- ═══════════════════════════════════════════════════════════════════════════
  v_s01 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s01, 'Tipi di dato');
  v_s02 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s02, 'Conversioni di tipo');
  v_s03 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s03, 'Variabili e assegnazione');
  v_s04 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s04, 'Operatori aritmetici');
  v_s05 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s05, 'Operatori di confronto');
  v_s06 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s06, 'Operatori logici');
  v_s07 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s07, 'Strutture condizionali');
  v_s08 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s08, 'Ciclo for');
  v_s09 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s09, 'Ciclo while');
  v_s10 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s10, 'Stringhe');
  v_s11 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s11, 'Formattazione output');
  v_s12 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s12, 'Liste');
  v_s13 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s13, 'Definizione funzioni');
  v_s14 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s14, 'Chiamata funzioni');
  v_s15 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s15, 'Funzioni built-in');
  v_s16 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s16, 'Lettura e scrittura file');
  v_s17 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s17, 'Gestione eccezioni');
  v_s18 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s18, 'Modulo math');
  v_s19 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s19, 'Modulo random');
  v_s20 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s20, 'Modulo datetime');
  v_s21 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s21, 'Modulo os e sys');
  v_s22 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s22, 'Unit testing');
  v_s23 := gen_random_uuid(); INSERT INTO subject (id, label) VALUES (v_s23, 'Documentazione');

  -- ═══════════════════════════════════════════════════════════════════════════
  -- TOPICS (3-level hierarchy: Programmazione Software → Python → Fondamenti)
  -- ═══════════════════════════════════════════════════════════════════════════
  v_t_root := gen_random_uuid();
  INSERT INTO topic (id, title, abbreviation, position, enabled, parent_id)
  VALUES (v_t_root, 'Programmazione Software', 'PS', 1, true, NULL);

  v_t_python := gen_random_uuid();
  INSERT INTO topic (id, title, abbreviation, position, enabled, parent_id)
  VALUES (v_t_python, 'Python', 'PY', 1, true, v_t_root);

  v_t_fond := gen_random_uuid();
  INSERT INTO topic (id, title, abbreviation, position, enabled, parent_id)
  VALUES (v_t_fond, 'Fondamenti', 'FO', 1, true, v_t_python);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- TOPIC-SUBJECT LINKS (23 subjects → Fondamenti)
  -- ═══════════════════════════════════════════════════════════════════════════
  INSERT INTO topic_subject (topic_id, subject_id, position) VALUES
    (v_t_fond, v_s01, 1),  (v_t_fond, v_s02, 2),  (v_t_fond, v_s03, 3),
    (v_t_fond, v_s04, 4),  (v_t_fond, v_s05, 5),  (v_t_fond, v_s06, 6),
    (v_t_fond, v_s07, 7),  (v_t_fond, v_s08, 8),  (v_t_fond, v_s09, 9),
    (v_t_fond, v_s10, 10), (v_t_fond, v_s11, 11), (v_t_fond, v_s12, 12),
    (v_t_fond, v_s13, 13), (v_t_fond, v_s14, 14), (v_t_fond, v_s15, 15),
    (v_t_fond, v_s16, 16), (v_t_fond, v_s17, 17), (v_t_fond, v_s18, 18),
    (v_t_fond, v_s19, 19), (v_t_fond, v_s20, 20), (v_t_fond, v_s21, 21),
    (v_t_fond, v_s22, 22), (v_t_fond, v_s23, 23);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- ASSESSMENT TEMPLATE
  -- ═══════════════════════════════════════════════════════════════════════════
  v_at_id := gen_random_uuid();
  INSERT INTO assessment_template (
    id, title, type, difficulty, timer_minutes, questions_per_assessment,
    pts_correct, pts_wrong, pts_unanswered, passing_score,
    shuffle_questions, shuffle_options, assessment_description
  ) VALUES (
    v_at_id,
    'Python Certification Exam Practice',
    'CERT_SIMULATION',
    'INTERMEDIATE',
    45,
    35,
    1.00, 0, 0,
    24.50,
    true, true,
    $d$Simulazione dell'esame Certified Entry-Level Python Programmer. 35 domande a risposta multipla estratte casualmente da un pool, su fondamenti del linguaggio, tipi di dato, strutture di controllo, funzioni e gestione degli errori.$d$
  );

  -- ═══════════════════════════════════════════════════════════════════════════
  -- ASSESSMENT TEMPLATE → SUBJECTS (23 links)
  -- ═══════════════════════════════════════════════════════════════════════════
  INSERT INTO assessment_template_subject (assessment_template_id, subject_id) VALUES
    (v_at_id, v_s01), (v_at_id, v_s02), (v_at_id, v_s03), (v_at_id, v_s04),
    (v_at_id, v_s05), (v_at_id, v_s06), (v_at_id, v_s07), (v_at_id, v_s08),
    (v_at_id, v_s09), (v_at_id, v_s10), (v_at_id, v_s11), (v_at_id, v_s12),
    (v_at_id, v_s13), (v_at_id, v_s14), (v_at_id, v_s15), (v_at_id, v_s16),
    (v_at_id, v_s17), (v_at_id, v_s18), (v_at_id, v_s19), (v_at_id, v_s20),
    (v_at_id, v_s21), (v_at_id, v_s22), (v_at_id, v_s23);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- ASSESSMENT TEMPLATE → TOPICS (1 link)
  -- ═══════════════════════════════════════════════════════════════════════════
  INSERT INTO assessment_template_topic (assessment_template_id, topic_id)
  VALUES (v_at_id, v_t_fond);

  -- ═══════════════════════════════════════════════════════════════════════════
  -- QUESTIONS (67) + OPTIONS (268) + SUBJECT LINKS (67)
  -- ═══════════════════════════════════════════════════════════════════════════

  -- ─── Tipi di dato ────────────────────────────────────────────────────────

  -- Q1 | Tipi di dato | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Python distingue tra variabili di tipo intero (Integer) e variabili in virgola mobile (Float)?$t$,
    $c$# Osserva il comportamento di Python con i tipi numerici
x = 10
y = 10.0
print(type(x))  # <class 'int'>
print(type(y))  # <class 'float'>$c$,
    $e$Python mantiene una distinzione chiara tra int e float. `10` è un intero, `10.0` è un float, e `type()` lo conferma.$e$,
    1, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, Python distingue sempre tra int e float$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, Python tratta tutti i numeri allo stesso modo$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Solo se si usa la funzione type()$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Solo nelle versioni Python 3.10+$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s01, 1.00);

  -- Q3 | Tipi di dato | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quando si assegna un valore booleano a una variabile in Python, il valore deve iniziare con la lettera maiuscola?$t$,
    $c$# Test con valori booleani
is_active = True
is_deleted = False
print(is_active, type(is_active))
print(is_deleted, type(is_deleted))$c$,
    $e$In Python, i letterali booleani sono `True` e `False` con la T e la F maiuscole. Scrivere `true` o `false` genera un `NameError`.$e$,
    2, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, True e False devono avere la prima lettera maiuscola$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, si può scrivere true e false in minuscolo$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Entrambe le forme sono accettate$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$I booleani si rappresentano solo con 1 e 0$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s01, 1.00);

  -- ─── Conversioni di tipo ─────────────────────────────────────────────────

  -- Q4 | Conversioni di tipo | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il codice per convertire i tipi di dato?$t$,
    $c$serialnumber = ______(55555)
amount = ______(44)
message = "Ordine " + serialnumber + " totale: " + str(amount)
print(message)$c$,
    $e$`serialnumber` viene concatenato con stringhe, quindi serve `str()`. `amount` deve essere un float (importo monetario), quindi serve `float()`. La concatenazione finale usa `str(amount)`.$e$,
    3, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$int(), str()$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$str(), float()$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$float(), str()$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$str(), int()$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s02, 1.00);

  -- Q5 | Conversioni di tipo | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale funzione converte correttamente un valore float in un intero troncando la parte decimale?$t$,
    $c$price = 19.95
whole_price = ______(price)
print(whole_price)  # Output: 19
print(type(whole_price))  # <class 'int'>$c$,
    $e$La funzione `int()` tronca la parte decimale di un float, convertendolo in intero. `round()` arrotonderebbe, `floor()` richiederebbe `math.floor()`, e `str()` convertirebbe in stringa.$e$,
    4, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$round$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$int$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$floor$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$str$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s02, 1.00);

  -- ─── Variabili e assegnazione ────────────────────────────────────────────

  -- Q2 | Variabili e assegnazione | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$In Python, quando si dichiara una variabile è necessario specificare il tipo di dato?$t$,
    $c$# Python usa la tipizzazione dinamica
age = 25
name = "Mario"
price = 19.99
print(age, name, price)$c$,
    $e$Python è un linguaggio a tipizzazione dinamica: il tipo viene dedotto automaticamente dal valore assegnato, senza dichiarazione esplicita.$e$,
    5, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, è obbligatorio specificare il tipo$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, Python usa il tipo dinamico e lo deduce automaticamente$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Solo per i tipi numerici$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$È obbligatorio solo nelle funzioni$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s03, 1.00);

  -- Q12 | Variabili e assegnazione | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Dopo l'esecuzione del codice, quali sono i valori delle variabili?$t$,
    $c$a = 5
b = 3
c = b
b = a
print(f"a={a}, b={b}, c={c}")$c$,
    $e$`c = b` assegna il valore corrente di `b` (3) a `c`. Poi `b = a` assegna il valore di `a` (5) a `b`. La variabile `c` resta 3 perché la riassegnazione di `b` non la influenza.$e$,
    6, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$a=5, b=5, c=5$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$a=5, b=5, c=3$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$a=5, b=3, c=3$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$a=5, b=3, c=5$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s03, 1.00);

  -- ─── Operatori aritmetici ────────────────────────────────────────────────

  -- Q6 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è il valore di z dopo l'esecuzione di questa espressione?$t$,
    $c$a = 10
b = 5
c = 2
z = a + b * c - a / b
print(z)$c$,
    $e$Per la precedenza degli operatori: `b * c = 10`, `a / b = 2.0`, quindi `z = 10 + 10 - 2.0 = 18.0`. Moltiplicazione e divisione hanno priorità su addizione e sottrazione.$e$,
    7, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$z = 22.0$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$z = 18.0$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$z = 8.0$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$z = 20.0$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q7 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Valuta l'espressione: a = 100 - 70 / 7. Il risultato è a == 90?$t$,
    $c$a = 100 - 70 / 7
print(a)
print(a == 90)$c$,
    $e$La divisione ha precedenza sulla sottrazione: `70 / 7 = 10.0`, quindi `a = 100 - 10.0 = 90.0`. Il confronto `a == 90` restituisce `True`.$e$,
    8, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, a vale 90.0$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, a vale 4.285...$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, a vale 100.0$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$No, a vale 80.0$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q8 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Valuta l'espressione: b = (35 % 15) // 2. Il risultato è b == 2.5?$t$,
    $c$b = (35 % 15) // 2
print(b)
print(b == 2.5)$c$,
    $e$`35 % 15 = 5` (resto della divisione), poi `5 // 2 = 2` (divisione intera). L'operatore `//` restituisce un intero troncato, non un float.$e$,
    9, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, b vale 2.5$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, b vale 2$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, b vale 5$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$No, b vale 0$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q9 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Valuta l'espressione: c = -3 ** 2. Il risultato è c == -9?$t$,
    $c$c = -3 ** 2
print(c)
print(c == -9)$c$,
    $e$L'operatore `**` ha precedenza maggiore del segno negativo `-`. Quindi Python valuta prima `3 ** 2 = 9`, poi applica il negativo: `-9`. Per ottenere 9, bisognerebbe scrivere `(-3) ** 2`.$e$,
    10, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, c vale -9$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, c vale 9$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, c vale -6$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$No, genera un errore$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q11 | Operatori aritmetici | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Dati a = 10 e b = 3, quale espressione restituisce il risultato della divisione intera?$t$,
    $c$a = 10
b = 3
print(a + b)    # 13
print(a - b)    # 7
print(a / b)    # 3.333...
print(a // b)   # ?
print(a % b)    # 1$c$,
    $e$L'operatore `//` esegue la divisione intera (floor division), troncando il risultato. `10 // 3 = 3`. L'operatore `/` restituisce un float: `10 / 3 = 3.333...`.$e$,
    11, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$a / b restituisce 3$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$a // b restituisce 3$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$a % b restituisce 3$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$a // b restituisce 3.333$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q15 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$In un'espressione complessa, quale operazione viene eseguita per prima secondo la precedenza degli operatori Python?$t$,
    $c$result = 5 + 3 * 2 ** 2 - 8 / 4
print(result)$c$,
    $e$La precedenza degli operatori in Python è: `**` (esponente) > `*`, `/`, `//`, `%` > `+`, `-`. Quindi `2 ** 2 = 4` viene valutato per primo, poi `3 * 4 = 12` e `8 / 4 = 2.0`, infine `5 + 12 - 2.0 = 15.0`.$e$,
    12, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$L'addizione 5 + 3$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$La moltiplicazione 3 * 2$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$L'esponente 2 ** 2$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$La divisione 8 / 4$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q16 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale espressione rappresenta correttamente l'ordine delle operazioni per calcolare il costo totale?$t$,
    $c$carLoan = 300
licenseFee = 50
intRate = 0.05
# Calcola: (carLoan + licenseFee) * intRate
total = ?
print(total)  # 17.5$c$,
    $e$Le parentesi forzano l'addizione prima della moltiplicazione: `(300 + 50) * 0.05 = 350 * 0.05 = 17.5`. Senza parentesi, la moltiplicazione avrebbe precedenza.$e$,
    13, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$total = carLoan + licenseFee * intRate$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$total = carLoan * intRate + licenseFee$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$total = intRate * carLoan + licenseFee$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$total = (carLoan + licenseFee) * intRate$o$, true, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q17 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Valuta le espressioni con gli operatori di assegnazione composta. Dopo l'esecuzione, c == 3?$t$,
    $c$a = 5
b = 2
c = 3
a *= b   # a = a * b
b *= c   # b = b * c
a //= b  # a = a // b
print(f"a={a}, b={b}, c={c}")$c$,
    $e$`c` non viene mai riassegnata, quindi resta 3. `a *= b` → `a = 10`, `b *= c` → `b = 6`, `a //= b` → `a = 10 // 6 = 1`.$e$,
    14, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, c vale 3 e non è stata modificata$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, c vale 6$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, c vale 0$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$No, c vale 1$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q18 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Dopo le operazioni di assegnazione composta, qual è il valore di b?$t$,
    $c$a = 5
b = 2
c = 3
a *= b   # a = 5 * 2 = 10
b *= c   # b = 2 * 3 = ?
a //= b  # a = 10 // b
print(b)$c$,
    $e$`b *= c` equivale a `b = b * c = 2 * 3 = 6`. L'operatore `*=` moltiplica e riassegna.$e$,
    15, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$b = 2$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$b = 6$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$b = 10$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$b = 3$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- Q19 | Operatori aritmetici | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Dopo tutte le operazioni, qual è il valore finale di a?$t$,
    $c$a = 5
b = 2
c = 3
a *= b   # a = 10
b *= c   # b = 6
a //= b  # a = ?
print(a)$c$,
    $e$`a //= b` equivale a `a = a // b = 10 // 6 = 1`. La divisione intera di 10 per 6 dà 1 con resto 4.$e$,
    16, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$a = 2$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$a = 1$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$a = 0$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$a = 10$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s04, 1.00);

  -- ─── Operatori di confronto ──────────────────────────────────────────────

  -- Q10 | Operatori di confronto | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale operatore di confronto completa correttamente le condizioni sui voti?$t$,
    $c$grade = 85
if grade ______ 100:
    print("Voto perfetto")
if grade ______ 60:
    print("Promosso")
if grade ______ 50:
    print("Non sufficiente")$c$,
    $e$Per il voto perfetto serve uguaglianza stretta (`==`), per "promosso" serve maggiore o uguale (`>=`), e per "non sufficiente" serve minore o uguale (`<=`).$e$,
    17, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$!=, >, <$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$==, >=, <=$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$is, >=, <=$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$==, >, <$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s05, 1.00);

  -- Q13 | Operatori di confronto | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale delle seguenti affermazioni è vera dopo l'esecuzione del codice?$t$,
    $c$a = 5
b = 3
c = b
b = a
print(a == b)   # ?
print(b == 3)   # ?$c$,
    $e$Dopo `b = a`, sia `a` che `b` valgono 5, quindi `a == b` è True. `b` ora vale 5, non più 3, quindi `b == 3` è False.$e$,
    18, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$a == b è True e b == 3 è True$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$a == b è True e b == 3 è False$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$a == b è False e b == 3 è True$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$a == b è False e b == 3 è False$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s05, 1.00);

  -- Q14 | Operatori di confronto | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale operatore verifica se una sottostringa è presente all'interno di una stringa?$t$,
    $c$quote = "To be or not to be"
word = "nine"
result = word ______ quote
print(result)  # False$c$,
    $e$L'operatore `in` verifica se una sottostringa è contenuta in una stringa. `"nine" in quote` restituisce False perché "nine" non è presente nella frase.$e$,
    19, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$==$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$is$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$in$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$has$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s05, 1.00);

  -- ─── Operatori logici ────────────────────────────────────────────────────

  -- Q20 | Operatori logici | ADVANCED
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale dei seguenti risultati è corretto per le espressioni booleane?$t$,
    $c$a = 10
b = 7
c = 5
print(a > b and b > c)
print(a >= c and not(b + c > a))
print(a + b * c == 85 or a - b * c == 15)$c$,
    $e$(1) `10>7 and 7>5` → True. (2) `10>=5` è True ma `not(12>10)` è False, quindi False. (3) `10+35=45≠85` e `10-35=-25≠15`, quindi False.$e$,
    20, 'ADVANCED');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$True, True, True$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$True, False, False$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$True, True, False$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$True, False, True$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s06, 1.00);

  -- ─── Strutture condizionali ──────────────────────────────────────────────

  -- Q25 | Strutture condizionali | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del seguente programma di vendite mensili?$t$,
    $c$month_sales = 15000
if month_sales > 10000:
    print("Obiettivo raggiunto!")
    print(f"Bonus: ${month_sales * 0.1:.2f}")
else:
    print("Obiettivo non raggiunto")$c$,
    $e$`15000 > 10000` è True, quindi entrambe le istruzioni nel blocco `if` vengono eseguite. Il bonus è `15000 * 0.1 = 1500.00`.$e$,
    21, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Obiettivo non raggiunto$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Obiettivo raggiunto! seguito da Bonus: $1500.00$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Solo Obiettivo raggiunto!$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Errore di sintassi$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s07, 1.00);

  -- Q26 | Strutture condizionali | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente la funzione che assegna i voti?$t$,
    $c$def get_grade(score):
    if ______:
        grade = "A"
    elif ______:
        grade = "B"
    elif ______:
        grade = "C"
    else:
        ______
    return grade$c$,
    $e$Le soglie dei voti usano `>=` per includere il valore limite (es. 90 merita "A"). Il blocco `else` cattura tutti i punteggi sotto 70 assegnando "F".$e$,
    22, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$score > 90, score > 80, score > 70, grade = "F"$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$score >= 90, score >= 80, score >= 70, grade = "F"$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$score == 90, score == 80, score == 70, grade = "F"$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$score >= 90, score >= 80, score >= 70, grade = "D"$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s07, 1.00);

  -- Q27 | Strutture condizionali | ADVANCED
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del seguente programma con if annidati?$t$,
    $c$sales = 150000
region = "Nord"
season = "Natale"
if sales > 100000:
    if region == "Nord" and season == "Natale":
        print("Bonus premium: 15%")
    elif region == "Nord":
        print("Bonus standard: 10%")
    else:
        print("Bonus base: 5%")$c$,
    $e$`sales > 100000` è True, poi `region == "Nord" and season == "Natale"` è True, quindi viene stampato il bonus premium del 15%.$e$,
    23, 'ADVANCED');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Bonus base: 5%$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Bonus standard: 10%$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Bonus premium: 15%$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$Nessun output$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s07, 1.00);

  -- Q67 | Strutture condizionali | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale modifica corregge l'errore di sintassi nel seguente codice?$t$,
    $c$# Codice con errore:
score = 85
if score >= 90
    print("Eccellente")$c$,
    $e$In Python, i due punti `:` sono obbligatori alla fine delle istruzioni `if`, `elif`, `else`, `for`, `while`, `def`, `class`, ecc. Senza i due punti si genera un `SyntaxError`.$e$,
    24, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Aggiungere le parentesi: if (score >= 90)$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Aggiungere i due punti: if score >= 90:$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Aggiungere il punto e virgola: if score >= 90;$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Aggiungere le parentesi graffe: if score >= 90 { }$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s07, 1.00);

  -- ─── Ciclo for ───────────────────────────────────────────────────────────

  -- Q29 | Ciclo for | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output di questo ciclo for con range?$t$,
    $c$for week in range(1, 6):
    print(f"Settimana {week}")$c$,
    $e$`range(1, 6)` genera i numeri da 1 a 5 (estremo superiore escluso). Quindi il ciclo stampa da "Settimana 1" a "Settimana 5".$e$,
    25, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Stampa "Settimana 1" fino a "Settimana 5"$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Stampa "Settimana 1" fino a "Settimana 6"$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Stampa "Settimana 0" fino a "Settimana 5"$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Stampa "Settimana 0" fino a "Settimana 6"$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s08, 1.00);

  -- Q31 | Ciclo for | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del ciclo for con break?$t$,
    $c$cities = ["Roma", "Milano", "Napoli", "Torino", "Firenze"]
for city in cities:
    if city == "Napoli":
        break
    print(city)$c$,
    $e$Il ciclo stampa ogni città finché non incontra "Napoli". Quando `city == "Napoli"`, `break` interrompe il ciclo prima del `print`, quindi vengono stampate solo "Roma" e "Milano".$e$,
    26, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Roma, Milano, Napoli, Torino, Firenze$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Roma, Milano$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Roma, Milano, Napoli$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Napoli$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s08, 1.00);

  -- Q33 | Ciclo for | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il ciclo for con continue?$t$,
    $c$______ day in range(1, ______):
    if day == 15 or day == 30:
        ______
    print(f"Giorno lavorativo: {day}")$c$,
    $e$`for` itera sui giorni, `range(1, 31)` copre i giorni da 1 a 30, e `continue` salta la stampa per i giorni 15 e 30 passando direttamente all'iterazione successiva.$e$,
    27, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$for, 31, continue$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$while, 31, break$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$for, 30, pass$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$for, 32, stop$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s08, 1.00);

  -- Q34 | Ciclo for | ADVANCED
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del seguente codice con cicli for annidati?$t$,
    $c$days = 7
students = 10
total = days * students
print(f"Presenze totali da registrare: {total}")
for day in range(1, 3):
    for student in range(1, 4):
        print(f"G{day}-S{student}", end=" ")
    print()$c$,
    $e$`7 * 10 = 70` presenze totali. I cicli annidati stampano le combinazioni per i giorni 1-2 e studenti 1-3: `G1-S1 G1-S2 G1-S3` (prima riga) e `G2-S1 G2-S2 G2-S3` (seconda riga).$e$,
    28, 'ADVANCED');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Stampa Presenze totali da registrare: 70 e poi 6 combinazioni su 2 righe$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Stampa Presenze totali da registrare: 17 e poi 70 combinazioni$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Stampa solo Presenze totali da registrare: 70$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Genera un errore per i cicli annidati$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s08, 1.00);

  -- ─── Ciclo while ─────────────────────────────────────────────────────────

  -- Q28 | Ciclo while | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il ciclo while con condizione di uscita?$t$,
    $c$scheduledEvent = 1
______  scheduledEvent <= 5:
    if scheduledEvent == 3:
        ______
    print(f"Evento {scheduledEvent}")
    ______$c$,
    $e$`while` crea il ciclo condizionale, `break` interrompe il ciclo quando l'evento è 3, e `scheduledEvent += 1` incrementa il contatore. Python non ha `++`.$e$,
    29, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$for, continue, scheduledEvent += 1$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$while, break, scheduledEvent += 1$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$while, stop, scheduledEvent++$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$loop, break, scheduledEvent += 1$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s09, 1.00);

  -- Q30 | Ciclo while | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$A cosa serve la keyword pass in Python?$t$,
    $c$for i in range(5):
    if i == 3:
        pass  # TODO: gestire caso speciale
    print(i)$c$,
    $e$`pass` è un'istruzione nulla che non fa niente. Si usa come segnaposto quando la sintassi richiede un blocco di codice ma non si vuole ancora implementare la logica.$e$,
    30, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Termina il ciclo immediatamente$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Serve come segnaposto e non esegue nessuna operazione$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Salta all'iterazione successiva del ciclo$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Stampa il valore corrente$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s09, 1.00);

  -- Q32 | Ciclo while | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Cosa succede se l'utente inserisce una stringa vuota due volte e poi "Mario"?$t$,
    $c$user_input = ""
while user_input == "":
    user_input = input("Inserisci il tuo nome: ")
    if user_input == "":
        print("Il campo non può essere vuoto!")
print(f"Benvenuto, {user_input}!")$c$,
    $e$Il ciclo while continua finché `user_input` è vuoto. Ad ogni stringa vuota stampa l'avviso. Quando l'utente inserisce "Mario", la condizione diventa False e il ciclo termina.$e$,
    31, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Stampa "Benvenuto, !" e termina$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Mostra il messaggio di errore due volte, poi stampa "Benvenuto, Mario!"$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Genera un errore dopo il primo tentativo vuoto$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Il ciclo non termina mai$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s09, 1.00);

  -- ─── Stringhe ────────────────────────────────────────────────────────────

  -- Q21 | Stringhe | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale notazione di slicing inverte correttamente una stringa in Python?$t$,
    $c$greeting = "Hello World"
reversed_greeting = greeting[______]
print(reversed_greeting)  # "dlroW olleH"$c$,
    $e$La notazione `[::-1]` usa lo step negativo per attraversare la stringa dall'ultimo al primo carattere, invertendola completamente.$e$,
    32, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$[-1:0]$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$[::-1]$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$[0:-1]$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$[-1:]$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s10, 1.00);

  -- Q24 | Stringhe | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale carattere si usa per continuare un'istruzione Python sulla riga successiva?$t$,
    $c$total = 100 + 200 + 300 ______
        400 + 500
print(total)$c$,
    $e$Il backslash `\` alla fine di una riga indica che l'istruzione continua sulla riga successiva. È utile per mantenere il codice leggibile quando le righe sono troppo lunghe.$e$,
    33, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$&$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$_$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$\$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$>>$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s10, 1.00);

  -- ─── Formattazione output ────────────────────────────────────────────────

  -- Q22 | Formattazione output | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale prefisso si usa in Python per creare una f-string (stringa formattata)?$t$,
    $c$name = "Alice"
age = 30
message = ______"Ciao {name}, hai {age} anni"
print(message)  # "Ciao Alice, hai 30 anni"$c$,
    $e$Il prefisso `f` prima delle virgolette crea una f-string, che permette di inserire espressioni Python tra parentesi graffe `{}` direttamente nella stringa.$e$,
    34, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$r$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$b$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$f$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$s$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s11, 1.00);

  -- ─── Liste ───────────────────────────────────────────────────────────────

  -- Q35 | Liste | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output corretto dell'iterazione su una tupla?$t$,
    $c$products = ("Laptop", "Mouse", "Tastiera")
for product in products:
    print(product)$c$,
    $e$Le tuple in Python sono iterabili. Il ciclo `for product in products` itera su ogni elemento della tupla, stampando "Laptop", "Mouse" e "Tastiera" su righe separate.$e$,
    35, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Stampa ogni prodotto su una riga separata: Laptop, Mouse, Tastiera$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Stampa ("Laptop", "Mouse", "Tastiera")$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Genera un TypeError perché le tuple non sono iterabili$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Stampa gli indici 0, 1, 2$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s12, 1.00);

  -- Q36 | Liste | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del seguente codice che manipola una lista?$t$,
    $c$animals = ["gatto", "cane", "coniglio"]
animals.append("pappagallo")
animals.sort()
print(animals)$c$,
    $e$`append()` aggiunge "pappagallo" alla lista, poi `sort()` ordina alfabeticamente: cane, coniglio, gatto, pappagallo.$e$,
    36, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$['gatto', 'cane', 'coniglio', 'pappagallo']$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$['cane', 'coniglio', 'gatto', 'pappagallo']$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$['pappagallo', 'gatto', 'coniglio', 'cane']$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$['coniglio', 'cane', 'gatto', 'pappagallo']$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s12, 1.00);

  -- Q37 | Liste | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Data la lista di pezzi degli scacchi ordinata, quale istruzione stampa "torre"?$t$,
    $c$pieces = ["alfiere", "cavallo", "pedone", "re", "regina", "torre"]
# La lista è già ordinata alfabeticamente
print(pieces[______])  # Deve stampare "torre"$c$,
    $e$In una lista ordinata alfabeticamente, "torre" è l'ultimo elemento. Con 6 elementi (indici 0-5), `pieces[5]` accede all'ultimo elemento. `pieces[-1]` funzionerebbe ugualmente.$e$,
    37, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$pieces[4]$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$pieces[5]$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$pieces[-2]$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$pieces[6]$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s12, 1.00);

  -- ─── Funzioni built-in ───────────────────────────────────────────────────

  -- Q38 | Funzioni built-in | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quali funzioni built-in restituiscono il valore massimo e minimo di una lista?$t$,
    $c$temperatures = [22, 18, 35, 12, 28]
highest = ______(temperatures)
lowest = ______(temperatures)
print(f"Max: {highest}, Min: {lowest}")$c$,
    $e$`max()` e `min()` sono funzioni built-in di Python che restituiscono rispettivamente il valore massimo e minimo di un iterabile.$e$,
    38, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$maximum, minimum$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$max, min$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$high, low$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$top, bottom$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s15, 1.00);

  -- ─── Definizione funzioni ────────────────────────────────────────────────

  -- Q39 | Definizione funzioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente la definizione della funzione?$t$,
    $c$______ calcSubtotal(amount, salesTaxRate):
    subtotal = amount + (amount * salesTaxRate)
    ______ subtotal

result = calcSubtotal(100, 0.22)
print(result)  # 122.0$c$,
    $e$In Python, `def` dichiara una funzione e `return` restituisce il valore al chiamante. Senza `return`, la funzione restituirebbe `None`.$e$,
    39, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$function, give$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$def, return$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$define, yield$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$func, output$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s13, 1.00);

  -- Q40 | Definizione funzioni | ADVANCED
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è il valore di order1?$t$,
    $c$def calcTotal(member, subtotal, tax, discount):
    if member == "Yes":
        total = subtotal - (subtotal * discount)
        return total
    elif member == "No":
        pass

order1 = calcTotal("No", 500, 0.07, 0)
print(order1)$c$,
    $e$Quando `member == "No"`, il blocco `elif` esegue solo `pass` senza alcun `return`. In Python, una funzione che non esegue `return` restituisce implicitamente `None`.$e$,
    40, 'ADVANCED');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$500$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$535.0$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$0$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$None$o$, true, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s13, 1.00);

  -- Q42 | Definizione funzioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output quando si chiama una funzione con un parametro di default?$t$,
    $c$def area(width, height=12):
    return width * height

print(area(5))
print(area(5, 8))$c$,
    $e$`area(5)` usa il valore di default per height (12): `5 * 12 = 60`. `area(5, 8)` sovrascrive il default: `5 * 8 = 40`. I parametri con default sono opzionali nella chiamata.$e$,
    41, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Errore: manca un argomento$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$60 e 60$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$60 e 40$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$17 e 13$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s13, 1.00);

  -- ─── Chiamata funzioni ───────────────────────────────────────────────────

  -- Q41 | Chiamata funzioni | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale istruzione chiama correttamente la funzione e salva il risultato?$t$,
    $c$def subtotal(amount, tax_rate):
    return amount + (amount * tax_rate)

______ = subtotal(500, 0.07)
print(order_total)  # 535.0$c$,
    $e$Per salvare il risultato di una funzione, basta assegnarlo a una variabile: `order_total = subtotal(500, 0.07)`. La variabile `order_total` riceve il valore restituito dalla funzione.$e$,
    42, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$subtotal(500, 0.07)$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$return subtotal(500, 0.07)$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$order_total$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$def order_total$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s14, 1.00);

  -- ─── Lettura e scrittura file ────────────────────────────────────────────

  -- Q44 | Lettura e scrittura file | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il codice per aprire e leggere un file?$t$,
    $c$file = open("prodotti.txt", "______")
content = file.______()
print(content)
file.close()$c$,
    $e$La modalità `"r"` apre il file in sola lettura (read-only). Il metodo `read()` legge l'intero contenuto del file come stringa.$e$,
    43, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$"w", write$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$"r", read$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$"a", append$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$"x", scan$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s16, 1.00);

  -- Q46 | Lettura e scrittura file | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Usando il costrutto with, perché non è necessario chiamare close()?$t$,
    $c$with open('log.txt', 'w') as f:
    f.write("Operazione completata")
# Il file è già chiuso qui$c$,
    $e$Il costrutto `with` (context manager) garantisce che il file venga chiuso automaticamente quando l'esecuzione esce dal blocco `with`, anche in caso di eccezioni.$e$,
    44, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Il costrutto with chiude automaticamente il file all'uscita del blocco$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Python chiude tutti i file alla fine del programma$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Il metodo write() chiude il file dopo la scrittura$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$La modalità 'w' chiude il file automaticamente$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s16, 1.00);

  -- ─── Gestione eccezioni ──────────────────────────────────────────────────

  -- Q48 | Gestione eccezioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente la struttura try/except/else/finally?$t$,
    $c$______:
    result = 10 / 2
______ ZeroDivisionError:
    print("Divisione per zero!")
______:
    print(f"Risultato: {result}")
______:
    print("Operazione terminata")$c$,
    $e$Python usa `try/except/else/finally`: `try` contiene il codice da provare, `except` gestisce le eccezioni, `else` si esegue se non ci sono eccezioni, `finally` si esegue sempre.$e$,
    45, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$try, except, else, finally$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$begin, catch, then, end$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$try, catch, else, finally$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$try, except, then, cleanup$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- Q49 | Gestione eccezioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa il codice per lanciare un'eccezione e gestirla?$t$,
    $c$def validate_age(age):
    if age < 0:
        ______ Exception("L'età non può essere negativa")
    return age

try:
    validate_age(-5)
except Exception as e:
    print(f"Errore: {e}")
______:
    print("Validazione completata")$c$,
    $e$`raise` lancia un'eccezione in Python. `finally` esegue il codice di pulizia indipendentemente dal fatto che si sia verificata un'eccezione o meno.$e$,
    46, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$throw, end$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$raise, finally$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$raise, else$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$throw, finally$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- Q50 | Gestione eccezioni | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il blocco try/except base?$t$,
    $c$______:
    number = int(input("Inserisci un numero: "))
    print(f"Hai inserito: {number}")
______ ValueError:
    print("Input non valido!")$c$,
    $e$`try` delimita il blocco di codice che potrebbe generare eccezioni, `except ValueError` cattura specificamente l'eccezione di valore non valido (es. input non numerico per `int()`).$e$,
    47, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$try, except$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$begin, catch$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$do, handle$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$check, error$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- Q51 | Gestione eccezioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quali affermazioni sui blocchi try/except/else/finally sono corrette? Il blocco else viene eseguito solo se il blocco try non genera eccezioni?$t$,
    $c$try:
    x = int("42")
except ValueError:
    print("Errore di conversione")
else:
    print("Conversione riuscita")
finally:
    print("Fine operazione")$c$,
    $e$Il blocco `else` si esegue solo se nessuna eccezione viene sollevata nel `try`. Il blocco `finally` si esegue sempre, indipendentemente dalle eccezioni.$e$,
    48, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, else viene eseguito solo quando try ha successo$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, else viene eseguito sempre$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, else viene eseguito solo quando c'è un'eccezione$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$else e finally si escludono a vicenda$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- Q52 | Gestione eccezioni | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Il blocco finally viene eseguito anche se si verifica un'eccezione non gestita?$t$,
    $c$try:
    result = 10 / 0
except TypeError:
    print("Errore di tipo")
finally:
    print("Pulizia risorse")
# ZeroDivisionError non è gestita da except TypeError$c$,
    $e$Il blocco `finally` è garantito essere eseguito in ogni caso: successo, eccezione gestita o eccezione non gestita. È il posto ideale per operazioni di pulizia.$e$,
    49, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, finally viene eseguito sempre, anche con eccezioni non gestite$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, finally viene saltato se l'eccezione non è gestita$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$No, il programma termina prima di finally$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Solo se si usa except Exception generico$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- Q53 | Gestione eccezioni | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Un IndexError in Python è classificato come quale tipo di errore?$t$,
    $c$fruits = ["mela", "banana", "arancia"]
try:
    print(fruits[5])
except IndexError as e:
    print(f"Errore: {e}")$c$,
    $e$`IndexError` è un errore a runtime: il codice è sintatticamente corretto ma fallisce durante l'esecuzione quando si tenta di accedere a un indice inesistente nella lista.$e$,
    50, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Errore di sintassi (Syntax Error)$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Errore a runtime (Runtime Error)$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Errore logico (Logic Error)$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Errore di compilazione (Compile Error)$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s17, 1.00);

  -- ─── Modulo math ─────────────────────────────────────────────────────────

  -- Q54 | Modulo math | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente l'uso del modulo math?$t$,
    $c$______ math

rounded_up = math.______(4.2)
rounded_down = math.______(4.8)
truncated = ______(4.9)
print(rounded_up, rounded_down, truncated)  # 5 4 4$c$,
    $e$`import math` importa il modulo. `math.ceil()` arrotonda per eccesso, `math.floor()` per difetto, e `int()` tronca semplicemente la parte decimale.$e$,
    51, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$import, ceil, floor, int$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$include, round_up, round_down, int$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$import, ceiling, flooring, truncate$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$using, ceil, floor, round$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s18, 1.00);

  -- Q56 | Modulo math | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale modulo standard contiene il metodo ceil() per arrotondare per eccesso?$t$,
    $c$import ______
result = ______.ceil(3.2)
print(result)  # 4$c$,
    $e$Il metodo `ceil()` appartiene al modulo `math`. I moduli standard hanno scopi diversi: `os` per il sistema operativo, `sys` per l'interprete, `io` per le operazioni di I/O.$e$,
    52, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$os$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$sys$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$math$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$io$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s18, 1.00);

  -- Q58 | Modulo math | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Qual è l'output del seguente programma che calcola l'area di un cerchio?$t$,
    $c$import math

pi = math.pi
radius = float(input("Raggio: "))  # L'utente inserisce: 5
area = pi * radius ** 2
print(f"Area: {area:.2f}")$c$,
    $e$`math.pi ≈ 3.14159`, `radius = 5.0`, `area = 3.14159 * 25 = 78.5398...`. Con `:.2f` viene formattato a 2 decimali: `78.54`.$e$,
    53, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Area: 78.54$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Area: 31.42$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Area: 157.08$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Area: 25.00$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s18, 1.00);

  -- ─── Modulo random ───────────────────────────────────────────────────────

  -- Q55 | Modulo random | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente l'uso del modulo random?$t$,
    $c$import random

colors = ["rosso", "blu", "verde", "giallo"]
picked = random.______(colors)
random.______(colors)
sample = random.______(colors, 2)
print(picked, colors, sample)$c$,
    $e$`random.choice()` seleziona un elemento casuale, `random.shuffle()` mescola la lista in-place, e `random.sample()` restituisce un campione di n elementi senza ripetizione.$e$,
    54, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$choice, shuffle, sample$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$pick, mix, select$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$choose, randomize, take$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$select, sort, slice$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s19, 1.00);

  -- Q61 | Modulo random | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa il gioco di indovinare il numero?$t$,
    $c$______ random ______ randint

secret = ______(1, 10)
guess = int(input("Indovina (1-10): "))
if guess == secret:
    print("Hai indovinato!")
else:
    print(f"Era {secret}")$c$,
    $e$`from random import randint` importa direttamente la funzione `randint`, permettendo di usarla senza il prefisso del modulo. `randint(1, 10)` genera un intero casuale tra 1 e 10 inclusi.$e$,
    55, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$from, import, randint$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$import, use, random$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$include, get, randint$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$from, import, random$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s19, 1.00);

  -- ─── Modulo datetime ─────────────────────────────────────────────────────

  -- Q59 | Modulo datetime | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale metodo del modulo datetime restituisce la data e ora corrente?$t$,
    $c$import datetime

now = datetime.datetime.______()
print(now)$c$,
    $e$Sia `datetime.datetime.today()` che `datetime.datetime.now()` restituiscono la data e ora corrente. `now()` accetta un parametro opzionale per il fuso orario.$e$,
    56, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$today() o now() — entrambi sono validi$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$Solo current()$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Solo getTime()$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Solo timestamp()$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s20, 1.00);

  -- Q60 | Modulo datetime | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente l'uso del modulo datetime per la formattazione?$t$,
    $c$______ datetime

now = datetime.datetime.______()
formatted = now.strftime("______")
day_name = now.______
print(formatted)  # Es: "06/24/2026"$c$,
    $e$`import datetime` importa il modulo, `now()` ottiene la data corrente, `%m/%d/%Y` è il formato mese/giorno/anno, e `weekday` restituisce il giorno della settimana (0=lunedì).$e$,
    57, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$import, now, %m/%d/%Y, weekday$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$include, today, dd/mm/yyyy, day$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$import, current, %D/%M/%Y, dayOfWeek$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$using, now, MM/DD/YYYY, weekday$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s20, 1.00);

  -- ─── Modulo os e sys ─────────────────────────────────────────────────────

  -- Q45 | Modulo os e sys | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa il codice per verificare l'esistenza del file e leggerne una riga?$t$,
    $c$import os
if os.path.______(workFile):
    f = open(workFile, "r")
    line = f.______()
    print(line)
    f.close()$c$,
    $e$`os.path.isfile()` verifica se il percorso è un file esistente. `readline()` legge una singola riga dal file. `exists()` controlla anche le directory, `isfile()` è più specifico.$e$,
    58, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$exists, read$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$isfile, readline$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$isdir, readlines$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$check, getline$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s21, 1.00);

  -- Q47 | Modulo os e sys | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Cosa rappresenta sys.argv[0] quando si esegue uno script Python dalla riga di comando?$t$,
    $c$import sys
print(sys.argv[0])
# Esecuzione: python script.py arg1 arg2$c$,
    $e$`sys.argv` è una lista degli argomenti della riga di comando. `sys.argv[0]` è sempre il nome dello script, `sys.argv[1]` è il primo argomento, ecc.$e$,
    59, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Il primo argomento passato (arg1)$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$Il nome del file script (script.py)$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$Il percorso dell'interprete Python$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Il numero totale di argomenti$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s21, 1.00);

  -- Q57 | Modulo os e sys | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale associazione tra modulo e metodo è corretta?$t$,
    $c$import io, math, os, sys

# Quale modulo contiene quale metodo?
# open() → ?    ceil() → ?    mkdir() → ?    exit() → ?$c$,
    $e$Ogni modulo ha il suo scopo: `io` gestisce gli stream (open), `math` le funzioni matematiche (ceil), `os` il sistema operativo (mkdir), `sys` l'interprete Python (exit).$e$,
    60, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$io→open, math→ceil, os→mkdir, sys→exit$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$os→open, math→ceil, io→mkdir, sys→exit$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$sys→open, io→ceil, os→mkdir, math→exit$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$io→open, sys→ceil, math→mkdir, os→exit$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s21, 1.00);

  -- ─── Unit testing ────────────────────────────────────────────────────────

  -- Q62 | Unit testing | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente lo unit test?$t$,
    $c$import ______

class TestMath(______.TestCase):
    def test_addition(self):
        self.______(2 + 2, 4)

if ______ == "__main__":
    unittest.main()$c$,
    $e$Il modulo `unittest` è lo standard per i test in Python. Si crea una classe che eredita da `unittest.TestCase`, si usa `assertEqual` per le asserzioni, e `__name__` per l'esecuzione diretta.$e$,
    61, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$unittest, unittest, assertEqual, __name__$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$testing, testing, assertSame, __file__$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$pytest, pytest, assertEquals, __name__$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$unittest, unittest, assertMatch, __module__$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s22, 1.00);

  -- Q63 | Unit testing | ADVANCED
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale metodo di asserzione verifica che due variabili puntino allo stesso oggetto in memoria?$t$,
    $c$import unittest

class TestIdentity(unittest.TestCase):
    def test_same_object(self):
        a = [1, 2, 3]
        b = a
        self.______(a, b)

    def test_different_objects(self):
        a = [1, 2, 3]
        b = [1, 2, 3]
        self.assertIsNot(a, b)$c$,
    $e$`assertIs(a, b)` verifica che `a` e `b` siano lo stesso oggetto in memoria (equivalente a `a is b`). `assertEqual` verifica solo l'uguaglianza dei valori.$e$,
    62, 'ADVANCED');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$assertEqual$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$assertTrue$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$assertIn$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$assertIs$o$, true, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s22, 1.00);

  -- ─── Documentazione ──────────────────────────────────────────────────────

  -- Q23 | Documentazione | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale carattere si usa in Python per inserire un commento su una singola riga?$t$,
    $c$______ Questo è un commento
price = 29.99  ______ prezzo del prodotto
print(price)$c$,
    $e$In Python il carattere `#` indica un commento. Tutto ciò che segue `#` sulla stessa riga viene ignorato dall'interprete.$e$,
    63, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$//$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$/* */$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$#$o$, true, false, 3),
    (gen_random_uuid(), v_q_id, $o$--$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s23, 1.00);

  -- Q43 | Documentazione | INTERMEDIATE
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale opzione completa correttamente il codice per definire e accedere a una docstring?$t$,
    $c$def area(width, height):
    ______ Calcola l'area di un rettangolo. ______
    return width * height

print(area.______)$c$,
    $e$Le docstring in Python sono racchiuse tra triple virgolette `"""..."""` e si accedono tramite l'attributo `__doc__`. Sono la documentazione standard delle funzioni.$e$,
    64, 'INTERMEDIATE');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$#, #, doc$o$, false, false, 1),
    (gen_random_uuid(), v_q_id, $o$""", """, __doc__$o$, true, false, 2),
    (gen_random_uuid(), v_q_id, $o$''', ''', help$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$//, //, __doc__$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s23, 1.00);

  -- Q64 | Documentazione | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Quale delle seguenti affermazioni su pydoc in Python è vera? Pydoc genera automaticamente documentazione dal codice sorgente?$t$,
    $c$# Per generare documentazione:
# python -m pydoc math
# python -m pydoc -w math  (genera HTML)$c$,
    $e$Pydoc è uno strumento integrato che genera automaticamente documentazione estraendo le docstring dal codice sorgente Python. Funziona con qualsiasi modulo.$e$,
    65, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, pydoc genera documentazione leggendo le docstring del codice$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, la documentazione deve essere scritta separatamente$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Pydoc funziona solo con moduli della libreria standard$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Pydoc richiede un file di configurazione separato$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s23, 1.00);

  -- Q65 | Documentazione | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$Pydoc può generare documentazione in formato HTML?$t$,
    $c$# Generazione documentazione HTML
# python -m pydoc -w mymodule
# Questo crea mymodule.html nella directory corrente$c$,
    $e$Il comando `python -m pydoc -w modulo` genera un file HTML con la documentazione del modulo specificato. Il flag `-w` sta per "write" e crea il file nella directory corrente.$e$,
    66, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, con il flag -w genera file HTML$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, genera solo testo in console$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$Solo con plugin aggiuntivi$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Solo per i moduli built-in$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s23, 1.00);

  -- Q66 | Documentazione | BEGINNER
  v_q_id := gen_random_uuid();
  INSERT INTO question_template (id, assessment_template_id, type, text, code, explanation, position, difficulty)
  VALUES (v_q_id, v_at_id, 'multiple',
    $t$La sintassi di pydoc nella shell Python è help(modulo)?$t$,
    $c$# Nella shell interattiva Python:
import math
help(math)
# Oppure dalla riga di comando:
# python -m pydoc math$c$,
    $e$La funzione built-in `help()` usa pydoc internamente per generare e mostrare la documentazione interattiva di moduli, classi, funzioni e metodi.$e$,
    67, 'BEGINNER');
  INSERT INTO option_template (id, question_template_id, text, is_correct, is_fallback, position) VALUES
    (gen_random_uuid(), v_q_id, $o$Sì, help() usa pydoc internamente per mostrare la documentazione$o$, true, false, 1),
    (gen_random_uuid(), v_q_id, $o$No, help() e pydoc sono completamente indipendenti$o$, false, false, 2),
    (gen_random_uuid(), v_q_id, $o$help() funziona solo con le stringhe$o$, false, false, 3),
    (gen_random_uuid(), v_q_id, $o$Bisogna importare pydoc prima di usare help()$o$, false, false, 4);
  INSERT INTO question_template_subject (question_template_id, subject_id, weight) VALUES (v_q_id, v_s23, 1.00);

  RAISE NOTICE 'Python Certification Exam Practice seeded: 23 subjects, 3 topics, 67 questions, 268 options';
END $$;
