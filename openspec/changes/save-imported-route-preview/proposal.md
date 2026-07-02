## Why

The previous route import slice can parse a selected GPX/KML file, but the user cannot turn that parsed result into a route asset. A production-grade hiking app needs a clear save step so imported tracks move from temporary preview into the route asset center without pretending they are verified platform routes.

## What Changes

- Add an explicit in-memory save action for a parsed import preview.
- Convert a parsed GPX/KML result into a `TrackOnly`, `Private`, unverified imported route asset.
- Update the route tab preview copy after saving so users can distinguish saved assets from temporary previews.
- Keep failed imports from exposing save/start-navigation actions.
- Keep this PR scoped away from Room/DataStore persistence, route detail navigation, MapLibre, GPS, and actual navigation launch.

## Capabilities

### New Capabilities

- `route-import-save`: Saving a successfully parsed GPX/KML import preview into the route asset list as a private track-only imported route.

### Modified Capabilities

- None.

## Impact

- Affected Android state/UI files under `app/src/main/java/com/trailmate/app/feature/routes/` and `app/src/main/java/com/trailmate/app/ui/`.
- New focused route import save helper/use-case may be added near route feature state.
- Unit tests will cover privacy defaults, duplicate-save behavior, saved copy, and old-surface absence.
- No new Android permissions, storage dependencies, database schema, network calls, or server changes.
