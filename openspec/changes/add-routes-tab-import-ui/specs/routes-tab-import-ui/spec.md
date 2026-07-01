## ADDED Requirements

### Requirement: Route tab asset center
The Android route tab SHALL present a focused route asset center for saved, offline, imported, favorite, and recently navigated hiking routes.

#### Scenario: Route tab shows asset center controls
- **WHEN** the user opens the `路线` tab
- **THEN** the screen shows the title `路线`, an import affordance for `GPX / KML`, route asset filters, and route cards with `开始导航` and `查看详情` affordances

#### Scenario: Route tab excludes deprecated surfaces
- **WHEN** the route tab screen text contract is inspected
- **THEN** it does not include primary surfaces for `规划`, `装备`, `社区`, or `商城`

### Requirement: Import result preview
The route tab SHALL show a compact GPX/KML import result preview based on parser output before persistence or navigation side effects are implemented.

#### Scenario: Parsed import shows required metrics
- **WHEN** a GPX/KML sample is parsed successfully for the route tab preview
- **THEN** the preview shows file name, parse status, distance, elevation gain, waypoint count, track point count, elevation data availability, and data quality notes

#### Scenario: Import preview explains route-only data
- **WHEN** an import result preview is displayed
- **THEN** the screen explains that imported files contain route tracks and waypoints for trail navigation, off-route judgement, and progress calculation, but do not contain commercial map base layers

### Requirement: Route asset cards
The route tab SHALL distinguish route asset source and offline readiness without implying full-map offline coverage.

#### Scenario: Imported track card is track-only
- **WHEN** an imported GPX/KML route appears in the route list
- **THEN** the card labels it as an imported track and marks it as `仅轨迹可用`

#### Scenario: Verified offline route card is navigable offline
- **WHEN** a platform route has verified route-level offline data
- **THEN** the card labels it as `可离线导航`

### Requirement: Static actions until later integration
The route tab SHALL keep import, save, detail, and start-navigation controls as UI affordances in this slice without performing file selection, persistence, detail routing, or live navigation.

#### Scenario: Action labels are present without side-effect contracts
- **WHEN** the route tab is implemented in this change
- **THEN** tests verify the labels and state contract, while file picker, storage, route detail routing, and navigation session effects remain out of scope
