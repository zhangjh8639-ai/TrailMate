## ADDED Requirements

### Requirement: Runtime PMTiles Style Asset Gate

The MapLibre PMTiles route map runtime SHALL construct its style JSON through a route style policy that resolves style asset readiness before enabling any offline label or icon layers.

#### Scenario: Runtime has no bundled style assets

- **WHEN** the route map builds style JSON without a glyph and sprite asset manifest
- **THEN** the style remains geometry-only
- **AND** it does not include glyphs, sprite, text-field, or icon-image entries

#### Scenario: Runtime has complete local style assets

- **WHEN** the route map builds style JSON with local asset glyphs, sprite JSON, and sprite image URLs
- **THEN** the style includes glyphs and sprite entries
- **AND** it includes text and icon symbol layers
- **AND** it does not include HTTP or HTTPS asset URLs

#### Scenario: Runtime receives network style assets

- **WHEN** the route map builds style JSON with any network-backed style asset URL
- **THEN** the style remains geometry-only
- **AND** it does not include network asset URLs

