# Change: TrailMate MapLibre PMTiles Offline Provider

## Why

TrailMate should not depend on AMap offline-map authorization as the core offline map strategy. The app needs a local-first offline basemap path that can pair imported GPX routes, checkpoints, safety guidance, and recorded tracks with a self-managed offline map package.

## What Changes

- Make MapLibre Native + Protomaps/PMTiles + OSM-derived data the primary offline basemap provider direction.
- Keep local GPX Canvas as the safe fallback when MapLibre runtime or a local PMTiles route pack is unavailable.
- Reposition AMap as an optional online/domestic provider instead of the primary offline-map path.
- Surface readiness copy that distinguishes route-only preview, PMTiles offline basemap readiness, and optional AMap availability.

## Impact

- Adds provider/readiness policy before introducing MapLibre rendering code.
- Does not commit OSM/PMTiles data files or map provider secrets.
- Does not claim production mainland China map compliance until data licensing, attribution, coordinate, and regulatory requirements are explicitly handled.
