## ADDED Requirements

### Requirement: Offline Emergency Info Sharing

TrailMate SHALL provide a manually shareable emergency information summary for the selected route.

#### Scenario: Reliable current location is available

- **GIVEN** TrailMate has finite latitude and longitude
- **AND** horizontal accuracy is no worse than 100 meters
- **AND** the location timestamp is within 2 minutes of the share action time
- **WHEN** TrailMate presents emergency information
- **THEN** it MUST include route name, route summary, progress, current checkpoint, next checkpoint, coordinates, accuracy, and coordinate timestamp
- **AND** it MAY include a coordinate map link for the static point-in-time location

#### Scenario: Location is stale, inaccurate, missing, or future-dated

- **GIVEN** TrailMate does not have a reliable current location
- **WHEN** TrailMate presents emergency information
- **THEN** it MUST still include route and progress context
- **AND** it MUST state that there is no reliable current GPS coordinate
- **AND** it MUST NOT include a coordinate map link

#### Scenario: Share action rechecks location freshness

- **GIVEN** TrailMate has already displayed emergency information with a reliable location
- **AND** the location becomes older than 2 minutes before the hiker taps share
- **WHEN** the hiker taps the emergency information share action
- **THEN** TrailMate MUST recompute the emergency information using the share action time
- **AND** it MUST NOT send the previously generated coordinate map link
- **AND** it MUST state that there is no reliable current GPS coordinate

#### Scenario: Active recording progress is available

- **GIVEN** TrailMate is recording a route
- **WHEN** TrailMate presents emergency information
- **THEN** it MUST include the recorded distance and indicate that the information is a point-in-time snapshot

#### Scenario: Safety boundary remains honest

- **GIVEN** TrailMate generates emergency information text
- **WHEN** the hiker shares it manually
- **THEN** the text MUST state that it is static information, not live tracking
- **AND** it MUST NOT imply TrailMate will automatically contact anyone, send alerts, monitor the hiker, dispatch rescue, or guarantee safety
