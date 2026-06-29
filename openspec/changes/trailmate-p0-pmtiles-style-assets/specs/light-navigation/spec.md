## ADDED Requirements

### Requirement: PMTiles labeled offline style assets shall be gated

TrailMate SHALL only enable labeled MapLibre PMTiles offline styles when the required offline glyph and sprite assets are bundled and readable.

#### Scenario: Style assets are missing

- **GIVEN** a local PMTiles offline basemap pack is ready
- **AND** Protomaps glyph or sprite assets are missing
- **WHEN** TrailMate builds the MapLibre PMTiles style
- **THEN** the style MUST remain geometry-only
- **AND** the style MUST NOT include text or icon layers
- **AND** the style MUST NOT include `glyphs` or `sprite` references

#### Scenario: Style assets are complete

- **GIVEN** a local PMTiles offline basemap pack is ready
- **AND** Protomaps glyph, sprite JSON, and sprite image assets are bundled
- **WHEN** TrailMate builds the MapLibre PMTiles style
- **THEN** the style MAY include text and icon layers
- **AND** the style MUST reference local offline glyph and sprite asset URLs
- **AND** TrailMate MUST NOT rely on network glyph or sprite URLs for field offline use

#### Scenario: Style asset readiness is disclosed

- **GIVEN** TrailMate evaluates MapLibre PMTiles offline style assets
- **WHEN** any required asset is missing
- **THEN** the readiness policy MUST report that labeled offline maps are not ready
- **AND** the user-facing copy MUST explain that route and geometry context remain available without offline labels

#### Scenario: Network style assets are rejected

- **GIVEN** TrailMate evaluates MapLibre PMTiles offline style assets
- **AND** any glyph or sprite asset URL uses `http://` or `https://`
- **WHEN** TrailMate resolves style asset readiness
- **THEN** the readiness policy MUST report that labeled offline maps are not ready
- **AND** the generated MapLibre style MUST remain geometry-only
- **AND** TrailMate MUST NOT emit network glyph or sprite URLs in the offline style JSON
