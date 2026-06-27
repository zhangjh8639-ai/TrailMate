# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Remote PMTiles imports shall verify catalog size metadata when provided

TrailMate SHALL verify downloaded remote PMTiles file length against catalog `sizeBytes` whenever the selected catalog item provides a positive value.

#### Scenario: Downloaded PMTiles size matches catalog metadata

- GIVEN the server PMTiles catalog selects a route-covering item with positive `sizeBytes`
- AND Android downloads the PMTiles file
- WHEN the downloaded file length equals catalog `sizeBytes`
- THEN Android continues SHA-256 validation when provided
- AND may import the file only after PMTiles archive validation also passes

#### Scenario: Downloaded PMTiles size does not match catalog metadata

- GIVEN the server PMTiles catalog selects a route-covering item with positive `sizeBytes`
- AND Android downloads the PMTiles file
- WHEN the downloaded file length does not equal catalog `sizeBytes`
- THEN Android deletes the temporary downloaded file
- AND opens the local `.pmtiles` file picker fallback
- AND does not mark the PMTiles basemap ready

#### Scenario: Catalog size is absent

- GIVEN the server PMTiles catalog selects a route-covering item with no `sizeBytes`
- AND Android downloads the PMTiles file
- WHEN the user imports the offline basemap from the route screen
- THEN Android keeps the existing preview-compatible behavior
- AND still requires SHA-256 validation when provided
- AND still requires local PMTiles archive validation before readiness changes
