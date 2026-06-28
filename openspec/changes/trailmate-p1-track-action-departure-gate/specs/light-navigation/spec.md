## ADDED Requirements

### Requirement: Secondary Track Action Departure Gate

TrailMate SHALL apply required departure readiness repairs to secondary track recording start entry points before departure.

#### Scenario: Pre-start idle recording start is blocked by a required repair

- **GIVEN** the route has not started recording
- **AND** the hike session is still ready to start
- **AND** departure readiness reports a required repair action
- **WHEN** the Route diagnostics track action is presented
- **THEN** the action MUST show the required repair label
- **AND** it MUST NOT start recording

#### Scenario: Pre-start finished recording restart is blocked by a required repair

- **GIVEN** a previous recording has finished
- **AND** the hike session is still ready to start
- **AND** departure readiness reports a required repair action
- **WHEN** the Route diagnostics track action is presented
- **THEN** the action MUST show the required repair label before allowing another recording start

#### Scenario: Active recording controls remain available

- **GIVEN** recording is already active or paused
- **AND** departure readiness later reports a repair action
- **WHEN** the Route diagnostics track action is presented
- **THEN** the action MUST keep the existing recording control available
- **AND** it MUST NOT replace pause or resume with a departure repair action

#### Scenario: Active hike can still start recording

- **GIVEN** the hike session is already active
- **AND** recording has not started yet
- **WHEN** the Route diagnostics track action is presented
- **THEN** TrailMate MUST keep the existing recording action available
- **AND** it MUST NOT block the action with a departure repair step
