# TrailMate PR Slices

This file defines the initial review-sized PR plan. Each PR must be implemented in its own worktree branch and reviewed for spec compliance and code quality before merge.

## Design And Product Sources

- Product rules: `AGENTS.md`
- Product spec: `TRAILMATE_CODEX_SPEC.md`
- OpenSpec changes: `openspec/changes/`
- Lovable reference prototype: `D:\workSpace\trailguide-pro`
- Android verification: connected device via `adb devices`

If the Lovable prototype conflicts with `AGENTS.md`, follow `AGENTS.md`.

## Initial PR Sequence

### PR 1: Android Shell And Quality Gate

- Branch: `codex/android-shell-foundation`
- OpenSpec: `bootstrap-android-trailmate-shell`
- Scope: Gradle wrapper, Android app module, Compose Material 3 theme, fixed 5 tabs, placeholder Chinese screens, docs, basic tests.
- Out of scope: GPX/KML parser, GPS, MapLibre, Foreground Service, offline packages, backend.
- Required verification: `:app:assembleDebug`, `:app:testDebugUnitTest`, optional real-device smoke launch.

### PR 2: Core Domain And Navigation Model

- Branch: `codex/core-navigation-model`
- Scope: route, waypoint, exit point, track point, navigation state, privacy defaults, reducer-level tests.
- Out of scope: Android location APIs and map rendering.
- Required verification: JVM unit tests for state transitions and privacy defaults.

### PR 3: Core Geo Algorithms

- Branch: `codex/core-geo-navigation`
- Scope: distance, cumulative route table, nearest segment projection, progress calculation, off-route candidate/confirmed state, reverse route support.
- Out of scope: UI and live GPS.
- Required verification: unit tests for normal route, GPS drift, confirmed wrong turn, loop progress anti-jump, reverse walking, and poor accuracy.

### PR 4: Route Assets And GPX Import

- Branch: `codex/routes-gpx-import`
- Scope: route tab assets, GPX parsing baseline, import result panel, save-to-routes flow.
- Out of scope: KML if GPX parsing is not stable yet; live navigation.
- Required verification: parser tests with fixture files and Compose tests for import result copy.

### PR 5: Navigation UI With Simulated Engine

- Branch: `codex/navigation-ui-simulated-engine`
- Scope: navigation tab idle and active states, normal/suspected/confirmed off-route UI, emergency-card entry, original-return UI using simulated data.
- Out of scope: Foreground Service and real location provider.
- Required verification: Compose tests for all navigation states and no unsafe copy.

### PR 6: Tracking Service Skeleton

- Branch: `codex/tracking-service-skeleton`
- Scope: Foreground Service skeleton, notification actions, session persistence interface, permission flow boundaries.
- Out of scope: production-grade sampling strategy and sync.
- Required verification: unit tests for service state reducer and real-device install/smoke check.

### PR 7: Offline Route Data Baseline

- Branch: `codex/offline-route-data-baseline`
- Scope: route manifest model, checksum status, offline route availability state, fallback display when base map is unavailable.
- Out of scope: full PMTiles/MBTiles rendering and download manager.
- Required verification: manifest/checksum unit tests and UI tests for offline availability state.

### PR 8: Records And Structured Feedback

- Branch: `codex/records-feedback-baseline`
- Scope: records tab summary, track recap model, structured feedback entry points.
- Out of scope: backend submission and public community.
- Required verification: UI tests for feedback categories and absence of social features.

## Review Checklist For Every PR

- Product: no `规划`, equipment, community, marketplace, or complex pre-trip-check scope.
- Safety: no copy that promises rescue, safe crossing, or automatic safe reroute.
- Privacy: imported routes, tracks, favorites, and navigation sessions default private.
- Architecture: UI does not directly access location, network, database, or file-system details.
- Tests: related unit/UI tests run and results are recorded.
- Device: navigation, location, background, or offline changes include a real-device manual verification note.
