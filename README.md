# BADGR Bolt - Advanced RSVP Speed Reader

**Version 2.5.2** | Android 8.0+ (API 26+)

BADGR Bolt is a precision speed reading app using Optimal Recognition Point (ORP) technology and Rapid Serial Visual Presentation (RSVP) to help you read faster with better comprehension.

---

## ✨ Features

### 📖 Core Reading Experience
- **ORP-Optimized Display**: Scientifically calculated focal point for each word minimizes eye movement
- **Chunk Reading (1-4 words)**: Train peripheral vision by displaying multiple words simultaneously
- **Punctuation Pauses**: Automatic slowdown at sentence boundaries (`.`, `?`, `!`) and clause separators (`,`, `;`, `:`) for natural rhythm
- **Adjustable WPM**: 60-1200 words per minute with live controls
- **6 Professional Fonts**: Monospace (System, JetBrains) and variable-width (Literata, Merriweather, Atkinson Hyperlegible, Open Sans)
- **5 ORP Highlight Colors**: Customizable focal point accent
- **Progress Tracking**: Resume exactly where you left off

### 📚 Library Management
- **Multi-Format Support**: TXT, PDF, EPUB, DOCX, IMAGE (OCR)
- **Cloud Sync** (Pro): Sync library and reading progress across devices via Firebase
- **Book Covers**: Automatic EPUB cover extraction
- **Free Tier**: Up to 5 books
- **Pro Tier**: Unlimited library

### 📊 Stats & Achievements
- **Bolt Rank System**: Dynamic rank (SPARK → BOLT → FLASH → STORM → THUNDER) based on effective WPM
- **20 Achievements**: Unlock milestones across 5 categories (Speed, Consistency, Volume, Dedication, Exploration)
- **Reading Sessions**: Track words read, duration, WPM, and rewind count
- **Streak Tracking**: Monitor consecutive reading days
- **WPM Improvement**: Compare baseline vs recent performance

### 🎨 Customization
- **Theme Modes**: System, Light, Dark
- **Font Size**: 24-60pt adjustable slider
- **ORP Toggle**: Show/hide focal point highlight
- **Punctuation Pause Multipliers**: 1.0x-3.0x configurable for sentences and clauses

### 🔐 Account & Sync
- **Firebase Authentication**: Email/password with verification
- **Cloud Backup** (Pro): Automatic Firestore sync
- **Google Play Billing**: Monthly subscription or lifetime purchase
- **Offline-First**: All features work without internet (except sync)

---

## 🚀 Getting Started

### Installation
1. Download from Google Play Store (coming soon) or build from source
2. Grant storage permissions for book import
3. Import your first book (TXT, PDF, EPUB, DOCX, or IMAGE)
4. Tap to open and start reading!

### First Read
1. **Import a book**: Tap the cyan FAB → select format → choose file
2. **Adjust WPM**: Start at 150-200 WPM, increase as comfortable
3. **Try chunk reading**: Settings → Default Words at a Time → 2 or 3
4. **Customize pauses**: Settings → Punctuation Pauses → adjust multipliers
5. **Track progress**: Stats tab shows your Bolt Rank and achievements

---

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 100%
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with StateFlow
- **Database**: Room (SQLite)
- **Preferences**: DataStore
- **Backend**: Render.com (Python/Flask) for PDF/EPUB/DOCX/IMAGE conversion
- **Cloud**: Firebase Auth + Firestore
- **Billing**: Google Play Billing Library v7

### Key Components
- **OrpEngine**: Pure calculation engine for ORP index and word segmentation
- **ProGate**: Single source of truth for feature entitlement
- **CloudSyncManager**: Firebase Auth + Firestore wrapper
- **AchievementsEngine**: Pure evaluation logic for unlocking achievements
- **InAppPurchaseManager**: Google Play Billing integration with purchase restoration

### Database Schema
- **books**: id, title, fileType, wordCount, createdAt, currentWordIndex, coverPath
- **reading_sessions**: id, bookId, bookTitle, timestamp, wordsRead, durationSeconds, wpm, rewindCount
- **achievements**: id, unlockedAt

---

## 🎯 Roadmap

### v2.5.3 (Next)
- [ ] Bookmarks and notes
- [ ] Export reading stats (CSV/JSON)
- [ ] Custom font upload

### v2.6.0
- [ ] Text-to-Speech (TTS) integration
- [ ] Bionic Reading mode
- [ ] Reading goals and reminders

### v3.0.0
- [ ] Tablet/foldable optimization
- [ ] Wear OS companion app
- [ ] Social features (reading challenges, leaderboards)

---

## 🐛 Known Issues

- **TD-004**: Deprecated `statusBarColor` in Theme.kt (cosmetic, deferred to 2.6.x)
- **TD-006**: No unit or instrumentation tests yet
- **TD-007**: Email verification sent but not enforced for app access

---

## 🔧 Building from Source

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- Gradle 8.9+

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/Ch405-L9/badgr_bolt.git
   cd badgr_bolt
   ```

2. Create `gradle.properties` in project root:
   ```properties
   android.useAndroidX=true
   android.enableJetifier=true
   STORE_PASSWORD=your_keystore_password
   KEY_PASSWORD=your_key_password
   ```

3. Add `google-services.json` to `app/` (from Firebase Console)

4. Build:
   ```bash
   ./gradlew assembleDebug
   ```

5. Install:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

### Latest: v2.5.2 (2026-03-14)
- ✅ Punctuation pause system with configurable multipliers
- ✅ AndroidX configuration fixed
- ✅ OrpEngine import resolved
- ✅ All features tested and working

---

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

Copyright © 2026 BADGRTechnologies LLC. All rights reserved.

This is proprietary software. Unauthorized copying, distribution, or modification is prohibited.

---

## 🙏 Acknowledgments

- **ORP Research**: Based on RSVP studies from MIT and Stanford
- **Fonts**: Google Fonts (Literata, Merriweather, Atkinson Hyperlegible, Open Sans)
- **Icons**: Material Design Icons
- **Backend**: Render.com free tier
- **Cloud**: Firebase free tier

---

## 📧 Support

- **Email**: support@badgr.app
- **Issues**: [GitHub Issues](https://github.com/Ch405-L9/badgr_bolt/issues)
- **Docs**: [Wiki](https://github.com/Ch405-L9/badgr_bolt/wiki)

---

**Made with ⚡ by BADGR**
