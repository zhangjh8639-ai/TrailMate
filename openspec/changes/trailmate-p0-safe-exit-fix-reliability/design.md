# Design: Safe-exit fix reliability guard

## Product Rule

Safe-exit choices should only become `READY` when TrailMate has a reliable current fix. Otherwise the app should ask the hiker to stabilize GPS and use recorded track/map evidence only as secondary context.

## Technical Approach

Add a `nowEpochMillis` parameter to `RouteExitGuidanceEngine.present`, defaulting to `System.currentTimeMillis()` for compatibility. Before calculating progress, reject fixes with non-finite/negative distance or error fields, negative/non-finite accuracy, non-positive timestamps, future timestamps, stale timestamps, or accuracy worse than 50 m.

Use the existing low-confidence presentation for rejected fixes so UI behavior remains simple and localized.

## Verification

- Add failing tests for stale, future, and malformed on-route fixes.
- Run targeted route-exit tests, OpenSpec validation, diff check, full Gradle tests, and read-only code review.
