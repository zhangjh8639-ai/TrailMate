## ADDED Requirements

### Requirement: Light Navigation Route Cockpit

TrailMate SHALL present the route tab as a map-first light-navigation cockpit for an imported route.

#### Scenario: First viewport prioritizes field action

- **GIVEN** the user opens the route detail screen for an imported route
- **WHEN** the user selects the "路线" tab
- **THEN** the first viewport shows the route map, current or estimated position, next checkpoint, route progress, and one primary action
- **AND** supporting diagnostics do not appear above the primary field controls unless they block map, GPS, or recording use

#### Scenario: Internal evidence stays out of the route cockpit

- **GIVEN** TrailMate has user profile, historical activity, body metrics, and equipment evidence
- **WHEN** the user views the route cockpit
- **THEN** the cockpit uses those inputs only through assessment, readiness, and gear status
- **AND** the screen does not display raw evidence bundles, AI prompt material, or historical import details

### Requirement: Cockpit Action Drawer

TrailMate SHALL provide state-driven actions in the route cockpit.

#### Scenario: Primary action follows session state

- **GIVEN** the route cockpit has location and recording state
- **WHEN** the state changes between no permission, ready, recording, paused, off-route, and completed
- **THEN** the primary action changes respectively to enabling location, starting the hike, pausing, resuming, viewing recovery advice, or reviewing the recorded track

#### Scenario: Off-route state exposes recovery without safety guarantees

- **GIVEN** the recorded position is outside the route match tolerance
- **WHEN** the cockpit detects an off-route state
- **THEN** it shows recovery guidance and keeps safety sharing visible
- **AND** it does not claim automatic rescue, guaranteed safety, or silent rerouting

### Requirement: Field Readiness Strip

TrailMate SHALL summarize departure and navigation readiness in a compact scan-first strip.

#### Scenario: Readiness is visible at a glance

- **GIVEN** the user is viewing the route cockpit
- **WHEN** GPS, recording, offline map, and gear statuses are available
- **THEN** the cockpit shows those four statuses in a compact strip near the primary action
- **AND** each status can lead to its detailed explanation without displacing the map as the primary surface
- **AND** actionable statuses route to the matching field workflow: GPS requests location, recording controls the track recorder, offline saves the route pack, and gear opens the route checklist

#### Scenario: AMap diagnostics are available but not prominent

- **GIVEN** AMap key, privacy, network, or SDK diagnostics are relevant
- **WHEN** the user views the route cockpit
- **THEN** the cockpit summarizes blocking map readiness problems in the readiness strip
- **AND** detailed diagnostics remain in an expandable or lower-priority section
