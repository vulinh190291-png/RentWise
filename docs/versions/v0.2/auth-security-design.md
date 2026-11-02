# RentWise v0.2 Authentication & RBAC Design

**Status:** Design approved in conversation; implementation not started  
**Target version:** v0.2  
**Architecture:** Modular Monolith  
**Scope:** Username/password registration, BCrypt password hashing, JWT access-token authentication, Spring Security integration, trusted current-user propagation, RBAC foundations, ownership checks, and migration of existing user-facing APIs away from client-supplied `userId`.

## 1. Goal

RentWise v0.2 adds a real identity and authorization boundary around the already-working v0.1 learning flow without changing the core adaptive-training product logic.

The version must preserve the existing business loop:

`diagnosis -> mastery profile -> weakest-topic training -> mastery update -> stage assessment`

while adding:

`register -> login -> JWT -> authenticated learning flow`

The central security rule is:

> The client must no longer be trusted to tell RentWise which user it is acting as.

Authenticated user identity must come from a verified JWT and be propagated through Spring Security into the application as a trusted `CurrentUser`.

## 2. Explicit Non-Goals

v0.2 intentionally does **not** implement:

- email or phone registration
- verification codes
- forgot-password or reset-password flows
- change-password flow
- refresh tokens
- token blacklist/revocation infrastructure
- Redis-backed sessions or token state
- forced logout / device session management
- OAuth2 / OpenID Connect
- third-party login (GitHub, Google, WeChat, etc.)
- profile/avatar management
- admin UI
- user/role CRUD screens
- dynamic permission tables (`permissions`, `role_permissions`)
- login audit/risk-control systems
- frontend login pages

The intent is a small but complete backend authentication increment, not a full identity platform.

## 3. Chosen Approach

Use:

- Spring Security 6 (through Spring Boot 3)
- `SecurityFilterChain`
- `AuthenticationManager`
- `DaoAuthenticationProvider`
- custom `UserDetailsService`
- `BCryptPasswordEncoder`
- custom JWT service using JJWT
- a `OncePerRequestFilter`-based JWT authentication filter
- `@AuthenticationPrincipal` with a small `CurrentUser` principal
- stateless HTTP security (`SessionCreationPolicy.STATELESS`)

This is preferred over OAuth2 Resource Server for v0.2 because RentWise currently owns both credential verification and token issuance and does not yet have a separate identity provider. It is preferred over fully custom authentication because Spring Security should remain responsible for authentication and authorization semantics.

## 4. Module Responsibilities

### 4.1 `user`

Owns persisted user identity data and user lookup.

Responsibilities:

- `UserAccount`
- `UserRole`
- `UserRepository`
- current-user representation endpoint (`GET /api/users/me`)
- user lookup services

### 4.2 `auth`

Owns registration and login use cases.

Responsibilities:

- registration validation
- duplicate username check
- password encoding
- login orchestration
- JWT issuance after successful authentication

### 4.3 `security`

Owns HTTP authentication/authorization mechanics.

Responsibilities:

- Spring Security filter-chain configuration
- JWT parsing and signature/expiry validation
- building `Authentication`
- placing authentication in `SecurityContext`
- authentication failure handling (401)
- access-denied handling (403)
- Spring Security `UserDetailsService`
- `CurrentUser` principal

`training`, `profile`, and `plan` remain business modules. They receive trusted user IDs from the authenticated request boundary but do not parse JWTs themselves.

## 5. User Domain Model

`UserAccount` evolves from the v0.1 `id + username` shape to:

- `id: Long`
- `username: String`
- `passwordHash: String`
- `role: UserRole`
- `enabled: boolean`
- `createdAt: LocalDateTime` (or equivalent persisted timestamp)

`UserRole` is a fixed enum:

- `LEARNER`
- `CONTENT_EDITOR`
- `ADMIN`

For v0.2 each user has exactly one role. A many-to-many role/permission schema is deliberately deferred.

### 5.1 Registration role rule

Public self-registration always creates:

- `role = LEARNER`
- `enabled = true`

The public registration request does not contain a `role` field. A user must not be able to register as `CONTENT_EDITOR` or `ADMIN`.

The conceptual role hierarchy is:

`ADMIN -> CONTENT_EDITOR -> LEARNER`

Higher roles may use learner-facing functions as well. Actual content-management/admin endpoints will be introduced only when real business functions require them.

## 6. Password Handling

Passwords are never stored or logged in plaintext.

Registration flow:

`raw password -> BCryptPasswordEncoder.encode(...) -> passwordHash -> users table`

Login verification is performed by Spring Security through `AuthenticationManager` / `DaoAuthenticationProvider` using BCrypt matching, not by manual string comparison in `AuthService`.

Input constraints for v0.2:

- username: 3-32 characters
- password: 8-64 characters

No mandatory uppercase/digit/special-character composition rule is introduced in this version.

## 7. JWT Design

v0.2 issues a single access token after successful login.

### 7.1 Claims

Token payload contains the minimum identity needed by the application:

- `sub`: user ID
- `username`: username
- `role`: current role
- `iat`: issued-at time
- `exp`: expiration time

