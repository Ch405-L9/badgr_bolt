# Project Architecture (BADGR Bolt)

This is an Android app built with:
- Kotlin, Jetpack Compose, MVVM, Room, and Navigation
- Core speed-reading engine (RSVP/ORP) in RSVPEngine-related files
- Zero-permission, offline-first MVP plus planned Pro features (billing, sync, advanced files, tracker, TTS)

## Coding Standards

- Use Kotlin for all app code
- Follow existing package structure (com.badgr.*) and naming
- Maintain offline reading path for Free users
- Do not introduce new Android permissions without explicit instruction

## Agent Rules

- Treat all generated code as draft; never assume it is correct until reviewed
- Prefer small, file-scoped changes over large refactors
- When unsure, ask clarifying questions instead of guessing
