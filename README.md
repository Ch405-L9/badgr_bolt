# BADGR Bolt

High-Performance Speed-Reader for Android.

BADGR Bolt is a professional speed-reading application designed for distraction-free text consumption. Utilizing the RSVP (Rapid Serial Visual Presentation) method and ORP (Optimal Recognition Point) centering, it enables users to process text at 200–1000+ WPM with reduced ocular fatigue.

## Core Features
- RSVP Engine: Precision focal-point centering with fixed vertical guide lines.
- Dynamic Theming: High-contrast Material 3 design with full Light and Dark mode support.
- Advanced Persistence: DataStore for persistent settings and Room for reading session analytics.
- Format Support: Local .TXT import via Storage Access Framework (SAF).

## Technology Stack
- UI: Jetpack Compose (Material 3).
- Architecture: MVVM with Kotlin Coroutines and StateFlow.
- Storage: DataStore, Room Database.
- Network: Retrofit/OkHttp (Backend-agnostic).

## Getting Started
1. Clone: git clone https://github.com/Ch405-L9/ReaderRSVP.git
2. Open in Android Studio (Ladybug or later).
3. Synchronize Gradle dependencies.
4. Deploy to target device (Min SDK 26).

## License
Core RSVP engine and Free modules are licensed under MIT. All Pro-tier modules, including OCR and Cloud Sync, are Proprietary to BADGRTechnologies LLC.