### 7.2 Lifetime

Default access-token lifetime: **60 minutes**.

There is no refresh token in v0.2.

### 7.3 Signing

Use HMAC SHA-256 (`HS256`) with a sufficiently long secret supplied through an environment variable.

Configuration must reference the environment, for example:

```yaml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration-seconds: ${JWT_EXPIRATION_SECONDS:3600}
```

No real secret is committed to GitHub.

RSA/JWKS is deferred until RentWise has a real independently deployed identity boundary where asymmetric verification would provide practical value.

## 8. Security Request Flow

For protected requests:

1. Client sends `Authorization: Bearer <access-token>`.
2. `JwtAuthenticationFilter` (a `OncePerRequestFilter`) reads the header.
3. `JwtService` verifies signature and expiration and parses identity claims.
4. The application builds a `CurrentUser` principal containing:
   - user ID
   - username
   - role
5. A Spring Security `Authentication` object is created and placed in `SecurityContext`.
6. Authorization rules are evaluated.
7. Controllers access the current identity with `@AuthenticationPrincipal CurrentUser`.
8. Controllers pass the trusted user ID into existing application/business services.

Controllers must not parse JWTs directly and must not repeatedly reach into `SecurityContextHolder` when `@AuthenticationPrincipal` can express the dependency directly.

## 9. `CurrentUser`

Use a compact principal such as:

```java
public record CurrentUser(
    Long id,
    String username,
    UserRole role
) {}
```

Its purpose is to isolate controller/business code from JWT details and reduce Spring Security coupling at the use-case boundary.

## 10. Security Configuration

Configure security using a `SecurityFilterChain` bean rather than the obsolete `WebSecurityConfigurerAdapter` style.

### 10.1 Session policy

Use:

`SessionCreationPolicy.STATELESS`

The server must not depend on an HTTP session or `JSESSIONID` for authenticated identity.

### 10.2 CSRF

CSRF protection may be disabled for these stateless bearer-token APIs because authentication is carried explicitly in the `Authorization` header rather than via browser session cookies.

### 10.3 Public paths

