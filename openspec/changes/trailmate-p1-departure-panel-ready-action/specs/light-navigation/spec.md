## ADDED Requirements

### Requirement: Departure Readiness Panel Ready Action

TrailMate SHALL make the diagnostics Departure readiness panel actionable when all required departure checks pass.

#### Scenario: Ready panel starts hike and recording

- **GIVEN** departure readiness reports `开始徒步并记录轨迹`
- **WHEN** the hiker taps the diagnostics Departure readiness panel primary action
- **THEN** TrailMate MUST start the hike session
- **AND** TrailMate MUST request track recording through the existing recording gate

#### Scenario: Repair actions stay routed

- **GIVEN** departure readiness reports an offline route, offline base map, location, or gear repair action
- **WHEN** the hiker taps the diagnostics Departure readiness panel primary action
- **THEN** TrailMate MUST route the tap to the matching repair action
- **AND** it MUST NOT start the hike
