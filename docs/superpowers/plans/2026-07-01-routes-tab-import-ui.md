# Routes Tab Import UI Plan

## Assumptions

- `AGENTS.md` is the product source of truth.
- Lovable's `trailguide-pro` route page is a visual reference, not production code.
- This slice is UI and state only; real file picking, route persistence, route detail routing, and live navigation start are later slices.
- The route tab must remain a route asset center and must not reintroduce planning, equipment, community, or marketplace surfaces.

## Success Criteria

1. The `路线` tab renders a focused route asset center instead of placeholder copy.
2. Route tab state is covered by JVM tests before production code is added.
3. Import preview metrics are derived from `RouteImportParser`.
4. The UI shows the required route-only/import-bottom-copy.
5. OpenSpec validates, unit tests pass, debug APK builds, and a device smoke test is attempted when a connected device is available.

## Steps

1. [x] Write route tab contract tests and verify they fail.
2. [x] Implement pure Kotlin route tab state and deterministic sample state.
3. [x] Build the Compose `RoutesScreen` from state.
4. [x] Wire the shell's `Routes` tab to the feature screen.
5. [ ] Run verification, request review, then commit and push a stacked PR.

## TDD Evidence

- Red: `.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest" --console=plain` failed because `RoutesTabSampleState` did not exist.
- Green: the same targeted test command passed after adding route tab state and parser-backed sample import preview.

## Out of Scope

- Android file picker.
- Room/DataStore.
- Backend sync.
- MapLibre.
- GPS and foreground tracking service.
- Actual navigation sessions.
- Route planner, equipment, community, or commerce features.
