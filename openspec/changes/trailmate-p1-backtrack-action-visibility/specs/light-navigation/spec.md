## ADDED Requirements

### Requirement: Backtrack Breadcrumb Action Visibility

TrailMate SHALL show a Backtrack breadcrumb panel action only when the action is immediately executable from the Route diagnostics panel.

#### Scenario: Usable breadcrumb opens track review

- **GIVEN** the walked breadcrumb is ready to use as a return reference
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST show an action to view the walked track
- **AND** that action MUST route to track review

#### Scenario: Saved breadcrumb opens track review

- **GIVEN** the walked breadcrumb has been saved after finishing a route
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST show an action to view the saved track
- **AND** that action MUST route to track review

#### Scenario: Stale breadcrumb refreshes location

- **GIVEN** the walked breadcrumb exists but the latest point is stale
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST show a refresh-location action
- **AND** that action MUST request location refresh

#### Scenario: Paused breadcrumb can continue recording

- **GIVEN** the walked breadcrumb exists
- **AND** track recording is paused
- **AND** the existing track recording action is enabled
- **AND** the existing track recording action label matches the breadcrumb action label
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST show a continue-recording action
- **AND** that action MUST route through the existing track recording action

#### Scenario: Non-action breadcrumb states hide the button

- **GIVEN** the breadcrumb is still warming up or no breadcrumb evidence exists
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST keep the guidance visible
- **AND** TrailMate MUST NOT show a pseudo-action button

#### Scenario: Unavailable breadcrumb never inherits status actions

- **GIVEN** no breadcrumb evidence exists
- **AND** the track recording status is paused or finished
- **WHEN** TrailMate presents the Backtrack breadcrumb panel
- **THEN** TrailMate MUST NOT show continue-recording or track-review actions
