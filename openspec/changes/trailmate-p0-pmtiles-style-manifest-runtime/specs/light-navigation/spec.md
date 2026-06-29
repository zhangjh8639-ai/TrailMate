## ADDED Requirements

### Requirement: Bundled PMTiles Style Asset Manifest Runtime

The route detail runtime SHALL resolve bundled PMTiles glyph and sprite assets before passing a style asset manifest to the MapLibre PMTiles route map.

#### Scenario: Bundled style assets are complete

- **WHEN** the runtime can find the glyph probe, sprite JSON, and sprite image assets inside the APK assets
- **THEN** it passes a complete local `asset://` style manifest to the route map
- **AND** the labeled style uses the same font stack as the glyph probe

#### Scenario: Bundled style assets are incomplete

- **WHEN** any bundled style asset probe is missing
- **THEN** the runtime passes an unavailable manifest
- **AND** the PMTiles route map remains geometry-only through the existing readiness gate

