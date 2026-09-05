# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.0.0](https://github.com/testero-app/testero-backend/compare/v2.2.0...v3.0.0) (2026-09-05)


### ⚠ BREAKING CHANGES

* POST /training/start no longer returns assessment_snapshot_id — a free session has no assessment. Clients fetch the paper from the new GET /submissions/{id}/questions using the submission id alone.

### Features

* add activation fields to ClassTest and startedAt to Submission ([#11](https://github.com/testero-app/testero-backend/issues/11)) ([4dafacd](https://github.com/testero-app/testero-backend/commit/4dafacda70ef99daec7a23c2e498d9d924365aac))
* add assessment_snapshot_subject table ([#188](https://github.com/testero-app/testero-backend/issues/188)) ([6007d3b](https://github.com/testero-app/testero-backend/commit/6007d3b9f58399fea239297b2be3fb601a09022a))
* add audit fields (created_at, updated_at) to all tables ([#71](https://github.com/testero-app/testero-backend/issues/71)) ([50aabe6](https://github.com/testero-app/testero-backend/commit/50aabe6457e59c703f0f87040776eb2c2b7a9d2d)), closes [#37](https://github.com/testero-app/testero-backend/issues/37)
* add dev seed data with Liquibase contexts ([#18](https://github.com/testero-app/testero-backend/issues/18)) ([7f4956b](https://github.com/testero-app/testero-backend/commit/7f4956b5fd16c2c1040c98bf47761e381f9818ee))
* add difficulty field to Assessment and AssessmentSnapshot ([#121](https://github.com/testero-app/testero-backend/issues/121)) ([a16a2a9](https://github.com/testero-app/testero-backend/commit/a16a2a9fa77926d4d86c80583ae5f8aa024a7221)), closes [#102](https://github.com/testero-app/testero-backend/issues/102)
* add endpoint to retrieve saved answers for session resume ([#148](https://github.com/testero-app/testero-backend/issues/148)) ([f04fa5e](https://github.com/testero-app/testero-backend/commit/f04fa5e35b6e1cf33f6d7bb782c8b5e84e24be54)), closes [#112](https://github.com/testero-app/testero-backend/issues/112)
* add explanation field to Question and QuestionSnapshot ([#125](https://github.com/testero-app/testero-backend/issues/125)) ([8ffe268](https://github.com/testero-app/testero-backend/commit/8ffe268673695d09f2eff6e9c4f0de631db0a5db)), closes [#104](https://github.com/testero-app/testero-backend/issues/104)
* add first-access set-password flow and prod role seed ([#153](https://github.com/testero-app/testero-backend/issues/153)) ([e32d46a](https://github.com/testero-app/testero-backend/commit/e32d46a1de29969af6c1bba975d597fabb91c459))
* add GET /api/topics/{id}/chapters endpoint ([#177](https://github.com/testero-app/testero-backend/issues/177)) ([013106c](https://github.com/testero-app/testero-backend/commit/013106ca610047b69c553e94ff1dcade7c8ac2b7)), closes [#170](https://github.com/testero-app/testero-backend/issues/170)
* add hierarchical topics and competency mastery endpoint ([#203](https://github.com/testero-app/testero-backend/issues/203)) ([6a7e38b](https://github.com/testero-app/testero-backend/commit/6a7e38b455fd3943fb2d3c6d9cf3117684a0464a))
* add in-app notification system ([#201](https://github.com/testero-app/testero-backend/issues/201)) ([11d04b1](https://github.com/testero-app/testero-backend/commit/11d04b1448cfeb60f8fd1cb1db9b55221ac356ed))
* add is_active flag to app_user ([#196](https://github.com/testero-app/testero-backend/issues/196)) ([4420bb8](https://github.com/testero-app/testero-backend/commit/4420bb84647d68e482ced0234f2d70386dd8576d))
* add Liquibase database migrations for initial schema ([#14](https://github.com/testero-app/testero-backend/issues/14)) ([948bae7](https://github.com/testero-app/testero-backend/commit/948bae7f32e8ca2cb7c7c8912acaa9ea954010f6))
* add Liquibase dev seed with demo data for contributors ([#55](https://github.com/testero-app/testero-backend/issues/55)) ([f72aba5](https://github.com/testero-app/testero-backend/commit/f72aba5df19664b94758eac39e06ccc896f38a10))
* add max_score to submission DTOs and support email login ([#139](https://github.com/testero-app/testero-backend/issues/139)) ([fc451f6](https://github.com/testero-app/testero-backend/commit/fc451f69585e7c3efd1e1067e160a7aa82d14782)), closes [#110](https://github.com/testero-app/testero-backend/issues/110) [#117](https://github.com/testero-app/testero-backend/issues/117)
* add notification preferences CRUD endpoints ([#174](https://github.com/testero-app/testero-backend/issues/174)) ([eebf6d9](https://github.com/testero-app/testero-backend/commit/eebf6d9599ea9cbef91a5d11dfab19135c0bb43a)), closes [#168](https://github.com/testero-app/testero-backend/issues/168)
* add pagination to assessment list and submission history ([#142](https://github.com/testero-app/testero-backend/issues/142)) ([9187077](https://github.com/testero-app/testero-backend/commit/91870773c7806600ee6509d3f85a223620ece688)), closes [#119](https://github.com/testero-app/testero-backend/issues/119)
* add passing_score to Assessment and pass/fail logic ([#131](https://github.com/testero-app/testero-backend/issues/131)) ([89d6b84](https://github.com/testero-app/testero-backend/commit/89d6b844c95755fd208f5bc180e4e03ff722074f)), closes [#103](https://github.com/testero-app/testero-backend/issues/103)
* add per-question points, subjects in DTOs, and subject scoring breakdown ([#146](https://github.com/testero-app/testero-backend/issues/146)) ([a3c3714](https://github.com/testero-app/testero-backend/commit/a3c3714b37ec708292f6e5663f0308e99aa9b686))
* add POST /api/training/start for dynamic training sessions ([#178](https://github.com/testero-app/testero-backend/issues/178)) ([a14f757](https://github.com/testero-app/testero-backend/commit/a14f757f6d6db506a22579e1f1c6799ad18a844b))
* add question flag/bookmark support during assessment ([#149](https://github.com/testero-app/testero-backend/issues/149)) ([9ec0061](https://github.com/testero-app/testero-backend/commit/9ec0061b621f0a6a1c05719df8f2a1beb999f7de)), closes [#111](https://github.com/testero-app/testero-backend/issues/111)
* add question-subject many-to-many relationship with weight ([#141](https://github.com/testero-app/testero-backend/issues/141)) ([8ea489f](https://github.com/testero-app/testero-backend/commit/8ea489fc655dd82776422d0e3bf65b610a0d40c0)), closes [#106](https://github.com/testero-app/testero-backend/issues/106)
* add Spring profiles (dev/prod) and Docker Compose for local PostgreSQL ([#16](https://github.com/testero-app/testero-backend/issues/16)) ([3517454](https://github.com/testero-app/testero-backend/commit/3517454f8beaf981c0115e953569fc92e92ec30c))
* add submission review endpoint with full snapshot data ([#84](https://github.com/testero-app/testero-backend/issues/84)) ([be05676](https://github.com/testero-app/testero-backend/commit/be056765374d69dcc20c6482afc5fdfdd8153fe6))
* add Swagger/OpenAPI documentation for REST endpoints ([#137](https://github.com/testero-app/testero-backend/issues/137)) ([f938a52](https://github.com/testero-app/testero-backend/commit/f938a526096bfe2e1e094b7e0e7f1becd8d635e8)), closes [#98](https://github.com/testero-app/testero-backend/issues/98)
* add TeacherClass M:N relation for teacher-class assignment ([#13](https://github.com/testero-app/testero-backend/issues/13)) ([3a679b4](https://github.com/testero-app/testero-backend/commit/3a679b46207ec24a9121cc158d50dc535206b3b5))
* add Topic entity and GET /api/topics endpoint ([#175](https://github.com/testero-app/testero-backend/issues/175)) ([8f5fd0a](https://github.com/testero-app/testero-backend/commit/8f5fd0aaa6a1f8191b49ace9572ce5200d837fc4)), closes [#169](https://github.com/testero-app/testero-backend/issues/169)
* add type field (CERTIFICATION/TRAINING) to assessments ([#172](https://github.com/testero-app/testero-backend/issues/172)) ([ff4452a](https://github.com/testero-app/testero-backend/commit/ff4452a300d66e0da2e26778511bc7f77a1bbd28)), closes [#167](https://github.com/testero-app/testero-backend/issues/167)
* add user profile and change password endpoints ([#135](https://github.com/testero-app/testero-backend/issues/135)) ([318d262](https://github.com/testero-app/testero-backend/commit/318d262cd317504c59a6f12bd58bf9b3ef5a69eb)), closes [#113](https://github.com/testero-app/testero-backend/issues/113) [#114](https://github.com/testero-app/testero-backend/issues/114)
* allow multiple submission attempts (retake support) ([#74](https://github.com/testero-app/testero-backend/issues/74)) ([1758179](https://github.com/testero-app/testero-backend/commit/1758179ffd13ffb76a8b718e684f3e9ac9871f65))
* assessment snapshot — git-style versioning for test history ([#79](https://github.com/testero-app/testero-backend/issues/79)) ([79db70b](https://github.com/testero-app/testero-backend/commit/79db70b4552241de341b048c03d238ed1d43d8b2)), closes [#78](https://github.com/testero-app/testero-backend/issues/78)
* **assessment:** add template ownership and enforce it on publish ([#215](https://github.com/testero-app/testero-backend/issues/215)) ([6fe5548](https://github.com/testero-app/testero-backend/commit/6fe554895ef8940956168bf06229faba49c81095))
* **db:** seed Python Certification Exam Practice assessment ([#210](https://github.com/testero-app/testero-backend/issues/210)) ([0201463](https://github.com/testero-app/testero-backend/commit/02014635da4cec82c54bfd0a2003e5586001aa03))
* difficulty-weighted mastery + date filter on competencies ([#207](https://github.com/testero-app/testero-backend/issues/207)) ([eddd4f1](https://github.com/testero-app/testero-backend/commit/eddd4f14ad079caf41cd0b2d8d8260423db89456))
* expose subject tags per question in assessment questions endpoint ([#151](https://github.com/testero-app/testero-backend/issues/151)) ([e93c2dc](https://github.com/testero-app/testero-backend/commit/e93c2dcef59a5173fb40afe3e2267539e15c58db))
* free training draws across pools without copying questions ([#247](https://github.com/testero-app/testero-backend/issues/247)) ([be55c49](https://github.com/testero-app/testero-backend/commit/be55c496902982bc4ff4d7ce25cc06c0c2ac2aba))
* freeze subject label in snapshot at publish time ([#193](https://github.com/testero-app/testero-backend/issues/193)) ([bf9551e](https://github.com/testero-app/testero-backend/commit/bf9551e508966deda70aec1b355a24d047a1cfe7))
* handle orphaned submissions with incremental save and auto-close ([#92](https://github.com/testero-app/testero-backend/issues/92)) ([e57ad0a](https://github.com/testero-app/testero-backend/commit/e57ad0ae24505446d5348431b53a563eb8991994)), closes [#68](https://github.com/testero-app/testero-backend/issues/68)
* **i18n:** backend language preference + localised notifications (+ package reorg) ([#234](https://github.com/testero-app/testero-backend/issues/234)) ([ae39cc7](https://github.com/testero-app/testero-backend/commit/ae39cc777d7de93ac153a8257de1b36afec5ccf9))
* include student submission status in assessment list response ([#127](https://github.com/testero-app/testero-backend/issues/127)) ([87feca8](https://github.com/testero-app/testero-backend/commit/87feca89b5dd65577b0500b4460c2b8ffd36ebd7)), closes [#108](https://github.com/testero-app/testero-backend/issues/108)
* link assessment templates to topics (M:N) ([#195](https://github.com/testero-app/testero-backend/issues/195)) ([3d1cd46](https://github.com/testero-app/testero-backend/commit/3d1cd4668f37716539e266bb8affe02e096b37d2))
* move availability window from template to class assignment ([#186](https://github.com/testero-app/testero-backend/issues/186)) ([ea7d461](https://github.com/testero-app/testero-backend/commit/ea7d46101e9eea23e92106bf4b0a6e3c8f5dbcb0))
* **notifications:** generate DEADLINE_REMINDER notifications ([#232](https://github.com/testero-app/testero-backend/issues/232)) ([4a3ae77](https://github.com/testero-app/testero-backend/commit/4a3ae77d60fb6b4466174e7c3d4b8f54037b8f15)), closes [#231](https://github.com/testero-app/testero-backend/issues/231)
* **notification:** store source event ID for click-to-navigate ([#255](https://github.com/testero-app/testero-backend/issues/255)) ([c0fd87f](https://github.com/testero-app/testero-backend/commit/c0fd87fb08da43787bfdb017d5c935e9abb9beee)), closes [#251](https://github.com/testero-app/testero-backend/issues/251)
* populate deadline fields and add maxAttempts/className to assessment API ([#269](https://github.com/testero-app/testero-backend/issues/269)) ([5947bf8](https://github.com/testero-app/testero-backend/commit/5947bf8b2a0ac355ee1fd9035f806d15c021e877))
* replace date + start_time with availability window ([#184](https://github.com/testero-app/testero-backend/issues/184)) ([d3132e4](https://github.com/testero-app/testero-backend/commit/d3132e45b21d5fc606aa1b5037b72630c3c7c9df))
* scaffold Spring Boot backend with auth, JWT and CORS config ([#3](https://github.com/testero-app/testero-backend/issues/3)) ([dfa9ea1](https://github.com/testero-app/testero-backend/commit/dfa9ea1b02d973dfb97573c4b8e75cdc6a02429f))
* seed demo submissions for all five students ([#88](https://github.com/testero-app/testero-backend/issues/88)) ([8c8476c](https://github.com/testero-app/testero-backend/commit/8c8476cfd33cb3bf73ac20c05c058ed400981e11))
* **snapshot:** auto-assign topic subjects to questions without explicit links ([#258](https://github.com/testero-app/testero-backend/issues/258)) ([c7c6156](https://github.com/testero-app/testero-backend/commit/c7c615625010dd51f7e0cecc4f91211e09fdeffc)), closes [#252](https://github.com/testero-app/testero-backend/issues/252)
* split name into first_name/last_name, add PUT /users/me ([#199](https://github.com/testero-app/testero-backend/issues/199)) ([0958c0a](https://github.com/testero-app/testero-backend/commit/0958c0aee48918a398e0bfc307204e6e837ab6ae))
* student personal area — submission history endpoint ([#76](https://github.com/testero-app/testero-backend/issues/76)) ([2edf951](https://github.com/testero-app/testero-backend/commit/2edf951fe5c287189e50a877e9103ab240555aa8))
* **submissions:** enforce max_attempts, frozen into the snapshot ([#228](https://github.com/testero-app/testero-backend/issues/228)) ([3d689c1](https://github.com/testero-app/testero-backend/commit/3d689c17ad808da11e94f8ed2a477f7cb4141f60))
* **tags:** teacher-scoped question tags with CRUD and tag filter ([#225](https://github.com/testero-app/testero-backend/issues/225)) ([7bc3035](https://github.com/testero-app/testero-backend/commit/7bc3035555a747896d694c29e06bd0d897b554e2))


### Bug Fixes

* add braces to single-line if statements (checkstyle) ([#129](https://github.com/testero-app/testero-backend/issues/129)) ([5ae34fe](https://github.com/testero-app/testero-backend/commit/5ae34fe156a56888f0727ba830d302f739a5305d))
* add is_fallback field to OptionSnapshot ([#123](https://github.com/testero-app/testero-backend/issues/123)) ([cf007da](https://github.com/testero-app/testero-backend/commit/cf007daeb72a2b065923c9f54621b8c930b03a00)), closes [#118](https://github.com/testero-app/testero-backend/issues/118)
* add precondition to unique username migration ([#267](https://github.com/testero-app/testero-backend/issues/267)) ([8be8d31](https://github.com/testero-app/testero-backend/commit/8be8d311b8e2a34a49dd32924aa706c72910ebcd))
* add question-subject links and topic to dev seed data ([#205](https://github.com/testero-app/testero-backend/issues/205)) ([8d85d59](https://github.com/testero-app/testero-backend/commit/8d85d592cbf504fa139ce20cb3c55fb95c3bf21c))
* add splitStatements:false to dev seed changeset ([#62](https://github.com/testero-app/testero-backend/issues/62)) ([a183775](https://github.com/testero-app/testero-backend/commit/a183775c8b805279d3c75a9d77061b3ac9f23a73))
* add unique constraint on app_user.username ([#259](https://github.com/testero-app/testero-backend/issues/259)) ([6daacb2](https://github.com/testero-app/testero-backend/commit/6daacb21c1730fe17c32a4b32e45c735d2426dac))
* add validCheckSum for dev seed changeset ([#86](https://github.com/testero-app/testero-backend/issues/86)) ([9e74b82](https://github.com/testero-app/testero-backend/commit/9e74b82c8d3cf07a040e315d38fc51ba2a0b3d88))
* **assessment:** freeze question draw per submission and honor shuffle flags ([#238](https://github.com/testero-app/testero-backend/issues/238)) ([fd3fe06](https://github.com/testero-app/testero-backend/commit/fd3fe06d525a1aa08241e56d4ffde3026be9717b)), closes [#237](https://github.com/testero-app/testero-backend/issues/237)
* **ci:** use config file in release-please to respect snapshot:false ([#59](https://github.com/testero-app/testero-backend/issues/59)) ([00ea62d](https://github.com/testero-app/testero-backend/commit/00ea62d46e02d5f0f19a03d36002e235266494b9))
* **ci:** use skip-snapshot to prevent SNAPSHOT release PRs ([#64](https://github.com/testero-app/testero-backend/issues/64)) ([edcfbfc](https://github.com/testero-app/testero-backend/commit/edcfbfcd1de66a3c24b665a0f7fd26d2264f3ed1))
* correct checkstyle indentation in TopicService ([#262](https://github.com/testero-app/testero-backend/issues/262)) ([ae98878](https://github.com/testero-app/testero-backend/commit/ae988781983e3c499b156905cdff5ef070dcc48f))
* **deps:** bump Tomcat to 11.0.22 and PostgreSQL driver to 42.7.11 ([#9](https://github.com/testero-app/testero-backend/issues/9)) ([3857f53](https://github.com/testero-app/testero-backend/commit/3857f53601a3e0d7935938ab694b09985d825461))
* **deps:** upgrade spring-dotenv to 5.1.0 for Spring Boot 4.x support ([#15](https://github.com/testero-app/testero-backend/issues/15)) ([243dd8d](https://github.com/testero-app/testero-backend/commit/243dd8d1f47d0b951d487b1c904ce99b7eb3eecb))
* duplicate key on submit with saved answers ([#133](https://github.com/testero-app/testero-backend/issues/133)) ([cbee7b8](https://github.com/testero-app/testero-backend/commit/cbee7b85978fb468c7361349c7370d7602ee985d))
* prevent duplicate key violation on submit after incremental save ([#145](https://github.com/testero-app/testero-backend/issues/145)) ([de89e45](https://github.com/testero-app/testero-backend/commit/de89e45d8b8926d6d3f41c6643a5053c8989ccbe))
* resolve checkstyle violations (file length, constant name) ([#95](https://github.com/testero-app/testero-backend/issues/95)) ([a1a0881](https://github.com/testero-app/testero-backend/commit/a1a088111d2091240e2d97d77e25073a5639f42e))
* **security:** address CodeQL alerts for CSRF and workflow permissions ([#52](https://github.com/testero-app/testero-backend/issues/52)) ([f257976](https://github.com/testero-app/testero-backend/commit/f2579762fa23959f8d1ee99b52d4fe69a4d9d427))
* **security:** restrict assessment publishing to teachers and admins ([#213](https://github.com/testero-app/testero-backend/issues/213)) ([a8d4007](https://github.com/testero-app/testero-backend/commit/a8d4007a89cdad4de4aa3ab66c3948f6d17a50b9))
* separate topic visibility for training vs competencies ([#260](https://github.com/testero-app/testero-backend/issues/260)) ([52a1854](https://github.com/testero-app/testero-backend/commit/52a18549fbee724dcb1e953e0a814862aa04cb5e))
* set Liquibase context to prod to exclude dev seed data ([#90](https://github.com/testero-app/testero-backend/issues/90)) ([4a09102](https://github.com/testero-app/testero-backend/commit/4a09102362857071e442f55a7d29e283c2d376b7))
* **submissions:** measure results against the drawn paper ([#240](https://github.com/testero-app/testero-backend/issues/240)) ([5e7782a](https://github.com/testero-app/testero-backend/commit/5e7782a9715146d788279b20d82b98209ac35183))
* switch Render to Docker runtime and add Dockerfile ([#7](https://github.com/testero-app/testero-backend/issues/7)) ([6dee0a0](https://github.com/testero-app/testero-backend/commit/6dee0a0db6e45ac8bfc56847982127736d889d9b))
* use correct question type and improve project setup ([#40](https://github.com/testero-app/testero-backend/issues/40)) ([b0f703d](https://github.com/testero-app/testero-backend/commit/b0f703d0efe552443dd71dad0fe32ed2a14e6a2b))


### Performance Improvements

* **snapshot:** batch the inserts that create an assessment snapshot ([#244](https://github.com/testero-app/testero-backend/issues/244)) ([cf0e56e](https://github.com/testero-app/testero-backend/commit/cf0e56e0e6ef59aa7d86be3a03ef45a0b5e25934))


### Documentation

* add architecture and workflow diagrams ([#69](https://github.com/testero-app/testero-backend/issues/69)) ([1583402](https://github.com/testero-app/testero-backend/commit/15834025eed92829876d5bc6b269167145e28546))
* add governance files (README, CONTRIBUTING, DCO, templates) ([#1](https://github.com/testero-app/testero-backend/issues/1)) ([d324115](https://github.com/testero-app/testero-backend/commit/d3241156c8c5f214ac307162ae9533d700172f92))
* add v4 data model and v2 assessment flow diagrams (draw.io) ([#82](https://github.com/testero-app/testero-backend/issues/82)) ([12c742c](https://github.com/testero-app/testero-backend/commit/12c742caff869d7b134d3341cbb9aae391699772))
* align data-model diagram with Assessment rename and remove v2.2 ([#48](https://github.com/testero-app/testero-backend/issues/48)) ([998cdd1](https://github.com/testero-app/testero-backend/commit/998cdd10ec10de9b6bc5d48e8676ff0aaa369933))
* move diagrams and docs to testero-docs repository ([#99](https://github.com/testero-app/testero-backend/issues/99)) ([1045e6e](https://github.com/testero-app/testero-backend/commit/1045e6e3f307f4b7cae8b654930232d281f0e42d))
* update README with env vars table and stack details ([#8](https://github.com/testero-app/testero-backend/issues/8)) ([b433404](https://github.com/testero-app/testero-backend/commit/b433404b46a889a4365b48f4497c526ed036544d))

## [2.2.0](https://github.com/testero-app/testero-backend/compare/v2.1.2...v2.2.0) (2026-08-24)


### Features

* populate deadline fields and add maxAttempts/className to assessment API ([#269](https://github.com/testero-app/testero-backend/issues/269)) ([8a68002](https://github.com/testero-app/testero-backend/commit/8a680024c9b969d73e64a93c40021625384a96ad))


### Bug Fixes

* add precondition to unique username migration ([#267](https://github.com/testero-app/testero-backend/issues/267)) ([7c16de3](https://github.com/testero-app/testero-backend/commit/7c16de321456ed47173b64378b1e21780a0a7c16))

## [2.1.2](https://github.com/testero-app/testero-backend/compare/v2.1.1...v2.1.2) (2026-08-20)


### Bug Fixes

* correct checkstyle indentation in TopicService ([#262](https://github.com/testero-app/testero-backend/issues/262)) ([306227b](https://github.com/testero-app/testero-backend/commit/306227b6fe178a7cff107162247a73c3b648b908))

## [2.1.1](https://github.com/testero-app/testero-backend/compare/v2.1.0...v2.1.1) (2026-08-20)


### Bug Fixes

* add unique constraint on app_user.username ([#259](https://github.com/testero-app/testero-backend/issues/259)) ([45c79cc](https://github.com/testero-app/testero-backend/commit/45c79ccc9a7d3606759fc66c617f15d9b3be8ce3))
* separate topic visibility for training vs competencies ([#260](https://github.com/testero-app/testero-backend/issues/260)) ([f26a14a](https://github.com/testero-app/testero-backend/commit/f26a14ab3652a5d6d315dca444ed61f5fae99ad9))

## [2.1.0](https://github.com/testero-app/testero-backend/compare/v2.0.0...v2.1.0) (2026-08-18)


### Features

* **notification:** store source event ID for click-to-navigate ([#255](https://github.com/testero-app/testero-backend/issues/255)) ([54b87fb](https://github.com/testero-app/testero-backend/commit/54b87fbee9be8a6bdd0b82f82518c5b23d1f8582)), closes [#251](https://github.com/testero-app/testero-backend/issues/251)
* **snapshot:** auto-assign topic subjects to questions without explicit links ([#258](https://github.com/testero-app/testero-backend/issues/258)) ([16a6d16](https://github.com/testero-app/testero-backend/commit/16a6d16017ee19d4ab9ac252442374b8c57b5120)), closes [#252](https://github.com/testero-app/testero-backend/issues/252)

## [2.0.0](https://github.com/testero-app/testero-backend/compare/v1.35.3...v2.0.0) (2026-08-04)


### ⚠ BREAKING CHANGES

* POST /training/start no longer returns assessment_snapshot_id — a free session has no assessment. Clients fetch the paper from the new GET /submissions/{id}/questions using the submission id alone.

### Features

* free training draws across pools without copying questions ([#247](https://github.com/testero-app/testero-backend/issues/247)) ([2ff192b](https://github.com/testero-app/testero-backend/commit/2ff192b7ee32b97e8049bfb87cc1866047be9165))

## [1.35.3](https://github.com/testero-app/testero-backend/compare/v1.35.2...v1.35.3) (2026-08-03)


### Performance Improvements

* **snapshot:** batch the inserts that create an assessment snapshot ([#244](https://github.com/testero-app/testero-backend/issues/244)) ([261d75e](https://github.com/testero-app/testero-backend/commit/261d75e07212f017e847999df0245b79e0da99eb))

## [1.35.2](https://github.com/testero-app/testero-backend/compare/v1.35.1...v1.35.2) (2026-08-03)


### Bug Fixes

* **submissions:** measure results against the drawn paper ([#240](https://github.com/testero-app/testero-backend/issues/240)) ([289ff25](https://github.com/testero-app/testero-backend/commit/289ff2583299131a532e803cba2184669a9bb45f))

## [1.35.1](https://github.com/testero-app/testero-backend/compare/v1.35.0...v1.35.1) (2026-07-30)


### Bug Fixes

* **assessment:** freeze question draw per submission and honor shuffle flags ([#238](https://github.com/testero-app/testero-backend/issues/238)) ([603168e](https://github.com/testero-app/testero-backend/commit/603168e9d5afe1c5eb10cb0350a3be1dab5f02f3)), closes [#237](https://github.com/testero-app/testero-backend/issues/237)

## [1.35.0](https://github.com/testero-app/testero-backend/compare/v1.34.0...v1.35.0) (2026-07-24)


### Features

* **i18n:** backend language preference + localised notifications (+ package reorg) ([#234](https://github.com/testero-app/testero-backend/issues/234)) ([95196de](https://github.com/testero-app/testero-backend/commit/95196de9d26d32ffa0332c451ef7e253a4296857))

## [1.34.0](https://github.com/testero-app/testero-backend/compare/v1.33.0...v1.34.0) (2026-07-23)


### Features

* **notifications:** generate DEADLINE_REMINDER notifications ([#232](https://github.com/testero-app/testero-backend/issues/232)) ([b186678](https://github.com/testero-app/testero-backend/commit/b186678890aa9c7fedee375acedbafdb960e07cb)), closes [#231](https://github.com/testero-app/testero-backend/issues/231)

## [1.33.0](https://github.com/testero-app/testero-backend/compare/v1.32.0...v1.33.0) (2026-07-20)


### Features

* **submissions:** enforce max_attempts, frozen into the snapshot ([#228](https://github.com/testero-app/testero-backend/issues/228)) ([f6eee39](https://github.com/testero-app/testero-backend/commit/f6eee39605ccb97709e7f49a05c367bff6835d41))

## [1.32.0](https://github.com/testero-app/testero-backend/compare/v1.31.1...v1.32.0) (2026-07-16)


### Features

* **tags:** teacher-scoped question tags with CRUD and tag filter ([#225](https://github.com/testero-app/testero-backend/issues/225)) ([fd48678](https://github.com/testero-app/testero-backend/commit/fd48678b759a44537235a6214ebe4861212e5879))

## [1.31.1](https://github.com/testero-app/testero-backend/compare/v1.31.0...v1.31.1) (2026-07-14)


### Bug Fixes

* **security:** restrict assessment publishing to teachers and admins ([#213](https://github.com/testero-app/testero-backend/issues/213)) ([6e27ba5](https://github.com/testero-app/testero-backend/commit/6e27ba5f1e9eb1bfba226d5fc997d52aff558a58))

## [1.31.0](https://github.com/testero-app/testero-backend/compare/v1.30.0...v1.31.0) (2026-07-14)


### Features

* **db:** seed Python Certification Exam Practice assessment ([#210](https://github.com/testero-app/testero-backend/issues/210)) ([dbacbbe](https://github.com/testero-app/testero-backend/commit/dbacbbe052685eb2dad8755637bbb704b5a3917d))

## [1.30.0](https://github.com/testero-app/testero-backend/compare/v1.29.0...v1.30.0) (2026-07-12)


### Features

* difficulty-weighted mastery + date filter on competencies ([#207](https://github.com/testero-app/testero-backend/issues/207)) ([7c99fb5](https://github.com/testero-app/testero-backend/commit/7c99fb53fdabe43dc9177320a162a70a1bb06c5f))


### Bug Fixes

* add question-subject links and topic to dev seed data ([#205](https://github.com/testero-app/testero-backend/issues/205)) ([1970713](https://github.com/testero-app/testero-backend/commit/1970713bd4ddbfe2660f68bf62c2e401581c1830))

## [1.29.0](https://github.com/testero-app/testero-backend/compare/v1.28.0...v1.29.0) (2026-07-08)


### Features

* add hierarchical topics and competency mastery endpoint ([#203](https://github.com/testero-app/testero-backend/issues/203)) ([66eecc6](https://github.com/testero-app/testero-backend/commit/66eecc6eaa74602ea1153c81072a97ec08e0db6e))

## [1.28.0](https://github.com/testero-app/testero-backend/compare/v1.27.0...v1.28.0) (2026-07-08)


### Features

* add in-app notification system ([#201](https://github.com/testero-app/testero-backend/issues/201)) ([5cd98d4](https://github.com/testero-app/testero-backend/commit/5cd98d41de1dc387fa7fba9fda6997c43d2d03c1))

## [1.27.0](https://github.com/testero-app/testero-backend/compare/v1.26.0...v1.27.0) (2026-07-08)


### Features

* split name into first_name/last_name, add PUT /users/me ([#199](https://github.com/testero-app/testero-backend/issues/199)) ([0a18a8f](https://github.com/testero-app/testero-backend/commit/0a18a8f8e9fc0409216c9f092d01c418ae9a9509))

## [1.26.0](https://github.com/testero-app/testero-backend/compare/v1.25.0...v1.26.0) (2026-07-07)


### Features

* add is_active flag to app_user ([#196](https://github.com/testero-app/testero-backend/issues/196)) ([bd0da14](https://github.com/testero-app/testero-backend/commit/bd0da148aca463a081375e7ec3655c739c672481))
* freeze subject label in snapshot at publish time ([#193](https://github.com/testero-app/testero-backend/issues/193)) ([0d5b6b1](https://github.com/testero-app/testero-backend/commit/0d5b6b1866da6288a63d210002a799001b8ef2d5))
* link assessment templates to topics (M:N) ([#195](https://github.com/testero-app/testero-backend/issues/195)) ([be617d3](https://github.com/testero-app/testero-backend/commit/be617d3811efe64519fb6f89737c5c8cb070215b))

## [1.25.0](https://github.com/testero-app/testero-backend/compare/v1.24.0...v1.25.0) (2026-07-06)


### Features

* add assessment_snapshot_subject table ([#188](https://github.com/testero-app/testero-backend/issues/188)) ([e106a92](https://github.com/testero-app/testero-backend/commit/e106a92b6eb9db67de34486a87e1216b0a28cb54))

## [1.24.0](https://github.com/testero-app/testero-backend/compare/v1.23.0...v1.24.0) (2026-07-05)


### Features

* move availability window from template to class assignment ([#186](https://github.com/testero-app/testero-backend/issues/186)) ([d4d4fe0](https://github.com/testero-app/testero-backend/commit/d4d4fe0ed0345df1cbe6d8f64d62c0ea44f1b3ea))

## [1.23.0](https://github.com/testero-app/testero-backend/compare/v1.22.0...v1.23.0) (2026-07-04)


### Features

* replace date + start_time with availability window ([#184](https://github.com/testero-app/testero-backend/issues/184)) ([66199ff](https://github.com/testero-app/testero-backend/commit/66199ff4edffcf916a1244fb066a7f69595f88ca))

## [1.22.0](https://github.com/testero-app/testero-backend/compare/v1.21.0...v1.22.0) (2026-06-19)


### Features

* add GET /api/topics/{id}/chapters endpoint ([#177](https://github.com/testero-app/testero-backend/issues/177)) ([e711d72](https://github.com/testero-app/testero-backend/commit/e711d720687861da9104b97f934d199a138e42ea)), closes [#170](https://github.com/testero-app/testero-backend/issues/170)
* add notification preferences CRUD endpoints ([#174](https://github.com/testero-app/testero-backend/issues/174)) ([053e37b](https://github.com/testero-app/testero-backend/commit/053e37b2c9e53bd503b594bbe3248545929e45a0)), closes [#168](https://github.com/testero-app/testero-backend/issues/168)
* add POST /api/training/start for dynamic training sessions ([#178](https://github.com/testero-app/testero-backend/issues/178)) ([b402fba](https://github.com/testero-app/testero-backend/commit/b402fba8454b9237462abce568450ccabce65ac8))
* add Topic entity and GET /api/topics endpoint ([#175](https://github.com/testero-app/testero-backend/issues/175)) ([0edd57e](https://github.com/testero-app/testero-backend/commit/0edd57e3cee270bf81a8b45e4386cd902b861b60)), closes [#169](https://github.com/testero-app/testero-backend/issues/169)

## [1.21.0](https://github.com/testero-app/testero-backend/compare/v1.20.0...v1.21.0) (2026-06-19)


### Features

* add type field (CERTIFICATION/TRAINING) to assessments ([#172](https://github.com/testero-app/testero-backend/issues/172)) ([6d98c99](https://github.com/testero-app/testero-backend/commit/6d98c9952519d439d58ee2d383da42311c106d99)), closes [#167](https://github.com/testero-app/testero-backend/issues/167)

## [1.20.0](https://github.com/testero-app/testero-backend/compare/v1.19.0...v1.20.0) (2026-06-17)


### Features

* add first-access set-password flow and prod role seed ([#153](https://github.com/testero-app/testero-backend/issues/153)) ([b44a49d](https://github.com/testero-app/testero-backend/commit/b44a49d838d59f3ffa30497b3c2b3b07765585ae))

## [1.19.0](https://github.com/testero-app/testero-backend/compare/v1.18.0...v1.19.0) (2026-06-15)


### Features

* expose subject tags per question in assessment questions endpoint ([#151](https://github.com/testero-app/testero-backend/issues/151)) ([8046e8e](https://github.com/testero-app/testero-backend/commit/8046e8ee30c692accaf7fc61ad6f6939c6c4b1fc))

## [1.18.0](https://github.com/testero-app/testero-backend/compare/v1.17.0...v1.18.0) (2026-06-15)


### Features

* add question flag/bookmark support during assessment ([#149](https://github.com/testero-app/testero-backend/issues/149)) ([f74df61](https://github.com/testero-app/testero-backend/commit/f74df6120d210e8da5c0a0515aaaf82604f0fca0)), closes [#111](https://github.com/testero-app/testero-backend/issues/111)

## [1.17.0](https://github.com/testero-app/testero-backend/compare/v1.16.0...v1.17.0) (2026-06-15)


### Features

* add endpoint to retrieve saved answers for session resume ([#148](https://github.com/testero-app/testero-backend/issues/148)) ([25d57dc](https://github.com/testero-app/testero-backend/commit/25d57dc3551032a65b5ae75a6f04adcc5a4d0216)), closes [#112](https://github.com/testero-app/testero-backend/issues/112)
* add per-question points, subjects in DTOs, and subject scoring breakdown ([#146](https://github.com/testero-app/testero-backend/issues/146)) ([b101864](https://github.com/testero-app/testero-backend/commit/b101864fe58186dda7cf4f07953af99b69b0b7b1))

## [1.16.0](https://github.com/testero-app/testero-backend/compare/v1.15.0...v1.16.0) (2026-06-15)


### Features

* add pagination to assessment list and submission history ([#142](https://github.com/testero-app/testero-backend/issues/142)) ([df782b1](https://github.com/testero-app/testero-backend/commit/df782b10b6de6f57db9e935dd48b59b95697cc07)), closes [#119](https://github.com/testero-app/testero-backend/issues/119)
* add question-subject many-to-many relationship with weight ([#141](https://github.com/testero-app/testero-backend/issues/141)) ([0b9d2a6](https://github.com/testero-app/testero-backend/commit/0b9d2a61349185843c4a80eb3b95c90b81d65d79)), closes [#106](https://github.com/testero-app/testero-backend/issues/106)


### Bug Fixes

* prevent duplicate key violation on submit after incremental save ([#145](https://github.com/testero-app/testero-backend/issues/145)) ([22e1526](https://github.com/testero-app/testero-backend/commit/22e1526fdc4392007cc1bd84100383b8f3346a36))

## [1.15.0](https://github.com/testero-app/testero-backend/compare/v1.14.0...v1.15.0) (2026-06-15)


### Features

* add max_score to submission DTOs and support email login ([#139](https://github.com/testero-app/testero-backend/issues/139)) ([c5ebf2d](https://github.com/testero-app/testero-backend/commit/c5ebf2d688ecfd73923bbc32a7d0f3889b2aaad2)), closes [#110](https://github.com/testero-app/testero-backend/issues/110) [#117](https://github.com/testero-app/testero-backend/issues/117)
* add Swagger/OpenAPI documentation for REST endpoints ([#137](https://github.com/testero-app/testero-backend/issues/137)) ([e4f669e](https://github.com/testero-app/testero-backend/commit/e4f669eaeb6452ced0b37465aee77d24e3e1ba6d)), closes [#98](https://github.com/testero-app/testero-backend/issues/98)

## [1.14.0](https://github.com/testero-app/testero-backend/compare/v1.13.0...v1.14.0) (2026-06-15)


### Features

* add user profile and change password endpoints ([#135](https://github.com/testero-app/testero-backend/issues/135)) ([e566a50](https://github.com/testero-app/testero-backend/commit/e566a506188b6803514b81ea59d9f8569234ae4c)), closes [#113](https://github.com/testero-app/testero-backend/issues/113) [#114](https://github.com/testero-app/testero-backend/issues/114)

## [1.13.0](https://github.com/testero-app/testero-backend/compare/v1.12.1...v1.13.0) (2026-06-14)


### Features

* add passing_score to Assessment and pass/fail logic ([#131](https://github.com/testero-app/testero-backend/issues/131)) ([65b3185](https://github.com/testero-app/testero-backend/commit/65b318525066fbe9e912ea050a2d02a1c9030d83)), closes [#103](https://github.com/testero-app/testero-backend/issues/103)


### Bug Fixes

* duplicate key on submit with saved answers ([#133](https://github.com/testero-app/testero-backend/issues/133)) ([2596742](https://github.com/testero-app/testero-backend/commit/25967424c29bcef4f7d0224e86ada10bb9c55abf))

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
