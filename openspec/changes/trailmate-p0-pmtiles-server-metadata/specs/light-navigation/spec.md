# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Server PMTiles catalogs shall publish local file integrity metadata

TrailMate SHALL include actual file size and SHA-256 metadata for locally hosted PMTiles catalog items when the configured PMTiles file exists on the server.

#### Scenario: Local PMTiles catalog item has a matching server file

- GIVEN the PMTiles catalog contains a route-covering item with download URL `/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles`
- AND the configured PMTiles directory contains `hangzhou-westlake.pmtiles`
- WHEN the server lists route-covering PMTiles catalog items
- THEN the returned catalog item includes `sizeBytes` equal to the actual file size
- AND includes `sha256` equal to the SHA-256 digest of the local file bytes

#### Scenario: Local PMTiles catalog item file is missing

- GIVEN the PMTiles catalog contains a route-covering item with download URL `/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles`
- BUT the configured PMTiles directory does not contain `hangzhou-westlake.pmtiles`
- WHEN the server lists route-covering PMTiles catalog items
- THEN the returned catalog item keeps its configured `sizeBytes`
- AND keeps its configured `sha256`

#### Scenario: External PMTiles catalog item

- GIVEN the PMTiles catalog contains a route-covering item with an external download URL
- WHEN the server lists route-covering PMTiles catalog items
- THEN the returned catalog item keeps its configured `sizeBytes`
- AND keeps its configured `sha256`
