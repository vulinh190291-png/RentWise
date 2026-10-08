# RentWise MVP Demo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable RentWise modular-monolith MVP that demonstrates diagnosis → mastery profile → adaptive training → feedback → stage assessment, with MySQL persistence and Swagger/OpenAPI.

**Architecture:** One Spring Boot application, internally divided into `user`, `training`, `profile`, and `plan` domain modules. Modules do not access each other's repositories directly; cross-module collaboration goes through DTOs/facades so the same boundaries can later become microservice APIs.

**Tech Stack:** Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, MySQL 8, Bean Validation, Lombok, springdoc-openapi, Maven, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-08-rentwise-mvp-design.md`

## Global Constraints

- Keep v0.1 as a modular monolith; do not add Nacos, Sentinel, Gateway, Seata, SkyWalking, Redis, RocketMQ, or Vue3.
- Preserve the domain boundary: Training = facts, Profile = state, Plan = decisions.
- Do not access another module's repository directly.
- Do not pass JPA entities across module boundaries; use DTO/facade interfaces.
- Mastery uses the most recent 8 answers for the same topic, weighted EASY=1, MEDIUM=2, HARD=3.
- Same-topic 2 consecutive wrong answers trigger a learning card and lower next-question difficulty by one level.
- Seed exactly 10 diagnosis cases for the first demo: 5 topics × 2 cases.
- MySQL is the source of truth for v0.1.

## Review Focus

1. Fewer than 8 same-topic answers must still compute mastery from the available records instead of returning an error or zero by default.
2. A user with no profile yet must receive a clear not-initialized response rather than an invented mastery profile.
3. Two consecutive wrong answers must be counted per topic, not globally across different topics.
4. The next-question selector must avoid immediately repeating the same case when another eligible case exists.
5. Finishing a diagnosis twice must not create duplicate mastery initialization or duplicate plans.

---

## File Structure

Key files to create during this plan:

```text
pom.xml
src/main/java/com/rentwise/RentWiseApplication.java
src/main/java/com/rentwise/common/...
src/main/java/com/rentwise/user/...
src/main/java/com/rentwise/training/...
src/main/java/com/rentwise/profile/...
src/main/java/com/rentwise/plan/...
src/main/resources/application.yml
src/main/resources/data.sql
src/test/java/com/rentwise/...
docs/architecture.md
docs/mvp.md
docs/api.md
README.md
sql/init.sql
```

The package roots above are fixed for v0.1. Each domain module may contain focused `domain`, `repository`, `service`, `controller`, and `dto` subpackages as needed.

---

### Task 1: Bootstrap the Spring Boot modular-monolith skeleton

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/rentwise/RentWiseApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/java/com/rentwise/common/response/ApiResponse.java`
- Create: `src/main/java/com/rentwise/common/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/rentwise/RentWiseApplicationTests.java`

**Interfaces:**
- Produces: bootable Spring application, common response envelope, global exception handling, MySQL/JPA/OpenAPI dependencies.

- [ ] **Step 1: Write the failing context-load test**

Create `RentWiseApplicationTests.contextLoads()` and assert the Spring context starts.

- [ ] **Step 2: Run the test and verify it fails before the project skeleton exists**

Run: `mvn -q -Dtest=RentWiseApplicationTests test`
Expected: FAIL because the project/application class is not yet present.

- [ ] **Step 3: Create the minimal Maven project and application bootstrap**

Use Java 17 and Spring Boot 3.x. Add dependencies for Web, Validation, Data JPA, MySQL driver, Lombok, springdoc-openapi, and Test.

- [ ] **Step 4: Run the context-load test**

Run: `mvn -q -Dtest=RentWiseApplicationTests test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main src/test
git commit -m "chore: bootstrap RentWise modular monolith"
```

---

### Task 2: Implement training content and diagnosis persistence

