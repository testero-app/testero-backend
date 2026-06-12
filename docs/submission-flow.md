# Submission Flow

This document describes the complete lifecycle of a student submission,
including incremental answer saving, auto-close on timeout, and recovery
mechanisms.

## Submission States

```
                ┌─────────────┐
       start    │ IN_PROGRESS │
    ──────────► │             │
                └──────┬──────┘
                       │
              ┌────────┴────────┐
              │                 │
     student submits     timer expires (+1s)
              │                 │
              ▼                 ▼
      ┌───────────┐     ┌─────────────┐
      │ SUBMITTED │     │ AUTO_CLOSED │
      └───────────┘     └─────────────┘
```

| Status        | Meaning                                      |
|---------------|----------------------------------------------|
| IN_PROGRESS   | Student has started but not yet submitted     |
| SUBMITTED     | Student submitted manually                   |
| AUTO_CLOSED   | System closed the submission after timeout   |

## Lifecycle

### 1. Start Assessment

**Endpoint:** `POST /assessments/{snapshotId}/start`

- Creates a `Submission` record with `status = IN_PROGRESS` and
  `started_at = now()`
- Idempotent: if the student already has an IN_PROGRESS submission for
  this snapshot, it is returned instead of creating a new one
- Publishes a `SubmissionStartedEvent` that schedules an auto-close task
  at `started_at + timer_minutes + 1 second`

### 2. Incremental Answer Save

**Endpoint:** `PUT /submissions/{id}/answers/{questionSnapshotId}`

- Saves a single answer as the student navigates between questions
- Idempotent upsert: creates the `UserAnswer` if new, updates if it
  already exists
- Selected options are replaced (delete + recreate) on update
- Only works when `status = IN_PROGRESS`
- Returns `204 No Content`
- **Fire-and-forget from the frontend** — errors are silently ignored

**When does the frontend save?**

Every time the student navigates to a different question (prev/next
buttons, sidebar click, or keyboard arrows), the current question's
answer is sent to the backend in the background. This provides a natural
debounce — answers are not sent on every keystroke.

### 3. Final Submit

**Endpoint:** `PUT /submissions/{id}`

- Deletes any pre-existing answers from incremental saves
- Re-creates all answers from the final submission payload
- Scores all multiple-choice answers against the snapshot's correct
  options
- Sets `status = SUBMITTED`, `submitted_at = now()`
- Publishes a `SubmissionCompletedEvent` that cancels the auto-close task
- Returns the scored feedback immediately

### 4. Auto-Close (Timeout)

When the scheduled auto-close task fires:

1. Checks if `status` is still `IN_PROGRESS` (if the student already
   submitted, this is a no-op)
2. Sets `status = AUTO_CLOSED`, `submitted_at = now()`
3. Scores whatever answers were saved incrementally
4. If no answers were saved, sets `score = 0.0`

Both `autoCloseSubmission` and `submitAnswers` check `status` inside a
`@Transactional` method. PostgreSQL row-level locking ensures only one
succeeds if they race.

## Recovery Mechanisms

### On Application Startup

When the Spring Boot application starts (`ApplicationReadyEvent`):

1. Queries all submissions with `status = IN_PROGRESS`
2. For each, calculates the auto-close deadline
   (`started_at + timer_minutes + 1s`)
3. If the deadline is **in the past** → closes immediately
4. If the deadline is **in the future** → schedules a new auto-close task

This handles server restarts during an active assessment.

### Nightly Safety Net

A `@Scheduled` cron job runs at **00:01 every day**:

1. Queries all submissions with `status = IN_PROGRESS`
2. For each, checks if the deadline has passed
3. If yes → closes the submission

This catches edge cases like crashes during the recovery process itself.

## Scoring

Scoring is identical for both manual submission and auto-close:

- **Multiple choice:** student's selected options are compared to the
  snapshot's correct options. Exact set match = correct, any mismatch =
  wrong, empty selection = unanswered (no penalty)
- **Open-ended:** not automatically scored (`is_correct = null`)
- Points are configured per assessment snapshot (`pts_correct`,
  `pts_wrong`)

## Database Schema

The `submission` table includes:

| Column                 | Type         | Description                   |
|------------------------|--------------|-------------------------------|
| status                 | varchar(20)  | IN_PROGRESS, SUBMITTED, AUTO_CLOSED |
| started_at             | timestamp    | When the student started      |
| submitted_at           | timestamp    | When submitted/auto-closed    |
| score                  | double       | Total score (null if in progress) |

The `user_answer` table has a unique constraint on
`(submission_id, question_snapshot_id)` to support idempotent upserts.

## Architecture

```
SubmissionService
  ├── startSubmission()      → publishes SubmissionStartedEvent
  ├── saveAnswer()           → upsert single answer
  ├── submitAnswers()        → final submit, publishes SubmissionCompletedEvent
  └── autoCloseSubmission()  → system-level close + scoring

SubmissionAutoCloseScheduler
  ├── @EventListener(SubmissionStartedEvent)    → schedule task
  ├── @EventListener(SubmissionCompletedEvent)  → cancel task
  ├── @EventListener(ApplicationReadyEvent)     → recovery
  └── @Scheduled(cron = "0 1 0 * * *")         → nightly cleanup
```

Events are used to decouple the service from the scheduler, avoiding
circular dependencies.
