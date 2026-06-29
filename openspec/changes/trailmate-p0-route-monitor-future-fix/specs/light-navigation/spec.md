## ADDED Requirements

### Requirement: Route Monitor Future Fix Guard

TrailMate SHALL NOT use recorded GPS points with future or non-positive timestamps as reliable evidence for route-deviation alerts.

#### Scenario: Future recorded point does not trigger off-route alert

- **GIVEN** TrailMate is monitoring a matching route during track recording
- **AND** the latest recorded point has a timestamp later than the current evaluation time
- **WHEN** TrailMate evaluates route-deviation alert delivery
- **THEN** TrailMate MUST wait for a reliable fix
- **AND** TrailMate MUST NOT send or vibrate an off-route alert from that point
