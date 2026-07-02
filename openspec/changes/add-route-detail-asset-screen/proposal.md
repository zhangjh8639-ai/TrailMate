## Why

Imported GPX/KML routes can now be saved and restored as route assets, but users still cannot inspect an asset's trust level, source, offline boundary, and safety-relevant route facts before deciding what to do next. A production hiking navigation app needs this detail layer so route assets are understandable before active navigation work is added.

## What Changes

- Add a route detail screen/state for route asset cards in the Routes tab.
- Let saved/restored imported assets and bundled/platform assets expose a detail action.
- Show route identity, source, region, offline/track status, distance, elevation gain, estimated duration, difficulty, confidence, risk tags, recent usage/update context, and route boundary notes.
- Keep imported routes clearly labeled as private/local, track-only, unverified, and not containing commercial or full offline basemap data.
- Provide a back path from route detail to the route asset center without losing the active import preview.
- Do not add active navigation, MapLibre rendering, GPS tracking, route editing, pretrip checks, equipment, community, server sync, or commercial basemap download behavior in this change.

## Capabilities

### New Capabilities

- `route-detail-asset-screen`: Route asset detail state and UI for inspecting saved/imported/platform route boundaries before later navigation work.

### Modified Capabilities

- None.

## Impact

- Android app route tab state, asset card actions, and Compose UI.
- App shell state for entering/leaving route detail from the Routes tab.
- Unit tests covering detail state generation, imported-route boundaries, and deprecated-surface exclusions.
- No new Android runtime permissions, network calls, map tile source, database schema, or server dependency.
