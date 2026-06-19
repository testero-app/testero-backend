-- ============================================================
-- Testero — Seed Topics, Granular Subjects, Difficulty & Explanations
-- Run manually on Supabase Cloud SQL Editor AFTER the certification seeds
-- ============================================================
-- This script:
--   1. Creates topics (Python, JavaScript, Java)
--   2. Creates granular subjects (chapters) for each topic
--   3. Links subjects to topics via topic_subject
--   4. Sets difficulty and explanation for every question
--   5. Remaps question_subject to the granular subjects
-- ============================================================

DO $$
DECLARE
  -- Python
  v_python_test_id     uuid;
  v_python_topic_id    uuid;
  v_subj_fondamenti    uuid;
  v_subj_operatori     uuid;
  v_subj_stringhe      uuid;
  v_subj_cicli         uuid;
  v_subj_funzioni      uuid;
  v_subj_errori        uuid;
  -- JavaScript
  v_js1_test_id        uuid;
  v_js2_test_id        uuid;
  v_js_topic_id        uuid;
  v_subj_js_fondamenti uuid;
  v_subj_js_operatori  uuid;
  v_subj_js_controllo  uuid;
  v_subj_js_cicli      uuid;
  v_subj_js_funzioni   uuid;
  v_subj_js_array      uuid;
  v_subj_js_stringhe   uuid;
  v_subj_js_dom        uuid;
  -- Java
  v_java_test_id       uuid;
  v_java_topic_id      uuid;
  v_subj_ja_fondamenti uuid;
  v_subj_ja_operatori  uuid;
  v_subj_ja_controllo  uuid;
  v_subj_ja_cicli      uuid;
  v_subj_ja_metodi     uuid;
  v_subj_ja_array      uuid;
  v_subj_ja_stringhe   uuid;
  v_subj_ja_oop        uuid;
  v_old_subject_id     uuid;
  v_q_id               uuid;
