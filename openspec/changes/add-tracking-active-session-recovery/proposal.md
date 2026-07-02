## Why

TrailMate now persists real tracking sessions locally, but an unfinished session can still become invisible after app recreation or service teardown. A production hiking app must not hide an active or recoverable navigation record from the user.

## What Changes

- Add a local active-session recovery state for the Navigation tab.
- Load the latest unfinished local tracking session from `TrackingRecordingStore` on app start.
- Show a clear Chinese recovery card when an unfinished session exists.
- Let the user explicitly end the recovered recording, marking the local session ended.
- Keep the route/session private and local; do not upload, share, simulate GPS points, or generate a route record in this slice.
- Do not add map rendering, off-route guidance, record summaries, or full crash-restart service recovery in this slice.

## Capabilities

### New Capabilities

- `tracking-active-session-recovery`: Recovers unfinished local tracking sessions into the Navigation tab and allows explicit user ending without fake GPS or sharing side effects.

### Modified Capabilities

- None.

## Impact

- Android Navigation tab state and copy.
- App startup/local store loading in `TrailMateApp`.
- Tracking recording store end-session behavior as used by recovered sessions.
- Unit tests for recovery state and reducer behavior.
- Instrumented/local persistence tests only if store behavior changes.
