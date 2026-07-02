## 1. Tests First

- [x] 1.1 Add failing unit test for tracking service start/stop intent actions.
- [x] 1.2 Add failing unit test for safe active notification content.
- [x] 1.3 Add failing manifest test for foreground service permissions and service registration.
- [x] 1.4 Add failing manifest test that background location remains absent.
- [x] 1.5 Add lifecycle controller tests for start, stop, null action, unknown action, and missing location permission.

## 2. Service Boundary

- [x] 2.1 Add tracking service contract/constants and start/stop intent helpers.
- [x] 2.2 Add active tracking notification metadata helper.
- [x] 2.3 Add Android `TrackingForegroundService` shell with start/stop handling.
- [x] 2.4 Keep service shell free of fake GPS points, uploads, persistence, and route simulation.
- [x] 2.5 Avoid sticky restart while session recovery is not implemented.
- [x] 2.6 Guard location foreground startup behind foreground location permission.

## 3. Manifest / Permissions

- [x] 3.1 Declare foreground service permissions and notification permission.
- [x] 3.2 Register tracking service with `foregroundServiceType="location"`.
- [x] 3.3 Do not declare background location in this slice.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run focused tracking service tests.
- [x] 4.3 Run full unit tests and debug build.
- [x] 4.4 Request code review before PR.
- [x] 4.5 Run install/launch smoke on a connected Android device if available.
