# TODO — Backend: endpoint mancanti per UI restyling studente

Ogni card è un intervento indipendente (o quasi). Risolverli uno alla volta nell'ordine indicato.

---

## 1. Campo `type` su Assessment (CERTIFICATION / TRAINING)

**Perché:** La tab "I miei risultati" nel FE mostra un tag "Certificazione" o "Allenamento" per ogni risultato. Oggi il FE indovina dal titolo (`guessType(title)`) — fragile e sbagliato.

**Cosa fare (BE):**
- [x] Changeset `v1.6/001-add-type-to-assessment.yaml`:
  - `ALTER TABLE test ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'CERTIFICATION'`
  - `ALTER TABLE assessment_snapshot ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'CERTIFICATION'`
- [x] Aggiungere campo `type` a `Assessment.java` e `AssessmentSnapshot.java` (entità JPA)
- [x] Propagare type nella creazione dello snapshot (`SnapshotService`)
- [x] Aggiungere `type` al DTO `AssessmentListResponse.AssessmentListItem`
- [x] Aggiungere `type` al DTO `SubmissionHistoryResponse.SubmissionSummary`
- [x] Aggiornare mapping in `AssessmentService` e `SubmissionService`
- [x] Test: verificare che GET `/assessments` e GET `/submissions/mine` restituiscano il campo `type`

**Cosa fare (FE) dopo:**
- `RisultatiTab.tsx` → usare `s.type` dal server invece di `guessType(s.assessment_title)`
- `CertificazioniTab.tsx` → filtrare assessments dove `type === 'CERTIFICATION'`

**File BE coinvolti:**
- `db/changelog/v1.6/001-add-type-to-assessment.yaml` (new)
- `entity/assessment/Assessment.java`
- `entity/snapshot/AssessmentSnapshot.java`
- `service/SnapshotService.java`
- `service/AssessmentService.java`
- `service/SubmissionService.java`
- `dto/AssessmentListResponse.java`
- `dto/SubmissionHistoryResponse.java`

---

## 2. Notification preferences (CRUD)

**Perché:** La sezione "Notifiche" nel Profilo ha 3 toggle (esito email, promemoria scadenze, novità). Oggi sono mock in state locale — nessuna persistenza.

**Cosa fare (BE):**
- [x] Changeset `v1.6/002-create-notification-preference.yaml`:
  - Tabella `notification_preference`: `id UUID PK`, `user_id UUID FK → app_user`, `type VARCHAR(50) NOT NULL`, `enabled BOOLEAN NOT NULL DEFAULT true`, `created_at`, `updated_at`
  - Constraint: `UNIQUE(user_id, type)`
- [x] Entità `NotificationPreference.java` in `entity/user/`
- [x] Repository `NotificationPreferenceRepository.java`
- [x] DTO `NotificationPreferenceResponse.java` — record con `type` e `enabled`
- [x] DTO `UpdateNotificationPreferencesRequest.java` — `List<{type, enabled}>`
- [x] Endpoint `GET /api/users/me/notifications`:
  - Se l'utente non ha ancora righe, restituire i 3 default: `EXAM_RESULT=true`, `DEADLINE_REMINDER=true`, `PRODUCT_NEWS=false`
- [x] Endpoint `PUT /api/users/me/notifications`:
  - Upsert delle preferenze ricevute
- [x] Logica in `UserService.java`
- [x] Test integration per entrambi gli endpoint

**Cosa fare (FE) dopo:**
- `ProfilePage.tsx` → fetch GET al mount, PUT al toggle change

**File BE coinvolti:**
- `db/changelog/v1.6/002-create-notification-preference.yaml` (new)
- `entity/user/NotificationPreference.java` (new)
- `repository/NotificationPreferenceRepository.java` (new)
- `dto/NotificationPreferenceResponse.java` (new)
- `dto/UpdateNotificationPreferencesRequest.java` (new)
- `controller/UserController.java` (add 2 endpoints)
- `service/UserService.java` (add preference logic)

---

## 3. Entità Topic + endpoint `GET /api/topics`

**Perché:** La tab "Allenamento" mostra argomenti (Fondamenti Python I, II, Git…) con capitoli e conteggi domande per livello. Oggi è tutto mock nel FE (`MOCK_TOPICS` array hardcoded).

**Modello attuale:** `Subject` è piatto (id, label). Le domande sono legate a Subject via `question_subject`. Non c'è gerarchia Topic→Subject.

