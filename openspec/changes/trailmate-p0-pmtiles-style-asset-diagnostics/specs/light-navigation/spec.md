## ADDED Requirements

### Requirement: PMTiles Style Asset Setup Diagnostics

The route map setup diagnostics SHALL report PMTiles label asset readiness when the MapLibre PMTiles basemap is selected.

#### Scenario: PMTiles basemap is ready but style assets are missing

- **WHEN** the PMTiles basemap is ready
- **AND** glyph or sprite style assets are unavailable
- **THEN** route setup diagnostics include a `地图标注` step with `待补齐`
- **AND** the PMTiles basemap remains available for geometry-only offline navigation

#### Scenario: PMTiles basemap and style assets are ready

- **WHEN** the PMTiles basemap is ready
- **AND** glyph and sprite style assets are complete
- **THEN** route setup diagnostics include a `地图标注` step with `已就绪`