**Files:**
- Create: `src/main/java/com/rentwise/training/domain/RiskTopic.java`
- Create: `src/main/java/com/rentwise/training/domain/Difficulty.java`
- Create: `src/main/java/com/rentwise/training/domain/TrainingCase.java`
- Create: `src/main/java/com/rentwise/training/domain/DiagnosisSession.java`
- Create: `src/main/java/com/rentwise/training/domain/AnswerRecord.java`
- Create: `src/main/java/com/rentwise/training/domain/LearningCard.java`
- Create: repositories under `src/main/java/com/rentwise/training/repository/`
- Create: `src/main/java/com/rentwise/training/service/TrainingFacade.java`
- Create: `src/main/java/com/rentwise/training/service/TrainingService.java`
- Create: `src/main/java/com/rentwise/training/controller/DiagnosisController.java`
- Create: DTOs under `src/main/java/com/rentwise/training/dto/`
- Create: `src/main/resources/data.sql`
- Test: `src/test/java/com/rentwise/training/TrainingServiceTest.java`

**Interfaces:**
- Produces: `TrainingFacade.startDiagnosis(userId)`, `TrainingFacade.submitDiagnosisAnswer(...)`, `TrainingFacade.finishDiagnosis(sessionId)`, `TrainingFacade.recentAnswers(userId, topic, limit)`, and case lookup for Plan.
- Later tasks consume answer facts through `TrainingFacade`; they must not read Training repositories directly.

- [ ] **Step 1: Write failing tests for diagnosis start and answer persistence**

Cover: exactly 10 seeded diagnosis cases, 5 topics × 2; submitted answer stores topic, difficulty, correctness, user, and case id.

- [ ] **Step 2: Run the tests**

Run: `mvn -q -Dtest=TrainingServiceTest test`
Expected: FAIL because Training domain/service does not exist.

- [ ] **Step 3: Implement Training domain, repositories, seed data, and facade**

Keep correctness evaluation inside Training. Keep `TrainingCase` entities private to the module; return DTOs from the facade.

- [ ] **Step 4: Add the review-focus test for duplicate diagnosis finish**

Call finish twice for the same session and assert the second call is idempotent at the Training boundary.

- [ ] **Step 5: Run Training tests**

Run: `mvn -q -Dtest=TrainingServiceTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/rentwise/training src/main/resources/data.sql src/test/java/com/rentwise/training
git commit -m "feat: add diagnosis and training fact model"
```

---

### Task 3: Implement mastery calculation and capability profile

**Files:**
- Create: `src/main/java/com/rentwise/profile/domain/UserMastery.java`
- Create: `src/main/java/com/rentwise/profile/repository/UserMasteryRepository.java`
- Create: `src/main/java/com/rentwise/profile/service/ProfileFacade.java`
- Create: `src/main/java/com/rentwise/profile/service/ProfileService.java`
- Create: `src/main/java/com/rentwise/profile/dto/UserProfileResponse.java`
- Create: `src/main/java/com/rentwise/profile/controller/ProfileController.java`
- Test: `src/test/java/com/rentwise/profile/ProfileServiceTest.java`

**Interfaces:**
- Consumes: `TrainingFacade.recentAnswers(userId, topic, limit)`.
- Produces: `ProfileFacade.initializeFromDiagnosis(userId)`, `ProfileFacade.recalculate(userId, topic)`, `ProfileFacade.getProfile(userId)`.

- [ ] **Step 1: Write failing mastery-rule tests**

Assert weighted mastery uses only the most recent 8 same-topic records, uses EASY=1/MEDIUM=2/HARD=3, and uses all available records when fewer than 8 exist.

- [ ] **Step 2: Add failing tests for profile initialization and no-profile behavior**

Assert five topics are created after initialization; requesting an uninitialized user returns the agreed domain error rather than fabricated zeros.

- [ ] **Step 3: Run Profile tests**

Run: `mvn -q -Dtest=ProfileServiceTest test`
Expected: FAIL.

- [ ] **Step 4: Implement `ProfileService` and `ProfileFacade`**