**Cosa fare (BE):**
- [x] Changeset `v1.6/003-create-topic-tables.yaml`:
  - Tabella `topic`: `id UUID PK`, `title VARCHAR(200)`, `description TEXT`, `abbreviation VARCHAR(4)`, `position INT DEFAULT 0`, `enabled BOOLEAN DEFAULT true`, `created_at`, `updated_at`
  - Tabella `topic_subject`: `topic_id UUID FK → topic`, `subject_id UUID FK → subject`, `position INT DEFAULT 0`, `PK(topic_id, subject_id)`
- [x] Changeset `v1.6/004-add-difficulty-to-question.yaml`:
  - `ALTER TABLE question ADD COLUMN difficulty VARCHAR(20)` (nullable, enum: BASE, INTERMEDIATE, ADVANCED)
  - Serve per contare domande per livello dentro ogni capitolo/subject
- [x] Entità `Topic.java` e `TopicSubject.java` in `entity/assessment/`
- [x] Aggiungere campo `difficulty` a `Question.java`
- [x] Repository `TopicRepository.java`
- [x] Service `TopicService.java`:
  - `getTopics()` — lista topic abilitati con subjects (capitoli) e conteggi domande per livello
  - Query: join `topic → topic_subject → subject → question_subject → question` raggruppando per difficulty
- [x] Controller `TopicController.java` — `GET /api/topics` (richiede autenticazione studente)
- [x] DTO `TopicListResponse.java`:
  ```
  TopicItem: id, title, abbreviation, description, enabled, chapters[], totalChapters, totalQuestions
  ChapterItem: id, label, questionCounts: {base, inter, avanz}
  ```
- [x] Test integration per il nuovo endpoint

**Cosa fare (FE) dopo:**
- `AllenamentoTab.tsx` → fetch `GET /api/topics`, rimpiazzare `MOCK_TOPICS`
- `api.js` → aggiungere `fetchTopics(token)`

**File BE coinvolti:**
- `db/changelog/v1.6/003-create-topic-tables.yaml` (new)
- `db/changelog/v1.6/004-add-difficulty-to-question.yaml` (new)
- `entity/assessment/Topic.java` (new)
- `entity/assessment/TopicSubject.java` (new)
- `entity/assessment/Question.java` (add difficulty)
- `repository/TopicRepository.java` (new)
- `service/TopicService.java` (new)
- `controller/TopicController.java` (new)
- `dto/TopicListResponse.java` (new)

---

## 4. Endpoint `GET /api/topics/{id}/chapters`

**Perché:** Il Configuratore mostra i capitoli selezionabili con conteggi per livello e il numero di domande disponibili in base ai filtri scelti. Dipende da #3.

**Cosa fare (BE):**
- [x] Aggiungere a `TopicController.java`: `GET /api/topics/{topicId}/chapters`
- [x] Aggiungere a `TopicService.java`: `getTopicChapters(topicId)`:
  - Validare che il topic esista ed è abilitato
  - Restituire subjects del topic con conteggi domande per livello
  - Conteggio totale disponibile
- [x] DTO `TopicChaptersResponse.java`:
  ```json
  {
    "topic_id": "uuid",
    "topic_title": "Fondamenti Python I",
    "chapters": [
      { "id": "subject-uuid", "label": "Variabili e tipi",
        "question_counts": { "base": 8, "intermedio": 5, "avanzato": 1 } }
    ],
    "available_questions": { "base": 24, "intermedio": 16, "avanzato": 6, "total": 46 }
  }
  ```
- [x] Test integration

**Cosa fare (FE) dopo:**
- `ConfiguratorePage.tsx` → fetch capitoli reali, rimpiazzare `MOCK_CHAPTERS`
- `api.js` → aggiungere `fetchTopicChapters(topicId, token)`

**File BE coinvolti:**
- `controller/TopicController.java` (add endpoint)
- `service/TopicService.java` (add method)
- `dto/TopicChaptersResponse.java` (new)

---

## 5. Endpoint `POST /api/training/start`

**Perché:** Il pulsante "Avvia allenamento" nel Configuratore deve creare una sessione di allenamento dinamica — selezionando domande dal pool in base a capitoli, difficoltà e numero scelti dallo studente. Dipende da #3 e #4.

**Cosa fare (BE):**
- [x] Controller `TrainingController.java` — `POST /api/training/start`
- [x] DTO `TrainingStartRequest.java`:
  ```json
  {
    "topic_id": "uuid",
    "chapter_ids": ["subject-uuid-1", "subject-uuid-2"],
    "difficulty": "base",
    "question_count": 15,
    "timer_enabled": true
  }
  ```
