## Why

TrailMate now reports PMTiles label asset readiness in route setup, but copied physical-device diagnostics still lack that evidence. When a real device report is shared, it should explain whether offline labels are disabled because local glyph/sprite assets are missing or ready to load.

## What Changes

- Add optional PMTiles style asset readiness fields to the copied device diagnostics report.
- Include safe local glyph/sprite asset URLs only when they are ready.
- Pass the runtime PMTiles style asset readiness from the route screen into diagnostics formatting.

## Impact

- Android-only diagnostics text formatting and route screen wiring.
- No binary style assets.
- No change to map rendering, GPS, or offline basemap import behavior.