Anonymous access is allowed for:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`

### 10.4 Protected paths

All other `/api/**` routes require authentication unless a future design explicitly adds another public endpoint.

### 10.5 Filter ordering

`JwtAuthenticationFilter` executes before `UsernamePasswordAuthenticationFilter`.

## 11. Authentication and Authorization Failure Semantics

RentWise must preserve a JSON API response shape for security failures.

### 11.1 401 Unauthorized

Returned when authentication is missing or invalid, including:

- no access token on a protected endpoint
- malformed token
- invalid signature
- expired token
- failed login credentials

Handled through a REST `AuthenticationEntryPoint` or equivalent security failure path.

### 11.2 403 Forbidden

Returned when identity is valid but the authenticated role is insufficient for the requested resource.

Handled through a REST `AccessDeniedHandler` or equivalent authorization failure path.

The semantic distinction is important:

- 401: identity was not successfully authenticated
- 403: identity is known but lacks permission

## 12. API Design

### 12.1 Public Auth APIs

#### `POST /api/auth/register`

Request:

```json
{
  "username": "atopos",
  "password": "12345678"
}
```

Response data includes at least:

- `id`
- `username`
- `role`

Registration does **not** automatically issue a token. Login remains a separate operation for clarity and testability.

Duplicate usernames fail with a clear domain error.

#### `POST /api/auth/login`

Request:

```json
{
  "username": "atopos",
  "password": "12345678"
}
```

Successful response data includes:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 12.2 Current User API

#### `GET /api/users/me`

Returns the current authenticated identity, including at least:

- `id`
- `username`
- `role`

This endpoint also serves as a simple verification point that JWT-to-SecurityContext propagation works.

## 13. Existing API Migration

The main v0.2 API change is removal of client-controlled user identity where the operation is inherently “for the current user.”

### 13.1 Diagnosis

Before:

`POST /api/diagnosis/start?userId={userId}`

After:

`POST /api/diagnosis/start`

The controller obtains `CurrentUser.id` from the authenticated principal and calls the existing business service with that ID.

The answer and finish endpoints retain the session ID in the path:

- `POST /api/diagnosis/{sessionId}/answers`
- `POST /api/diagnosis/{sessionId}/finish`

but must verify session ownership against the authenticated user.

### 13.2 Profile

Before:

`GET /api/profile/{userId}`

After:

`GET /api/profile/me`

No client-supplied user ID is accepted for the ordinary learner profile endpoint.

### 13.3 Training

Before:

`GET /api/training/next?userId={userId}`

After:

`GET /api/training/next`

The training-answer request changes from a body containing `userId` to a body containing only training input, for example:

```json
{
  "caseId": 5,
  "selectedClarify": true
}
```

The controller supplies the authenticated user ID to the workflow service.

### 13.4 Assessment

Before:

`POST /api/assessment/start?userId={userId}`

After:

`POST /api/assessment/start`

`POST /api/assessment/{sessionId}/finish` remains session-addressed but must enforce that the session belongs to the authenticated user.

## 14. Ownership / IDOR Protection

Removing explicit `userId` parameters is necessary but not sufficient.

Any endpoint operating on an object identifier (especially `sessionId`) must verify ownership before mutating or revealing user-specific state.

Examples:

- diagnosis answer
- diagnosis finish
- assessment finish

Required invariant:

`session.userId == authenticated CurrentUser.id`

A logged-in user must not be able to use another user's session ID to answer, finish, or observe another user's learning state.

This explicitly addresses IDOR-style authorization risk.

## 15. RBAC in v0.2

Learner-facing routes are available to all authenticated roles:

- `LEARNER`
- `CONTENT_EDITOR`
- `ADMIN`

This includes:

- `/api/users/me`
- `/api/diagnosis/**`
- `/api/profile/me`
- `/api/training/**`
- `/api/assessment/**`

v0.2 establishes the role model and Spring Security authorization mechanism but does **not** invent fake admin/content endpoints purely to demonstrate RBAC.

When real content-management functions arrive, routes such as case creation/editing may be restricted to `CONTENT_EDITOR` and `ADMIN`. True admin-only operations will be added only when a real admin use case exists.

Tests may use a minimal protected test configuration/resource to prove 403 behavior without making that test-only concept part of the production product API.

## 16. Database Migration

The `users` table evolves to include approximately:

```text
users
- id
- username
- password_hash
- role
- enabled
- created_at
```

Development may continue to use Hibernate `ddl-auto: update` during iteration, but `sql/init.sql` must also be updated so a fresh database can initialize the v0.2 schema correctly.

The old passwordless `demo_user` initializer and seed row are removed/replaced because v0.2 users must be valid authentication subjects.

If development/admin/editor seed accounts are needed later, their passwords must come from environment variables rather than hard-coded source or SQL plaintext.

No new cross-module database foreign keys from Training/Profile/Plan to the users table are required in v0.2. The modular-monolith boundary should remain compatible with eventual service/database separation.

## 17. Testing Strategy

Keep existing business-layer tests that directly exercise Training/Profile/Plan with explicit user IDs. Those tests validate business rules and do not need to become HTTP security tests.

Add separate authentication/security tests.

### 17.1 Auth service tests

Cover at least:

- successful learner registration
- BCrypt storage (stored value is not plaintext and matches through encoder)
- duplicate username rejection
- registration cannot choose `ADMIN` or `CONTENT_EDITOR`
- successful authentication
- wrong-password authentication failure
- disabled-user authentication failure

### 17.2 JWT/security tests

Cover at least:

- token generation and parsing
- expiry validation
- tampered signature rejection
- missing token -> 401 on protected routes
- malformed/invalid token -> 401
- insufficient role -> 403

### 17.3 HTTP integration tests

Exercise the real external flow:

`register -> login -> token -> /users/me -> diagnosis -> profile -> next training -> assessment`

Also cover ownership isolation with two users:

- user A creates session A
- user B cannot answer or finish session A

### 17.4 Regression requirement

Existing adaptive-training behavior must remain valid after security integration:

- diagnosis still creates the five-topic mastery profile
- weakest-topic recommendation still works
- training updates mastery
- repeated wrong answers still trigger the learning-card/difficulty behavior
- stage assessment still updates the profile/plan

## 18. Acceptance Criteria

v0.2 is complete only when all of the following are true:

- [ ] public registration creates a `LEARNER`
- [ ] plaintext passwords are never persisted
- [ ] duplicate usernames are rejected
- [ ] login with valid credentials returns a JWT access token
- [ ] wrong credentials return 401
- [ ] authenticated JWT can call `GET /api/users/me`
- [ ] protected business APIs reject unauthenticated requests with 401
- [ ] valid users without sufficient role receive 403 on role-restricted resources
- [ ] current-user business APIs no longer trust client-supplied `userId`
- [ ] a user cannot operate on another user's diagnosis/assessment session
- [ ] existing diagnosis/training/mastery/assessment behavior still passes
- [ ] `sql/init.sql` represents the v0.2 user schema
- [ ] real JWT secret is externalized and not committed
- [ ] `mvn clean test` succeeds
- [ ] application starts successfully against MySQL

## 19. Resulting v0.2 Architecture

```text
Register
   |
   v
 BCrypt
   |
   v
 UserAccount (MySQL)

Login
   |
   v
AuthenticationManager
   |
   v
DaoAuthenticationProvider
   |
   v
CustomUserDetailsService + BCrypt
   |
   v
JwtService
   |
   v
Access Token

Protected request
   |
   v
Authorization: Bearer <JWT>
   |
   v
JwtAuthenticationFilter
   |
   v
JwtService validation
   |
   v
CurrentUser
   |
   v
SecurityContext
   |
   v
Controller
   |
   v
trusted userId
   |
   +-------------------------------+
   | Training / Profile / Plan     |
   +-------------------------------+
```

## 20. Version Definition

**RentWise v0.2 — JWT Authentication & RBAC**

v0.2 remains a modular monolith. Its purpose is to make the existing RentWise learning system operate behind a trustworthy identity and authorization boundary. It does not introduce microservices, distributed middleware, or additional product features unrelated to authentication.
