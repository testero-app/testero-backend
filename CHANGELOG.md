# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0](https://github.com/testero-app/testero-backend/compare/v1.1.1...v1.2.0) (2026-06-09)


### Features

* add activation fields to ClassTest and startedAt to Submission ([#11](https://github.com/testero-app/testero-backend/issues/11)) ([09d6880](https://github.com/testero-app/testero-backend/commit/09d68809a1c41671ef9be6075e5bfa291d35b2af))
* add dev seed data with Liquibase contexts ([#18](https://github.com/testero-app/testero-backend/issues/18)) ([019f320](https://github.com/testero-app/testero-backend/commit/019f320b298a7f6f4049ba08f220126181a0aa13))
* add Liquibase database migrations for initial schema ([#14](https://github.com/testero-app/testero-backend/issues/14)) ([52257a6](https://github.com/testero-app/testero-backend/commit/52257a638355c64ff9a771154ce8eac92666fcb9))
* add Liquibase dev seed with demo data for contributors ([#55](https://github.com/testero-app/testero-backend/issues/55)) ([9a38388](https://github.com/testero-app/testero-backend/commit/9a383885ab22ffeb64f48554fe6f5cb18419fed6))
* add Spring profiles (dev/prod) and Docker Compose for local PostgreSQL ([#16](https://github.com/testero-app/testero-backend/issues/16)) ([bbd5b5c](https://github.com/testero-app/testero-backend/commit/bbd5b5c7d93c2fb6b2e76e8854a0e4ffe16397b3))
* add TeacherClass M:N relation for teacher-class assignment ([#13](https://github.com/testero-app/testero-backend/issues/13)) ([4519725](https://github.com/testero-app/testero-backend/commit/4519725c680ef92599b2f593412284e71e013936))
* scaffold Spring Boot backend with auth, JWT and CORS config ([#3](https://github.com/testero-app/testero-backend/issues/3)) ([9cfed30](https://github.com/testero-app/testero-backend/commit/9cfed30201f752e6b0879d888ac2e242e549d4fd))


### Bug Fixes

* add splitStatements:false to dev seed changeset ([#62](https://github.com/testero-app/testero-backend/issues/62)) ([8bbe99f](https://github.com/testero-app/testero-backend/commit/8bbe99f83ca149c78a66468383b34db083bbd8b3))
* **ci:** use config file in release-please to respect snapshot:false ([#59](https://github.com/testero-app/testero-backend/issues/59)) ([25d600a](https://github.com/testero-app/testero-backend/commit/25d600ab7212e88982434709701d938c1facc258))
* **ci:** use skip-snapshot to prevent SNAPSHOT release PRs ([#64](https://github.com/testero-app/testero-backend/issues/64)) ([526045d](https://github.com/testero-app/testero-backend/commit/526045d56ff9f6211e4dab75612ae0f92a98377d))
* **deps:** bump Tomcat to 11.0.22 and PostgreSQL driver to 42.7.11 ([#9](https://github.com/testero-app/testero-backend/issues/9)) ([bfd32b3](https://github.com/testero-app/testero-backend/commit/bfd32b3cb61517184fe07e306339eabd0a5888cd))
* **deps:** upgrade spring-dotenv to 5.1.0 for Spring Boot 4.x support ([#15](https://github.com/testero-app/testero-backend/issues/15)) ([cae2e9e](https://github.com/testero-app/testero-backend/commit/cae2e9ea634687c01e0aa385a02687e27bf2983d))
* **security:** address CodeQL alerts for CSRF and workflow permissions ([#52](https://github.com/testero-app/testero-backend/issues/52)) ([d9549fe](https://github.com/testero-app/testero-backend/commit/d9549fe456419b19d437159ade80d21126c54dc1))
* switch Render to Docker runtime and add Dockerfile ([#7](https://github.com/testero-app/testero-backend/issues/7)) ([7c3f5c1](https://github.com/testero-app/testero-backend/commit/7c3f5c1b42c4b4cb978855a982d7c9436bf05a4a))
* use correct question type and improve project setup ([#40](https://github.com/testero-app/testero-backend/issues/40)) ([93826c8](https://github.com/testero-app/testero-backend/commit/93826c8132316a2cdfb196b778eb23959cfc7343))


### Documentation

* add governance files (README, CONTRIBUTING, DCO, templates) ([#1](https://github.com/testero-app/testero-backend/issues/1)) ([80f4e09](https://github.com/testero-app/testero-backend/commit/80f4e09a65196e280d4de511de53a92709cf15ea))
* align data-model diagram with Assessment rename and remove v2.2 ([#48](https://github.com/testero-app/testero-backend/issues/48)) ([0f1b2d7](https://github.com/testero-app/testero-backend/commit/0f1b2d758601eb5bd8656d98dcbbc5ebb868449b))
* update README with env vars table and stack details ([#8](https://github.com/testero-app/testero-backend/issues/8)) ([ea5ab7f](https://github.com/testero-app/testero-backend/commit/ea5ab7f3355e98f1d8745d3b407a51e8df6534c2))

## [1.1.1](https://github.com/testero-app/testero-backend/compare/v1.1.0...v1.1.1) (2026-06-09)


### Bug Fixes

* add splitStatements:false to dev seed changeset ([#62](https://github.com/testero-app/testero-backend/issues/62)) ([8bbe99f](https://github.com/testero-app/testero-backend/commit/8bbe99f83ca149c78a66468383b34db083bbd8b3))
* **ci:** use skip-snapshot to prevent SNAPSHOT release PRs ([#64](https://github.com/testero-app/testero-backend/issues/64)) ([526045d](https://github.com/testero-app/testero-backend/commit/526045d56ff9f6211e4dab75612ae0f92a98377d))

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
