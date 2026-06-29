## Why

TrailMate can now gate labeled PMTiles styles, but the route detail runtime still has no way to discover bundled glyph and sprite assets. Without a runtime resolver, labeled offline maps remain dormant even when the APK later includes the required local style resources.

## What Changes

- Add a bundled PMTiles style asset manifest resolver with explicit local asset probes.
- Pass the resolved manifest from the route detail runtime into the MapLibre PMTiles route map.
- Make label layers use the same font stack that the bundled glyph probe verifies.

## Impact

- Android-only runtime style manifest wiring.
- No binary map/font/sprite assets are added.
- Missing bundled assets continue to degrade to geometry-only offline maps.

