## Why

The PMTiles style asset readiness gate exists, but the real route map runtime still builds style JSON through the old geometry-only factory path. TrailMate should make the runtime path explicit so future bundled glyph and sprite assets can safely enable offline labels without accidentally accepting network style dependencies.

## What Changes

- Add a route style policy that resolves PMTiles style asset readiness before building MapLibre style JSON.
- Keep the route map default geometry-only when no local glyph/sprite manifest is provided.
- Allow labeled offline style only when the runtime receives a complete local asset manifest.

## Impact

- Android-only runtime style construction.
- No new binary map assets.
- No network behavior changes.

