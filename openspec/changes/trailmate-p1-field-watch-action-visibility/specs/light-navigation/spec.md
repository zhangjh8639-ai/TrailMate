## ADDED Requirements

### Requirement: Field Watch Action Visibility

TrailMate SHALL only render tappable actions on field safety watch cards when the action is immediately executable from the route page.

#### Scenario: Safety-share watch action is visible

- **GIVEN** a daylight or progress safety watch card requires safety sharing
- **AND** the card primary action label is not blank
- **WHEN** TrailMate presents the watch card
- **THEN** TrailMate MUST show a primary action button
- **AND** that action MUST route through the existing safety-share action

#### Scenario: Manual review suggestion is not a button

- **GIVEN** a daylight or progress safety watch card gives manual review or rest guidance
- **AND** the card primary action does not require safety sharing
- **WHEN** TrailMate presents the watch card
- **THEN** TrailMate MUST keep the guidance visible
- **AND** TrailMate MUST NOT render the guidance label as a tappable button

#### Scenario: Blank safety-share label is not a button

- **GIVEN** a daylight or progress safety watch card requires safety sharing
- **AND** the card primary action label is blank
- **WHEN** TrailMate presents the watch card
- **THEN** TrailMate MUST NOT render an empty tappable button
