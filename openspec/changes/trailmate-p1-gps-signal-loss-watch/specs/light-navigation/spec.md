## ADDED Requirements

### Requirement: GPS Signal Loss Watch

TrailMate SHALL warn the hiker when active track recording depends on stale or blocked GPS evidence.

#### Scenario: Fresh location during recording

- **GIVEN** track recording is active
- **AND** the latest location snapshot is fresh
- **WHEN** the Route tab or fullscreen navigation renders
- **THEN** TrailMate MUST NOT show a GPS signal-loss warning

#### Scenario: Stale location during recording

- **GIVEN** track recording is active
- **AND** the latest location snapshot is more than 60 seconds old
- **WHEN** the Route tab or fullscreen navigation renders
- **THEN** TrailMate MUST show a warning that the current location may be stale
- **AND** it MUST ask the hiker to stop and refresh location before relying on route progress

#### Scenario: Severely stale location during recording

- **GIVEN** track recording is active
- **AND** the latest location snapshot is at least 5 minutes old
- **WHEN** the Route tab or fullscreen navigation renders
- **THEN** TrailMate MUST show a stronger warning that the shown position must not be treated as current
- **AND** it MUST direct the hiker to verify offline map, trail markers, and visible path

#### Scenario: Location blocked while recording

- **GIVEN** track recording is active
- **AND** location permission, provider, or availability blocks fresh fixes
- **WHEN** the Route tab or fullscreen navigation renders
- **THEN** TrailMate MUST show a location-interrupted warning
- **AND** the warning action MUST request location recovery

#### Scenario: Recording is not active

- **GIVEN** track recording is idle, paused, or finished
- **WHEN** TrailMate evaluates GPS signal loss
- **THEN** TrailMate MUST NOT show a GPS signal-loss warning

#### Scenario: Safety boundary remains advisory

- **GIVEN** TrailMate shows a GPS signal-loss warning
- **WHEN** the hiker reads or acts on it
- **THEN** TrailMate MUST NOT imply turn-by-turn navigation
- **AND** it MUST NOT imply automatic rescue, automatic contact, guaranteed safety, or medical diagnosis
