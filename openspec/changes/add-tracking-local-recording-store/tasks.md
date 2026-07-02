## 1. Tests First

- [x] 1.1 Add failing unit tests for tracking recording models and privacy defaults.
- [x] 1.2 Add failing store tests for creating active sessions, appending ordered real samples, querying active session, and marking ended.
- [x] 1.3 Add failing session tests proving `TrackingLocationSession` writes only real samples and writes nothing before the first provider fix.
- [x] 1.4 Add failing service contract tests proving start requests require real route/session context.

## 2. Local Recording Store

- [x] 2.1 Add tracking recording persistence models and mappers from `NavigationSession` / `LocationSample`.
- [x] 2.2 Add `TrackingRecordingStore` interface.
- [x] 2.3 Implement SQLite tracking session and track point tables using the existing local persistence style.
- [x] 2.4 Keep stored sessions private and free of upload/share/route progress side effects.
- [x] 2.5 Add version 1 to version 2 migration coverage for existing imported route databases.
- [x] 2.6 Assign point indexes inside the store append transaction to avoid overwriting recovered sessions.

## 3. Tracking Session Wiring

- [x] 3.1 Add a typed tracking start request carrying route id, session id, start timestamp, and direction.
- [x] 3.2 Extend `TrackingLocationSession` to accept optional recording context and store.
- [x] 3.3 Append samples only from `onLocationSample` and never from `start()`.
- [x] 3.4 Mark recording ended when the session stops.
- [x] 3.5 Wire `TrackingForegroundService` to construct the SQLite recording store from valid start context without adding fake samples.
- [x] 3.6 Keep service disposal separate from explicit user stop so recoverable sessions are not ended by lifecycle teardown.
- [x] 3.7 Prevent route changes from replacing the active tracking route while foreground tracking is running.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run focused database/tracking unit tests.
- [x] 4.3 Run full unit tests and debug build.
- [x] 4.4 Request subagent spec/code review.
- [x] 4.5 Install and smoke test on the connected Android device if available.
