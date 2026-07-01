## Why

The routes tab currently shows a deterministic import preview, but a hiker cannot yet select a real GPX/KML file from Android and see that file parsed in the app. This change turns the route asset center into a usable import entry point while keeping the scope focused on route-track assets, not map basemaps or route planning.

## What Changes

- Add an Android system document picker entry from the `路线` tab for GPX/KML-like files.
- Read the selected document through a small importer boundary instead of letting Compose UI parse streams directly.
- Parse the selected file with the existing route import core and update the route tab import preview with real file metadata and route metrics.
- Show cancel, parsing, success, and failure states with concise Chinese copy.
- Preserve the route-only explanation that imported files contain route geometry and waypoints, not commercial map basemaps.
- Keep "保存到路线", "查看详情", and "开始轨迹导航" as non-persistent next-step affordances unless a later PR wires those flows.

## Capabilities

### New Capabilities
- `route-import-file-picker`: Android route tab can launch a system file picker, read a selected GPX/KML document, parse it, and show the import result preview.

### Modified Capabilities
None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/feature/routes`, route import UI state/tests, and app shell wiring for the file picker.
- Affected tests: route tab state tests, import action tests, and full app unit test/build commands.
- New dependencies: none expected; use AndroidX Activity Result APIs already available through Compose activity integration if present.
- Out of scope: Room persistence, route detail navigation, actual navigation launch, MapLibre rendering, GPS, background tracking, offline route packages, equipment, planning, social, and marketplace features.
