# BADGR Bolt

**BADGR Bolt** is a commercial Android speed-reading application developed by BADGRTechnologies LLC. It uses RSVP (Rapid Serial Visual Presentation) technology with ORP (Optimal Recognition Point) centering to enable users to read at 200 to 1000+ words per minute with reduced eye movement and improved comprehension retention.

The application is currently in active development under an open-core freemium model, targeting Google Play distribution with free and Pro tiers.

---

## Features

**Free Tier**
- RSVP reader with ORP focal-point centering
- EPUB, PDF, and TXT import via Storage Access Framework
- Adjustable WPM speed control
- Customizable font size, theme, and focus point
- Local reading library with persistent progress
- Offline reading with no internet required for imported content
- Material 3 design with full Light and Dark mode support
- Reading session statistics and analytics

**Pro Tier**
- Cloud library and progress sync via Firebase Firestore
- Unlimited book imports
- Priority access to new features
- Available via monthly subscription or one-time lifetime purchase

---

## Technology Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Kotlin Coroutines, StateFlow |
| Local Storage | Room Database v2.6.1 (schema v4), DataStore |
| Networking | Retrofit 2, OkHttp 4 |
| Authentication | Firebase Auth (email/password) |
| Cloud Sync | Firebase Firestore |
| Billing | Google Play Billing Library v7.0.0 |
| Crash Reporting | Firebase Crashlytics |
| Build | AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9 |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 35 (Android 15) |

---

## Project Structure
```
app/src/main/java/com/badgr/orbreader/
  billing/
    InAppPurchaseManager.kt   # Google Play Billing singleton
    ProGate.kt                # Pro entitlement enforcement
  data/
    local/                    # Room database, DAOs, entities
    model/                    # Data models
    preferences/              # DataStore preferences repository
    remote/                   # Retrofit API client
    repository/               # Repository layer
  sync/
    CloudSyncManager.kt       # Firebase Firestore sync
  ui/
    account/                  # Account and subscription screens
    library/                  # Book library screen and ViewModel
    reader/                   # RSVP reader screen and ViewModel
    settings/                 # Settings screen and ViewModel
    stats/                    # Reading stats screen and ViewModel
    theme/                    # Material 3 theme, colors, typography
  util/
    OrpEngine.kt              # ORP focal-point calculation engine
    WordTokenizer.kt          # Word tokenization and chunking
    CoverExtractor.kt         # Book cover image extraction
    EpubMetadata.kt           # EPUB metadata parsing
  MainActivity.kt
  OrbReaderApp.kt             # Application class, billing singleton host
```

---

## Getting Started

**Prerequisites**
- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android device or emulator with API 26+

**Setup**

1. Clone the repository:
```
   git clone https://github.com/Ch405-L9/badgr_bolt.git
   cd badgr_bolt
```

2. Add your google-services.json to app/ (not included — obtain from Firebase Console).

3. Create gradle.properties in the project root:
```
   STORE_PASSWORD=your_keystore_password
   KEY_PASSWORD=your_key_password
```

4. Open in Android Studio and sync Gradle dependencies.

5. Build:
```
   ./gradlew assembleDebug
```

---

## Version History

| Version | Status | Description |
|---|---|---|
| v2.3.0 | Current | Google Play Billing infrastructure, InAppPurchaseManager, OrbReaderApp singleton |
| v2.2.5 | Released | Hardening sprint, ProGuard enabled, migrations hardened, documentation |
| v2.2.0 | Released | Firebase Auth, Firestore cloud sync, ProGate entitlement enforcement |
| v2.1.0 | Released | Material 3 migration, high-contrast themes |
| v1.0.0 | Released | Core RSVP engine, local library, Room database |

---

## Roadmap

| Milestone | Scope |
|---|---|
| v2.3.x | Billing infrastructure, ProGate wiring, purchase UI |
| v2.4.x | Performance tracker, advanced reading analytics |
| v2.5.x | UX/UI polish, onboarding flow |
| v2.6.x | Store readiness, Play Console compliance |
| v3.0.0 | Public launch on Google Play |

---

## Security

Before every push, verify no secrets are present:
```
grep -r "AIza\|sk-ant\|AKIA" app/src --include="*.kt"
```

Never commit: google-services.json, *.jks, keystore.properties, gradle.properties.

---

## License

The core RSVP engine and all free-tier modules are licensed under the MIT License.
All Pro-tier modules are proprietary and owned exclusively by BADGRTechnologies LLC.

Copyright 2026 BADGRTechnologies LLC. All rights reserved.

---

## Legal

- Third-Party Notices: THIRD_PARTY_NOTICES.md
- Data Safety: docs/DATA_SAFETY.md
- Privacy Policy: Pending publication

---

*Developed and maintained by BADGRTechnologies LLC.*
