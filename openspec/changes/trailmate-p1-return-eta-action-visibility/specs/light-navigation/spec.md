## ADDED Requirements

### Requirement: Return ETA Action Visibility

TrailMate SHALL only render a tappable return-ETA action when the action is immediately executable from the route page.

#### Scenario: Overdue return ETA safety share is visible

- **GIVEN** return-ETA guidance requires safety sharing
- **AND** the primary action label is not blank
- **WHEN** TrailMate presents the return-ETA card
- **THEN** TrailMate MUST show a primary action button
- **AND** that action MUST route through the existing safety-share action

#### Scenario: Passive return ETA guidance is not a button

- **GIVEN** return-ETA guidance provides passive status or review copy
- **AND** the primary action does not require safety sharing
- **WHEN** TrailMate presents the return-ETA card
- **THEN** TrailMate MUST keep the guidance label visible
- **AND** TrailMate MUST NOT render that label as a tappable button

#### Scenario: Blank overdue action is not a button

- **GIVEN** return-ETA guidance requires safety sharing
- **AND** the primary action label is blank
- **WHEN** TrailMate presents the return-ETA card
- **THEN** TrailMate MUST NOT render an empty tappable button
