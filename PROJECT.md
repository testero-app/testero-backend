# Project Management

This document describes how the Testero project organizes and tracks
work across both repositories
([testero-backend](https://github.com/testero-app/testero-backend) and
[testero-web](https://github.com/testero-app/testero-web)).

## Kanban Board

All work is tracked on a shared organization-level board:

**[Testero Board](https://github.com/orgs/testero-app/projects/1)**

The board follows a 5-column workflow:

| Column | Purpose |
|--------|---------|
| **Backlog** | Ideas and future tasks, not yet prioritized |
| **Todo** | Next up — prioritized and ready to be picked up |
| **In Progress** | Currently being worked on (assigned to someone) |
| **In Review** | Pull request open, awaiting review |
| **Done** | Completed and merged |

Cards move left to right. When you start working on an issue, move it
to **In Progress** and assign yourself. When you open a PR, move it to
**In Review**. Once merged, it goes to **Done**.

## Labels

Labels are consistent across both repositories and organized in three
groups:

### Component

| Label | Description |
|-------|-------------|
| `fe` | Frontend (testero-web) |
| `be` | Backend (testero-backend) |
| `devops` | CI/CD, infrastructure, deployment |

### Type

| Label | Description |
|-------|-------------|
| `feature` | New functionality |
| `fix` | Bug fix |
| `refactor` | Code improvement without behavior change |
| `docs` | Documentation only |
| `test` | Tests only |

### Priority

| Label | Description |
|-------|-------------|
| `priority: high` | Should be addressed soon |
| `priority: low` | Nice to have, no rush |

A typical issue carries one label per group — for example:
`be` + `feature` + `priority: high`.

### Community

| Label | Description |
|-------|-------------|
| `good first issue` | Good for newcomers |
| `help wanted` | Extra attention is needed |

## Issue Templates

Both repositories provide two issue templates:

- **Bug report** — for reporting problems (auto-labels: `fix`)
- **Feature request** — for suggesting new functionality (auto-labels: `feature`)

Add the relevant component (`fe`, `be`, `devops`) and priority labels
manually after creating the issue.
