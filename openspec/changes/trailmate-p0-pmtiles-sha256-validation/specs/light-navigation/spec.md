# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Remote PMTiles imports shall verify catalog checksums when provided

TrailMate SHALL verify downloaded remote PMTiles files against the catalog SHA-256 checksum whenever the selected catalog item provides one.

#### Scenario: Downloaded PMTiles checksum matches catalog metadata

- GIVEN the server PMTiles catalog selects a route-covering item with a non-blank `sha256`
- AND Android downloads the PMTiles file
- WHEN the computed SHA-256 matches the catalog checksum
- THEN Android continues local PMTiles archive validation
- AND may import the file only after the archive validation also passes

#### Scenario: Downloaded PMTiles checksum does not match catalog metadata

- GIVEN the server PMTiles catalog selects a route-covering item with a non-blank `sha256`
- AND Android downloads the PMTiles file
- WHEN the computed SHA-256 does not match the catalog checksum
- THEN Android deletes the temporary downloaded file
- AND opens the local `.pmtiles` file picker fallback
- AND does not mark the PMTiles basemap ready

#### Scenario: Catalog checksum is absent

- GIVEN the server PMTiles catalog selects a route-covering item with no `sha256`
- AND Android downloads the PMTiles file
- WHEN the user imports the offline basemap from the route screen
- THEN Android keeps the existing preview-compatible behavior
- AND still requires local PMTiles archive validation before readiness changes
