# Changelog
All notable changes to BADGR Bolt are documented here.
Format: [VERSION] — YYYY-MM-DD

---

## [2.2.5] — 2026-03-03

### Added
- OrbReaderApp registered in AndroidManifest.xml (TD-005 resolved)
- ProGuard/R8 enabled for release builds with full keep rules
- Firebase, Crashlytics, Billing, CloudSyncManager, ProGate ProGuard rules
- CHANGELOG.md, THIRD_PARTY_NOTICES.md, docs/DATA_SAFETY.md scaffolded
- Firebase Auth with email/password and email verification
- Firestore cloud sync: books and progress wired to LibraryViewModel and ReaderViewModel
- Firestore security rules: user-scoped production mode
- AccountScreen.kt and AccountViewModel.kt: full auth UI
- CloudSyncManager.kt: Auth and Firestore singleton
- ProGate.kt restructured with PRIVATE_ROLLOUT_ALL_OPEN toggle

### Changed
- fallbackToDestructiveMigration() removed from BookDatabase (TD-002 resolved)
- Backend /convert endpoint restricted to documents only
- Backend /upload-image endpoint added for image files

### Fixed
- Unresolved reference errors in MainActivity from missing account package files
- CloudSyncManager duplicate property declarations from Gemini session

### Known Issues
- TD-003: Google Play Billing not implemented — PRIVATE_ROLLOUT_ALL_OPEN=true
- TD-004: Deprecated statusBarColor warning in Theme.kt
- TD-006: No unit or instrumentation tests exist
- TD-007: Email verification sent but not enforced for app access

### Next Milestone
- 2.3.6: Google Play Billing and entitlement enforcement
