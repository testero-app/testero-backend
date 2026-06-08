# Contributing to testero-backend

Thank you for your interest in contributing to Testero! This document
explains how to contribute to the backend.

## Code of Conduct

All participants are expected to follow the
[Code of Conduct](https://github.com/testero-app/.github/blob/main/CODE_OF_CONDUCT.md).

## Language

All code, commit messages, issues, pull requests, and documentation must
be written in **English**. The user-facing interface may be localized
separately.

## Project Scope

Testero focuses on administering in-person tests and assessments for small
educational settings. Before starting work on a new feature or a significant
change, please **open an issue or contact the organization first** to discuss
whether it fits the project's scope and direction. This avoids wasted effort
on contributions that may fall outside what the project aims to do.

For small fixes (typos, bugs, minor improvements), feel free to open a pull
request directly.

## Project Board and Labels

Work is tracked on a shared Kanban board with consistent labels across
both repositories. See [PROJECT.md](./PROJECT.md) for details.

## How to Contribute

### Reporting Bugs

Open an [issue](https://github.com/testero-app/testero-backend/issues/new/choose)
using the "Bug report" template. Include:

- Testero version (commit hash or tag)
- JDK version and operating system
- Steps to reproduce
- Expected vs. actual behavior

### Suggesting Features

Open an [issue](https://github.com/testero-app/testero-backend/issues/new/choose)
using the "Feature request" template. Describe the problem the feature
would solve and any proposed solution.

### Submitting Code

1. **Fork** the repository
2. Create a branch from `main`:
   `git checkout -b feature/descriptive-name`
3. Make your changes with **DCO-signed commits** (see below)
4. Push to your fork and open a **Pull Request** against `main`
5. Ensure all checks pass (DCO check **and** test suite — see below)

## Developer Certificate of Origin (DCO)

Testero uses the [Developer Certificate of Origin](https://developercertificate.org/)
to certify the origin of contributions. Every commit must be signed off.

Sign off your commits with the `-s` flag:

```bash
git commit -s -m "feat: your change description"
```

This appends a line to your commit message:

```
Signed-off-by: Your Name <your@email.com>
```

By signing off, you certify the DCO 1.1:

> a. The contribution was created in whole or in part by you, and you
>    have the right to submit it under the open source license of this
>    project; OR
> b. The contribution is based upon previous work that, to the best of
>    your knowledge, is covered under an appropriate open source license
>    and you have the right under that license to submit that work with
>    modifications; OR
> c. The contribution was provided directly to you by some other person
>    who certified (a), (b), or (c) and you have not modified it.
>
> d. You understand and agree that this project and the contribution are
>    public and that a record of the contribution (including all personal
>    information submitted with it, including the sign-off) is maintained
>    indefinitely.

### Configure Git

Make sure your name and email are configured correctly. The email must
match (or be a verified alias of) your GitHub account:

```bash
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

### Convenience Alias

To sign off automatically with a shorter command:

```bash
git config --global alias.cs 'commit -s'
```

Then use `git cs -m "message"`.

### Fixing a Missing Sign-off

If a commit is missing its sign-off:

```bash
# Last commit only
git commit --amend --signoff
git push --force-with-lease

# Multiple commits (last N)
git rebase --signoff HEAD~N
git push --force-with-lease
```

## Authorship and AI Tools

You are welcome to use any AI coding tools you find helpful (Claude Code,
Copilot, and similar). However, **every contribution must be authored by a
human**, who takes full responsibility for the submitted code.

Commits must be attributed solely to their human author. Do **not** add
`Co-authored-by` trailers, "Generated with..." signatures, or any other
attribution referencing AI tools or assistants. The only trailer required
is the DCO `Signed-off-by:` line with your name.

## Testing

Tests are a **required** part of every contribution that changes backend
logic. The CI pipeline enforces this — pull requests with failing tests
cannot be merged.

### Running Tests Locally

```bash
./mvnw test
```

### What to Test

- **Bug fixes**: add a test that reproduces the bug before fixing it.
- **New features**: add unit tests for the new business logic.
- **Refactors**: ensure existing tests still pass; add tests if coverage
  is lacking.

### CI Pipeline

Tests run automatically at two stages:

1. **GitHub Actions** — on every PR to `main`. The "Run tests" check is
   a required status check; PRs cannot be merged until it passes.
2. **Docker build (Render)** — tests run again during the production
   image build. If tests fail, the deploy is aborted.

This double gate ensures that no untested code reaches production.

### Test Organization

| Directory | Purpose |
|-----------|---------|
| `src/test/java/app/testero/service/` | Unit tests for service-layer business logic |
| `src/test/java/app/testero/fixture/` | Shared test data fixtures (self-contained, no DB) |

Unit tests use **JUnit 5 + Mockito** (no Spring context, no database).
They should be fast and deterministic.

## Coding Standards

### Commit Messages

Use the [Conventional Commits](https://www.conventionalcommits.org/) format:

- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation
- `refactor:` for refactoring without functional changes
- `test:` for adding or modifying tests
- `chore:` for tooling, dependencies, or configuration changes

### Branch Naming

- `feature/descriptive-name` for new features
- `fix/bug-name` for fixes
- `docs/topic` for documentation-only changes

### Pull Requests

- One logical change per PR (avoid bundling unrelated changes)
- Include a clear description and motivation
- Reference related issues with `Closes #N`
- All commits must be DCO-signed (verified automatically)
- At least one approval is required before merging

## How Pull Requests Are Reviewed

After you open a pull request:

- A maintainer will review it and may request changes.
- All status checks (DCO check and test suite) must pass before merging.
- At least one approval is required.

Testero is a small project maintained in spare time, so reviews may take a
few days. Please be patient, and feel free to leave a polite reminder if a
PR goes unanswered for a while.

## License

By contributing to this repository, you agree that your contributions
will be licensed under the project's
[GNU Affero General Public License v3.0](./LICENSE).

## Questions

For questions about contributing, open a
[Discussion](https://github.com/testero-app/testero-backend/discussions).
