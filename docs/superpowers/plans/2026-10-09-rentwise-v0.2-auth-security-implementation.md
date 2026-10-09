# RentWise v0.2 Authentication & RBAC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add username/password registration, BCrypt password storage, JWT access-token authentication, Spring Security authorization, trusted current-user propagation, RBAC foundations, and session ownership protection without changing RentWise's adaptive-training business rules.

**Architecture:** Keep RentWise as a modular monolith. `user` persists identity, `auth` owns register/login use cases, and `security` owns HTTP authentication/authorization. Controllers derive the acting user from `@AuthenticationPrincipal CurrentUser`; Training/Profile/Plan continue to receive `Long userId` internally and never parse JWTs.

**Tech Stack:** Java 17, Spring Boot 3.4.5, Spring Security 6, Spring Data JPA, MySQL 8, H2 tests, JJWT 0.13.0, JUnit 5, AssertJ, MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-09-rentwise-v0.2-auth-security-design.md`

## Global Constraints

- Remain a modular monolith; do not introduce microservices or distributed middleware in v0.2.
- Public self-registration always creates `LEARNER`; clients cannot choose `CONTENT_EDITOR` or `ADMIN`.
- Username length is 3-32 characters; password length is 8-64 characters.
- Passwords are stored only as BCrypt hashes and are never logged or persisted in plaintext.
- JWT access tokens use HS256 and expire after 3600 seconds by default.
- The real JWT signing secret comes from `${JWT_SECRET}`; no real secret is committed.
- HTTP authentication is stateless with `SessionCreationPolicy.STATELESS`.
- Public endpoints are registration, login, Swagger UI, and OpenAPI docs; other `/api/**` endpoints require authentication.
- Current-user APIs must not accept client-controlled `userId` for learner operations.
- Session-addressed operations must enforce `session.userId == authenticated userId`.
- Existing Training/Profile/Plan business tests remain business tests; security is verified in separate auth/security/HTTP tests.
- Do not add refresh tokens, token blacklist, OAuth2/OIDC, verification codes, forgot-password flows, admin UI, dynamic permission tables, or frontend login pages.

## Review Focus

- **Weak/invalid JWT secret:** application/test configuration must provide a sufficiently long HS256 secret and fail clearly rather than silently signing with an unsafe key.
- **Username boundary/normalization:** 2/33-character usernames must fail validation; duplicate detection must behave consistently for the chosen exact-string policy.
- **Malformed Authorization header:** headers other than `Bearer <token>` must not create authentication and protected routes must return 401 JSON.
- **Cross-user session identifiers:** a valid user presenting another user's diagnosis/assessment `sessionId` must not learn or mutate that session; treat it as not found for that user.
- **Security integration regression:** enabling Spring Security must not break the existing diagnosis/mastery/training/assessment business flow when invoked through authenticated HTTP APIs.

---

## Planned File Structure

### New production files

- `src/main/java/com/rentwise/user/domain/UserRole.java` — fixed role enum.
- `src/main/java/com/rentwise/user/dto/CurrentUserResponse.java` — `/api/users/me` response.
- `src/main/java/com/rentwise/user/controller/UserController.java` — current-user endpoint.
- `src/main/java/com/rentwise/auth/dto/RegisterRequest.java` — registration input.
- `src/main/java/com/rentwise/auth/dto/RegisterResponse.java` — registration output.
- `src/main/java/com/rentwise/auth/dto/LoginRequest.java` — login input.
- `src/main/java/com/rentwise/auth/dto/LoginResponse.java` — JWT login output.
- `src/main/java/com/rentwise/auth/service/AuthService.java` — registration and login orchestration.
- `src/main/java/com/rentwise/auth/controller/AuthController.java` — `/api/auth/**` endpoints.
- `src/main/java/com/rentwise/security/CurrentUser.java` — trusted request principal.
- `src/main/java/com/rentwise/security/JwtService.java` — token issue/parse/validation.
- `src/main/java/com/rentwise/security/CustomUserDetailsService.java` — Spring Security user loading.
- `src/main/java/com/rentwise/security/JwtAuthenticationFilter.java` — bearer-token filter.
- `src/main/java/com/rentwise/security/RestAuthenticationEntryPoint.java` — JSON 401.
- `src/main/java/com/rentwise/security/RestAccessDeniedHandler.java` — JSON 403.
- `src/main/java/com/rentwise/security/SecurityConfig.java` — filter chain, provider, encoder, authentication manager, authorization rules.

### Production files to modify

- `pom.xml`
- `src/main/java/com/rentwise/user/domain/UserAccount.java`
- `src/main/java/com/rentwise/user/repository/UserRepository.java`
- `src/main/java/com/rentwise/user/service/UserDemoDataInitializer.java` — remove after authenticated user creation replaces it.
- `src/main/java/com/rentwise/training/repository/DiagnosisSessionRepository.java`
- `src/main/java/com/rentwise/training/service/TrainingFacade.java`
- `src/main/java/com/rentwise/training/service/TrainingService.java`
- `src/main/java/com/rentwise/training/service/DiagnosisWorkflowService.java`
- `src/main/java/com/rentwise/training/service/AssessmentWorkflowService.java`
- `src/main/java/com/rentwise/training/controller/DiagnosisController.java`
- `src/main/java/com/rentwise/training/controller/TrainingController.java`
- `src/main/java/com/rentwise/training/controller/AssessmentController.java`
- `src/main/java/com/rentwise/profile/controller/ProfileController.java`
- `src/main/java/com/rentwise/plan/controller/PlanController.java`
- `src/main/java/com/rentwise/training/dto/TrainingAnswerRequest.java`
- `src/main/resources/application.yml`
- `sql/init.sql`
- `docs/api.md`
- `README.md`

### New/modified tests

- `src/test/java/com/rentwise/user/UserRepositoryTest.java`
- `src/test/java/com/rentwise/auth/AuthServiceTest.java`
- `src/test/java/com/rentwise/security/JwtServiceTest.java`
- `src/test/java/com/rentwise/security/SecurityIntegrationTest.java`
- `src/test/java/com/rentwise/integration/AuthenticatedLearningFlowIntegrationTest.java`
- `src/test/java/com/rentwise/integration/SessionOwnershipIntegrationTest.java`
- Existing Training/Profile/Plan/integration tests updated only where method signatures change.
- `src/test/resources/application-test.yml`

---

### Task 1: Upgrade the user persistence model and dependency baseline

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/com/rentwise/user/domain/UserRole.java`
- Modify: `src/main/java/com/rentwise/user/domain/UserAccount.java`
- Modify: `src/main/java/com/rentwise/user/repository/UserRepository.java`
- Delete: `src/main/java/com/rentwise/user/service/UserDemoDataInitializer.java`
- Modify: `sql/init.sql`
- Modify: `src/test/resources/application-test.yml`
- Test: `src/test/java/com/rentwise/user/UserRepositoryTest.java`

**Interfaces:**
- Produces: `UserRole { LEARNER, CONTENT_EDITOR, ADMIN }`.
- Produces: `UserAccount(String username, String passwordHash, UserRole role, boolean enabled)` plus getters for `id`, `username`, `passwordHash`, `role`, `enabled`, `createdAt`.
- Produces: `Optional<UserAccount> UserRepository.findByUsernameIgnoreCase(String username)`.
- Produces: `boolean UserRepository.existsByUsernameIgnoreCase(String username)`.

- [ ] **Step 1: Write the failing repository test**

Create `UserRepositoryTest` that saves a learner with an encoded-looking placeholder hash, then asserts `findByUsernameIgnoreCase` returns the same username/role/enabled state; add a second assertion that `existsByUsernameIgnoreCase` is true. The test must also assert `createdAt` is populated.

- [ ] **Step 2: Run the focused test and verify failure**

Run:

```bash
mvn -Dtest=UserRepositoryTest test
```

Expected: FAIL because the new role/model/repository API does not exist yet.

- [ ] **Step 3: Add authentication dependencies and user model fields**

In `pom.xml`, add `spring-boot-starter-security` and JJWT 0.13.0 artifacts:

- `io.jsonwebtoken:jjwt-api` compile scope
- `io.jsonwebtoken:jjwt-impl` runtime scope
- `io.jsonwebtoken:jjwt-jackson` runtime scope

Add `UserRole`; evolve `UserAccount` to persist `passwordHash`, `role` with `EnumType.STRING`, `enabled`, and `createdAt`. Preserve the existing generated `id` and unique `username` mapping.

- [ ] **Step 4: Add repository lookup methods and remove passwordless demo initialization**

Add the two case-insensitive repository signatures from the Interfaces block. Delete `UserDemoDataInitializer`; do not silently manufacture a passwordless user after security is introduced.

- [ ] **Step 5: Update SQL and H2 test configuration**

Update `sql/init.sql` so a fresh `users` table includes `password_hash`, `role`, `enabled`, and `created_at`; remove the `demo_user` seed. Add a long test-only JWT secret and `expiration-seconds: 3600` to `application-test.yml` so future context-loading tests have deterministic security configuration.

- [ ] **Step 6: Run focused and full tests**

Run:

```bash
mvn -Dtest=UserRepositoryTest test
mvn clean test
```

Expected: both PASS.

- [ ] **Step 7: Commit**

```bash
git add pom.xml sql/init.sql src/main/java/com/rentwise/user src/test/java/com/rentwise/user/UserRepositoryTest.java src/test/resources/application-test.yml
git commit -m "feat: upgrade RentWise user identity model"
```

---

### Task 2: Implement learner registration with BCrypt

**Files:**
- Create: `src/main/java/com/rentwise/auth/dto/RegisterRequest.java`
- Create: `src/main/java/com/rentwise/auth/dto/RegisterResponse.java`
- Create: `src/main/java/com/rentwise/auth/service/AuthService.java`
- Create/Modify: `src/main/java/com/rentwise/security/SecurityConfig.java`
- Test: `src/test/java/com/rentwise/auth/AuthServiceTest.java`

**Interfaces:**
- Produces: `RegisterRequest(String username, String password)` with Bean Validation enforcing username 3-32 and password 8-64.
- Produces: `RegisterResponse(Long id, String username, UserRole role)`.
- Produces: `RegisterResponse AuthService.register(RegisterRequest request)`.
- Produces: `PasswordEncoder passwordEncoder()` using `BCryptPasswordEncoder`.

- [ ] **Step 1: Write failing registration tests**

Add tests asserting:

- registration persists `role == LEARNER` and `enabled == true`;
- stored `passwordHash` is not the raw password and `passwordEncoder.matches(raw, hash)` is true;
- duplicate username throws the project's domain exception with conflict semantics;
- a case-only variant of an existing username (for example `Atopos` vs `atopos`) is also rejected, keeping H2/MySQL behavior consistent while preserving the originally registered display casing;
- `RegisterRequest` exposes no role field, so caller-controlled role assignment is impossible at the DTO boundary.

- [ ] **Step 2: Run registration tests and verify failure**

```bash
mvn -Dtest=AuthServiceTest test
```

Expected: FAIL because registration service/DTOs/encoder are not implemented.

- [ ] **Step 3: Implement registration DTOs and password encoder bean**

Create the exact request/response records above. Add a `BCryptPasswordEncoder` bean to `SecurityConfig`; keep the class minimal at this stage if the full filter chain is not yet present.

- [ ] **Step 4: Implement `AuthService.register(RegisterRequest)`**

Use `UserRepository.existsByUsernameIgnoreCase`, encode with the injected `PasswordEncoder`, create a `UserAccount` with `LEARNER` and `enabled=true`, save it, and return `RegisterResponse`. Do not accept a role argument.

- [ ] **Step 5: Add Bean Validation boundary tests**

Using Jakarta Validator or MVC-level validation, pin these cases:

- username length 2 -> invalid;
- username length 33 -> invalid;
- password length 7 -> invalid;
- password length 65 -> invalid.

- [ ] **Step 6: Run focused and full tests**

```bash
mvn -Dtest=AuthServiceTest test
mvn clean test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/rentwise/auth src/main/java/com/rentwise/security/SecurityConfig.java src/test/java/com/rentwise/auth/AuthServiceTest.java
git commit -m "feat: add secure learner registration"
```

---

### Task 3: Implement JWT issuance and parsing

**Files:**
- Create: `src/main/java/com/rentwise/security/CurrentUser.java`
- Create: `src/main/java/com/rentwise/security/JwtService.java`
- Modify: `src/main/resources/application.yml`
- Test: `src/test/java/com/rentwise/security/JwtServiceTest.java`

**Interfaces:**
- Produces: `CurrentUser(Long id, String username, UserRole role)`.
- Produces: `String JwtService.generateToken(UserAccount user)`.
- Produces: `CurrentUser JwtService.parse(String token)`; invalid, expired, malformed, or tampered tokens raise a JWT/security exception consumed later by the filter.
- Configuration: `security.jwt.secret=${JWT_SECRET}` and `security.jwt.expiration-seconds=${JWT_EXPIRATION_SECONDS:3600}`.

- [ ] **Step 1: Write failing JWT tests**

Tests must assert:

- generated token parses back to the same `id`, `username`, and `role`;
- `exp - iat` equals 3600 seconds within a small clock tolerance;
- a token signed with one secret fails when parsed with another;
- an expired token fails;
- a malformed token fails;
- a too-short HS256 secret is rejected rather than accepted silently.

- [ ] **Step 2: Run JWT tests and verify failure**

```bash
mvn -Dtest=JwtServiceTest test
```

Expected: FAIL because `JwtService` and `CurrentUser` do not exist.

- [ ] **Step 3: Add externalized JWT configuration**

Update `application.yml` with the exact keys from the Interfaces block. Do not provide a production default secret.

- [ ] **Step 4: Implement HS256 token creation and parsing**

`generateToken(UserAccount)` writes:

- `sub = user.id.toString()`
- `username`
- `role = user.role.name()`
- `iat`
- `exp`

`parse(String)` validates signature/expiry and returns `CurrentUser`.

- [ ] **Step 5: Run focused and full tests**

```bash
mvn -Dtest=JwtServiceTest test
mvn clean test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/rentwise/security/CurrentUser.java src/main/java/com/rentwise/security/JwtService.java src/main/resources/application.yml src/test/java/com/rentwise/security/JwtServiceTest.java
git commit -m "feat: add JWT token service"
```

---

### Task 4: Implement Spring Security credential authentication and auth APIs

**Files:**
- Create: `src/main/java/com/rentwise/security/CustomUserDetailsService.java`
- Modify: `src/main/java/com/rentwise/security/SecurityConfig.java`
- Create: `src/main/java/com/rentwise/auth/dto/LoginRequest.java`
- Create: `src/main/java/com/rentwise/auth/dto/LoginResponse.java`
- Modify: `src/main/java/com/rentwise/auth/service/AuthService.java`
- Create: `src/main/java/com/rentwise/auth/controller/AuthController.java`
- Create: `src/main/java/com/rentwise/user/dto/CurrentUserResponse.java`
- Create: `src/main/java/com/rentwise/user/controller/UserController.java`
- Modify/Test: `src/test/java/com/rentwise/auth/AuthServiceTest.java`

**Interfaces:**
- Produces: `UserDetails CustomUserDetailsService.loadUserByUsername(String username)`.
- Produces: `LoginRequest(String username, String password)`.
- Produces: `LoginResponse(String accessToken, String tokenType, long expiresIn)` with `tokenType="Bearer"` and `expiresIn=3600`.
- Produces: `LoginResponse AuthService.login(LoginRequest request)` using `AuthenticationManager.authenticate(...)`.
- Produces: `POST /api/auth/register`.
- Produces: `POST /api/auth/login`.
- Produces: `GET /api/users/me` accepting `@AuthenticationPrincipal CurrentUser`.

- [ ] **Step 1: Extend auth tests for credential authentication**

Add assertions for:

- valid credentials produce a nonblank JWT login response;
- wrong password fails authentication;
- unknown username fails authentication without exposing a special public response that confirms account existence;
- disabled user fails authentication.

- [ ] **Step 2: Run the tests and verify failure**

```bash
mvn -Dtest=AuthServiceTest test
```

Expected: FAIL because login/provider/user-details wiring is missing.

- [ ] **Step 3: Implement `CustomUserDetailsService`**

Load `UserAccount` case-insensitively by username, map `enabled`, BCrypt password hash, and `ROLE_<UserRole>` authority into Spring Security `UserDetails`.

- [ ] **Step 4: Configure `DaoAuthenticationProvider` and `AuthenticationManager`**

Wire the custom user-details service with the BCrypt encoder. Expose an `AuthenticationManager` from `AuthenticationConfiguration` for `AuthService`.

- [ ] **Step 5: Implement login DTOs and `AuthService.login`**

Authenticate via `AuthenticationManager`; after success, resolve the persisted `UserAccount` case-insensitively by authenticated username and issue a JWT through `JwtService`. Catch Spring `AuthenticationException` and translate all bad/unknown/disabled credential outcomes to one `DomainException(HttpStatus.UNAUTHORIZED, "Invalid username or password")`; do not compare password strings manually.

- [ ] **Step 6: Add auth and current-user controllers**

`AuthController` delegates register/login. `UserController.me(CurrentUser)` returns `CurrentUserResponse(id, username, role)` and contains no JWT parsing logic.

- [ ] **Step 7: Run focused and full tests**

```bash
mvn -Dtest=AuthServiceTest test
mvn clean test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/rentwise/auth src/main/java/com/rentwise/security/CustomUserDetailsService.java src/main/java/com/rentwise/security/SecurityConfig.java src/main/java/com/rentwise/user/controller src/main/java/com/rentwise/user/dto src/test/java/com/rentwise/auth/AuthServiceTest.java
git commit -m "feat: add credential login and auth APIs"
```

---

### Task 5: Add bearer-token filter, stateless authorization, and JSON 401/403 handling

**Files:**
- Create: `src/main/java/com/rentwise/security/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/rentwise/security/RestAuthenticationEntryPoint.java`
- Create: `src/main/java/com/rentwise/security/RestAccessDeniedHandler.java`
- Create: `src/main/java/com/rentwise/security/OpenApiSecurityConfig.java`
- Modify: `src/main/java/com/rentwise/security/SecurityConfig.java`
- Test: `src/test/java/com/rentwise/security/SecurityIntegrationTest.java`

**Interfaces:**
- Produces: `JwtAuthenticationFilter extends OncePerRequestFilter`.
- Produces: JSON `ApiResponse.error(...)` semantics for 401 and 403 responses.
- Produces: stateless `SecurityFilterChain` with JWT filter before `UsernamePasswordAuthenticationFilter`.
- Public: `/api/auth/register`, `/api/auth/login`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`.
- Protected: other `/api/**`.

- [ ] **Step 1: Write failing MockMvc security tests**

Cover:

- `/api/users/me` without token -> 401 JSON;
- malformed bearer token -> 401 JSON;
- tampered token -> 401 JSON;
- valid token -> `/api/users/me` succeeds and reports token identity;
- wrong credentials at `/api/auth/login` -> 401 JSON with the same generic message for wrong password and unknown username;
- header `Authorization: Basic ...` on protected route -> 401 JSON;
- a nested test-only controller imported by `SecurityIntegrationTest` exposes `/test/admin` with `@PreAuthorize("hasRole('ADMIN')")`; it returns 403 to a LEARNER and succeeds for ADMIN;
- `/v3/api-docs` includes a `bearerAuth` HTTP-bearer security scheme so Swagger UI can send JWTs.

- [ ] **Step 2: Run security tests and verify failure**

```bash
mvn -Dtest=SecurityIntegrationTest test
```

Expected: FAIL because bearer authentication and explicit authorization handlers are not wired.

- [ ] **Step 3: Implement REST 401/403 handlers**

Serialize the project's `ApiResponse.error(message)` shape with HTTP 401/403 and `application/json`; do not return Spring's default HTML or generic error body.

- [ ] **Step 4: Implement `JwtAuthenticationFilter`**

For a valid `Bearer` header, parse with `JwtService`, create a `UsernamePasswordAuthenticationToken` whose principal is `CurrentUser`, grant `ROLE_<role>`, and place it in `SecurityContextHolder`. Invalid/malformed/expired JWTs must leave the request unauthenticated and enter the configured 401 path.

- [ ] **Step 5: Complete `SecurityFilterChain`**

Disable CSRF for the stateless API, configure `STATELESS`, public paths, protected `/api/**`, `@EnableMethodSecurity`, 401/403 handlers, and filter ordering. Do not add production role-restricted endpoints yet.

- [ ] **Step 6: Add OpenAPI bearer scheme**

Create `OpenApiSecurityConfig` defining an HTTP bearer scheme named `bearerAuth` with `bearerFormat="JWT"`, and add the corresponding security requirement so Swagger UI exposes the Authorize control.

- [ ] **Step 7: Run focused and full tests**

```bash
mvn -Dtest=SecurityIntegrationTest test
mvn clean test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/rentwise/security src/test/java/com/rentwise/security/SecurityIntegrationTest.java
git commit -m "feat: secure APIs with JWT bearer authentication"
```

---

### Task 6: Remove client-controlled user identity from learner-facing APIs

**Files:**
- Modify: `src/main/java/com/rentwise/training/controller/DiagnosisController.java`
- Modify: `src/main/java/com/rentwise/training/controller/TrainingController.java`
- Modify: `src/main/java/com/rentwise/training/controller/AssessmentController.java`
- Modify: `src/main/java/com/rentwise/profile/controller/ProfileController.java`
- Modify: `src/main/java/com/rentwise/plan/controller/PlanController.java`
- Modify: `src/main/java/com/rentwise/training/dto/TrainingAnswerRequest.java`
- Modify: `docs/api.md`
- Test: `src/test/java/com/rentwise/integration/AuthenticatedLearningFlowIntegrationTest.java`

**Interfaces:**
- `POST /api/diagnosis/start` — no `userId`; uses `CurrentUser.id`.
- `GET /api/profile/me` — replaces `/api/profile/{userId}` for learner use.
- `GET /api/training/next` — no `userId`; uses `CurrentUser.id`.
- `POST /api/training/answers` request becomes `TrainingAnswerRequest(Long caseId, boolean selectedClarify)`.
- `POST /api/assessment/start` — no `userId`; uses `CurrentUser.id`.
- Existing session-path endpoints remain path-addressed pending ownership enforcement in Task 7.

- [ ] **Step 1: Write an authenticated API migration test**

Register/login a learner, call the five current-user endpoints using the JWT, and assert none requires a `userId` parameter/body field. Also assert legacy current-user shapes such as `/api/profile/{otherUserId}` are not available as a way to read another profile.

- [ ] **Step 2: Run the focused test and verify failure**

```bash
mvn -Dtest=AuthenticatedLearningFlowIntegrationTest test
```

Expected: FAIL because controllers still accept explicit user IDs.

- [ ] **Step 3: Replace controller userId inputs with `@AuthenticationPrincipal CurrentUser`**

Each controller extracts `currentUser.id()` and passes that trusted value into the unchanged business facade/service where possible.

- [ ] **Step 4: Remove `userId` from `TrainingAnswerRequest`**

Update `TrainingController.answer(...)` to call `workflowService.submit(currentUser.id(), request.caseId(), request.selectedClarify())`.

- [ ] **Step 5: Update API documentation**

Document auth endpoints, bearer-token usage, `/users/me`, and the v0.2 current-user route shapes. Remove examples telling learners to send their own `userId`.

- [ ] **Step 6: Run focused and full tests**

```bash
mvn -Dtest=AuthenticatedLearningFlowIntegrationTest test
mvn clean test
```

Expected: PASS except any intentionally deferred ownership test that belongs to Task 7.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/rentwise/training/controller src/main/java/com/rentwise/profile/controller/ProfileController.java src/main/java/com/rentwise/plan/controller/PlanController.java src/main/java/com/rentwise/training/dto/TrainingAnswerRequest.java docs/api.md src/test/java/com/rentwise/integration/AuthenticatedLearningFlowIntegrationTest.java
git commit -m "refactor: derive learner identity from JWT"
```

---

### Task 7: Enforce diagnosis and assessment session ownership

**Files:**
- Modify: `src/main/java/com/rentwise/training/repository/DiagnosisSessionRepository.java`
- Modify: `src/main/java/com/rentwise/training/service/TrainingFacade.java`
- Modify: `src/main/java/com/rentwise/training/service/TrainingService.java`
- Modify: `src/main/java/com/rentwise/training/service/DiagnosisWorkflowService.java`
- Modify: `src/main/java/com/rentwise/training/service/AssessmentWorkflowService.java`
- Modify: `src/main/java/com/rentwise/training/controller/DiagnosisController.java`
- Modify: `src/main/java/com/rentwise/training/controller/AssessmentController.java`
- Modify: existing tests whose service signatures change
- Test: `src/test/java/com/rentwise/integration/SessionOwnershipIntegrationTest.java`

**Interfaces:**
- Add: `Optional<DiagnosisSession> DiagnosisSessionRepository.findByIdAndUserId(Long id, Long userId)`.
- Change: `AnswerResult TrainingFacade.submitDiagnosisAnswer(Long userId, Long sessionId, Long caseId, boolean selectedClarify)`.
- Change: `DiagnosisFinishResult TrainingFacade.finishDiagnosis(Long userId, Long sessionId)`.
- Change: `AnswerResult TrainingFacade.submitAssessmentAnswer(Long userId, Long sessionId, Long caseId, boolean selectedClarify)`.
- Change: `DiagnosisFinishResult TrainingFacade.finishAssessment(Long userId, Long sessionId)`.
- Change: `DiagnosisCompleteResponse DiagnosisWorkflowService.finish(Long userId, Long sessionId)`.
- Change: `AssessmentResultResponse AssessmentWorkflowService.finish(Long userId, Long sessionId, AssessmentFinishRequest request)`.
- Ownership mismatch outcome: return the same not-found semantics as an unknown session for that user; do not reveal that another user's session exists.

- [ ] **Step 1: Write the failing two-user ownership test**

Create users A and B through the authenticated API. User A starts diagnosis. With B's token, assert B cannot:

- submit an answer to A's diagnosis session;
- finish A's diagnosis session.

After A completes diagnosis and starts assessment, assert B cannot finish A's assessment session. Expected result for ownership mismatch: 404/domain not-found semantics, not a successful mutation and not a response revealing A's identity/state.

- [ ] **Step 2: Run ownership test and verify failure**

```bash
mvn -Dtest=SessionOwnershipIntegrationTest test
```

Expected: FAIL because current session methods trust session ID alone.

- [ ] **Step 3: Add owned-session repository lookup**

Implement `findByIdAndUserId` and use it as the entry point for user-addressed session operations.

- [ ] **Step 4: Change TrainingFacade/TrainingService signatures to require acting user ID**

Update diagnosis/assessment answer and finish methods to require `userId`. Replace unscoped `findById(sessionId)`/`findOpenSession(sessionId, ...)` with owned-session lookup. Continue using the session's persisted `userId` when writing answer records after ownership is proven.

- [ ] **Step 5: Propagate acting user through workflow services and controllers**

`DiagnosisWorkflowService.finish` and `AssessmentWorkflowService.finish` receive authenticated `userId`; controllers pass `CurrentUser.id()`.

- [ ] **Step 6: Update existing business tests for the new signatures**

Existing tests may still use explicit synthetic user IDs because they validate business logic. Pass the same test user ID through the new ownership-aware signatures; do not rewrite them into HTTP tests.

- [ ] **Step 7: Run ownership, regression, and full tests**

```bash
mvn -Dtest=SessionOwnershipIntegrationTest test
mvn -Dtest=DiagnosisWorkflowIntegrationTest,AssessmentWorkflowIntegrationTest test
mvn clean test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/rentwise/training src/test/java/com/rentwise/integration src/test/java/com/rentwise/training
git commit -m "fix: enforce training session ownership"
```

---

### Task 8: Verify the complete authenticated learning flow and finalize v0.2 docs

**Files:**
- Modify: `src/test/java/com/rentwise/integration/AuthenticatedLearningFlowIntegrationTest.java`
- Modify: `README.md`
- Modify: `docs/api.md` if final response examples need alignment
- Modify: `src/main/resources/application.yml` only if verification exposes configuration mismatch

**Interfaces:**
- External flow: `register -> login -> /users/me -> diagnosis -> profile -> training -> assessment`.
- Runtime environment requires `JWT_SECRET` and existing DB settings.

- [ ] **Step 1: Complete the end-to-end HTTP test**

The test must:

1. register a learner;
2. log in and capture JWT;
3. call `/api/users/me`;
4. start diagnosis without userId;
5. answer all 10 diagnosis cases with authenticated requests;
6. finish diagnosis;
7. read `/api/profile/me` and assert five mastery topics exist;
8. call `/api/training/next`;
9. submit at least one training answer without userId in the body;
10. start and finish a five-topic assessment;
11. confirm profile/next-training responses remain available after assessment.

- [ ] **Step 2: Add final security assertions to the end-to-end test**

Assert the same protected flow fails with 401 when the bearer token is omitted, and that a learner cannot access the test-only ADMIN-restricted resource (403).

- [ ] **Step 3: Run the full automated suite**

```bash
mvn clean test
```

Expected: `BUILD SUCCESS`, zero failures/errors.

- [ ] **Step 4: Update README for v0.2 local startup**

Document, without committing a secret:

```powershell
$env:JWT_SECRET="<your-long-local-secret>"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<your-local-password>"
mvn spring-boot:run
```

Include the auth flow, Swagger URL, and note that the access token expires after 3600 seconds.

- [ ] **Step 5: Manually verify MySQL startup**

With a real local `JWT_SECRET` and existing MySQL `rentwise` database, run:

```bash
mvn spring-boot:run
```

Expected log evidence:

- Hikari pool starts successfully;
- Tomcat starts on configured port;
- `Started RentWiseApplication` appears;
- no JWT-secret configuration failure occurs.

- [ ] **Step 6: Manual Swagger smoke test**

Through Swagger/OpenAPI:

- register;
- login;
- authorize with `Bearer <token>`;
- call `/api/users/me`;
- start diagnosis;
- verify a protected endpoint without authorization returns 401.

- [ ] **Step 7: Run final Git diff/status review**

```bash
git diff --check
git status
```

Expected: no whitespace errors; only intended v0.2 files changed before commit.

- [ ] **Step 8: Commit final docs/test verification changes**

```bash
git add README.md docs/api.md src/test/java/com/rentwise/integration/AuthenticatedLearningFlowIntegrationTest.java
git commit -m "docs: finalize RentWise v0.2 auth workflow"
```

- [ ] **Step 9: Final verification**

```bash
mvn clean test
git status
```

Expected: `BUILD SUCCESS` and clean working tree after all intended commits.

---

## Completion Gate

Do not declare RentWise v0.2 complete until all of these are evidenced by tests or the final manual startup check:

- [ ] self-registration creates only `LEARNER`;
- [ ] BCrypt hash, not plaintext password, is persisted;
- [ ] duplicate username is rejected;
- [ ] valid login returns a 3600-second JWT access token;
- [ ] wrong/unknown/disabled credentials fail authentication;
- [ ] missing, malformed, expired, and tampered JWTs produce 401 on protected routes;
- [ ] valid learner JWT reaches `/api/users/me`;
- [ ] role-restricted access produces 403 for insufficient role;
- [ ] learner-facing APIs no longer trust client-supplied user IDs;
- [ ] cross-user diagnosis/assessment session access is blocked;
- [ ] original adaptive diagnosis/mastery/training/assessment behavior still passes;
- [ ] `sql/init.sql` represents the v0.2 user schema;
- [ ] `JWT_SECRET` remains externalized;
- [ ] `mvn clean test` passes;
- [ ] application starts successfully against MySQL.
