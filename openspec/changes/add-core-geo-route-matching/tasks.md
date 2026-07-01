## 1. OpenSpec and plan

- [x] Create proposal, design, requirements, and task checklist for `core.geo`.
- [x] Define this slice as pure Kotlin algorithm work with no UI, live GPS, MapLibre, or reroute generation.

## 2. Route projection

- [x] Add failing JVM tests for nearest route segment projection and overlapping route progress stabilization.
- [x] Implement local WGS84 distance, bearing, and route projection helpers.
- [x] Verify targeted route projection tests pass.

## 3. Progress metrics

- [x] Add failing JVM tests for completed distance, remaining distance, remaining elevation, next waypoint, nearest exit, and reverse direction.
- [x] Implement progress calculation from `RouteGeometry` and `RouteProjection`.
- [x] Verify targeted progress tests pass.

## 4. Off-route evidence

- [x] Add failing JVM tests for poor GPS accuracy, suspected off-route, and confirmed off-route.
- [x] Implement configurable thresholds and evidence reducer.
- [x] Verify targeted off-route tests pass.

## 5. Documentation and verification

- [x] Add `docs/core-geo-route-matching.md`.
- [x] Address spec-review findings for nearest exit semantics and bearing-to-route output.
- [x] Address code-review findings for distance-first projection hysteresis, explicit bearing-to-route construction, and time-gated off-route confirmation.
- [x] Run `openspec validate add-core-geo-route-matching`.
- [x] Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.
- [x] Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [x] Run whitespace/status checks before commit.
