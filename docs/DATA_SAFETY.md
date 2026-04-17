# Data Safety — BADGR Bolt
## BADGRTechnologies LLC — Internal Reference for Google Play Data Safety Form

### Data Collected

| Data Type          | Purpose                        | Encrypted | User Can Delete | Shared With     |
|--------------------|-------------------------------|-----------|-----------------|-----------------|
| Email address      | Firebase Auth account creation | Yes (Firebase) | Yes — delete account | Firebase/Google |
| Reading progress   | Cloud sync for Pro users       | Yes (Firestore) | Yes — delete account | Firebase/Google |
| Book metadata      | Cloud sync for Pro users       | Yes (Firestore) | Yes — delete account | Firebase/Google |
| Crash logs         | Crashlytics stability reporting | Yes | No — anonymized | Firebase/Google |
| App analytics      | Usage patterns (anonymous)     | Yes | No — anonymized | Firebase/Google |

### Data NOT Collected
- Book content (processed server-side, not stored)
- Device identifiers beyond Firebase installation ID
- Location data
- Payment information (handled entirely by Google Play)

### Notes
- Free users with no account: zero data leaves the device
- Pro users: only data listed above is transmitted
- Cover images: stored locally only, never transmitted
- Update this file at every MINOR release that adds a new SDK or permission
