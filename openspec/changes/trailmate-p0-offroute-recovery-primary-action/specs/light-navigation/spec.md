## ADDED Requirements

### Requirement: Off-route Recovery CTA Priority

TrailMate SHALL prioritize route recovery guidance over routine recording controls whenever the route cockpit is in an active or paused off-route or recently-rejoined state.

#### Scenario: Active recording still opens recovery guidance while off route

- **GIVEN** TrailMate is recording a track
- **AND** a hike session is active
- **AND** route guidance says the hiker should check the route
- **WHEN** the route cockpit resolves its primary action
- **THEN** the primary action MUST open recovery guidance
- **AND** the primary action MUST NOT be the pause-recording action.

#### Scenario: Recent rejoin still opens recovery guidance while recording

- **GIVEN** TrailMate is recording a track
- **AND** a hike session is active
- **AND** the hiker has recently rejoined after an off-route episode
- **WHEN** the route cockpit resolves its primary action
- **THEN** the primary action MUST open recovery guidance so the user can acknowledge the recovery state.

#### Scenario: Departure gate remains primary before hike starts

- **GIVEN** the hike session has not started
- **AND** departure readiness requires a route or field preparation action
- **AND** route guidance says the hiker should check the route
- **WHEN** the route cockpit resolves its primary action
- **THEN** the primary action MUST stay on the departure readiness action.
