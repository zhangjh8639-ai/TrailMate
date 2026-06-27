# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Light navigation shall prefer a local PMTiles offline basemap when available

TrailMate SHALL use MapLibre Native with a local Protomaps/PMTiles basemap package as the primary offline basemap direction.

#### Scenario: PMTiles offline basemap is ready

- GIVEN the imported route contains drawable geometry
- AND the MapLibre runtime is available in the Android build
- AND a local PMTiles basemap pack covers the target route region
- WHEN the route map readiness is resolved
- THEN TrailMate selects the MapLibre PMTiles provider
- AND the route screen presents this state as an offline basemap, not merely a saved GPX route
- AND the map readiness layers include GPX route, checkpoints, and PMTiles offline basemap context

#### Scenario: PMTiles offline basemap is missing

- GIVEN the imported route contains drawable geometry
- AND the MapLibre runtime is available in the Android build
- BUT no local PMTiles basemap pack covers the target route region
- WHEN the route map readiness is resolved
- THEN TrailMate uses the local GPX preview provider
- AND the user-facing action is `导入离线地图包`
- AND the route screen explains that the offline basemap package still needs to be imported or downloaded
- AND the route, checkpoints, GPS status, and track recording remain available

#### Scenario: Readiness steps use explicit route and basemap language

- GIVEN TrailMate resolves route map readiness for a drawable GPX route
- WHEN the route, GPS, and PMTiles preparation states are presented
- THEN the setup steps use `路线`, `离线路线`, `GPS`, and `离线地图包`
- AND the PMTiles preparation state is not labeled only as `底图`
- AND the GPX route-saving state is not labeled only as `离线`

#### Scenario: PMTiles local pack file exists

- GIVEN a local PMTiles file exists for the imported route pack key
- AND the file uses the `.pmtiles` extension
- AND the file has a valid PMTiles v3 header
- AND the file contains MapLibre-compatible vector tile data
- AND the PMTiles header bounds fully contain the imported GPX route bounds
- WHEN TrailMate evaluates offline basemap readiness
- THEN the PMTiles basemap pack is considered ready

#### Scenario: PMTiles route map surface is selected

- GIVEN the route map readiness selects the MapLibre PMTiles provider
- AND the local PMTiles file exists for the imported route pack key
- WHEN the route map is rendered
- THEN TrailMate loads the local PMTiles style through MapLibre Native
- AND overlays the imported GPX route, track recording, and checkpoints on the map surface

#### Scenario: User imports a local PMTiles file

- GIVEN the route map readiness says the PMTiles basemap is missing
- WHEN the user selects a valid `.pmtiles` file from Android document storage
- AND the PMTiles header bounds fully contain the imported GPX route bounds
- THEN TrailMate copies the selected file into the app PMTiles basemap directory using the imported route pack key
- AND refreshes PMTiles readiness for the current route

#### Scenario: Server PMTiles catalog offers a route-covering pack

- GIVEN Android knows the target route geographic bounds
- WHEN Android requests `/api/v1/offline-basemaps/pmtiles/catalog` with those bounds
- THEN the server returns PMTiles pack metadata whose bounds fully contain the route bounds
- AND Android selects a non-empty `downloadUrl` item with `tileType=MVT`
- AND Android does not mark the PMTiles basemap ready until the file is downloaded/imported and passes local PMTiles header validation

#### Scenario: Android downloads a valid catalog PMTiles pack

- GIVEN the server catalog returns a PMTiles pack whose metadata fully contains the route bounds
- AND the pack `downloadUrl` returns a non-empty PMTiles v3 vector archive
- AND the archive header bounds fully contain the route bounds
- WHEN the user chooses to import the offline basemap from the route screen
- THEN Android downloads the pack into app storage using the route pack key
- AND refreshes PMTiles readiness for the current route

#### Scenario: Remote PMTiles download cannot be used

- GIVEN the server catalog is unavailable, has no matching pack, the download fails, or the downloaded archive fails PMTiles validation
- WHEN the user chooses to import the offline basemap from the route screen
- THEN Android keeps the existing local GPX route preview available
- AND opens the local `.pmtiles` file picker fallback
- AND does not mark the PMTiles basemap ready

#### Scenario: User selects an invalid PMTiles import file

- GIVEN the route map readiness says the PMTiles basemap is missing
- WHEN the user selects a file that is not `.pmtiles`, is empty, lacks a valid PMTiles v3 header, uses an unsupported tile type, or does not cover the imported route bounds
- THEN TrailMate rejects the file
- AND keeps the local GPX preview available

#### Scenario: PMTiles local pack file is invalid

- GIVEN a local PMTiles route-pack entry exists
- BUT the file is missing, empty, not a `.pmtiles` file, lacks a valid PMTiles v3 header, uses an unsupported tile type, or does not cover the imported route bounds
- WHEN TrailMate evaluates offline basemap readiness
- THEN the PMTiles basemap pack is not considered ready
- AND TrailMate keeps the local GPX preview available

#### Scenario: PMTiles style assets are not bundled

- GIVEN TrailMate has not bundled Protomaps glyph and sprite assets
- WHEN TrailMate builds the default offline MapLibre style
- THEN the style uses geometry-only OSM/Protomaps layers
- AND the style does not include text or icon layers that require missing glyph or sprite assets

#### Scenario: MapLibre runtime is not available

- GIVEN the imported route contains drawable geometry
- AND a PMTiles basemap pack exists
- BUT the MapLibre runtime is not available in the Android build
- WHEN the route map readiness is resolved
- THEN TrailMate uses the local GPX preview provider
- AND the route screen does not claim PMTiles offline basemap readiness

### Requirement: AMap shall be optional after the PMTiles offline direction is introduced

TrailMate SHALL treat AMap as an optional provider after the PMTiles offline direction is configured.

#### Scenario: Both PMTiles and AMap gates are ready

- GIVEN the imported route contains drawable geometry
- AND the PMTiles offline basemap gates are ready
- AND AMap gates are also ready
- WHEN the route map readiness is resolved
- THEN TrailMate prefers the MapLibre PMTiles provider for offline route use
- AND AMap remains available as an optional provider path rather than replacing the local offline basemap direction
