## ADDED Requirements

### Requirement: Route Page Flow Separation

TrailMate SHALL separate route preparation, route suitability assessment, and field navigation into distinct user-facing states.

#### Scenario: Route workspace stays preparation-focused

- **GIVEN** the user opens the bottom route tab
- **WHEN** an imported route is available but route detail is not open
- **THEN** the page shows the current route, import or replace GPX action, and a continue-preparation action
- **AND** it does not show full assessment, GPS recording controls, map diagnostics, or route cockpit content

#### Scenario: Route workspace separates GPX and offline basemap preparation

- **GIVEN** the user opens the bottom route tab
- **WHEN** an imported route is available but route detail is not open
- **THEN** the route workspace shows GPX route management and PMTiles offline basemap preparation as separate actions
- **AND** the offline basemap action explains that PMTiles adds map context around the GPX route
- **AND** it does not use AMap offline map management as the primary route workspace action
- **AND** it does not show the legacy mixed map-diagnostics status panel

#### Scenario: Route workspace summarizes departure preparation

- **GIVEN** the user opens the bottom route tab
- **WHEN** an imported route is available but route detail is not open
- **THEN** the route workspace shows a departure preparation summary for GPX route import, offline route saving, and offline basemap package readiness
- **AND** saving the GPX-derived offline route is available as `保存离线路线` without entering the field navigation cockpit
- **AND** the summary explains that the offline route stores route line, checkpoints, and plan data while the offline basemap package supplies roads, place names, and terrain context
- **AND** GPS, recording, fullscreen navigation, and technical map diagnostics remain outside the route workspace

#### Scenario: PMTiles preparation opens map setup details

- **GIVEN** the user has an imported route in the route workspace
- **WHEN** the user chooses the PMTiles basemap preparation action
- **THEN** TrailMate opens the route map setup view with PMTiles status visible
- **AND** the user can start the offline basemap import from that setup view
- **AND** field navigation controls remain outside the route workspace

#### Scenario: Assessment answers suitability before navigation

- **GIVEN** the user opens route detail
- **WHEN** the assessment tab is selected
- **THEN** the first viewport shows match level, distance, ascent, estimated duration, confidence, key risk factors, and one clear next action
- **AND** it does not show the full route map as the primary surface

#### Scenario: Route cockpit owns field navigation

- **GIVEN** the user selects the route tab inside route detail
- **WHEN** GPS and recording state are available
- **THEN** the first viewport shows the map, current checkpoint, next checkpoint, progress, readiness strip, and one primary field action
- **AND** technical diagnostics remain collapsed behind a secondary settings affordance

### Requirement: Route Plan Timeline

TrailMate SHALL present route plans as an advisory checkpoint timeline.

#### Scenario: Plan tab shows advisory checkpoints

- **GIVEN** a route plan has checkpoints
- **WHEN** the user selects the plan tab
- **THEN** checkpoints are shown in order as a timeline with distance, estimated time, and suggested action
- **AND** the wording remains advisory and avoids safety guarantees
