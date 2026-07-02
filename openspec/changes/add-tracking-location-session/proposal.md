## Why

TrailMate's tracking foreground service currently proves the Android lifecycle boundary, but it does not subscribe to real system location updates yet. Production hiking navigation needs the service to own a real GPS sampling session before later slices can add route matching, persistence, off-route alerts, and crash recovery.

## What Changes

- Add a testable tracking location session that starts and stops a `TrailLocationProvider` subscription.
- Wire `TrackingForegroundService` start/stop decisions to start and release real location updates.
- Store only in-memory session status and the latest real sample in this slice.
- Preserve the existing no-fake-data boundary: no simulated GPS points, no track persistence, no route matching, no uploads, and no safety claims.

## Capabilities

### New Capabilities
- `tracking-location-session`: Foreground tracking service owns a real location provider subscription and exposes honest in-memory sampling status.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/services/tracking/`, `app/src/main/java/com/trailmate/app/platform/location/`, focused tracking tests, and OpenSpec artifacts.
- No new third-party dependencies.
- Later slices can build persistence, route matching, off-route detection, and recovery on top of this service-owned real sampling boundary.
