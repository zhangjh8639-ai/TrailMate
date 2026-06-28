## ADDED Requirements

### Requirement: Departure Brief Action Visibility

TrailMate SHALL only show the Route diagnostics departure brief primary action when the brief can be sent.

#### Scenario: Sendable departure brief shows action

- **GIVEN** the route has enough duration information to generate a departure brief share payload
- **AND** the departure brief action label is not blank
- **WHEN** TrailMate presents the Route diagnostics departure brief panel
- **THEN** the primary action MUST be visible
- **AND** the primary action label MUST match the send action label

#### Scenario: Blank action label hides action

- **GIVEN** the route can generate a departure brief share payload
- **AND** the departure brief action label is blank
- **WHEN** TrailMate presents the Route diagnostics departure brief panel
- **THEN** the primary action MUST be hidden

#### Scenario: Unsendable departure brief hides action

- **GIVEN** the route cannot generate a departure brief share payload
- **WHEN** TrailMate presents the Route diagnostics departure brief panel
- **THEN** the primary action MUST be hidden
- **AND** TrailMate MUST keep the explanatory status, caption, and details visible
