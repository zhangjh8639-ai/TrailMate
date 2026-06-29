## ADDED Requirements

### Requirement: Safe-exit Fix Reliability

TrailMate SHALL NOT recommend a safe-exit direction from an unreliable `HikeLocationFix`.

#### Scenario: Stale on-route fix waits for GPS

- **GIVEN** safe-exit guidance is evaluating an on-route location
- **AND** the fix is older than the field reliability window
- **WHEN** safe-exit guidance is presented
- **THEN** TrailMate MUST ask the hiker to stabilize GPS
- **AND** TrailMate MUST NOT emphasize an exit direction.

#### Scenario: Future or malformed on-route fix waits for GPS

- **GIVEN** safe-exit guidance is evaluating an on-route location
- **AND** the fix has a future timestamp or malformed numeric fields
- **WHEN** safe-exit guidance is presented
- **THEN** TrailMate MUST ask the hiker to stabilize GPS
- **AND** TrailMate MUST NOT recommend original backtracking or the next checkpoint as ready.
