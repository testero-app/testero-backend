# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0](https://github.com/testero-app/testero-backend/compare/v1.0.1...v1.1.0) (2026-06-09)


### Features

* add Liquibase dev seed with demo data for contributors ([#55](https://github.com/testero-app/testero-backend/issues/55)) ([9a38388](https://github.com/testero-app/testero-backend/commit/9a383885ab22ffeb64f48554fe6f5cb18419fed6))


### Bug Fixes

* **ci:** use config file in release-please to respect snapshot:false ([#59](https://github.com/testero-app/testero-backend/issues/59)) ([25d600a](https://github.com/testero-app/testero-backend/commit/25d600ab7212e88982434709701d938c1facc258))

## [1.0.1](https://github.com/testero-app/testero-backend/compare/v1.0.0...v1.0.1) (2026-06-08)


### Bug Fixes

* **security:** address CodeQL alerts for CSRF and workflow permissions ([#52](https://github.com/testero-app/testero-backend/issues/52)) ([d9549fe](https://github.com/testero-app/testero-backend/commit/d9549fe456419b19d437159ade80d21126c54dc1))

## [1.0.0] - 2026-06-06

### Added

- Spring Boot backend scaffold with JWT authentication and CORS configuration
- Spring profiles (`dev`, `prod`) with Docker Compose for local PostgreSQL
- Liquibase database migrations for the initial schema
- Role-based entity model: User, Teacher, Student, Admin
- TeacherClass M:N relation for teacher-class assignment
- Activation fields on ClassTest and `startedAt` on Submission
- Render deploy configuration with Dockerfile
- Environment variables documentation and `.env.example`

### Changed

- Upgraded Spring Boot from 3.3.7 to 4.0.6
- Upgraded spring-dotenv to 5.1.0 for Spring Boot 4.x support
- Refactored entities for multi-role model (v2.0)
- Added Lombok to replace boilerplate in entity classes
- Bumped Tomcat to 11.0.22 and PostgreSQL driver to 42.7.11

[1.0.0]: https://github.com/testero-app/testero-backend/releases/tag/v1.0.0