BEGIN

  -- ================================================================
  -- PYTHON SECTION
  -- ================================================================

  -- ── Resolve test ID ──────────────────────────────────────────
  SELECT id INTO v_python_test_id FROM test WHERE title = 'Python Certification Simulation 1';
  IF v_python_test_id IS NULL THEN
    RAISE NOTICE 'Python test not found, skipping Python section';
  ELSE

  -- ── Topic ───────────────────────────────────────────────────────
  INSERT INTO topic (id, title, abbreviation, description)
  VALUES (gen_random_uuid(), 'Fondamenti Python', 'Py',
    'Variabili, tipi, operatori e controllo di flusso. Le basi del linguaggio.')
  ON CONFLICT DO NOTHING;

  SELECT id INTO v_python_topic_id FROM topic WHERE abbreviation = 'Py';

  -- ── Subjects (granular chapters) ────────────────────────────────
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Fondamenti Python')      ON CONFLICT DO NOTHING;
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Operatori')              ON CONFLICT DO NOTHING;
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Stringhe e formattazione') ON CONFLICT DO NOTHING;
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Cicli e controllo di flusso') ON CONFLICT DO NOTHING;
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Funzioni')               ON CONFLICT DO NOTHING;
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Gestione errori e testing') ON CONFLICT DO NOTHING;

  SELECT id INTO v_subj_fondamenti    FROM subject WHERE label = 'Fondamenti Python';
  SELECT id INTO v_subj_operatori     FROM subject WHERE label = 'Operatori';
  SELECT id INTO v_subj_stringhe      FROM subject WHERE label = 'Stringhe e formattazione';
  SELECT id INTO v_subj_cicli         FROM subject WHERE label = 'Cicli e controllo di flusso';
  SELECT id INTO v_subj_funzioni      FROM subject WHERE label = 'Funzioni';
  SELECT id INTO v_subj_errori        FROM subject WHERE label = 'Gestione errori e testing';

  -- ── Link Topic -> Subjects ──────────────────────────────────────
  INSERT INTO topic_subject (topic_id, subject_id, position) VALUES
    (v_python_topic_id, v_subj_fondamenti, 1),
    (v_python_topic_id, v_subj_operatori,  2),
    (v_python_topic_id, v_subj_stringhe,   3),
    (v_python_topic_id, v_subj_cicli,      4),
    (v_python_topic_id, v_subj_funzioni,   5),
    (v_python_topic_id, v_subj_errori,     6)
  ON CONFLICT DO NOTHING;

  -- ── Difficulty & Explanation per question ────────────────────────

  -- Position 1 — Slicing ([::-1])
  -- Subject: Stringhe e formattazione
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'La sintassi [::-1] inverte una sequenza in Python. Il terzo parametro dello slicing indica il passo: -1 significa che si percorre la stringa dall''ultimo carattere al primo. Le altre opzioni restituiscono solo singoli caratteri o sotto-stringhe, senza invertire.'
  WHERE test_id = v_python_test_id AND position = 1;
  UPDATE question_subject SET subject_id = v_subj_stringhe
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 1);

  -- Position 2 — Type Conversion (int)
  -- Subject: Fondamenti Python
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'La funzione int() tronca la parte decimale senza arrotondare, convertendo 19.95 in 19. round() arrotonderebbe a 20, floor() e ceil() appartengono al modulo math e non sono builtin diretti. La chiave e'' capire la differenza tra troncamento e arrotondamento.'
  WHERE test_id = v_python_test_id AND position = 2;
  UPDATE question_subject SET subject_id = v_subj_fondamenti
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 2);

  -- Position 3 — Operator Precedence (z = ... and ... or ...)
  -- Subject: Operatori
  UPDATE question SET difficulty = 'ADVANCED', explanation = E'In Python la precedenza degli operatori segue quest''ordine: prima le operazioni aritmetiche (* + -), poi i confronti (> <), poi and, poi or, e infine l''assegnamento (=). L''operatore = viene eseguito per ultimo, quindi z riceve il risultato finale dell''intera espressione booleana.'
  WHERE test_id = v_python_test_id AND position = 3;
  UPDATE question_subject SET subject_id = v_subj_operatori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 3);

  -- Position 4 — Membership operator (in)
  -- Subject: Operatori
  UPDATE question SET difficulty = 'BEGINNER', explanation = E'L''operatore in verifica se un elemento e'' presente in una sequenza (stringa, lista, tupla, ecc.). is confronta l''identita'' degli oggetti, mentre has e contains non sono keyword Python valide per questo scopo.'
  WHERE test_id = v_python_test_id AND position = 4;
  UPDATE question_subject SET subject_id = v_subj_operatori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 4);

  -- Position 5 — For loop with range (range(1, 6))
  -- Subject: Cicli e controllo di flusso
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'La funzione range(1, 6) genera i numeri da 1 a 5 inclusi, poiche'' il limite superiore e'' escluso. Per ottenere le settimane da 1 a 5 e'' necessario usare range(1, 6). range(1, 5) si fermerebbe a 4, mentre range(0, 5) partirebbe da 0.'
  WHERE test_id = v_python_test_id AND position = 5;
  UPDATE question_subject SET subject_id = v_subj_cicli
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 5);

  -- Position 6 — Pass keyword
  -- Subject: Cicli e controllo di flusso
  UPDATE question SET difficulty = 'BEGINNER', explanation = E'La keyword pass in Python e'' un''istruzione segnaposto che non esegue alcuna operazione. Viene usata quando la sintassi richiede un''istruzione ma non si vuole ancora scrivere il codice. break interrompe il ciclo, continue salta all''iterazione successiva, while e'' una struttura di ciclo.'
  WHERE test_id = v_python_test_id AND position = 6;
  UPDATE question_subject SET subject_id = v_subj_cicli
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 6);

  -- Position 7 — Function with pass and UnboundLocalError
  -- Subject: Funzioni
  UPDATE question SET difficulty = 'ADVANCED', explanation = E'Quando taxable e'' "No" e shipping e'' 0, il codice entra nel ramo elif che contiene solo pass, quindi la variabile subtotal non viene mai assegnata. Il return subtotal successivo causa un UnboundLocalError perche'' subtotal non esiste nel namespace locale. E'' un errore comune quando non tutti i rami di un if assegnano la variabile restituita.'
  WHERE test_id = v_python_test_id AND position = 7;
  UPDATE question_subject SET subject_id = v_subj_funzioni
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 7);

  -- Position 8 — Function call syntax
  -- Subject: Funzioni
  UPDATE question SET difficulty = 'BEGINNER', explanation = E'In Python una funzione si chiama semplicemente con il suo nome seguito dagli argomenti tra parentesi, assegnando il risultato a una variabile. La sintassi corretta e'' order_total = subtotal(500, .07). Le keyword call e def non si usano nella chiamata di funzione.'
  WHERE test_id = v_python_test_id AND position = 8;
  UPDATE question_subject SET subject_id = v_subj_funzioni
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 8);

  -- Position 9 — Comment character (#)
  -- Subject: Fondamenti Python
  UPDATE question SET difficulty = 'BEGINNER', explanation = E'In Python il carattere # indica l''inizio di un commento su singola riga. Tutto cio'' che segue il # sulla stessa riga viene ignorato dall''interprete. // e'' usato in linguaggi come Java e C++, /* in C, e le virgolette singole delimitano stringhe.'
  WHERE test_id = v_python_test_id AND position = 9;
  UPDATE question_subject SET subject_id = v_subj_fondamenti
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 9);

  -- Position 10 — F-string prefix
  -- Subject: Stringhe e formattazione
  UPDATE question SET difficulty = 'BEGINNER', explanation = E'Il prefisso f prima di una stringa crea una f-string, che permette di inserire espressioni Python tra parentesi graffe {}. Senza il prefisso f, il testo {items} verrebbe stampato letteralmente invece del valore della variabile. E'' la sintassi di formattazione introdotta in Python 3.6.'
  WHERE test_id = v_python_test_id AND position = 10;
  UPDATE question_subject SET subject_id = v_subj_stringhe
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 10);

  -- Position 11 — sys.argv[0]
  -- Subject: Gestione errori e testing
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'sys.argv e'' una lista che contiene gli argomenti passati allo script Python dalla riga di comando. L''elemento all''indice 0 (sys.argv[0]) contiene sempre il nome del file dello script in esecuzione. Gli argomenti effettivi passati dall''utente iniziano dall''indice 1.'
  WHERE test_id = v_python_test_id AND position = 11;
  UPDATE question_subject SET subject_id = v_subj_errori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 11);

  -- Position 12 — Operator precedence fix (parentheses)
  -- Subject: Operatori
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'Senza parentesi, Python esegue prima la moltiplicazione licenseFee * intRate e poi la somma, calcolando un totale errato. La soluzione corretta e'' (carLoan + licenseFee) * intRate, che prima somma il prestito e la tassa di immatricolazione, poi moltiplica il tutto per il tasso di interesse. E'' un problema classico di precedenza degli operatori.'
  WHERE test_id = v_python_test_id AND position = 12;
  UPDATE question_subject SET subject_id = v_subj_operatori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 12);

  -- Position 13 — assertIs (identity test)
  -- Subject: Gestione errori e testing
  UPDATE question SET difficulty = 'ADVANCED', explanation = E'assertIs verifica che due variabili puntino allo stesso oggetto in memoria (test di identita'', equivalente a is). assertEqual verifica solo l''uguaglianza del valore, assertTrue controlla solo se un valore e'' truthy, e assertIn verifica l''appartenenza. Per testare la condivisione dello spazio di memoria serve assertIs.'
  WHERE test_id = v_python_test_id AND position = 13;
  UPDATE question_subject SET subject_id = v_subj_errori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 13);

  -- Position 14 — Break in for loop (4 cities)
  -- Subject: Cicli e controllo di flusso
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'Il ciclo for stampa ogni citta'' prima di controllare la condizione di break. Vengono stampate Anchorage, Juneau, Fairbanks e Ketchikan (4 citta''). Quando city vale "Ketchikan", il print viene eseguito prima del break, quindi anche Ketchikan viene stampata prima dell''interruzione del ciclo.'
  WHERE test_id = v_python_test_id AND position = 14;
  UPDATE question_subject SET subject_id = v_subj_cicli
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 14);

  -- Position 15 — Default parameter (height = 12)
  -- Subject: Funzioni
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'In Python i parametri con valore predefinito si definiscono nella firma della funzione con la sintassi parametro = valore. Per avere height uguale a 12 quando non viene specificato, bisogna scrivere def area(width, height = 12). Definire la variabile nel corpo della funzione o prima della dichiarazione non creerebbe un valore predefinito per il parametro.'
  WHERE test_id = v_python_test_id AND position = 15;
  UPDATE question_subject SET subject_id = v_subj_funzioni
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 15);

  -- Position 16 — Runtime error (IndexError)
  -- Subject: Gestione errori e testing
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = E'La lista trees ha 3 elementi con indici 0, 1 e 2. Accedere a trees[3] causa un IndexError, che e'' un errore a runtime perche'' il codice e'' sintatticamente corretto ma fallisce durante l''esecuzione. Non e'' un errore di sintassi (il codice e'' valido) ne'' un errore logico (il programma si interrompe invece di produrre un risultato errato).'
  WHERE test_id = v_python_test_id AND position = 16;
  UPDATE question_subject SET subject_id = v_subj_errori
  WHERE question_id = (SELECT id FROM question WHERE test_id = v_python_test_id AND position = 16);

  END IF; -- end Python section

  -- ================================================================
  -- JAVASCRIPT SECTION
  -- ================================================================

  -- ── Resolve test IDs ──────────────────────────────────────────
  SELECT id INTO v_js1_test_id FROM test WHERE title = 'JavaScript Certification Simulation 1';
  SELECT id INTO v_js2_test_id FROM test WHERE title = 'JavaScript Certification Simulation 2';

  IF v_js1_test_id IS NULL OR v_js2_test_id IS NULL THEN
    RAISE NOTICE 'One or both JS tests not found, skipping JS section';
  ELSE

  -- ── Topic ─────────────────────────────────────────────────────
  v_js_topic_id := gen_random_uuid();
  INSERT INTO topic (id, title, abbreviation, description)
  VALUES (v_js_topic_id, 'JavaScript Essentials', 'JS',
    'Sintassi, DOM, eventi e programmazione web client-side.')
  ON CONFLICT DO NOTHING;
  -- If already exists, fetch its id
  SELECT id INTO v_js_topic_id FROM topic WHERE abbreviation = 'JS';

  -- ── Granular Subjects ─────────────────────────────────────────

  v_subj_js_fondamenti := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_fondamenti, 'Fondamenti JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_fondamenti FROM subject WHERE label = 'Fondamenti JS';

  v_subj_js_operatori := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_operatori, 'Operatori JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_operatori FROM subject WHERE label = 'Operatori JS';

  v_subj_js_controllo := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_controllo, 'Controllo di flusso JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_controllo FROM subject WHERE label = 'Controllo di flusso JS';

  v_subj_js_cicli := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_cicli, 'Cicli JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_cicli FROM subject WHERE label = 'Cicli JS';

  v_subj_js_funzioni := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_funzioni, 'Funzioni e scope JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_funzioni FROM subject WHERE label = 'Funzioni e scope JS';

  v_subj_js_array := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_array, 'Array e strutture dati JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_array FROM subject WHERE label = 'Array e strutture dati JS';

  v_subj_js_stringhe := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_stringhe, 'Stringhe JS')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_stringhe FROM subject WHERE label = 'Stringhe JS';

  v_subj_js_dom := gen_random_uuid();
  INSERT INTO subject (id, label) VALUES (v_subj_js_dom, 'DOM, eventi e form')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_js_dom FROM subject WHERE label = 'DOM, eventi e form';

  -- ── Link Topic -> Subjects ──────────────────────────────────────
  INSERT INTO topic_subject (topic_id, subject_id, position) VALUES
    (v_js_topic_id, v_subj_js_fondamenti, 1),
    (v_js_topic_id, v_subj_js_operatori,  2),
    (v_js_topic_id, v_subj_js_controllo,  3),
    (v_js_topic_id, v_subj_js_cicli,      4),
    (v_js_topic_id, v_subj_js_funzioni,   5),
    (v_js_topic_id, v_subj_js_array,      6),
    (v_js_topic_id, v_subj_js_stringhe,   7),
    (v_js_topic_id, v_subj_js_dom,        8)
  ON CONFLICT DO NOTHING;

  -- ================================================================
  -- JS Set 1 — Difficulty, Explanation & Granular Subject
  -- ================================================================

  -- JS Set 1: position 1 — Operator precedence (10 + 5*2 - 8/4)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In JavaScript la moltiplicazione e la divisione hanno precedenza su addizione e sottrazione. Quindi 5*2=10 e 8/4=2, poi 10+10-2=18. Bisogna conoscere le regole di precedenza degli operatori per risolvere correttamente l''espressione.'
  WHERE test_id = v_js1_test_id AND position = 1;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 1);

  -- JS Set 1: position 2 — Single-line comment
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In JavaScript i commenti su singola riga si scrivono con //. Le altre sintassi appartengono ad HTML (<!-- -->), non esistono o sono di altri linguaggi.'
  WHERE test_id = v_js1_test_id AND position = 2;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 2);

  -- JS Set 1: position 3 — External script inclusion
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per includere uno script esterno in HTML si usa il tag <script> con l''attributo src che punta al file .js. Gli altri attributi come link, href o tag custom non sono validi per gli script.'
  WHERE test_id = v_js1_test_id AND position = 3;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 3);

  -- JS Set 1: position 4 — Modulo operator
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''operatore modulo (%) restituisce il resto della divisione intera. 7 diviso 3 fa 2 con resto 1, quindi 7 % 3 restituisce 1.'
  WHERE test_id = v_js1_test_id AND position = 4;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 4);

  -- JS Set 1: position 5 — Reserved keyword
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La parola return e'' una keyword riservata in JavaScript e non puo'' essere usata come nome di variabile. Le altre opzioni (amount, result, value) sono identificatori validi.'
  WHERE test_id = v_js1_test_id AND position = 5;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 5);

  -- JS Set 1: position 6 — try-catch-finally
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La sintassi corretta per gestire eccezioni in JavaScript e'' try { ... } catch (err) { ... } finally { ... }. Il blocco catch riceve l''oggetto errore e finally viene eseguito sempre, indipendentemente dall''esito.'
  WHERE test_id = v_js1_test_id AND position = 6;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 6);

  -- JS Set 1: position 7 — window.alert (BOM)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo window.alert() fa parte del Browser Object Model (BOM) e mostra un semplice dialogo modale con un messaggio. Gli altri metodi proposti non esistono nel BOM standard.'
  WHERE test_id = v_js1_test_id AND position = 7;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 7);

  -- JS Set 1: position 8 — Compound assignment +=
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''operatore di assegnazione composta += aggiunge il valore a destra alla variabile a sinistra. total += tax equivale a total = total + tax. Le altre sintassi (=+, ++, =:) non sono operatori validi.'
  WHERE test_id = v_js1_test_id AND position = 8;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 8);

  -- JS Set 1: position 9 — typeof null
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'typeof null restituisce "object", un bug storico di JavaScript presente fin dalla prima versione. Nonostante null non sia un oggetto, questo comportamento e'' stato mantenuto per retrocompatibilita''.'
  WHERE test_id = v_js1_test_id AND position = 9;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 9);

  -- JS Set 1: position 10 — parseInt
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'parseInt() converte una stringa in un intero, troncando la parte decimale. Per "42.75" restituisce 42. parseFloat e Number restituirebbero 42.75, mentre Math.round restituirebbe 43.'
  WHERE test_id = v_js1_test_id AND position = 10;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 10);

  -- JS Set 1: position 11 — toFixed(2)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo toFixed(2) formatta un numero con esattamente 2 cifre decimali e restituisce una stringa. 19.5 diventa "19.50", aggiungendo lo zero necessario per raggiungere le due cifre.'
  WHERE test_id = v_js1_test_id AND position = 11;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 11);

  -- JS Set 1: position 12 — Array literal declaration
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In JavaScript gli array si dichiarano con le parentesi quadre []. Le parentesi tonde definiscono espressioni, le graffe oggetti, e array() non e'' un costruttore valido.'
  WHERE test_id = v_js1_test_id AND position = 12;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 12);

  -- JS Set 1: position 13 — push and length
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo push() aggiunge un elemento alla fine dell''array. L''array [1,2,3] dopo push(4) diventa [1,2,3,4], quindi la proprieta'' length restituisce 4.'
  WHERE test_id = v_js1_test_id AND position = 13;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 13);

  -- JS Set 1: position 14 — shift()
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo shift() rimuove e restituisce il primo elemento di un array. pop() rimuove l''ultimo, unshift() aggiunge all''inizio, e splice(0) rimuove dal primo indice ma restituisce un array.'
  WHERE test_id = v_js1_test_id AND position = 14;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 14);

  -- JS Set 1: position 15 — getMonth() zero-based
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In JavaScript getMonth() restituisce il mese con indice a base zero: gennaio = 0, febbraio = 1, ecc. Questo e'' un errore comune per chi si aspetta che gennaio sia 1.'
  WHERE test_id = v_js1_test_id AND position = 15;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 15);

  -- JS Set 1: position 16 — Math.random()
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Math.random() genera un numero pseudo-casuale in virgola mobile tra 0 (incluso) e 1 (escluso). Non esiste Math.rand() ne'' Math.range() in JavaScript.'
  WHERE test_id = v_js1_test_id AND position = 16;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 16);

  -- JS Set 1: position 17 — Function call: calc(3,4)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La funzione calc moltiplica a per b e aggiunge 2. Con a=3 e b=4: 3*4+2 = 14. La moltiplicazione viene eseguita prima dell''addizione per la precedenza degli operatori.'
  WHERE test_id = v_js1_test_id AND position = 17;
  UPDATE question_subject SET subject_id = v_subj_js_funzioni WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 17);

  -- JS Set 1: position 18 — Variable scope with let
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Una variabile dichiarata con let all''interno di una funzione ha scope locale: e'' visibile solo dentro quella funzione. Non diventa globale automaticamente e non e'' accessibile dall''esterno.'
  WHERE test_id = v_js1_test_id AND position = 18;
  UPDATE question_subject SET subject_id = v_subj_js_funzioni WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 18);

  -- JS Set 1: position 19 — String + Number concatenation
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Quando si usa l''operatore + tra una stringa e un numero, JavaScript converte il numero in stringa e li concatena. "Hello" + 5 produce "Hello5" senza spazio.'
  WHERE test_id = v_js1_test_id AND position = 19;
  UPDATE question_subject SET subject_id = v_subj_js_stringhe WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 19);

  -- JS Set 1: position 20 — "use strict"
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La direttiva "use strict"; attiva la modalita'' strict in JavaScript, che impone regole piu'' rigide e segnala errori altrimenti silenti. Le altre sintassi non sono valide.'
  WHERE test_id = v_js1_test_id AND position = 20;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 20);

  -- JS Set 1: position 21 — Logical operators evaluation
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''operatore && restituisce true solo se entrambe le condizioni sono vere. (5 > 3) e'' true e (2 < 4) e'' true, quindi l''intera espressione e'' true. Le altre opzioni contengono almeno una condizione falsa o una negazione.'
  WHERE test_id = v_js1_test_id AND position = 21;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 21);

  -- JS Set 1: position 22 — if-else if chain (first match wins)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In una catena if-else if, solo il primo blocco la cui condizione e'' vera viene eseguito. x=10 soddisfa x>5, quindi stampa "A" e ignora il ramo else if (x>8) anche se sarebbe anch''esso vero.'
  WHERE test_id = v_js1_test_id AND position = 22;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 22);

  -- JS Set 1: position 23 — && vs || in conditions
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per verificare che ENTRAMBE le condizioni siano vere (gruppo VIP E punteggio > 80) si usa l''operatore logico AND (&&). L''operatore || verificherebbe che almeno una sia vera, il che non e'' il requisito.'
  WHERE test_id = v_js1_test_id AND position = 23;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 23);

  -- JS Set 1: position 24 — do-while loop
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il ciclo do-while esegue il corpo almeno una volta prima di valutare la condizione, perche'' il test avviene alla fine. Gli altri cicli (while, for, for-of) verificano la condizione prima di entrare nel corpo.'
  WHERE test_id = v_js1_test_id AND position = 24;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 24);

  -- JS Set 1: position 25 — continue in for loop
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''istruzione continue salta il resto del corpo del ciclo per l''iterazione corrente e passa alla successiva. Quando i===1 il console.log viene saltato, quindi vengono stampati solo 0 e 2.'
  WHERE test_id = v_js1_test_id AND position = 25;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 25);

  -- JS Set 1: position 26 — switch break keyword
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Nello switch, la keyword break interrompe l''esecuzione del blocco e previene il fall-through ai casi successivi. Senza break, l''esecuzione proseguirebbe nei case sottostanti. continue, exit e return hanno scopi diversi.'
  WHERE test_id = v_js1_test_id AND position = 26;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 26);

  -- JS Set 1: position 27 — while loop count
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il ciclo while con i che parte da 0 e condizione i < 5 esegue il corpo per i = 0, 1, 2, 3, 4, cioe'' 5 volte. Al termine i vale 5 e la condizione diventa falsa.'
  WHERE test_id = v_js1_test_id AND position = 27;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 27);

  -- JS Set 1: position 28 — for-in iterates indexes
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il ciclo for-in itera sulle chiavi enumerabili di un oggetto. Per un array, le chiavi sono gli indici (0, 1, 2). for-of invece itererebbe sui valori. Le sintassi for each e foreach non sono standard in JavaScript.'
  WHERE test_id = v_js1_test_id AND position = 28;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 28);

  -- JS Set 1: position 29 — getElementById
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo document.getElementById() seleziona un elemento del DOM tramite il suo attributo id. E'' il metodo standard e piu'' usato per accedere a un singolo elemento specifico.'
  WHERE test_id = v_js1_test_id AND position = 29;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 29);

  -- JS Set 1: position 30 — innerHTML
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La proprieta'' innerHTML consente di leggere o impostare il contenuto HTML all''interno di un elemento, inclusi tag HTML. textContent e innerText gestiscono solo il testo senza interpretare i tag.'
  WHERE test_id = v_js1_test_id AND position = 30;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 30);

  -- JS Set 1: position 31 — addEventListener
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo addEventListener() registra una funzione callback per un evento specifico su un elemento. Si passa il nome dell''evento senza il prefisso "on" e il riferimento alla funzione senza parentesi di invocazione.'
  WHERE test_id = v_js1_test_id AND position = 31;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 31);

  -- JS Set 1: position 32 — onmouseover event
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''evento onmouseover si attiva quando il puntatore del mouse entra nell''area di un elemento. onmousedown si attiva alla pressione del pulsante, onmouseenter e'' simile ma non propaga, e onhover non esiste.'
  WHERE test_id = v_js1_test_id AND position = 32;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 32);

  -- JS Set 1: position 33 — createElement
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'document.createElement() crea un nuovo elemento HTML in memoria senza aggiungerlo al DOM. Per renderlo visibile nella pagina occorre poi usare appendChild() o un metodo simile.'
  WHERE test_id = v_js1_test_id AND position = 33;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 33);

  -- JS Set 1: position 34 — setAttribute
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo setAttribute() imposta il valore di un attributo su un elemento. In questo caso sovrascrive completamente l''attributo class con "highlight". Non aggiunge alla lista esistente: per quello si userebbe classList.add().'
  WHERE test_id = v_js1_test_id AND position = 34;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 34);

  -- JS Set 1: position 35 — onload event
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''evento onload si attiva quando la pagina e'' completamente caricata, incluse immagini e fogli di stile. onstart, onready e oninit non sono eventi standard del DOM.'
  WHERE test_id = v_js1_test_id AND position = 35;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 35);

  -- JS Set 1: position 36 — getElementsByClassName
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'document.getElementsByClassName() restituisce una HTMLCollection live di tutti gli elementi che hanno la classe specificata. Il nome del metodo include "Elements" al plurale perche'' puo'' restituire piu'' elementi.'
  WHERE test_id = v_js1_test_id AND position = 36;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 36);

  -- JS Set 1: position 37 — onblur event
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''evento onblur si attiva quando un elemento perde il focus (ad esempio quando l''utente clicca altrove). onfocus e'' l''opposto (quando l''elemento riceve il focus), onchange richiede anche che il valore sia cambiato.'
  WHERE test_id = v_js1_test_id AND position = 37;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 37);

  -- JS Set 1: position 38 — DOM tree root
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''oggetto document e'' la radice dell''albero DOM e rappresenta l''intero documento HTML. window e'' l''oggetto globale del BOM che contiene document, ma non fa parte del DOM stesso.'
  WHERE test_id = v_js1_test_id AND position = 38;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 38);

  -- JS Set 1: position 39 — document.write
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'document.write() scrive contenuto direttamente nel documento HTML durante il caricamento della pagina. E'' un metodo legacy: se chiamato dopo il caricamento, sovrascrive l''intero documento.'
  WHERE test_id = v_js1_test_id AND position = 39;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 39);

  -- JS Set 1: position 40 — Reading input value
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per leggere il valore digitato dall''utente in un campo input si usa la proprieta'' .value. innerHTML restituisce il contenuto HTML interno (non il valore digitato), e .text e .content non sono proprieta'' standard degli input.'
  WHERE test_id = v_js1_test_id AND position = 40;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 40);

  -- JS Set 1: position 41 — isNaN()
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La funzione isNaN() verifica se il valore non e'' un numero valido. typeof input !== "number" controlla solo il tipo ma non gestisce stringhe non numeriche. !Number(input) e'' meno preciso e isNotNumber non esiste.'
  WHERE test_id = v_js1_test_id AND position = 41;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 41);

  -- JS Set 1: position 42 — GET form method
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo GET appende i dati del form alla URL come parametri di query string (?chiave=valore). Il metodo POST invia i dati nel corpo della richiesta, non visibili nella URL.'
  WHERE test_id = v_js1_test_id AND position = 42;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 42);

  -- JS Set 1: position 43 — onsubmit event
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''evento onsubmit si attiva quando un form viene inviato e permette di intercettare l''invio per validare i dati. Restituendo false o chiamando preventDefault() si puo'' annullare la sottomissione.'
  WHERE test_id = v_js1_test_id AND position = 43;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 43);

  -- JS Set 1: position 44 — Setting input value
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per pre-popolare un campo input si assegna un valore alla proprieta'' .value dell''elemento. .text e .setValue() non sono proprieta'' standard, e innerHTML non funziona per i campi di input.'
  WHERE test_id = v_js1_test_id AND position = 44;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 44);

  -- JS Set 1: position 45 — code !== "" check
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''espressione code !== "" verifica che la stringa non sia vuota usando il confronto strict. code != null controllerebbe solo null/undefined, code.length === 0 verificherebbe la stringa vuota (l''opposto del requisito), e code === undefined non controlla il contenuto.'
  WHERE test_id = v_js1_test_id AND position = 45;
  UPDATE question_subject SET subject_id = v_subj_js_stringhe WHERE question_id = (SELECT id FROM question WHERE test_id = v_js1_test_id AND position = 45);

  -- ================================================================
  -- JS Set 2 — Difficulty, Explanation & Granular Subject
  -- ================================================================

  -- JS Set 2: position 1 — Compound assignment (*= and -=)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Gli operatori di assegnazione composta si eseguono in sequenza: x *= 3 porta x a 12 (4*3), poi x -= 2 porta x a 10 (12-2). Bisogna applicare ogni operazione nell''ordine corretto.'
  WHERE test_id = v_js2_test_id AND position = 1;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 1);

  -- JS Set 2: position 2 — Multi-line comment
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In JavaScript i commenti multi-riga si scrivono tra /* e */. I doppi slash (//) sono per commenti su singola riga, <!-- --> e'' la sintassi HTML, e i triple apici non esistono in JS.'
  WHERE test_id = v_js2_test_id AND position = 2;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 2);

  -- JS Set 2: position 3 — debugger keyword
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La keyword debugger; pausa l''esecuzione del codice nel punto in cui e'' inserita, aprendo gli strumenti di debug del browser. debug(), console.debugger e break hanno funzionalita'' diverse o non esistono.'
  WHERE test_id = v_js2_test_id AND position = 3;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 3);

  -- JS Set 2: position 4 — <noscript> tag
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il tag <noscript> mostra il suo contenuto solo quando JavaScript e'' disabilitato nel browser o non supportato. Serve come fallback per fornire informazioni agli utenti senza JS abilitato.'
  WHERE test_id = v_js2_test_id AND position = 4;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 4);

  -- JS Set 2: position 5 — try-catch-finally execution order
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'parseInt("abc") restituisce NaN, isNaN(NaN) e'' true, quindi viene lanciata l''eccezione. Il catch stampa "Not a number", poi il finally viene sempre eseguito e stampa "Done". L''ordine e'' determinato dal flusso try-catch-finally.'
  WHERE test_id = v_js2_test_id AND position = 5;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 5);

  -- JS Set 2: position 6 — screen.width (BOM)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La proprieta'' screen.width del BOM restituisce la larghezza dello schermo in pixel. window.size, document.width e navigator.screenWidth non sono proprieta'' standard del BOM.'
  WHERE test_id = v_js2_test_id AND position = 6;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 6);

  -- JS Set 2: position 7 — Variable naming conventions
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In JavaScript la convenzione standard per i nomi delle variabili e'' il camelCase (es. fullName). Nomi troppo corti (fn) sono poco leggibili, PascalCase e'' per classi/costruttori, e nomi troppo lunghi riducono la leggibilita''.'
  WHERE test_id = v_js2_test_id AND position = 7;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 7);

  -- JS Set 2: position 8 — console.log()
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'console.log() stampa un messaggio nella console del browser, utilizzata principalmente per il debug. Non salva file, non mostra popup e non invia dati al server.'
  WHERE test_id = v_js2_test_id AND position = 8;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 8);

  -- JS Set 2: position 9 — typeof undefined variable
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Una variabile dichiarata ma non inizializzata ha valore undefined. L''operatore typeof applicato a essa restituisce la stringa "undefined". Non viene lanciato alcun errore perche'' la variabile e'' stata dichiarata.'
  WHERE test_id = v_js2_test_id AND position = 9;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 9);

  -- JS Set 2: position 10 — Number(true) conversion
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La funzione Number() converte true nel numero 1 e false in 0. parseInt(true) restituirebbe NaN perche'' tenta di parsare la stringa "true". toNumber() e Integer() non esistono in JavaScript.'
  WHERE test_id = v_js2_test_id AND position = 10;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 10);

  -- JS Set 2: position 11 — toPrecision(4)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Il metodo toPrecision(4) formatta il numero con esattamente 4 cifre significative totali. Per 1234.5678, le prime 4 cifre significative sono 1, 2, 3, 5 (con arrotondamento della quinta cifra), restituendo "1235".'
  WHERE test_id = v_js2_test_id AND position = 11;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 11);

  -- JS Set 2: position 12 — Array indexing (zero-based)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Gli array in JavaScript usano indici a base zero. Il secondo elemento si trova all''indice 1, quindi cities[1] restituisce "Milan". cities[2] restituirebbe il terzo elemento "Naples".'
  WHERE test_id = v_js2_test_id AND position = 12;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 12);

  -- JS Set 2: position 13 — sort() default behavior
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo sort() senza argomenti ordina gli elementi come stringhe in ordine lessicografico. Per numeri a singola cifra il risultato coincide con l''ordine numerico: [1, 1, 3, 4, 5]. Il primo elemento (indice 0) e'' 1.'
  WHERE test_id = v_js2_test_id AND position = 13;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 13);

  -- JS Set 2: position 14 — unshift()
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo unshift() aggiunge uno o piu'' elementi all''inizio dell''array. push() aggiunge alla fine, shift() rimuove dal''inizio, e prepend() non e'' un metodo degli array JavaScript.'
  WHERE test_id = v_js2_test_id AND position = 14;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 14);

  -- JS Set 2: position 15 — getFullYear()
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo getFullYear() restituisce l''anno completo a 4 cifre di un oggetto Date. Per una data del 2026, restituisce 2026. getYear() (deprecato) restituirebbe 126 (anno - 1900).'
  WHERE test_id = v_js2_test_id AND position = 15;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 15);

  -- JS Set 2: position 16 — Math.floor + Math.ceil
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Math.floor(7.9) arrotonda per difetto a 7 e Math.ceil(2.1) arrotonda per eccesso a 3. La somma 7 + 3 = 10. Bisogna conoscere il comportamento opposto di floor e ceil per risolvere l''espressione.'
  WHERE test_id = v_js2_test_id AND position = 16;
  UPDATE question_subject SET subject_id = v_subj_js_array WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 16);

  -- JS Set 2: position 17 — Default parameter
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'I parametri con valore predefinito in JavaScript usano la sintassi (name = "Guest"). Quando la funzione viene chiamata senza argomenti, name assume il valore "Guest", producendo "Hello, Guest".'
  WHERE test_id = v_js2_test_id AND position = 17;
  UPDATE question_subject SET subject_id = v_subj_js_funzioni WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 17);

  -- JS Set 2: position 18 — Pass by reference (arrays)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'In JavaScript gli array sono passati per riferimento alle funzioni. Modificare l''array dentro la funzione con push(99) modifica l''array originale. Dopo la chiamata, list contiene [1, 2, 3, 99] e length e'' 4.'
  WHERE test_id = v_js2_test_id AND position = 18;
  UPDATE question_subject SET subject_id = v_subj_js_funzioni WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 18);

  -- JS Set 2: position 19 — typeof "hello"
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''operatore typeof applicato a un valore stringa restituisce "string" (tutto minuscolo). JavaScript e'' case-sensitive, quindi "String" sarebbe errato. I tipi "text" e "char" non esistono in JavaScript.'
  WHERE test_id = v_js2_test_id AND position = 19;
  UPDATE question_subject SET subject_id = v_subj_js_fondamenti WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 19);

  -- JS Set 2: position 20 — string length property
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In JavaScript la lunghezza di una stringa si ottiene con la proprieta'' length (senza parentesi). Non e'' un metodo, quindi non si scrive length(). size() e count() non sono proprieta'' delle stringhe JavaScript.'
  WHERE test_id = v_js2_test_id AND position = 20;
  UPDATE question_subject SET subject_id = v_subj_js_stringhe WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 20);

  -- JS Set 2: position 21 — == (loose equality)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'L''operatore == esegue la coercizione di tipo prima del confronto. Il numero 5 e la stringa "5" vengono considerati uguali perche'' JavaScript converte la stringa in numero. Con === (strict) sarebbero diversi.'
  WHERE test_id = v_js2_test_id AND position = 21;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 21);

  -- JS Set 2: position 22 — Range condition with &&
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Per verificare un intervallo inclusivo in JavaScript servono due condizioni collegate con &&: value >= 10 && value <= 20. La sintassi 10 <= value <= 20 non funziona come atteso perche'' JavaScript la valuta da sinistra a destra.'
  WHERE test_id = v_js2_test_id AND position = 22;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 22);

  -- JS Set 2: position 23 — switch with break
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Lo switch confronta il valore di day con i vari case. Con day = 3 viene eseguito il case 3 che stampa "Wed". Il break impedisce il fall-through al default.'
  WHERE test_id = v_js2_test_id AND position = 23;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 23);

  -- JS Set 2: position 24 — switch fall-through
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Senza break, lo switch esegue il fall-through: dopo il case corrispondente, continua ad eseguire tutti i case successivi fino alla fine del blocco switch o fino a un break. Questo e'' un comportamento specifico di JavaScript ereditato dal C.'
  WHERE test_id = v_js2_test_id AND position = 24;
  UPDATE question_subject SET subject_id = v_subj_js_controllo WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 24);

  -- JS Set 2: position 25 — for loop sum (1+2+3+4)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il ciclo for somma i numeri da 1 a 4: total = 0+1+2+3+4 = 10. La condizione i <= 4 include il valore 4 nell''ultima iterazione, diversamente da i < 4 che si fermerebbe a 3.'
  WHERE test_id = v_js2_test_id AND position = 25;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 25);

  -- JS Set 2: position 26 — break keyword
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La keyword break termina immediatamente il ciclo in cui si trova, indipendentemente dalla condizione del loop. continue salta solo l''iterazione corrente, return esce dalla funzione, e exit non esiste in JavaScript.'
  WHERE test_id = v_js2_test_id AND position = 26;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 26);

  -- JS Set 2: position 27 — Logical operators in context
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Per guidare servono ENTRAMBE le condizioni (eta'' >= 18 E patente), quindi && per il primo blank. Per entrare basta essere studente OPPURE insegnante, quindi || per il secondo. La scelta dell''operatore logico dipende dalla semantica del requisito.'
  WHERE test_id = v_js2_test_id AND position = 27;
  UPDATE question_subject SET subject_id = v_subj_js_operatori WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 27);

  -- JS Set 2: position 28 — for-of iterates values
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il ciclo for-of itera direttamente sui valori di un array. Per ["x", "y", "z"], stampa i valori x, y, z. Il ciclo for-in invece itererebbe sugli indici 0, 1, 2.'
  WHERE test_id = v_js2_test_id AND position = 28;
  UPDATE question_subject SET subject_id = v_subj_js_cicli WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 28);

  -- JS Set 2: position 29 — textContent vs innerHTML
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La proprieta'' textContent imposta o legge il contenuto testuale di un elemento senza interpretare tag HTML. A differenza di innerHTML, non e'' vulnerabile a XSS perche'' tratta tutto come testo puro.'
  WHERE test_id = v_js2_test_id AND position = 29;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 29);

  -- JS Set 2: position 30 — Inline onclick handler
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per gestire un evento click inline in HTML si usa l''attributo onclick="funzione()". Gli attributi click, event e on-click non sono validi in HTML standard.'
  WHERE test_id = v_js2_test_id AND position = 30;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 30);

  -- JS Set 2: position 31 — getElementsByTagName
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'document.getElementsByTagName("p") restituisce una HTMLCollection live di tutti gli elementi <p> nel documento. Non e'' un singolo elemento, non e'' il conteggio e non e'' un Array statico ma una collezione live.'
  WHERE test_id = v_js2_test_id AND position = 31;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 31);

  -- JS Set 2: position 32 — onkeydown event
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''evento onkeydown si attiva quando l''utente preme un tasto sulla tastiera. onkeyup si attiva al rilascio del tasto, onclick riguarda il mouse, e onkeyhold non esiste come evento standard.'
  WHERE test_id = v_js2_test_id AND position = 32;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 32);

  -- JS Set 2: position 33 — createElement + appendChild
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'createElement crea un nuovo elemento in memoria, poi appendChild lo inserisce come ultimo figlio dell''elemento specificato. L''immagine viene creata e aggiunta come figlio di #box, non come sostituzione o fratello.'
  WHERE test_id = v_js2_test_id AND position = 33;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 33);

  -- JS Set 2: position 34 — removeEventListener
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Per rimuovere un listener aggiunto con addEventListener si usa removeEventListener, passando lo stesso tipo di evento e la stessa funzione di riferimento. deleteEventListener, off e detachEvent non sono metodi standard del DOM moderno.'
  WHERE test_id = v_js2_test_id AND position = 34;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 34);

  -- JS Set 2: position 35 — onchange event
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''evento onchange si attiva quando il valore di un campo cambia E il campo perde il focus. oninput si attiva ad ogni modifica del valore in tempo reale, senza aspettare la perdita di focus.'
  WHERE test_id = v_js2_test_id AND position = 35;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 35);

  -- JS Set 2: position 36 — innerHTML + textContent interaction
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Impostando innerHTML a "<em>New</em>" il browser interpreta il tag <em> e crea un nodo figlio. textContent restituisce solo il testo visibile senza tag, quindi il risultato e'' "New" e non il markup HTML.'
  WHERE test_id = v_js2_test_id AND position = 36;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 36);

  -- JS Set 2: position 37 — window.innerWidth (BOM)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'window.innerWidth restituisce la larghezza interna del viewport del browser (area visibile), escluse le barre degli strumenti. screen.width restituisce la larghezza totale dello schermo del dispositivo.'
  WHERE test_id = v_js2_test_id AND position = 37;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 37);

  -- JS Set 2: position 38 — onmouseout/onmouseleave
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Sia onmouseout che onmouseleave si attivano quando il mouse lascia un elemento, ma differiscono nel bubbling: onmouseout propaga, onmouseleave no. Entrambi sono eventi validi in JavaScript, quindi la risposta corretta e'' "Both A and B".'
  WHERE test_id = v_js2_test_id AND position = 38;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 38);

  -- JS Set 2: position 39 — document.body === getElementsByTagName("body")[0]
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'document.body e'' una scorciatoia che punta allo stesso oggetto DOM restituito da getElementsByTagName("body")[0]. Poiche'' si tratta dello stesso riferimento in memoria, l''operatore === restituisce true.'
  WHERE test_id = v_js2_test_id AND position = 39;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 39);

  -- JS Set 2: position 40 — Reading input value as number
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La proprieta'' .value di un input restituisce sempre una stringa. Per ottenere un numero e'' necessario convertire esplicitamente con Number() o parseInt(). Senza conversione si otterrebbe una stringa, non un valore numerico.'
  WHERE test_id = v_js2_test_id AND position = 40;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 40);

  -- JS Set 2: position 41 — POST for sensitive data
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il metodo POST invia i dati nel corpo della richiesta HTTP, non visibili nella URL. Per dati sensibili come le password, POST e'' preferito a GET che esporrebbe i dati nella barra degli indirizzi e nei log del server.'
  WHERE test_id = v_js2_test_id AND position = 41;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 41);

  -- JS Set 2: position 42 — onsubmit typical use
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''uso tipico di onsubmit e'' validare i campi del form prima dell''invio al server. Se la validazione fallisce, si puo'' cancellare la sottomissione restituendo false o chiamando event.preventDefault().'
  WHERE test_id = v_js2_test_id AND position = 42;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 42);

  -- JS Set 2: position 43 — toLowerCase() for case-insensitive comparison
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Per un confronto case-insensitive si converte la stringa in minuscolo con toLowerCase() e poi si confronta con ===. Il metodo .equals() non esiste per le stringhe JavaScript. Elencare tutte le varianti (||) sarebbe fragile e incompleto.'
  WHERE test_id = v_js2_test_id AND position = 43;
  UPDATE question_subject SET subject_id = v_subj_js_stringhe WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 43);

  -- JS Set 2: position 44 — Select element .value
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La proprieta'' .value di un elemento <select> restituisce il valore (attributo value) dell''opzione attualmente selezionata, non il testo visibile. Per ottenere il testo si userebbe .options[.selectedIndex].text.'
  WHERE test_id = v_js2_test_id AND position = 44;
  UPDATE question_subject SET subject_id = v_subj_js_dom WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 44);

  -- JS Set 2: position 45 — name.trim() !== "" for blank check
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'name.trim() !== "" e'' il controllo piu'' robusto perche'' trim() rimuove spazi iniziali e finali. Una stringa di soli spazi diventerebbe "" dopo il trim. name !== "" da solo non catturerebbe stringhe composte solo da spazi.'
  WHERE test_id = v_js2_test_id AND position = 45;
  UPDATE question_subject SET subject_id = v_subj_js_stringhe WHERE question_id = (SELECT id FROM question WHERE test_id = v_js2_test_id AND position = 45);

  END IF; -- end JS section

  -- ================================================================
  -- JAVA SECTION
  -- ================================================================

  -- ── Resolve test ID ──────────────────────────────────────────
  SELECT id INTO v_java_test_id
    FROM test WHERE title = 'Java Certification Exam Practice 1';
  IF v_java_test_id IS NULL THEN
    RAISE NOTICE 'Java test not found, skipping Java section';
  ELSE

  -- ── Resolve old generic subject ──────────────────────────────
  SELECT id INTO v_old_subject_id FROM subject WHERE label = 'Java';

  -- ── Topic ────────────────────────────────────────────────────
  IF NOT EXISTS (SELECT 1 FROM topic WHERE title = 'Java Fundamentals') THEN
    v_java_topic_id := gen_random_uuid();
    INSERT INTO topic (id, title, abbreviation, description, position, enabled)
    VALUES (v_java_topic_id,
            'Java Fundamentals',
            'Ja',
            'Tipi, OOP, gestione errori e strutture dati in Java.',
            4, true);
  ELSE
    SELECT id INTO v_java_topic_id FROM topic WHERE title = 'Java Fundamentals';
  END IF;

  -- ── 8 Granular Subjects (chapters) ───────────────────────────

  -- 1. Fondamenti Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Fondamenti Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_fondamenti FROM subject WHERE label = 'Fondamenti Java';

  -- 2. Operatori Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Operatori Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_operatori FROM subject WHERE label = 'Operatori Java';

  -- 3. Controllo di flusso Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Controllo di flusso Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_controllo FROM subject WHERE label = 'Controllo di flusso Java';

  -- 4. Cicli Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Cicli Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_cicli FROM subject WHERE label = 'Cicli Java';

  -- 5. Metodi Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Metodi Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_metodi FROM subject WHERE label = 'Metodi Java';

  -- 6. Array e collections Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Array e collections Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_array FROM subject WHERE label = 'Array e collections Java';

  -- 7. Stringhe e I/O Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'Stringhe e I/O Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_stringhe FROM subject WHERE label = 'Stringhe e I/O Java';

  -- 8. OOP e gestione errori Java
  INSERT INTO subject (id, label) VALUES (gen_random_uuid(), 'OOP e gestione errori Java')
  ON CONFLICT DO NOTHING;
  SELECT id INTO v_subj_ja_oop FROM subject WHERE label = 'OOP e gestione errori Java';

  -- ── Link Topic -> Subjects ────────────────────────────────────
  INSERT INTO topic_subject (topic_id, subject_id, position) VALUES
    (v_java_topic_id, v_subj_ja_fondamenti, 1),
    (v_java_topic_id, v_subj_ja_operatori,  2),
    (v_java_topic_id, v_subj_ja_controllo,  3),
    (v_java_topic_id, v_subj_ja_cicli,      4),
    (v_java_topic_id, v_subj_ja_metodi,     5),
    (v_java_topic_id, v_subj_ja_array,      6),
    (v_java_topic_id, v_subj_ja_stringhe,   7),
    (v_java_topic_id, v_subj_ja_oop,        8)
  ON CONFLICT DO NOTHING;

  -- ── Reassign question_subject to granular subjects ───────────
  -- Remove old generic "Java" links and add granular ones

  -- Q1 -> Operatori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 1;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_operatori, 1.00) ON CONFLICT DO NOTHING;

  -- Q2 -> Stringhe e I/O Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 2;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_stringhe, 1.00) ON CONFLICT DO NOTHING;

  -- Q3 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 3;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- Q4 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 4;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q5 -> Controllo di flusso Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 5;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_controllo, 1.00) ON CONFLICT DO NOTHING;

  -- Q6 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 6;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q7 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 7;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q8 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 8;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q9 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 9;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q10 -> Controllo di flusso Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 10;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_controllo, 1.00) ON CONFLICT DO NOTHING;

  -- Q11 -> Operatori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 11;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_operatori, 1.00) ON CONFLICT DO NOTHING;

  -- Q12 -> Cicli Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 12;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_cicli, 1.00) ON CONFLICT DO NOTHING;

  -- Q13 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 13;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q14 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 14;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q15 -> Cicli Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 15;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_cicli, 1.00) ON CONFLICT DO NOTHING;

  -- Q16 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 16;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q17 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 17;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q18 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 18;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q19 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 19;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q20 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 20;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q21 -> Stringhe e I/O Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 21;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_stringhe, 1.00) ON CONFLICT DO NOTHING;

  -- Q22 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 22;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- Q23 -> Stringhe e I/O Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 23;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_stringhe, 1.00) ON CONFLICT DO NOTHING;

  -- Q24 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 24;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q25 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 25;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q26 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 26;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- Q27 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 27;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- Q28 -> Stringhe e I/O Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 28;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_stringhe, 1.00) ON CONFLICT DO NOTHING;

  -- Q29 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 29;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q30 -> Operatori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 30;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_operatori, 1.00) ON CONFLICT DO NOTHING;

  -- Q31 -> Array e collections Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 31;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_array, 1.00) ON CONFLICT DO NOTHING;

  -- Q32 -> Cicli Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 32;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_cicli, 1.00) ON CONFLICT DO NOTHING;

  -- Q33 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 33;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- Q34 -> Operatori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 34;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_operatori, 1.00) ON CONFLICT DO NOTHING;

  -- Q35 -> Stringhe e I/O Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 35;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_stringhe, 1.00) ON CONFLICT DO NOTHING;

  -- Q36 -> Array e collections Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 36;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_array, 1.00) ON CONFLICT DO NOTHING;

  -- Q37 -> Array e collections Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 37;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_array, 1.00) ON CONFLICT DO NOTHING;

  -- Q38 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 38;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q39 -> OOP e gestione errori Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 39;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_oop, 1.00) ON CONFLICT DO NOTHING;

  -- Q40 -> Metodi Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 40;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_metodi, 1.00) ON CONFLICT DO NOTHING;

  -- Q41 -> Fondamenti Java
  SELECT id INTO v_q_id FROM question WHERE test_id = v_java_test_id AND position = 41;
  DELETE FROM question_subject WHERE question_id = v_q_id AND subject_id = v_old_subject_id;
  INSERT INTO question_subject (question_id, subject_id, weight) VALUES (v_q_id, v_subj_ja_fondamenti, 1.00) ON CONFLICT DO NOTHING;

  -- ── Difficulty & Explanation UPDATEs ─────────────────────────

  -- Java: position 1 — Operatori (precedenza operatori, calcolo espressioni)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''espressione originale calcola prima licensing * rate (1000 * 0.10 = 100) e poi somma ad amount, ottenendo 30100. Per ottenere 33100 bisogna prima sommare amount + licensing (30000 + 1000 = 31000), moltiplicare per rate (31000 * 0.10 = 3100) e poi aggiungere amount. La formula corretta e'' quindi (amount + licensing) * rate + amount.'
  WHERE test_id = v_java_test_id AND position = 1;

  -- Java: position 2 — Stringhe e I/O (System.out.print, concatenazione stringhe)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In Java, System.out.print() accetta un singolo argomento stringa. L''operatore + concatena la stringa letterale con il valore della variabile total. Le altre opzioni usano sintassi errata: println con virgola, oppure l''operatore & che in Java e'' un operatore bitwise, non di concatenazione.'
  WHERE test_id = v_java_test_id AND position = 2;

  -- Java: position 3 — Fondamenti (tipi primitivi vs wrapper classes)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Un tipo primitivo (int, double, boolean, ecc.) e'' un tipo di dato predefinito nel linguaggio Java che contiene direttamente il valore. Una wrapper class (Integer, Double, Boolean, ecc.) converte i tipi primitivi in oggetti, permettendo di usarli in contesti che richiedono riferimenti come le collezioni generiche.'
  WHERE test_id = v_java_test_id AND position = 3;

  -- Java: position 4 — OOP e gestione errori (stack trace, debugging)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Lo stack trace mostra la catena di chiamate in ordine inverso: main chiama a1 alla riga 4, a1 chiama a2 alla riga 7, e cosi'' via fino a a4 che invoca Thread.dumpStack(). Quindi a1 viene effettivamente chiamata alla riga 7 e Thread.dumpStack() e'' il metodo che genera l''output dello stack trace.'
  WHERE test_id = v_java_test_id AND position = 4;

  -- Java: position 5 — Controllo di flusso (if-else-if, condizioni annidate)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'La logica richiede condizioni annidate: dentro il blocco North si controlla se le vendite superano 50000, altrimenti nel blocco else si controlla se superano 40000. L''operatore > (strettamente maggiore) e'' corretto perche'' la specifica dice "exceed" (superare). Il bonus e'' del 5% (0.05) in entrambi i casi, non del 10%.'
  WHERE test_id = v_java_test_id AND position = 5;

  -- Java: position 6 — OOP e gestione errori (InputMismatchException, Scanner)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''eccezione InputMismatchException indica che il metodo nextInt() dello Scanner ha ricevuto un input non convertibile in intero. L''errore alla riga 9 di ScannerTest.java indica un uso scorretto della classe Scanner, non un errore nella definizione del metodo. Il numero 9 si riferisce alla riga del codice sorgente, non al numero di errori.'
  WHERE test_id = v_java_test_id AND position = 6;

  -- Java: position 7 — OOP e gestione errori (exception handling, concetti)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'getMessage() restituisce una stringa con i dettagli dell''eccezione. getStackTrace() restituisce un array di StackTraceElement. InputMismatchException si verifica quando si inserisce un tipo di dato errato (es. stringa invece di intero), mentre IOException si verifica per errori di I/O come file inesistenti.'
  WHERE test_id = v_java_test_id AND position = 7;

  -- Java: position 8 — OOP e gestione errori (try-catch-finally, Scanner)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il costrutto try-catch-finally e'' la struttura standard per la gestione delle eccezioni in Java. Il blocco try contiene il codice che potrebbe generare un''eccezione, catch cattura l''eccezione e getMessage() ne restituisce il messaggio descrittivo, infine finally viene eseguito sempre indipendentemente dall''esito.'
  WHERE test_id = v_java_test_id AND position = 8;

  -- Java: position 9 — OOP e gestione errori (classi, costruttori, istanziazione)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per istanziare un oggetto in Java si usa il nome della classe come tipo della variabile: Shirt shirt1 = new Shirt(...). Per accedere a un campo pubblico si usa la notazione con il punto: shirt1.color. Non esiste un metodo getColor() perche'' non e'' stato definito nella classe, e il tipo deve essere Shirt, non Object o String.'
  WHERE test_id = v_java_test_id AND position = 9;

  -- Java: position 10 — Controllo di flusso (switch-case, break, default)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Lo statement switch valuta il valore di una variabile e confronta con i vari case. La keyword break interrompe l''esecuzione per evitare il fall-through. La keyword default gestisce tutti i valori non coperti dai case, funzionando come un "else" per lo switch. select, continue e if non sono validi in questo contesto.'
  WHERE test_id = v_java_test_id AND position = 10;

  -- Java: position 11 — Operatori (operatori logici && e ||)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''operatore && (AND logico) richiede che entrambe le condizioni siano vere: la regione deve essere "South" E lo stato deve essere "Texas". L''operatore || (OR logico) richiede che almeno una condizione sia vera: la regione e'' "North" OPPURE "West". In Java si usano && e ||, non le parole chiave AND e OR.'
  WHERE test_id = v_java_test_id AND position = 11;

  -- Java: position 12 — Cicli (do-while, operatore +=)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il ciclo do-while garantisce almeno un''esecuzione del blocco prima di verificare la condizione, soddisfacendo il requisito "almeno una notifica". L''operatore += 8 incrementa practiceHours di 8 ad ogni iterazione. La condizione while (practiceHours < totalHours) termina il ciclo quando si raggiungono le 40 ore.'
  WHERE test_id = v_java_test_id AND position = 12;

  -- Java: position 13 — Metodi (overloading, parametri, tipi di ritorno)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Il method overloading richiede metodi con lo stesso nome ma parametri diversi. Il primo metodo accetta un int (ore intere), il secondo un double (ore parziali). Entrambi devono dichiarare la variabile String msgHours, assegnarle un valore e restituirla con return msgHours. I tipi dei parametri devono corrispondere al tipo di dato atteso.'
  WHERE test_id = v_java_test_id AND position = 13;

  -- Java: position 14 — Metodi (return type void)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La keyword void indica che il metodo non restituisce alcun valore. Viene usata nella dichiarazione del metodo al posto del tipo di ritorno quando il metodo esegue operazioni senza dover fornire un risultato al chiamante. Gli altri tipi (boolean, String, integer) indicano che il metodo restituisce un valore di quel tipo.'
  WHERE test_id = v_java_test_id AND position = 14;

  -- Java: position 15 — Cicli (enhanced for-each)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La sintassi corretta del ciclo for-each in Java e'' for(Type variable : collection). In questo caso for(String region : regions) itera su ogni elemento dell''array regions. Java non ha una keyword foreach (tutto attaccato), non permette while con la sintassi :, e for(region in regions) e'' sintassi di altri linguaggi come Python.'
  WHERE test_id = v_java_test_id AND position = 15;

  -- Java: position 16 — Metodi (compilazione ed esecuzione, args da riga di comando)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In Java, prima si compila il file sorgente con javac NomeFile.java, poi si esegue la classe compilata con java NomeClasse seguito dagli argomenti. I valori 5000 e .06 vengono passati come args[0] e args[1] al metodo main. Non si puo'' compilare e eseguire in un solo comando, e javac richiede l''estensione .java.'
  WHERE test_id = v_java_test_id AND position = 16;

  -- Java: position 17 — OOP e gestione errori (modificatori di accesso)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'public rende un membro accessibile da qualsiasi classe. static permette di chiamare un metodo senza creare un''istanza della classe. protected limita l''accesso al package corrente e alle sottoclassi. private restringe l''accesso alla sola classe in cui il membro e'' dichiarato. Queste definizioni sono fondamentali per l''incapsulamento in Java.'
  WHERE test_id = v_java_test_id AND position = 17;

  -- Java: position 18 — OOP e gestione errori (ereditarieta'', extends, super)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La keyword extends indica che DressShirt eredita da BaseShirt. Il costruttore della sottoclasse deve chiamare super() per invocare il costruttore della classe padre e inizializzare i campi ereditati (size, sleeve, color). Il parametro aggiuntivo String style e'' specifico della sottoclasse e viene assegnato con this.style = style.'
  WHERE test_id = v_java_test_id AND position = 18;

  -- Java: position 19 — OOP e gestione errori (override di metodi)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''annotazione @Override indica che un metodo nella sottoclasse sta ridefinendo un metodo della classe padre. Questo permette di fornire un''implementazione diversa dello stesso metodo. @Overload non esiste in Java, @Inherited riguarda le annotazioni, e @SuppressWarnings serve per sopprimere avvisi del compilatore.'
  WHERE test_id = v_java_test_id AND position = 19;

  -- Java: position 20 — OOP e gestione errori (modificatore private, incapsulamento)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il modificatore private limita l''accesso alla variabile radius alla sola classe CircleArea, realizzando il principio di incapsulamento. protected permetterebbe l''accesso anche dalle sottoclassi e dal package, public da qualsiasi classe, e static non e'' un modificatore di visibilita'' ma indica che il membro appartiene alla classe.'
  WHERE test_id = v_java_test_id AND position = 20;

  -- Java: position 21 — Stringhe e I/O (length, String.format, formato esadecimale)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il metodo length() restituisce il numero di caratteri in una stringa. String.format() con il formato "%x" converte un numero intero nella sua rappresentazione esadecimale. size() non e'' un metodo della classe String in Java, charAt() restituisce un singolo carattere, e "%o" produce la rappresentazione ottale, non esadecimale.'
  WHERE test_id = v_java_test_id AND position = 21;

  -- Java: position 22 — Fondamenti (tipi primitivi, ottimizzazione memoria)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Per usare meno memoria possibile: 3.14 richiede un double (8 byte, necessario per la precisione decimale), 1 sta in un byte (-128 a 127), 5000 sta in uno short (-32768 a 32767), e 33000 supera il range di short quindi richiede un integer. Float non e'' la scelta migliore per 3.14 perche'' i letterali decimali in Java sono double per default.'
  WHERE test_id = v_java_test_id AND position = 22;

  -- Java: position 23 — Stringhe e I/O (Scanner, import, System.in)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La classe Scanner si trova nel package java.util, quindi l''import corretto e'' java.util.Scanner. Lo Scanner va creato con System.in per leggere l''input da tastiera. Dopo l''uso, e'' buona pratica chiudere lo Scanner con il metodo close(). java.io.Scanner non esiste, System.out e'' per l''output, e Input non e'' una classe Java standard.'
  WHERE test_id = v_java_test_id AND position = 23;

  -- Java: position 24 — OOP e gestione errori (protected, visibilita'' ed ereditarieta'')
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Il modificatore protected rende le variabili accessibili dalla classe stessa, dal suo package e da tutte le sottoclassi che la estendono. Questo e'' esattamente il requisito: accessibilita'' dalla classe InputCircleArea e da qualsiasi classe che ne eredita. public sarebbe troppo permissivo, private troppo restrittivo.'
  WHERE test_id = v_java_test_id AND position = 24;

  -- Java: position 25 — Metodi (invocazione metodi statici)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Per invocare un metodo statico di un''altra classe si usa la sintassi NomeClasse.nomeMetodo(). Le parentesi tonde sono obbligatorie perche'' indicano una chiamata a metodo. Senza parentesi, Java interpreta l''espressione come un riferimento a un campo. Senza il nome della classe, il metodo non viene trovato se non e'' nella stessa classe.'
  WHERE test_id = v_java_test_id AND position = 25;

  -- Java: position 26 — Fondamenti (conversione di tipo, casting)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Quando un double viene convertito (cast) in byte, Java tronca la parte decimale e poi applica il modulo rispetto al range del byte (-128 a 127). Il valore 39.99 troncato diventa 39, che rientra nel range del byte. La conversione in int darebbe 39 (non 40, perche'' Java tronca, non arrotonda). Un double convertito in String manterrebbe il valore "39.99".'
  WHERE test_id = v_java_test_id AND position = 26;

  -- Java: position 27 — Fondamenti (wrapper classes, Integer.parseInt)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Integer.parseInt() e'' il metodo standard per convertire una stringa in un intero in Java. Si usa args[0] con le parentesi quadre per accedere al primo elemento dell''array. Int non e'' una classe wrapper valida (il nome corretto e'' Integer), e le parentesi tonde args(0) non sono la sintassi corretta per accedere agli array in Java.'
  WHERE test_id = v_java_test_id AND position = 27;

  -- Java: position 28 — Stringhe e I/O (isEmpty, stringa vuota vs null)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Una stringa inizializzata con "" e'' una stringa vuota, non null. Il metodo isEmpty() restituisce true quando la stringa ha lunghezza zero. region == null restituisce false perche'' la variabile punta a un oggetto stringa valido (anche se vuoto). Il metodo isNull() non esiste nella classe String, e length() restituisce 0, non null.'
  WHERE test_id = v_java_test_id AND position = 28;

  -- Java: position 29 — Metodi (main method, keyword static)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'La keyword static nella dichiarazione del metodo main e'' obbligatoria e non puo'' essere cambiata. La JVM invoca main senza creare un''istanza della classe, quindi deve essere statico. Il tipo di ritorno void e'' convenzionale ma teoricamente sostituibile, il nome del parametro args puo'' essere cambiato, e String puo'' essere sostituito con String[].'
  WHERE test_id = v_java_test_id AND position = 29;

  -- Java: position 30 — Operatori (compound assignment +=)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'L''operatore += (compound assignment) aggiunge il valore a destra alla variabile a sinistra e assegna il risultato. hours += 8 equivale a hours = hours + 8. L''operatore =+ non esiste come compound assignment (assegnerebbe +8), ++ e'' solo per incremento di 1, e = 8 sovrascrive il valore esistente.'
  WHERE test_id = v_java_test_id AND position = 30;

  -- Java: position 31 — Array e collections (dichiarazione array 1D)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'La sintassi corretta per dichiarare un array in Java e'' Type[] name = new Type[size]. Servono 5 elementi (indici da 0 a 4), quindi new String[5]. Le parentesi tonde () non sono valide per la dichiarazione di array in Java; si usano sempre le parentesi quadre []. String[4] creerebbe solo 4 posizioni (indici 0-3), insufficienti.'
  WHERE test_id = v_java_test_id AND position = 31;

  -- Java: position 32 — Cicli (for-each annidato, break, continue, &&)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Il ciclo esterno usa la sintassi for-each (for con i due punti) per iterare sui mesi. La condizione if (m == 2 && d > 28) usa l''AND logico: se il mese e'' febbraio E il giorno supera 28, continue salta all''iterazione successiva. L''operatore || sarebbe errato perche'' salterebbe tutti i giorni di febbraio e anche tutti i giorni > 28 di qualsiasi mese.'
  WHERE test_id = v_java_test_id AND position = 32;

  -- Java: position 33 — Fondamenti (tipi di dato, scelta appropriata)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'Una domanda testuale richiede il tipo String, una risposta vero/falso richiede Boolean, e un punteggio intero richiede int. Usare String per una risposta booleana o double per un punteggio intero spreca memoria e non riflette la semantica dei dati. char contiene un solo carattere, non una frase intera.'
  WHERE test_id = v_java_test_id AND position = 33;

  -- Java: position 34 — Operatori (aritmetici: *, %, / con interi)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Con operandi int: a * b = 20 * 3 = 60. a % b = 20 % 3 = 2 (resto della divisione intera). a / b = 20 / 3 = 6 (divisione intera, il risultato viene troncato, non 6.67). In Java la divisione tra due interi produce sempre un intero, scartando la parte decimale.'
  WHERE test_id = v_java_test_id AND position = 34;

  -- Java: position 35 — Stringhe e I/O (printf, formato numerico con separatore migliaia)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'Il formato %,d in printf inserisce il separatore delle migliaia (virgola nella localizzazione inglese) nella rappresentazione di un intero. Il nome della classe deve seguire la convenzione PascalCase (IncludeCommas, non includecommas). %d senza virgola non aggiunge il separatore, e %%.d non e'' un formato valido.'
  WHERE test_id = v_java_test_id AND position = 35;

  -- Java: position 36 — Array e collections (ArrayList, get, remove)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In un ArrayList con 5 elementi, l''ultimo elemento ha indice 4 (gli indici partono da 0). Il metodo get(4) restituisce l''elemento all''indice 4, e remove(4) lo rimuove. La sintassi regions[4] con parentesi quadre non e'' valida per ArrayList (si usa solo per array nativi). Il metodo delete() e item() non esistono in ArrayList.'
  WHERE test_id = v_java_test_id AND position = 36;

  -- Java: position 37 — Array e collections (array 2D, indicizzazione)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'In un array 2D, il primo indice indica la riga e il secondo la colonna. Il valore 79 si trova nella seconda riga (indice 1) e prima colonna (indice 0), quindi golfScores[1][0]. golfScores[0][0] restituisce 83 (prima riga, prima colonna), golfScores[1][1] restituisce 82, e golfScores[2][1] causerebbe un ArrayIndexOutOfBoundsException.'
  WHERE test_id = v_java_test_id AND position = 37;

  -- Java: position 38 — OOP e gestione errori (inner class, scope, static context)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Il codice non compila per diversi motivi: il metodo main e'' statico e non puo'' accedere direttamente alla variabile d''istanza circumference della inner class CircleCircumference. Inoltre, la inner class non statica non puo'' essere usata in un contesto statico senza un''istanza della classe esterna. La variabile circumference non e'' visibile nel main.'
  WHERE test_id = v_java_test_id AND position = 38;

  -- Java: position 39 — OOP e gestione errori (inner class, risoluzione scope)
  UPDATE question SET difficulty = 'INTERMEDIATE', explanation = 'L''errore "circumference cannot be resolved to a variable" indica che la variabile non e'' accessibile nel metodo main. Spostare la dichiarazione di circumference nella classe esterna CircleArea la rende accessibile direttamente. Spostarla in un metodo o spostare l''intera inner class non risolve il problema di accessibilita'' dal main.'
  WHERE test_id = v_java_test_id AND position = 39;

  -- Java: position 40 — Metodi (static, passaggio parametri, flusso di esecuzione)
  UPDATE question SET difficulty = 'ADVANCED', explanation = 'Il metodo runMatch modifica la variabile statica score. Quando match vale "Win", updatedScore = score + bonus = 220 + 30 = 250. Poi score = updatedScore aggiorna la variabile statica a 250. Al ritorno nel main, System.out.println(score) stampa 250 perche'' score e'' una variabile statica condivisa, non una copia locale.'
  WHERE test_id = v_java_test_id AND position = 40;

  -- Java: position 41 — Fondamenti (commenti Java)
  UPDATE question SET difficulty = 'BEGINNER', explanation = 'In Java, il simbolo // introduce un commento su singola riga: tutto cio'' che segue sulla stessa riga viene ignorato dal compilatore. /* inizia un commento multi-riga, /** inizia un commento Javadoc per la documentazione, e # non e'' un simbolo di commento valido in Java (lo e'' in Python e in shell script).'
  WHERE test_id = v_java_test_id AND position = 41;

  END IF; -- end Java section

  RAISE NOTICE 'All topics, subjects, difficulty and explanations applied successfully';

END $$;
