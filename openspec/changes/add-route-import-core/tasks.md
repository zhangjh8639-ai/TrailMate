## 1. OpenSpec and plan

- [x] Create proposal, design, requirements, and task checklist for route import core.
- [x] Define this slice as parser/domain work with no UI, file picker, persistence, map rendering, or live GPS.

## 2. GPX parsing

- [x] Add failing JVM tests for GPX track parsing and GPX route-point fallback.
- [x] Implement route import result models and GPX parsing.
- [x] Verify targeted GPX tests pass.

## 3. KML parsing

- [x] Add failing JVM tests for KML LineString parsing and KML Point waypoint projection.
- [x] Implement KML parsing.
- [x] Verify targeted KML tests pass.

## 4. Quality and validation

- [x] Add failing JVM tests for missing elevation, sparse track, large point gap, unsupported extension, missing route geometry, and private imported route conversion.
- [x] Implement quality warning and validation behavior.
- [x] Verify targeted validation tests pass.

## 5. Documentation and verification

- [x] Add `docs/route-import-core.md`.
- [x] Address review findings for XML fail-closed hardening and insufficient GPX track fallback.
- [x] Filter non-finite imported elevation values.
- [x] Run `openspec validate add-route-import-core`.
- [x] Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.
- [x] Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [x] Run whitespace/status/scope checks before commit.
