## ADDED Requirements

### Requirement: Route Exit Action Visibility

TrailMate SHALL distinguish Route exit recommendations from executable Route exit repair actions.

#### Scenario: Ready route-exit recommendation has no button

- **GIVEN** Route exit guidance has reliable location progress
- **AND** TrailMate recommends returning along the route or continuing to a planned reference
- **WHEN** TrailMate presents the Route exit guidance panel
- **THEN** the recommendation MUST be shown as guidance content
- **AND** TrailMate MUST NOT show it as a primary action button

#### Scenario: Caution route-exit guidance shows repair action

- **GIVEN** Route exit guidance cannot choose a safe direction because location needs repair
- **AND** the repair action label is not blank
- **WHEN** TrailMate presents the Route exit guidance panel
- **THEN** TrailMate MUST show the repair action as the primary button

#### Scenario: Blank route-exit action label has no button

- **GIVEN** Route exit guidance is in a caution state
- **AND** the primary action label is blank
- **WHEN** TrailMate presents the Route exit guidance panel
- **THEN** TrailMate MUST NOT show a primary action button
