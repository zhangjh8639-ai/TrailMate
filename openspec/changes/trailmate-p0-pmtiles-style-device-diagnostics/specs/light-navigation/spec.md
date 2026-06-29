## ADDED Requirements

### Requirement: PMTiles Style Asset Device Diagnostics

Copied physical-device diagnostics SHALL include PMTiles offline label asset readiness when route runtime style asset readiness is available.

#### Scenario: PMTiles style assets are missing

- **WHEN** copied diagnostics are formatted with unavailable PMTiles style assets
- **THEN** the report includes the PMTiles style asset status
- **AND** it states labels are not ready
- **AND** it does not include glyph or sprite URLs

#### Scenario: PMTiles style assets are ready

- **WHEN** copied diagnostics are formatted with ready local PMTiles style assets
- **THEN** the report states labels are ready
- **AND** it includes local glyph and sprite asset URLs
- **AND** it does not include HTTP or HTTPS asset URLs