- [x] DTO `TrainingStartResponse.java`:
  ```json
  {
    "submission_id": "uuid",
    "assessment_snapshot_id": "uuid",
    "timer_minutes": 23,
    "total_questions": 15
  }
  ```
- [x] Service `TrainingService.java`:
  1. Validare topic e chapter_ids (devono appartenere al topic)
  2. Query domande: `question JOIN question_subject` WHERE `subject_id IN (chapter_ids)` AND `difficulty = ?` (o tutte se "mista")
  3. Selezionare random `question_count` domande dal pool
  4. Creare un `AssessmentSnapshot` con `type = 'TRAINING'`, titolo auto-generato (es. "Allenamento — Fondamenti Python I")
  5. Creare `QuestionSnapshot` + `OptionSnapshot` per le domande selezionate
  6. Creare `Submission` con status `IN_PROGRESS`
  7. Calcolare `timer_minutes` = `question_count * 1.5` (arrotondato) se timer abilitato, altrimenti null
  8. Restituire submission_id
- [x] Modificare `QuestionPrepService.java` — estrarre la logica di shuffling/selezione in metodo riusabile con filtri opzionali per subject e difficulty
- [x] Test unitari per la selezione domande (edge case: pool insufficiente)
- [x] Test integration per l'endpoint

**Cosa fare (FE) dopo:**
- `AppRouter.tsx` / `ConfiguratorePage.tsx` → POST, poi navigare a `/assessment` con i dati ricevuti
- `api.js` → aggiungere `startTrainingSession(request, token)`
- Rimuovere l'`alert()` mockup

**File BE coinvolti:**
- `controller/TrainingController.java` (new)
- `service/TrainingService.java` (new)
- `service/QuestionPrepService.java` (modify — extract reusable selection logic)
- `dto/TrainingStartRequest.java` (new)
- `dto/TrainingStartResponse.java` (new)

---

## Fix solo FE (nessun lavoro BE)

Questi non richiedono endpoint nuovi — i dati sono già disponibili.

### ~~FE-fix A: Usare `difficulty` dal server in CertificazioniTab~~ ✔ DONE
- `AssessmentListItem` già restituisce `difficulty` — il FE usa `a.difficulty` dal server con `formatDifficulty()`

### ~~FE-fix B: Usare `explanation` nel Ripasso~~ ✔ DONE
- `ExplanationBox` legge `question.explanation` dal server con fallback su `motivation`

### ~~FE-fix C: Caricare submission specifica dalla history~~ ✔ DONE
- History row click → ResultsView con summary da `TSubmissionSummary` via location state
- "Rivedi gli errori" → fetch review → RipassoView con dati reali
- PR: testero-app/testero-web#101

---

# ════════════════════════════════════════════════════════════════
# T0 — Allineamento UI/UX al design definitivo (Testero Restyling.zip)
# ════════════════════════════════════════════════════════════════
#
# Riferimento: testero-assets/Testero Restyling.zip
# Screenshots vincolanti: design_handoff_testero/screenshots/01–12
# Token ufficiali: design_handoff_testero/testero-theme.css
#
# La UI è già al ~95%. Queste card coprono i delta residui.

---

## ~~T0-1. [FE] Icona card argomento: quadrata → circolare (AllenamentoTab)~~ ✔ DONE

- [x] `AllenamentoTab.module.css` → `.cardIcon` → `border-radius: 50%`

**File coinvolti:**
- `src/components/AllenamentoTab.module.css`

**Effort:** XS (~5 min)

---

## ~~T0-2. [FE] Icona riga certificazione: quadrata → circolare (CertificazioniTab)~~ ✔ DONE

- [x] `CertificazioniTab.module.css` → `.rowIcon` → `border-radius: 50%`

**File coinvolti:**
- `src/components/CertificazioniTab.module.css`

**Effort:** XS (~5 min)

---

## ~~T0-3. [FE] Toggle "Tutte/Solo errate" nel Ripasso: stile teal → neutro~~ ✔ DONE

- [x] `RipassoPage.module.css` → `.toggleBtnActive` → navy anziché teal

**File coinvolti:**
- `src/components/RipassoPage.module.css`
- `src/components/RipassoPage.tsx` (se serve adattare classi)

**Effort:** S (~15 min)

---

## ~~T0-4. [FE] Icona busta nel campo Email del Profilo~~ ✔ DONE

