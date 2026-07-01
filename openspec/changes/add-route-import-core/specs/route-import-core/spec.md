## ADDED Requirements

### Requirement: GPX import parsing
The system SHALL parse GPX route content into a navigation-ready route import summary.

#### Scenario: GPX track creates route geometry and summary metrics
- **WHEN** a GPX file contains `trkpt` coordinates with elevation
- **THEN** the parser returns a parsed result with GPX format, route geometry, total distance, elevation gain, track point count, waypoint count, and `hasElevation = true`

#### Scenario: GPX route points are accepted when track points are absent
- **WHEN** a GPX file contains `rtept` coordinates but no `trkpt`
- **THEN** the parser uses the route points as the navigable geometry

#### Scenario: GPX route points are accepted when track geometry is insufficient
- **WHEN** a GPX file contains fewer than two usable `trkpt` coordinates and at least two usable `rtept` coordinates
- **THEN** the parser uses the route points as the navigable geometry

### Requirement: KML import parsing
The system SHALL parse KML route content into a navigation-ready route import summary.

#### Scenario: KML LineString creates route geometry
- **WHEN** a KML file contains a `LineString` coordinate sequence
- **THEN** the parser returns a parsed result with KML format, route geometry, total distance, track point count, and optional elevation

#### Scenario: KML point placemarks become waypoints
- **WHEN** a KML file contains `Point` placemarks
- **THEN** the parser converts them into route waypoints projected onto the route geometry

### Requirement: Import quality warnings
The system SHALL return structured quality warnings that the route tab can map to concise Chinese copy.

#### Scenario: Missing elevation is reported
- **WHEN** an imported route contains no elevation values
- **THEN** the result includes a missing-elevation warning and `hasElevation = false`

#### Scenario: Sparse route is reported
- **WHEN** an imported route has too few route points for stable navigation
- **THEN** the result includes a sparse-track warning

#### Scenario: Large point gap is reported
- **WHEN** adjacent route points are farther apart than the configured stable-navigation gap
- **THEN** the result includes a large-point-gap warning

### Requirement: Import validation
The system SHALL reject unsupported or unusable route files with structured status rather than throwing UI-facing exceptions.

#### Scenario: Unsupported extension is rejected
- **WHEN** the filename is not GPX or KML
- **THEN** the result status is unsupported format and no geometry is returned

#### Scenario: Missing route geometry is rejected
- **WHEN** a GPX/KML file has fewer than two usable route coordinates
- **THEN** the result status is missing track geometry and no geometry is returned

#### Scenario: Unsafe XML doctype is rejected
- **WHEN** a GPX/KML file contains a doctype or external entity declaration
- **THEN** the result status is invalid XML and no geometry is returned

### Requirement: Imported route privacy
The system SHALL preserve privacy defaults when turning an import result into a route domain object.

#### Scenario: Imported result creates private track-only route
- **WHEN** a parsed import result is converted to `TrailRoute`
- **THEN** the route uses the matching imported source type, has `TrackOnly` offline status, and remains private by default

### Requirement: Import parser scope stays local
The system SHALL NOT introduce UI, file picker, persistence, map rendering, network, route planning, equipment, community, marketplace, or pre-trip-check functionality in this slice.

#### Scenario: Import package is pure parsing/domain code
- **WHEN** the import source tree is inspected
- **THEN** it does not depend on Compose, Android file pickers, Room, DataStore, Retrofit, MapLibre, GPS APIs, planner, equipment, community, marketplace, or pre-trip-check packages