The mastery formula must exist only in this module. Persist score and same-topic consecutive-wrong count per user/topic.

- [ ] **Step 5: Add test proving consecutive-wrong count is topic-scoped**

Wrong on REPAIR, wrong on DEPOSIT, wrong on REPAIR must not count as two consecutive REPAIR errors.

- [ ] **Step 6: Run Profile tests**

Run: `mvn -q -Dtest=ProfileServiceTest test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/rentwise/profile src/test/java/com/rentwise/profile
git commit -m "feat: add mastery profile calculation"
```

---

### Task 4: Implement adaptive plan and next-question recommendation

**Files:**
- Create: `src/main/java/com/rentwise/plan/domain/TrainingPlan.java`
- Create: `src/main/java/com/rentwise/plan/repository/TrainingPlanRepository.java`
- Create: `src/main/java/com/rentwise/plan/service/PlanFacade.java`
- Create: `src/main/java/com/rentwise/plan/service/PlanService.java`
- Create: `src/main/java/com/rentwise/plan/dto/NextTrainingResponse.java`
- Create: `src/main/java/com/rentwise/plan/controller/PlanController.java`
- Test: `src/test/java/com/rentwise/plan/PlanServiceTest.java`

**Interfaces:**
- Consumes: `ProfileFacade.getProfile(userId)` and Training facade case-selection methods.
- Produces: `PlanFacade.initializePlan(userId)` and `PlanFacade.nextTraining(userId)`.

- [ ] **Step 1: Write failing tests for weakest-topic selection**

Given five mastery values, assert the lowest topic is selected. For ties, use a deterministic rule: choose the first topic by enum order for v0.1.

- [ ] **Step 2: Write failing tests for difficulty downgrade after two same-topic wrong answers**

Assert HARD→MEDIUM, MEDIUM→EASY, and EASY remains EASY.

- [ ] **Step 3: Add failing test for immediate case repetition avoidance**

When another eligible case exists in the selected topic/difficulty, `nextTraining` must not return the user's most recently answered case.

- [ ] **Step 4: Run Plan tests**

Run: `mvn -q -Dtest=PlanServiceTest test`
Expected: FAIL.

- [ ] **Step 5: Implement Plan domain/service/facade**

Recommendation logic stays inside Plan; Training only supplies eligible case DTOs.

- [ ] **Step 6: Run Plan tests**

Run: `mvn -q -Dtest=PlanServiceTest test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/rentwise/plan src/test/java/com/rentwise/plan
git commit -m "feat: add adaptive training plan"
```

---

### Task 5: Wire the diagnosis-to-training end-to-end flow

**Files:**
- Modify: `src/main/java/com/rentwise/training/controller/DiagnosisController.java`
- Create: `src/main/java/com/rentwise/training/controller/TrainingController.java`
- Create: `src/main/java/com/rentwise/training/service/DiagnosisWorkflowService.java`
- Test: `src/test/java/com/rentwise/integration/DiagnosisWorkflowIntegrationTest.java`

**Interfaces:**
- Consumes: Training, Profile, and Plan facades.
- Produces: working REST flow for `POST /api/diagnosis/start`, answer submission, `POST /api/diagnosis/{sessionId}/finish`, profile read, `GET /api/training/next`, and training-answer submission.

- [ ] **Step 1: Write failing integration test for the complete first-run flow**

Test: start diagnosis → submit 10 answers → finish → receive five mastery values → obtain next question from the weakest topic.

- [ ] **Step 2: Run the integration test**

Run: `mvn -q -Dtest=DiagnosisWorkflowIntegrationTest test`
Expected: FAIL.

- [ ] **Step 3: Implement the workflow orchestration service and REST wiring**

Keep orchestration out of controllers. The workflow may call multiple facades because this is still one process; those calls become remote service calls after microservice split.

- [ ] **Step 4: Add idempotency assertion for a second diagnosis-finish request**

