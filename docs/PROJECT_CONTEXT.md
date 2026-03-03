# PROJECT_CONTEXT: BADGR Bolt

Project Owner: BADGRTechnologies LLC  
Product: High-Performance RSVP Speed-Reader (Android)  
Monetization: Freemium (Open Core MIT / Proprietary Pro)

---

## Core Architectural Logic
1. RSVP Engine: Uses a calculated Optimal Recognition Point (ORP) to center words horizontally. Focus is maintained via fixed vertical guide lines.
2. Entitlement: Gated by ProGate.kt. Boolean flags control access to Stats, Cloud Sync, and Advanced Formats.
3. Persistence:
    - DataStore: Persists user settings (WPM, Font Size, ORP Color).
    - Room (v4): Persists library metadata and reading session history.
4. Backend: FastAPI-based badgr-text-service handles complex PDF/EPUB/DOCX/OCR conversion.

---

## Key Files & Functions
- util/OrpEngine.kt: Precision math for focal centering and long-word hyphenation.
- ui/reader/ReaderScreen.kt: The distraction-free, full-screen RSVP environment.
- data/preferences/UserPreferencesRepository.kt: Single source for persistent settings.
- billing/ProGate.kt: Central logic for feature locks.

---

## Phase 2/3 Roadmap (Verified Goals)
1. [PRO] Cloud Sync: Firebase Auth/Firestore for cross-device library and progress.
2. [PRO] Advanced OCR: Image-to-Text conversion for physical book ingestion.
3. [PRO] Text-To-Speech: Synchronized audio feedback for multi-modal learning.
4. [QA] Release Polish: ProGuard, accessibility audits, and Play Store ASO.

---

## Environment & Constraints
- Target SDK: 35 (Android 15)  | Min SDK: 26 (Android 8.0)
- Language: Kotlin 2.0.21 | UI: Jetpack Compose (BOM 2024.09.00)
- Secrets: Never commit API keys. Use BuildConfig or local.properties.
