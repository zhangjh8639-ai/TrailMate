## Why

Imported GPX/KML routes can currently be saved only into in-memory route tab state. That makes the route asset center unreliable because a process restart loses the imported route, which is unacceptable for a production-grade hiking navigation app.

## What Changes

- Add local SQLite persistence for imported route assets created from successfully parsed GPX/KML files.
- Persist route metadata, privacy/trust boundaries, source format, compact import metrics, and route geometry points needed by later navigation work.
- Load persisted imported route assets when the Android app starts and merge them into the route asset list before bundled/sample routes.
- Upsert a repeated save of the same import identity instead of creating duplicate route cards.
- Keep failed imports unsaveable and keep imported routes private, track-only, and unverified by default.
- Do not add route detail, active navigation, MapLibre rendering, GPS recording, server sync, or commercial basemap download behavior in this change.

## Capabilities

### New Capabilities

- `imported-route-persistence`: Local persistence and startup restoration for imported GPX/KML route assets.

### Modified Capabilities

- None.

## Impact

- Android app route tab state, import save flow, and startup wiring.
- New lightweight SQLite store and mappers for imported route records and route geometry.
- Unit tests for persistence mapping, duplicate handling, privacy/trust defaults, and startup merge behavior.
- Documentation update for the route import file picker and route asset persistence boundary.
- No new Android runtime permissions, network calls, server dependency, or map tile source.