Assert no duplicate mastery rows and no duplicate initial training plan are created.

- [ ] **Step 5: Run integration tests**

Run: `mvn -q -Dtest=DiagnosisWorkflowIntegrationTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/rentwise/training src/test/java/com/rentwise/integration
git commit -m "feat: wire diagnosis to adaptive training flow"
```

---

### Task 6: Add stage assessment flow

**Files:**
- Create: `src/main/java/com/rentwise/training/controller/AssessmentController.java`
- Create: `src/main/java/com/rentwise/training/service/AssessmentWorkflowService.java`
- Create: assessment DTOs under `src/main/java/com/rentwise/training/dto/`
- Test: `src/test/java/com/rentwise/integration/AssessmentWorkflowIntegrationTest.java`

**Interfaces:**
- Consumes: Training facts, Profile recalculation, Plan next-stage decision.
- Produces: `POST /api/assessment/start` and `POST /api/assessment/{sessionId}/finish`.

- [ ] **Step 1: Write failing integration test for mixed-topic assessment**

Assert the assessment contains multiple topics, submitted results are persisted, mastery changes, and a next-stage recommendation is returned.

- [ ] **Step 2: Run the test**

Run: `mvn -q -Dtest=AssessmentWorkflowIntegrationTest test`
Expected: FAIL.

- [ ] **Step 3: Implement the assessment workflow**

Reuse Training answer persistence and Profile recalculation; do not duplicate mastery logic.

- [ ] **Step 4: Run assessment tests**

Run: `mvn -q -Dtest=AssessmentWorkflowIntegrationTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/rentwise/training src/test/java/com/rentwise/integration
git commit -m "feat: add stage assessment flow"
```

---

### Task 7: Finish developer experience, SQL, and GitHub documentation

**Files:**
- Create: `sql/init.sql`
- Create: `docs/architecture.md`
- Create: `docs/mvp.md`
- Create: `docs/api.md`
- Create: `README.md`
- Create: `.gitignore`
- Modify: `src/main/resources/application.yml`
- Test: all tests

**Interfaces:**
- Produces: repository that another developer can clone, configure MySQL, run, open Swagger UI, and reproduce the MVP demo.

- [ ] **Step 1: Write README run instructions and environment contract**

Document Java 17, MySQL 8, database name, configuration keys, `mvn spring-boot:run`, Swagger URL, and demo flow.

- [ ] **Step 2: Write architecture/MVP/API docs**

Explain the four internal modules and the future split into four services. Clearly mark Nacos/Sentinel/Seata/SkyWalking as planned later, not implemented in v0.1.

- [ ] **Step 3: Add `sql/init.sql` and validate schema/data bootstrap**

Keep table ownership aligned to module boundaries; do not introduce cross-module foreign-key coupling.

- [ ] **Step 4: Run the complete verification suite**

Run: `mvn clean test`
Expected: BUILD SUCCESS with all unit and integration tests passing.

- [ ] **Step 5: Start the app against MySQL and perform a Swagger smoke test**

Verify the complete path: diagnosis → profile → next training → answer → stage assessment.

- [ ] **Step 6: Commit**

```bash
git add README.md .gitignore docs sql src/main/resources/application.yml
git commit -m "docs: make RentWise MVP demo reproducible"
```

---

## Final Verification

- [ ] `mvn clean test` passes.
- [ ] Application starts with Java 17 + MySQL 8.
- [ ] Swagger/OpenAPI loads successfully.
- [ ] 10 diagnosis cases exist across all 5 topics.
- [ ] Diagnosis produces 5 mastery values.
- [ ] Different weak topics produce different recommended training topics.
- [ ] Answering new questions changes mastery when the weighted recent window changes.
- [ ] Two same-topic consecutive errors trigger a learning card and a one-level difficulty downgrade.
- [ ] Stage assessment updates mastery and returns the next recommendation.
- [ ] README accurately distinguishes v0.1 from later microservice/Nacos/Sentinel work.
