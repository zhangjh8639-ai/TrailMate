## ADDED Requirements

### Requirement: Production Journey Separation

TrailMate SHALL present account setup, route preparation, route assessment, route preview, gear preparation, and full-screen field navigation as distinct user-facing states.

#### Scenario: Home stays route-preparation focused

- **GIVEN** the user opens the home tab
- **WHEN** no real daily health source is connected
- **THEN** the first viewport shows route import, current route preparation, and at most three quick actions
- **AND** it does not show fake daily steps, fake calories, raw evidence inputs, or diagnostic panels

#### Scenario: Route workspace manages routes only

- **GIVEN** the user opens the bottom route tab
- **WHEN** a target route is imported
- **THEN** the page shows current route, route facts, import or replace action, and a route detail entry
- **AND** it does not show GPS recording controls, full-screen navigation controls, or map readiness diagnostics in the first viewport

#### Scenario: Gear tab shows server catalog candidates

- **GIVEN** a target route has gear recommendations
- **WHEN** the user opens the Gear tab
- **THEN** the page shows route needs, brand candidates from the server catalog, and read-only equipment details
- **AND** the page uses server-provided thumbnail metadata when available
- **AND** it does not show personal gear creation, "My Gear", add-owned-gear, or save-to-my-gear actions

### Requirement: Full-Screen Navigation Field Focus

TrailMate SHALL keep full-screen navigation focused on field use.

#### Scenario: Full-screen navigation excludes preparation diagnostics

- **GIVEN** the user enters full-screen navigation
- **WHEN** GPS and route data are available
- **THEN** the first viewport shows map, current location, route polyline, checkpoints, recorded track, current checkpoint, next checkpoint, recording action, and compact status indicators
- **AND** it does not directly show auth setup, baseline profile evidence, AI gear explanation, Amap launch diagnostics, or long offline base-map education copy

#### Scenario: Field status opens details secondarily

- **GIVEN** the user sees GPS, recording, base map, or gear status in full-screen navigation
- **WHEN** the user taps a status indicator
- **THEN** TrailMate may show a focused bottom sheet or detail screen for that single status
- **AND** the detail does not replace the primary field controls
