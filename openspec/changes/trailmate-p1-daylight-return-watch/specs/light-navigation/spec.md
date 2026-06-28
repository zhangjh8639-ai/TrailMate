## ADDED Requirements

### Requirement: Daylight Return Watch

TrailMate SHALL warn hikers when an active route recording is likely to finish near or after the local daylight window and there is enough route geometry to estimate that window.

#### Scenario: Missing route geometry or active recording

- **GIVEN** TrailMate does not have route coordinates or an active recording
- **WHEN** the route tab presents daylight guidance
- **THEN** it MUST NOT show a daylight return warning

#### Scenario: Expected finish remains comfortably before sunset

- **GIVEN** the hiker is recording a route
- **AND** TrailMate can estimate the route day's sunset
- **AND** the expected finish time is comfortably before sunset
- **WHEN** TrailMate presents daylight guidance
- **THEN** it MUST keep the daylight warning hidden

#### Scenario: Expected finish is close to sunset

- **GIVEN** the hiker is recording a route
- **AND** the expected finish time is within the caution window before sunset
- **WHEN** TrailMate presents daylight guidance
- **THEN** it MUST show a caution card
- **AND** it MUST recommend reviewing headlamp, exit options, pace, and whether to shorten the route

#### Scenario: Expected finish reaches dusk or later

- **GIVEN** the hiker is recording a route
- **AND** the expected finish time is after sunset or near civil dusk
- **WHEN** TrailMate presents daylight guidance
- **THEN** it MUST show an alert card
- **AND** it MUST recommend shortening or exiting the route before dark
- **AND** it MUST offer manual location sharing

#### Scenario: Expected finish crosses midnight

- **GIVEN** the hiker is recording a route before the current day's sunset
- **AND** the expected finish time is after midnight
- **WHEN** TrailMate presents daylight guidance
- **THEN** it MUST compare the expected finish against the current day's daylight window
- **AND** it MUST show an alert card

#### Scenario: Current recording is already after sunset

- **GIVEN** the hiker is recording a route
- **AND** TrailMate can estimate the route day's sunset
- **AND** the current time is after sunset
- **WHEN** TrailMate presents daylight guidance
- **THEN** it MUST show an alert card even if a planned finish time is unavailable
- **AND** it MUST offer manual location sharing

#### Scenario: Safety boundary remains honest

- **GIVEN** TrailMate presents daylight return guidance
- **WHEN** the hiker reads the guidance
- **THEN** it MUST state or imply only an estimate
- **AND** it MUST NOT guarantee visibility, contact anyone automatically, dispatch rescue, or guarantee safety
