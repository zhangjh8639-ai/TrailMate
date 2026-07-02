## 1. Tests First

- [x] 1.1 Add failing controller tests for start/stop location update commands and command ordering.
- [x] 1.2 Add failing tracking location session tests for provider subscription request, ready state, real sample capture, unavailable statuses, and idempotent stop.
- [x] 1.3 Add failing test that a started session has no latest sample before the provider emits one.

## 2. Tracking Location Session

- [x] 2.1 Implement in-memory `TrackingLocationSessionState` and status values.
- [x] 2.2 Implement `TrackingLocationSession` as a `LocationProviderObserver` that starts and stops a `TrailLocationProvider` subscription.
- [x] 2.3 Keep the session free of persistence, uploads, fake samples, and route progress simulation.

## 3. Foreground Service Wiring

- [x] 3.1 Extend tracking service commands so permitted start enters foreground before starting location updates.
- [x] 3.2 Extend stop/null/unknown action handling so location updates stop before foreground shutdown.
- [x] 3.3 Wire `TrackingForegroundService` to create an Android location provider session and execute the new commands.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run focused tracking service/session unit tests.
- [x] 4.3 Run full unit tests and debug build.
- [x] 4.4 Request subagent code/spec review.
- [x] 4.5 Install and smoke test on the connected Android device if available.
