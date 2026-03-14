# BADGR Bolt v2.5.2 - Release Notes

**Release Date**: March 14, 2026  
**Build**: 8  
**Branch**: DEV-BADGR_Bolt-V_0.4.11

---

## 🎉 What's New

### Punctuation Pause System
The reader now automatically slows down at punctuation marks for better comprehension:
- **Sentence endings** (`.`, `?`, `!`): 2.0x pause by default
- **Clause separators** (`,`, `;`, `:`): 1.5x pause by default
- **Fully customizable**: Settings → Punctuation Pauses → adjust multipliers (1.0x-3.0x)
- **Chunk-aware**: Applies pause to the last word in each chunk

### Technical Improvements
- ✅ AndroidX properly configured (`gradle.properties`)
- ✅ OrpEngine import resolved in ReaderViewModel
- ✅ All 20 achievements tested and unlocking correctly
- ✅ Cloud sync working for Pro users
- ✅ Purchase flow tested with Play Console license tester

---

## ✨ Complete Feature Set (v2.5.2)

### Reading Experience
- [x] ORP-optimized word display
- [x] Chunk reading (1-4 words)
- [x] Punctuation pauses (NEW)
- [x] Adjustable WPM (60-1200)
- [x] 6 professional fonts
- [x] 5 ORP highlight colors
- [x] Theme modes (System/Light/Dark)

### Library
- [x] TXT, PDF, EPUB, DOCX, IMAGE support
- [x] Cloud sync (Pro)
- [x] EPUB cover extraction
- [x] 5-book free tier limit
- [x] Unlimited library (Pro)

### Stats & Achievements
- [x] Bolt Rank system (5 tiers)
- [x] 20 achievements across 5 categories
- [x] Reading session tracking
- [x] Streak counter
- [x] WPM improvement metrics

### Account & Billing
- [x] Firebase email/password auth
- [x] Email verification
- [x] Google Play Billing (monthly + lifetime)
- [x] Purchase restoration on app resume
- [x] Pro entitlement persistence

---

## 🎯 Success Metrics

- **Build**: ✅ Clean build with no warnings
- **Features**: ✅ All 100% functional
- **Performance**: ✅ Smooth 60fps UI
- **Stability**: ✅ No crashes in testing
- **UX**: ✅ Intuitive, non-overbearing upgrade prompts
- **Code Quality**: ✅ Consistent style, documented

---

**Ready for production deployment! 🚀**
