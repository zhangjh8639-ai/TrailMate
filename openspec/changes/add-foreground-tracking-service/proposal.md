## Why

TrailMate needs a real foreground execution boundary before GPS tracking can safely continue while the app is backgrounded or the screen is locked. The current code has location and navigation core pieces, but no Android service lifecycle or notification contract.

## What Changes

- Add a foreground tracking service shell with explicit start/stop actions.
- Add notification channel and notification content helpers for active tracking.
- Declare foreground service and notification permissions needed by modern Android.
- Register the service with `foregroundServiceType="location"`.
- Keep runtime permission prompts, actual GPS subscription, route session persistence, crash recovery, and navigation UI wiring out of scope.

## Capabilities

### New Capabilities
- `foreground-tracking-service`: Android foreground service boundary for active hiking navigation/tracking.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/services/tracking/`, `app/src/main/AndroidManifest.xml`, focused tests, and OpenSpec artifacts.
- No new third-party dependencies.
- This slice creates the service shell only; later slices will inject the location provider and runtime reducer.
