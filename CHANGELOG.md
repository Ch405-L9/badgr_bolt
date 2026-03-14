## [2.3.5] — 2026-03-13
### Changed
- InAppPurchaseManager: queryExistingPurchases() visibility changed from private to public to support on-resume restoration
- MainActivity: added onResume() override; calls purchaseManager.queryExistingPurchases() via lifecycleScope if billing client is connected — restores entitlement after app returns from background or device wake
### Next
- 2.3.6: Release commit — flip PRIVATE_ROLLOUT_ALL_OPEN=false, regression check, tag

## [2.3.4] — 2026-03-13
### Changed
- InAppPurchaseManager: entitlement now granted only after verified acknowledgement; already-acknowledged purchases grant immediately (safe); unacknowledged purchases must ack successfully before _isPro emits true — withheld on failure
- acknowledgePurchase() refactored to return Boolean; true = ack OK and entitlement granted, false = ack failed and error surfaced
- Fixed: enablePendingPurchases() updated to PendingPurchasesParams.newBuilder().enableOneTimeProducts().build() — resolves Billing v7 deprecation warning
- onPurchasesUpdated: handlePurchaseList call moved into scope.launch to enforce suspend context
### Fixed
- Race condition: _isPro could be set true before acknowledgement confirmed — purchase could be revoked by Google within 3 days if ack failed silently
### Next
- 2.3.5: Purchase restoration — queryPurchasesAsync on app resume

## [2.3.3] — 2026-03-13
### Added
- UserPreferencesRepository: added IS_PRO key (booleanPreferencesKey); added setIsPro(Boolean) suspend function; isPro field added to UserPreferences data class (default false)
- OrbReaderApp: added userPreferencesRepository singleton; on startup reads persisted isPro from DataStore and restores ProGate before billing reconnects; collector now writes isPro to DataStore on every emission in addition to updating ProGate
### Changed
- Pro entitlement now survives app restart, process death, and device reboot
### Next
- 2.3.4: Wire ProGate.setProEntitlement() to verified purchase acknowledgement

## [2.3.2] — 2026-03-13
### Added
- AccountViewModel: upgraded from ViewModel to AndroidViewModel; added isPro StateFlow sourced from ProGate.isProFlow; added launchSubscription(activity) and launchLifetime(activity) passthroughs to InAppPurchaseManager
- AccountScreen: SignedIn branch now shows Pro status badge (AssistChip) when entitlement is active; shows Monthly and Lifetime purchase buttons when not Pro; Activity sourced from LocalContext
### Next
- 2.3.3: Persist Pro entitlement to DataStore — survive restart and process death

## [2.3.1] — 2026-03-13
### Changed
- ProGate.kt: upgraded isPro from plain Boolean to MutableStateFlow; exposed isProFlow: StateFlow<Boolean> for UI observation and isPro: Boolean for sync access; setProEntitlement() and revokeEntitlement() now update the flow and respect PRIVATE_ROLLOUT_ALL_OPEN flag
- OrbReaderApp.kt: added applicationScope (SupervisorJob + Dispatchers.Main.immediate); collector wires purchaseManager.isPro StateFlow to ProGate.setProEntitlement() on every emission; applicationScope cancelled in onTerminate()
### Fixed
- TD-003: ProGate was not observing InAppPurchaseManager.isPro — entitlement changes during a session were not reflected in feature gates
### Known Issues
- TD-004: Deprecated statusBarColor in Theme.kt (deferred to 2.5.x)
- TD-006: No unit or instrumentation tests
- TD-007: Email verification not enforced for app access
### Next
- 2.3.2: Implement purchase flow — launchBillingFlow, PurchasesUpdatedListener handling

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
