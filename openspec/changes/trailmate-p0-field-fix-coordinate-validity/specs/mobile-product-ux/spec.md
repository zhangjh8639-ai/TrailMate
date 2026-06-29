## ADDED Requirements

### Requirement: Field Location Reliability Shall Reject Malformed Coordinates

TrailMate SHALL treat missing or non-finite coordinates as unreliable for field-use decisions.

#### Scenario: Missing coordinates are not reliable field evidence

- **GIVEN** a location snapshot has `LOCATED` status, valid accuracy, and a fresh timestamp
- **AND** latitude or longitude is missing
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return unreliable
- **AND** downstream route readiness, cockpit state, recording gates, and map overlays MUST NOT receive reliable-field evidence from that snapshot

#### Scenario: Non-finite coordinates are not reliable field evidence

- **GIVEN** a location snapshot has `LOCATED` status, valid accuracy, and a fresh timestamp
- **AND** latitude or longitude is NaN or infinite
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST return unreliable

#### Scenario: Valid coordinates keep existing reliability behavior

- **GIVEN** a location snapshot has `LOCATED` status, finite coordinates, valid accuracy, and a fresh timestamp
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** it MUST preserve the existing reliable result
