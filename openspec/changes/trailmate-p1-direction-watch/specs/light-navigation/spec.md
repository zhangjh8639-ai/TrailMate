## ADDED Requirements

### Requirement: Route Direction Watch

TrailMate SHALL warn the hiker when reliable on-route GPS samples indicate movement backward along the planned route.

#### Scenario: Reliable backward movement

- **GIVEN** route recording is active
- **AND** the latest route-aligned GPS samples are fresh and accurate
- **AND** the hiker remains within the on-route threshold
- **WHEN** the distance along the planned route decreases by at least 0.15 km over at least 60 seconds
- **THEN** TrailMate MUST show a direction warning
- **AND** the warning MUST tell the hiker to stop and verify route direction against the map, trail markers, and visible path

#### Scenario: GPS jitter

- **GIVEN** route recording is active
- **AND** the hiker remains within the on-route threshold
- **WHEN** the distance along the planned route decreases by less than 0.15 km
- **THEN** TrailMate MUST NOT show a direction warning

#### Scenario: Low confidence state

- **GIVEN** GPS confidence is low, stale, missing, off-route, paused, or finished
- **WHEN** TrailMate evaluates route direction
- **THEN** TrailMate MUST NOT show a direction warning
- **AND** existing low-accuracy, off-route, paused, or finished guidance remains authoritative

#### Scenario: Safety boundary remains advisory

- **GIVEN** TrailMate shows a route direction warning
- **WHEN** the hiker reads or acts on it
- **THEN** TrailMate MUST NOT imply turn-by-turn navigation
- **AND** it MUST NOT imply automatic rescue, automatic contact, guaranteed safety, or medical diagnosis
