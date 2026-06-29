## Why

TrailMate already has a MapLibre + PMTiles offline basemap path, but the default style intentionally avoids labels because Protomaps glyph and sprite assets are not yet modeled as a readiness gate. A field user should not see TrailMate claim a labeled offline map unless the app can prove the required offline style assets are bundled.

## What Changes

- Add an explicit MapLibre PMTiles style asset manifest/readiness policy.
- Keep the default offline style geometry-only unless glyph and sprite assets are complete.
- Allow labeled offline style JSON only when glyph, sprite JSON, and sprite image assets are all available.

## Impact

- Android-only pure policy and style JSON changes.
- No new network or storage behavior.
- No large binary asset files are introduced in this step.
