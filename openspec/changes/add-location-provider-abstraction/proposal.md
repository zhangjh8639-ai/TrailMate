## Why

TrailMate has navigation algorithms and runtime reducers, but no boundary for real device location input. A production hiking app needs a testable location provider abstraction before wiring foreground tracking services or navigation UI.

## What Changes

- Add a platform-independent location provider contract for starting and stopping location updates.
- Add location request, provider status, subscription, and observer models.
- Add a pure mapper from system location readings into `LocationSample` with validation.
- Add an Android `LocationManager` implementation that emits `LocationSample` updates through the abstraction.
- Declare foreground fine/coarse location permissions in the manifest.
- Keep runtime permission prompts, foreground service, persistence, map rendering, and UI out of scope.

## Capabilities

### New Capabilities
- `location-provider-abstraction`: Platform boundary for real GPS/location updates feeding TrailMate navigation.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/location/`, `app/src/main/java/com/trailmate/app/platform/location/`, `app/src/main/AndroidManifest.xml`, and focused unit tests.
- No new third-party dependencies.
- This slice compiles Android location integration but does not request runtime permission or start background tracking.
