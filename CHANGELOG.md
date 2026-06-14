# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.12.1](https://github.com/testero-app/testero-backend/compare/v1.12.0...v1.12.1) (2026-06-14)


### Bug Fixes

* add braces to single-line if statements (checkstyle) ([#129](https://github.com/testero-app/testero-backend/issues/129)) ([60b6541](https://github.com/testero-app/testero-backend/commit/60b6541b699e40325f610e867c014d9ac0d6ba5f))

## [1.12.0](https://github.com/testero-app/testero-backend/compare/v1.11.0...v1.12.0) (2026-06-14)


### Features

* include student submission status in assessment list response ([#127](https://github.com/testero-app/testero-backend/issues/127)) ([1e53549](https://github.com/testero-app/testero-backend/commit/1e535499c0ef8d57faf7d9de299d37e3033198f9)), closes [#108](https://github.com/testero-app/testero-backend/issues/108)

## [1.11.0](https://github.com/testero-app/testero-backend/compare/v1.10.1...v1.11.0) (2026-06-14)


### Features

* add explanation field to Question and QuestionSnapshot ([#125](https://github.com/testero-app/testero-backend/issues/125)) ([6a3f76f](https://github.com/testero-app/testero-backend/commit/6a3f76fb453323e4eec5b83d25b1c731d1eac80d)), closes [#104](https://github.com/testero-app/testero-backend/issues/104)

## [1.10.1](https://github.com/testero-app/testero-backend/compare/v1.10.0...v1.10.1) (2026-06-14)


### Bug Fixes

* add is_fallback field to OptionSnapshot ([#123](https://github.com/testero-app/testero-backend/issues/123)) ([9311562](https://github.com/testero-app/testero-backend/commit/93115629e7dd738a447cce62fa4121f995dd5d7a)), closes [#118](https://github.com/testero-app/testero-backend/issues/118)

## [1.10.0](https://github.com/testero-app/testero-backend/compare/v1.9.2...v1.10.0) (2026-06-14)


### Features

* add difficulty field to Assessment and AssessmentSnapshot ([#121](https://github.com/testero-app/testero-backend/issues/121)) ([0002980](https://github.com/testero-app/testero-backend/commit/000298059618a3e8432a6884828b72e861f72f2b)), closes [#102](https://github.com/testero-app/testero-backend/issues/102)

## [1.9.2](https://github.com/testero-app/testero-backend/compare/v1.9.1...v1.9.2) (2026-06-13)


### Documentation

* move diagrams and docs to testero-docs repository ([#99](https://github.com/testero-app/testero-backend/issues/99)) ([1ba6e58](https://github.com/testero-app/testero-backend/commit/1ba6e58164513aea70a0143919109d439b52321a))

## [1.9.1](https://github.com/testero-app/testero-backend/compare/v1.9.0...v1.9.1) (2026-06-13)


### Bug Fixes

* resolve checkstyle violations (file length, constant name) ([#95](https://github.com/testero-app/testero-backend/issues/95)) ([a2fe6d6](https://github.com/testero-app/testero-backend/commit/a2fe6d6f321cf4b099fbaf088f2e03f1ade0b5b6))

## [1.9.0](https://github.com/testero-app/testero-backend/compare/v1.8.1...v1.9.0) (2026-06-12)


### Features

* handle orphaned submissions with incremental save and auto-close ([#92](https://github.com/testero-app/testero-backend/issues/92)) ([3cf5856](https://github.com/testero-app/testero-backend/commit/3cf5856e64c9569a9ce7925540552e9d2d23b6d8)), closes [#68](https://github.com/testero-app/testero-backend/issues/68)

## [1.8.1](https://github.com/testero-app/testero-backend/compare/v1.8.0...v1.8.1) (2026-06-12)


### Bug Fixes

* set Liquibase context to prod to exclude dev seed data ([#90](https://github.com/testero-app/testero-backend/issues/90)) ([dd4418b](https://github.com/testero-app/testero-backend/commit/dd4418bedf4cc1d214bb2e0d15427a92d8777837))

## [1.8.0](https://github.com/testero-app/testero-backend/compare/v1.7.1...v1.8.0) (2026-06-11)


### Features

* seed demo submissions for all five students ([#88](https://github.com/testero-app/testero-backend/issues/88)) ([b6084de](https://github.com/testero-app/testero-backend/commit/b6084dee9d13d58d963c8894598261f1ac72545c))

## [1.7.1](https://github.com/testero-app/testero-backend/compare/v1.7.0...v1.7.1) (2026-06-11)


### Bug Fixes

* add validCheckSum for dev seed changeset ([#86](https://github.com/testero-app/testero-backend/issues/86)) ([e5462b0](https://github.com/testero-app/testero-backend/commit/e5462b0d0aaae0e5a66b2ac189ad588bffcac0d2))

## [1.7.0](https://github.com/testero-app/testero-backend/compare/v1.6.0...v1.7.0) (2026-06-11)


### Features

* add submission review endpoint with full snapshot data ([#84](https://github.com/testero-app/testero-backend/issues/84)) ([a33c1e4](https://github.com/testero-app/testero-backend/commit/a33c1e4fd1615b5fdf93979f90a733751c61dfc0))


### Documentation

* add v4 data model and v2 assessment flow diagrams (draw.io) ([#82](https://github.com/testero-app/testero-backend/issues/82)) ([7ab5d67](https://github.com/testero-app/testero-backend/commit/7ab5d670ea7bec1b014c2c77985117915b7956be))

## [1.6.0](https://github.com/testero-app/testero-backend/compare/v1.5.0...v1.6.0) (2026-06-11)


### Features

* assessment snapshot — git-style versioning for test history ([#79](https://github.com/testero-app/testero-backend/issues/79)) ([b141416](https://github.com/testero-app/testero-backend/commit/b1414162218b626e5cd3ef5d902e6b03a4204010)), closes [#78](https://github.com/testero-app/testero-backend/issues/78)

## [1.5.0](https://github.com/testero-app/testero-backend/compare/v1.4.0...v1.5.0) (2026-06-10)


### Features

* student personal area — submission history endpoint ([#76](https://github.com/testero-app/testero-backend/issues/76)) ([e8acdc0](https://github.com/testero-app/testero-backend/commit/e8acdc01cdd79cd7dc2c8649bc0e7fde2ab5d813))

## [1.4.0](https://github.com/testero-app/testero-backend/compare/v1.3.0...v1.4.0) (2026-06-10)


### Features

* allow multiple submission attempts (retake support) ([#74](https://github.com/testero-app/testero-backend/issues/74)) ([b6b80fe](https://github.com/testero-app/testero-backend/commit/b6b80feae3c92398c6c45d22098a8e731b64afd5))

## [1.3.0](https://github.com/testero-app/testero-backend/compare/v1.2.0...v1.3.0) (2026-06-10)


### Features

* add audit fields (created_at, updated_at) to all tables ([#71](https://github.com/testero-app/testero-backend/issues/71)) ([f3d44f1](https://github.com/testero-app/testero-backend/commit/f3d44f1411936211479e49c589f9655332354386)), closes [#37](https://github.com/testero-app/testero-backend/issues/37)


### Documentation

* add architecture and workflow diagrams ([#69](https://github.com/testero-app/testero-backend/issues/69)) ([6cef8ad](https://github.com/testero-app/testero-backend/commit/6cef8ade6146c7b5b6659be06699b4c72cafa5c0))

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
