## ADDED Requirements

### Requirement: Route projection
The system SHALL project a WGS84 coordinate onto a navigation-ready route geometry without depending on Android framework location classes, MapLibre, network DTOs, or persistence annotations.

#### Scenario: Coordinate projects onto nearest route segment
- **WHEN** a current coordinate is near a route segment
- **THEN** the projection returns the nearest route coordinate, segment index, distance from route, progress from start, segment bearing, and bearing from the current coordinate to the nearest route point

#### Scenario: Previous progress stabilizes overlapping routes
- **WHEN** multiple route segments are similarly close to the current coordinate
- **AND** previous route progress is available
- **THEN** the projection prefers the candidate closest to the previous progress window rather than jumping to a distant progress value

#### Scenario: Physical distance wins over stale progress
- **WHEN** the current coordinate is clearly closer to a different segment than the previous progress candidate
- **THEN** the projection selects the physically closer segment

### Requirement: Route progress metrics
The system SHALL calculate navigation progress metrics from route geometry and a projection result.

#### Scenario: Remaining distance is derived from route progress
- **WHEN** the projection is halfway along a route
- **THEN** completed distance equals projection progress and remaining distance equals total distance minus projection progress

#### Scenario: Remaining elevation gain ignores completed climbs
- **WHEN** the route contains elevation values
- **THEN** remaining elevation gain includes only positive elevation gains after the projected progress

#### Scenario: Navigation anchors are route-distance based
- **WHEN** waypoints and exit points exist after the projected progress
- **THEN** the next waypoint is selected by travel direction and the nearest exit point is selected by absolute route-distance distance from current progress

#### Scenario: Nearest exit can be behind current progress
- **WHEN** an exit point behind current progress is closer than exit points ahead
- **THEN** the nearest exit point is the behind exit point

### Requirement: Off-route evidence
The system SHALL evaluate off-route evidence from GPS accuracy, distance from route, and sustained deviation.

#### Scenario: Poor GPS accuracy does not trigger off-route warning
- **WHEN** location accuracy is worse than the configured maximum acceptable accuracy
- **THEN** the detector reports unreliable GPS rather than suspected or confirmed off-route

#### Scenario: Poor GPS accuracy resets off-route evidence
- **WHEN** a poor-accuracy sample arrives after suspected or confirmed off-route evidence
- **THEN** the detector reports unreliable GPS without carrying forward prior off-route start time or consecutive samples

#### Scenario: Single accurate far point is suspected off-route
- **WHEN** the location is farther than the suspected off-route distance threshold
- **AND** GPS accuracy is acceptable
- **THEN** the detector reports suspected off-route

#### Scenario: Sustained accurate deviation becomes confirmed off-route
- **WHEN** accurate off-route evidence continues past the confirmation duration
- **AND** enough consecutive accurate off-route samples have been observed
- **THEN** the detector reports confirmed off-route

#### Scenario: Frequent samples do not confirm before duration threshold
- **WHEN** many accurate off-route samples arrive before the confirmation duration
- **THEN** the detector remains suspected rather than confirmed

### Requirement: Algorithm scope stays local and safe
The system SHALL NOT generate automatic safe reroutes, direct-return instructions, or map-sdk-specific snapping in this slice.

#### Scenario: Geo package excludes unsafe reroute concepts
- **WHEN** the geo source tree is inspected
- **THEN** it does not contain APIs named safe route, auto reroute, or direct return
