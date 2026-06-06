# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
