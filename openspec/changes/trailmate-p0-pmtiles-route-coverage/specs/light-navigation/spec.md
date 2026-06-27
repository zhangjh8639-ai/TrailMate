# light-navigation Specification Delta

## ADDED Requirements

### Requirement: PMTiles route packs shall fully cover route bounds before readiness

TrailMate SHALL treat a PMTiles pack as suitable for a route only when the pack bounds fully contain the route bounds.

#### Scenario: Server catalog excludes a partial-overlap PMTiles pack

- GIVEN a PMTiles catalog item intersects the requested route bounds
- BUT the catalog item does not fully contain the requested route bounds
- WHEN the server lists route-covering PMTiles catalog items
- THEN the partial-overlap item is not returned

#### Scenario: Android remote catalog selection excludes a partial-overlap PMTiles pack

- GIVEN Android receives a PMTiles catalog item whose bounds intersect the target route bounds
- BUT the catalog item does not fully contain the target route bounds
- WHEN Android selects a remote PMTiles pack for the route
- THEN Android does not select the partial-overlap item

#### Scenario: Android local PMTiles import rejects a partial-overlap archive

- GIVEN the user imports a PMTiles file with valid archive metadata
- BUT the archive bounds only partially overlap the target route bounds
- WHEN Android evaluates the file for local PMTiles import
- THEN Android rejects the file as not covering the current route
- AND does not mark the PMTiles basemap ready

#### Scenario: Android readiness rejects a partial-overlap installed archive

- GIVEN a PMTiles route-pack file exists locally with valid archive metadata
- BUT the archive bounds only partially overlap the target route bounds
- WHEN Android evaluates offline basemap readiness
- THEN Android reports the PMTiles basemap as not covering the target route
- AND does not mark the PMTiles basemap ready
