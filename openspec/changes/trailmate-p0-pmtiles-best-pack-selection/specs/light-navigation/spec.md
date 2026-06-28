# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Android remote PMTiles selection shall prefer the smallest full-covering pack

TrailMate SHALL minimize offline basemap download size when multiple remote PMTiles catalog items are valid for the same route.

#### Scenario: Android selects the smallest full-covering PMTiles pack

- GIVEN Android receives multiple PMTiles catalog items whose bounds fully contain the target route bounds
- AND each item has `tileType=MVT` and a non-empty `downloadUrl`
- AND more than one item has a known positive `sizeBytes`
- WHEN Android selects a remote PMTiles pack for the route
- THEN Android selects the item with the smallest known positive `sizeBytes`

#### Scenario: Android keeps unknown-size PMTiles packs usable

- GIVEN Android receives a PMTiles catalog item whose bounds fully contain the target route bounds
- AND the item has `tileType=MVT` and a non-empty `downloadUrl`
- BUT the item has missing or non-positive `sizeBytes`
- WHEN Android has no smaller known-size eligible item
- THEN Android can still select the unknown-size item