- [x] `ProfilePage.tsx` → aggiunta SVG busta nel campo Email

**File coinvolti:**
- `src/components/ProfilePage.tsx`

**Effort:** XS (~10 min)

---

## ~~T0-5. [FE] Label argomento nelle card del Ripasso~~ ✔ DONE

- [x] `AssessmentContext.tsx` → aggiunto `subjects` a `TReviewQuestion`
- [x] `RipassoPage.tsx` → usa `question.subjects?.[0]?.label` come topic label

**File coinvolti:**
- `src/context/AssessmentContext.tsx` (tipo)
- `src/components/RipassoPage.tsx` (consumo)
- `src/components/ReviewQuestionCard.tsx` (display, se necessario)

**Effort:** S (~20 min)

---

## ~~T0-6. [Seed] Aggiungere `explanation` alle 147 domande seed~~ ✔ DONE

- [x] Python: 16 explanations
- [x] JS set1: 45 explanations
- [x] JS set2: 45 explanations
- [x] Java: 41 explanations
- Nota: `docs/db_seed/` è gitignored — le modifiche sono solo locali

**File coinvolti:**
- `docs/db_seed/prod/30_tests/10_python_certification.sql` (32 domande)
- `docs/db_seed/prod/30_tests/20_js_certification_set1.sql` (~45 domande)
- `docs/db_seed/prod/30_tests/30_js_certification_set2.sql` (~45 domande)
- `docs/db_seed/prod/30_tests/40_java_certification.sql` (~25 domande)

**Effort:** L (~2-3 ore, il grosso è scrivere 147 spiegazioni)

---

## ~~T0-7. [FE] Aggiornare `tokens.css` al `testero-theme.css` ufficiale~~ ✔ DONE

- [x] Allineati shadow values (`.05`, `16px 40px`, `.12`)
- [x] Aggiunto `--ts-r-phone`
- [x] Rimosso `--ts-shadow-card-el`, sostituito con `--ts-shadow-pop` in RisultatiTab
- [x] Rimosso `--m-shadow-md` inutilizzato

**File coinvolti:**
- `src/styles/tokens.css`
- Potenzialmente vari `.module.css` se usano alias rimossi

**Effort:** M (~30-45 min)

---

# Riepilogo T0

| Card | Repo | Effort | Dipendenze |
|------|------|--------|------------|
| T0-1 | FE | XS | ✔ DONE |
| T0-2 | FE | XS | ✔ DONE |
| T0-3 | FE | S | ✔ DONE |
| T0-4 | FE | XS | ✔ DONE |
| T0-5 | FE | S | ✔ DONE |
| T0-6 | Seed | L | ✔ DONE (locale, gitignored) |
| T0-7 | FE | M | ✔ DONE |

**T0 completato.** FE: branch `feature/t0-ui-alignment` su testero-web.

---

# ────────────────────────────────────────────────────────────────

# URGENT — Review seed questions against current data model

The seed questions in `docs/db_seed/prod/30_tests/` were written before the
latest model changes (question_subject with weight, per-question points,
explanation field, etc.). They likely miss:

- [x] `question_subject` entries — questions are not tagged with subjects
- [x] `explanation` field — all 147 questions now have Italian explanations
- [x] `points` per question — set to 1.00 on all questions
- [x] `question_snapshot_subject` — snapshots now include subject mappings
- [x] `difficulty` + `passing_score` — set ADVANCED + 70% on all assessments and snapshots
- [x] `question_snapshot.points` — snapshots now copy points from questions
- [x] General data consistency check against the current schema

Files to review:
- `docs/db_seed/prod/30_tests/10_python_certification.sql`
- `docs/db_seed/prod/30_tests/20_js_certification_set1.sql`
- `docs/db_seed/prod/30_tests/30_js_certification_set2.sql`
- `docs/db_seed/prod/30_tests/40_java_certification.sql`

---

# Issue #31: Structured Logging with Request Correlation

- [x] Create `RequestIdFilter` (MDC + X-Request-Id header)
- [x] ~~Register filter in SecurityConfig~~ Not needed — @Component + @Order runs before security chain
- [x] Create `logback-spring.xml` (dev human-readable, prod JSON)
- [x] Remove logging levels from application-*.properties
- [x] Add log statements to AuthService, SubmissionService, JwtAuthFilter
- [x] Write `RequestIdFilterTest`
- [x] Update `AuthControllerTest` to verify X-Request-Id header
- [x] Run full test suite — 133 tests, 0 failures
