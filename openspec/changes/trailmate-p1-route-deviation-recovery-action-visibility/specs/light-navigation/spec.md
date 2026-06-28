## ADDED Requirements

### Requirement: Route Deviation Recovery Action Visibility

TrailMate SHALL render route-deviation recovery primary buttons only when the route page can execute the represented action, and SHALL dispatch those actions by explicit action kind rather than by display-copy comparison.

#### Scenario: Rejoined recovery acknowledges route rejoin

- **GIVEN** route-deviation recovery indicates the hiker has rejoined the route
- **WHEN** TrailMate presents the recovery panel
- **THEN** TrailMate MUST show a primary action for continuing navigation
- **AND** that action MUST acknowledge the route-rejoin state

#### Scenario: Low-accuracy recovery requests a fresh location

- **GIVEN** route-deviation recovery is waiting for location accuracy to stabilize
- **WHEN** TrailMate presents the recovery panel
- **THEN** TrailMate MUST show a primary action for refreshing location
- **AND** that action MUST request a fresh location

#### Scenario: Off-route recovery shares only with available share text

- **GIVEN** route-deviation recovery offers location sharing
- **AND** safety-share text is available
- **WHEN** TrailMate presents the recovery panel
- **THEN** TrailMate MUST show a share-location primary action
- **AND** that action MUST route through the existing safety-share action

#### Scenario: Missing share text falls back to location refresh

- **GIVEN** route-deviation recovery display copy would otherwise offer location sharing
- **AND** safety-share text is unavailable
- **WHEN** TrailMate presents the recovery panel
- **THEN** TrailMate MUST present a location-refresh action instead of a share action
- **AND** that action MUST request a fresh location
