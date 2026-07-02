## Why

TrailMate can now keep a foreground service alive and subscribe to real system location updates, but those samples are still only in memory. Production hiking navigation needs local recording storage so a lock-screen/background session can later recover, resume, and produce a trustworthy route record without fabricating points.

## What Changes

- Add a local tracking recording store for navigation sessions and track points.
- Persist session metadata with private visibility by default.
- Persist real `LocationSample` updates as ordered track points with coordinate, accuracy, timestamp, speed, and bearing.
- Expose queries for an active unfinished session and ordered session track points.
- Wire `TrackingLocationSession` to append samples through a store boundary while preserving the existing no-fake-data rule.
- Keep UI record screens, route matching, off-route alerts, uploads, and live sharing out of scope.

## Capabilities

### New Capabilities
- `tracking-local-recording-store`: Local persistence for active tracking sessions and real GPS track points.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/database/`, `app/src/main/java/com/trailmate/app/services/tracking/`, focused tests, and OpenSpec artifacts.
- No new third-party dependencies; follows the existing `SQLiteOpenHelper` persistence style.
- Later slices can use this store for crash recovery, route matching input, record summaries, and privacy-respecting export/delete flows.
