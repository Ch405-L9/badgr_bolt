# BADGR Bolt - Advanced RSVP Speed Reader

**Version 2.6.0** | Android 8.0+ (API 26+) | BADGRTechnologies LLC

BADGR Bolt is a precision speed reading application using Optimal Recognition Point (ORP)
technology and Rapid Serial Visual Presentation (RSVP) to help you read faster with
better comprehension.

---

## Feature Status

| Feature                      | Status       | Version  | Notes                                    |
|------------------------------|--------------|----------|------------------------------------------|
| RSVP / ORP Engine            | Complete     | 1.0.0    | Core reading engine                      |
| TXT Import via SAF           | Complete     | 1.0.0    |                                          |
| PDF and EPUB Import          | Complete     | 1.0.0    | Via backend conversion service           |
| DOCX and IMAGE Import        | Complete     | 2.4.2    | DOCX via backend; IMAGE via OCR          |
| Firebase Auth                | Complete     | 2.2.5    | Email/password with verification         |
| Firestore Cloud Sync         | Complete     | 2.2.5    | Books and progress; Pro + verified only  |
| Firestore Security Rules     | Complete     | 2.2.5    | User-scoped, production mode             |
| Google Play Billing          | Complete     | 2.3.6    | Monthly subscription + lifetime purchase |
| Pro Entitlement Persistence  | Complete     | 2.3.3    | DataStore-backed, survives process death |
| Purchase Restoration         | Complete     | 2.3.5    | Restored on app resume                   |
| Free Book Limit (5)          | Complete     | 2.3.6    | Upgrade dialog on limit reached          |
| Performance Tracker          | Complete     | 2.4.0    | Session logging, WPM, active time        |
| Achievements (20)            | Complete     | 2.4.0    | 5 categories, Room-persisted             |
| Bolt Rank System             | Complete     | 2.4.0    | 5 tiers based on effective WPM           |
| Achievement Notifications    | Complete     | 2.4.1    | Slide-in toast on unlock                 |
| ORP Color Picker (5)         | Complete     | 2.4.3    | Persisted to DataStore                   |
| Delete Confirmation Dialog   | Complete     | 2.4.3    | BookRow trash icon                       |
| Theme Mode (System/Light/Dark)| Complete    | 2.4.2    | Persisted to DataStore                   |
| Font Picker (6 fonts)        | Complete     | 2.4.4    | System + downloadable fonts              |
| Chunk Reading (1-4 words)    | Complete     | 2.5.0    | ORP focal on first word                  |
| Punctuation Pauses           | Complete     | 2.5.2    | Configurable multipliers                 |
| Email Verification Gate      | Complete     | 2.6.0    | Sync requires verified email (TD-007)    |
| Account Pro Status Card      | Complete     | 2.6.0    | Lifetime vs Monthly display              |
| Forgot Password              | Complete     | 2.6.0    | Firebase password reset                  |
| Terms of Service             | Complete     | 2.6.0    | docs/terms_of_service.html               |
| Upgrade Navigation           | Complete     | 2.6.0    | Unlock buttons route to Account tab      |
| UX / UI Redesign             | Planned      | 2.5.6    | Full BADGR brand pass                    |
| BADGR Logo Integration       | Planned      | 2.5.6    | Requires final logo assets               |
| Closed Beta                  | Pending      | 2.6.1    | Signed AAB + 5 testers                   |
| Public Launch                | Planned      | 3.0.0    |                                          |

---

## Architecture

- **Language**: Kotlin 100%
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with StateFlow
- **Local storage**: Room v2.6.1 (schema v5, explicit migrations)
- **Preferences**: DataStore
- **Network**: Retrofit + OkHttp (30s connect / 60s read)
- **Backend**: Render.com (Python/FastAPI) for PDF/EPUB/DOCX/IMAGE conversion
- **Cloud**: Firebase Auth + Firestore (Crashlytics, Analytics)
- **Billing**: Google Play Billing Library v7.0.0

### Key Components

| File | Purpose |
|---|---|
| `OrpEngine.kt` | ORP index calculation and word segmentation |
| `ProGate.kt` | Single source of truth for feature entitlement |
| `InAppPurchaseManager.kt` | Google Play Billing, purchase acknowledgement, restoration |
| `CloudSyncManager.kt` | Firebase Auth + Firestore sync (requires verified email) |
| `AchievementsEngine.kt` | Pure achievement evaluation, no side effects |
| `ReadingSessionRepository.kt` | Session recording, streak, Bolt Rank, achievement dispatch |
| `BookDatabase.kt` | Room database, schema v5, migrations 1 through 5 |

### Database Schema (v5)

- `books`: id, title, fileType, wordCount, createdAt, currentWordIndex, coverPath
- `reading_sessions`: id, bookId, bookTitle, wordsRead, durationSeconds, avgWpm, rewindCount, timestamp
- `achievements`: id, unlockedAt

---

## Building from Source

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- Gradle 8.9+

### Setup

```bash
git clone https://github.com/Ch405-L9/badgr_bolt.git
cd badgr_bolt
```

Create `gradle.properties` in project root (gitignored):

```properties
android.useAndroidX=true
android.enableJetifier=true
STORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_key_password
```

Add `google-services.json` to `app/` (from Firebase Console, gitignored).

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Known Issues

| ID | Description | Status |
|---|---|---|
| TD-004 | Deprecated `statusBarColor` in Theme.kt | Deferred to 2.5.6 |
| TD-006 | No unit or instrumentation tests | Pre-launch |

---

## Security

- `google-services.json` — gitignored, never committed
- `*.jks` keystore — offline only, never committed
- `gradle.properties` — gitignored, credentials never in source
- ProGuard/R8 enabled for all release builds
- Firestore rules: user-scoped, no cross-user access

---

## Legal

- Privacy Policy: https://ch405-l9.github.io/badgr_bolt/privacy_policy.html
- Terms of Service: https://ch405-l9.github.io/badgr_bolt/terms_of_service.html
- Delete Account: https://ch405-l9.github.io/badgr_bolt/delete_account.html
- Copyright (c) 2026 BADGRTechnologies LLC. All rights reserved.

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for full version history.
