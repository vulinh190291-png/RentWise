# Changelog

## v0.2.0

### Added

- Username/password registration and login
- BCrypt password hashing
- JWT access-token authentication
- Spring Security stateless security
- LEARNER / CONTENT_EDITOR / ADMIN roles
- Authenticated CurrentUser context
- Session ownership validation

### Changed

- Learner APIs no longer trust client-provided userId
- Diagnosis, training, profile and assessment flows use authenticated identity

### Fixed

- Concurrent duplicate username registration is handled as 409 Conflict
- Unlisted endpoints default to authenticated access

---

## v0.1.0

### Added

- Modular monolith MVP
- Initial diagnosis workflow
- Five-topic Mastery profile
- Weak-topic adaptive training
- Learning feedback and learning cards
- Stage assessment workflow
- User / Training / Profile / Plan module boundaries
