## ADDED Requirements

### Requirement: Route Field Battery Status

TrailMate SHALL include battery state in the route cockpit field status summary so hikers can see whether the phone has enough power context before relying on GPS navigation and track recording.

#### Scenario: Battery level is available

- **GIVEN** Android reports a battery percentage between 0 and 100
- **WHEN** TrailMate builds route field status
- **THEN** the field status items MUST include `电量`
- **AND** values at or above 30% MUST show the percentage
- **AND** values from 16% through 29% MUST show `偏低 xx%`
- **AND** values at or below 15% MUST show `危险 xx%`

#### Scenario: Battery level is low during field use

- **GIVEN** battery status is low or critical
- **WHEN** TrailMate builds the route field status caption
- **THEN** the caption MUST use conservative guidance that asks the hiker to reduce screen use and consider turning back or charging
- **AND** TrailMate MUST NOT silently present low battery as normal field readiness

#### Scenario: Battery level is unavailable

- **GIVEN** Android battery status is unavailable or invalid
- **WHEN** TrailMate builds route field status
- **THEN** the battery item MUST show `未知`
- **AND** existing GPS, track, basemap, and notification status behavior MUST remain unchanged
