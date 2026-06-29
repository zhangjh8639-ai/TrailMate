## Why

TrailMate can now resolve bundled PMTiles style assets, but the route setup state still reports only the PMTiles basemap package. A field build should make it explicit when the offline basemap geometry is available but labels/icons are intentionally disabled because glyph or sprite assets are missing.

## What Changes

- Add PMTiles style asset readiness to route map setup diagnostics.
- Show a dedicated map-label setup step only when the MapLibre PMTiles basemap is ready.
- Keep geometry-only PMTiles maps usable when label assets are missing.

## Impact

- Android-only readiness presentation and route detail wiring.
- No new binary assets.
- No change to PMTiles basemap import or GPS behavior.

