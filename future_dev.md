# BADGR Bolt — Future Development Priorities

Ordered top-to-bottom by user impact and technical urgency.

---

## P0 — Critical / Production Blockers

1. **EPUB spine-based chapter detection**
   Current: heuristic keyword scan on word list (loses structure).
   Fix: parse EPUB spine manifest and record per-chapter word-index boundaries at import time. Store in Room DB alongside BookEntity. Enables accurate Ch X / Y tracking.

2. **Dark-mode color contrast audit (WCAG AA)**
   Run the full app UI through a contrast checker. Several textDimmed elements (0xFF9E9E9E on 0xFF121212 = 5.3:1) pass, but light-theme equivalents have not been audited. Required before Play Store accessibility review.

3. **Font size migration for existing users**
   Default changed from 46 → 36 in v0.4.11. Users with stored value of 46+ will not see the new default. Add a one-time migration in OrbReaderApp that clamps saved font sizes to the new 16–52 range on first launch.

---

## P1 — High Impact Features

4. **Pinch-to-zoom live font scaling in reader**
   Let users resize text without leaving the reader via a two-finger pinch gesture, saving the result to DataStore. Remove the need to visit Settings mid-session.

5. **Swipe-left / Swipe-right chapter navigation**
   Horizontal swipe on the word-display area as a secondary gesture for chapter skip (complementing current long-press). Industry-standard pattern for e-readers (Kindle, Moon+ Reader).

6. **Chapter list / table-of-contents drawer**
   A bottom sheet or side drawer showing all detected chapters as a tappable list. Allows direct jump to any chapter. Pair with EPUB spine parsing (P0-1).

7. **ORP position visual indicator**
   Show a subtle vertical tick or bracket above/below the word display box whose horizontal position tracks the ORP character across words. Helps train peripheral anchoring for new users.

8. **Percentage + estimated time remaining**
   Show "~12 min left" alongside the word counter using `(remainingWords / currentWpm)`. Motivates session completion.

---

## P2 — Quality-of-Life Improvements

9. **Session pause on screen-off / background**
   Currently playback may continue if the screen turns off. Register a BroadcastReceiver for `ACTION_SCREEN_OFF` to auto-pause and save progress.

10. **TalkBack / accessibility service full pass**
    Add `Modifier.semantics { role = Role.Button; stateDescription = "…" }` to all interactive elements. Test with TalkBack enabled. Required for Google Play accessibility compliance.

11. **Haptic rhythm mode**
    Optional: emit a subtle haptic pulse on each word flash, synchronized with WPM timing. Aids focus and can substitute for audio cues in silent environments.

12. **Import from cloud / URL**
    Allow importing public-domain books by URL (Project Gutenberg .txt). Uses existing HTTP client; add a URL input dialog in LibraryScreen.

13. **Color blindness simulator preview in Settings**
    Show a live mini-preview of the ORP word display rendered in the selected color blindness palette before the user commits, reducing trial-and-error.

14. **Per-book font and WPM override**
    Some books read better at different speeds or fonts. Store per-book overrides in BookEntity and apply them when opening that book.

---

## P3 — Analytics / Monetisation

15. **Reading streak widget (home screen)**
    Android App Widget showing current streak days and daily word-count goal. Drives re-engagement without opening the app.

16. **WPM history chart in Stats**
    Line graph of effective WPM per session over time (data already stored in ReadingSessionEntity). Makes progress visible.

17. **Achievement share card**
    When an achievement unlocks, offer a share sheet with a pre-rendered card (book title, achievement name, WPM). Social loop for organic growth.

18. **Offline AI summary (on-device)**
    Use Android ML Kit or a bundled small model to generate a one-paragraph chapter summary surfaced after each chapter completes. Pro feature.

---

## P4 — Platform / Infrastructure

19. **iOS / KMP port**
    Evaluate Kotlin Multiplatform + Compose Multiplatform to share ViewModel and data layers with an iOS client. UI layer would need platform-specific wrappers.

20. **CI/CD pipeline (GitHub Actions)**
    Automate lint → unit tests → assemble release APK → upload to Play internal track on every push to `main`. Currently build and deploy are manual.

21. **Firebase Remote Config for feature flags**
    Gate experimental features (AI summary, new UI patterns) without forcing an app update. Enables A/B testing on WPM defaults, chunk-size defaults, etc.
