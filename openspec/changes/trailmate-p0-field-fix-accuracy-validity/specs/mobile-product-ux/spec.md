## ADDED Requirements

### Requirement: Field Location Reliability Shall Reject Malformed Accuracy

TrailMate SHALL treat malformed horizontal accuracy values as unreliable for field-use decisions.

#### Scenario: Malformed accuracy is not reliable field evidence

- **GIVEN** a location snapshot has `LOCATED` status and a fresh timestamp
- **AND** its horizontal accuracy is missing, negative, NaN, or infinite
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return unreliable
- **AND** downstream route readiness, cockpit state, recording gates, and map overlays MUST NOT receive reliable-field evidence from that snapshot

#### Scenario: Over-threshold accuracy remains unreliable

- **GIVEN** a location snapshot has `LOCATED` status and a fresh timestamp
- **AND** its horizontal accuracy is finite and non-negative but worse than the configured field threshold
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return unreliable

#### Scenario: Valid accuracy and fresh timestamp remain reliable

- **GIVEN** a location snapshot has `LOCATED` status, a fresh timestamp, and finite non-negative accuracy within the configured field threshold
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return reliable

#### Scenario: Invalid caller threshold is not accepted

- **GIVEN** a caller supplies a negative, NaN, or infinite maximum accuracy threshold
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return unreliable
