## 1. Tests First

- [x] 1.1 Add failing unit test that a valid system location reading maps to `LocationSample`.
- [x] 1.2 Add failing unit test that invalid coordinates or negative accuracy are rejected.
- [x] 1.3 Add failing unit test for request validation and stopped subscription behavior.
- [x] 1.4 Add manifest-focused check for foreground-only location permissions.
- [x] 1.5 Add lifecycle tests for stop suppression and runtime provider disable/enable status.
- [x] 1.6 Add mapper coverage for NaN, infinity, out-of-range coordinates, and negative speed.

## 2. Core Location Boundary

- [x] 2.1 Add location provider request, status, observer, subscription, and provider contract.
- [x] 2.2 Add system location reading model and mapper to `LocationSample`.
- [x] 2.3 Keep the core location package independent from Android framework APIs.

## 3. Android Provider

- [x] 3.1 Add Android `LocationManager` provider implementation.
- [x] 3.2 Handle missing permission through typed status instead of crashing the navigation core.
- [x] 3.3 Declare foreground fine/coarse location permissions only.
- [x] 3.4 Suppress queued location callbacks after subscription stop.
- [x] 3.5 Report disabled status when all subscribed Android providers become unavailable.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run focused location provider tests.
- [x] 4.3 Run full unit tests and debug build.
- [x] 4.4 Request code review before PR.
