## Why

TrailMate now has a foreground tracking service shell, but users still cannot start it from the navigation flow with a clear Android permission contract. Before wiring GPS sampling and persistence, the app needs a production-safe entry point that explains why location and notifications are needed, requests them only when navigation starts, and starts or stops the real foreground service without fake tracking data.

## What Changes

- Add a navigation-screen start control for beginning active track navigation.
- Add a runtime permission gate for foreground location and Android 13+ notifications.
- Add a small service launcher abstraction so Compose UI can start/stop `TrackingForegroundService` without owning Android intent details.
- Show Chinese, safety-focused fallback copy when permission is denied.
- Keep background location, GPS subscription, track persistence, crash recovery, and route-specific navigation session storage out of scope.

## Capabilities

### New Capabilities
- `tracking-permission-start-ui`: User-facing navigation entry, runtime permission gate, and service launcher for starting/stopping the Android foreground tracking service.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/feature/navigation/`, `app/src/main/java/com/trailmate/app/services/tracking/`, `app/src/main/java/com/trailmate/app/MainActivity.kt`, focused tests, and OpenSpec artifacts.
- No new third-party dependencies.
- This slice makes the foreground service reachable from UI, but does not yet collect GPS points or persist a navigation session.
