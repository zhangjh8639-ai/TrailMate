## ADDED Requirements

### Requirement: Shared Departure Primary Action Policy

TrailMate SHALL use the same departure readiness primary action policy for the Route cockpit main action and secondary departure actions.

#### Scenario: Unsupported departure action is blocked

- **GIVEN** the hike has not started
- **AND** departure readiness reports an unsupported primary action
- **WHEN** TrailMate builds the Route cockpit main action
- **THEN** the main action MUST keep the unsupported label visible
- **AND** the main action MUST be disabled
- **AND** the main action MUST NOT resolve to reset-session behavior

#### Scenario: Blocked departure actions do not expose navigation shortcuts

- **GIVEN** the Route cockpit main action is blocked
- **WHEN** TrailMate evaluates fullscreen navigation policy
- **THEN** the blocked action MUST NOT enter fullscreen navigation
- **AND** the blocked action MUST NOT show the fullscreen shortcut

#### Scenario: Departure panel button follows shared action enabled state

- **GIVEN** departure readiness resolves to a blocked primary action
- **WHEN** TrailMate presents the diagnostics Departure readiness panel button
- **THEN** the button MUST be disabled from the shared action policy
- **AND** it MUST NOT rely on a single hard-coded blocked label

#### Scenario: Supported repairs keep their existing behavior

- **GIVEN** the hike has not started
- **AND** departure readiness reports a supported offline route, offline base map, location, system location, or gear repair
- **WHEN** TrailMate builds the Route cockpit main action
- **THEN** TrailMate MUST route the action to the matching repair kind
