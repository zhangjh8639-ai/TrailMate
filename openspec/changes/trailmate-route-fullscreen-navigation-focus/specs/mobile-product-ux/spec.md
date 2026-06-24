## ADDED Requirements

### Requirement: Route Cockpit Default View Is Compact

TrailMate SHALL keep the default route cockpit focused and scannable.

#### Scenario: First viewport avoids dense diagnostics

- **GIVEN** a user opens the `路线` tab for a usable route
- **WHEN** the default cockpit is displayed
- **THEN** the first viewport prioritizes a standalone map preview, a separate action panel, current checkpoint, next checkpoint, route progress, one primary action, and full-screen entry
- **AND** it may show lightweight status chips for location, recording, and offline route state
- **AND** it does not display map diagnostics, authorization panels, a mixed map-diagnostics block, or a multi-row field-control stack by default

#### Scenario: Route details are framed around hiking tasks

- **GIVEN** a user needs more route information from the default cockpit
- **WHEN** the secondary detail entry is displayed
- **THEN** the entry is framed around checkpoints, supply, rest, or route details
- **AND** it is not labeled as map status, light-navigation authorization, or diagnostics

### Requirement: Route Page Does Not Own Map Service Authorization

TrailMate SHALL keep online map service authorization in first-use onboarding or app preparation, not route diagnostics.

#### Scenario: Route diagnostics show status only

- **GIVEN** online map support is configured but map service consent is not accepted
- **WHEN** the user expands route map/status details
- **THEN** the route page explains that online base maps are not enabled
- **AND** it does not show an AMap consent button or route-local authorization form

### Requirement: First-Use Setup Owns Location Authorization

TrailMate SHALL request foreground location authorization during first-use setup so route navigation and track recording do not introduce a separate default authorization step.

#### Scenario: Onboarding requests shared location authorization

- **GIVEN** a new user completes baseline setup
- **WHEN** the user accepts the app's map and navigation preparation step
- **THEN** TrailMate requests Android foreground location permission
- **AND** subsequent route navigation, current-position support, safety sharing, and track recording use that shared permission state

#### Scenario: Route page avoids duplicate authorization setup

- **GIVEN** the user opens the route cockpit after first-use setup
- **WHEN** the default cockpit or full-screen navigation is displayed
- **THEN** the route page does not present map-status or light-navigation authorization as normal setup controls
- **AND** any route-level location action is limited to recovery when the system permission is missing or unavailable

### Requirement: Route Navigation Has A Full-Screen Focus Mode

TrailMate SHALL provide a full-screen route mode for in-field navigation and track recording.

#### Scenario: User enters full-screen navigation

- **GIVEN** the user is on the `路线` tab
- **WHEN** the user chooses the full-screen navigation action
- **THEN** TrailMate hides app bottom navigation and route detail chrome
- **AND** the screen focuses on the route map, current checkpoint, progress, route-match status, location status, track recording status, and field-critical actions

#### Scenario: Default route cockpit does not start a hike directly

- **GIVEN** the user is viewing the normal route cockpit
- **WHEN** the route is ready for field use
- **THEN** the normal cockpit does not expose `开始徒步`, pause/resume, or mark-checkpoint controls
- **AND** its primary field action opens full-screen navigation
- **AND** `开始徒步`, live checkpoint action guidance, mark-checkpoint, safety share, and recording controls are available only in full-screen navigation

#### Scenario: Expanded route details do not reintroduce field controls

- **GIVEN** the user is viewing the normal route cockpit
- **WHEN** they expand route details such as checkpoints, supply, diagnostics, location reliability, or offline-map status
- **THEN** TrailMate may show route explanation and troubleshooting evidence
- **AND** it MUST NOT show `开始徒步`, pause/resume, mark-checkpoint, or other in-field control buttons outside full-screen navigation

#### Scenario: User exits full-screen navigation

- **GIVEN** the user is in full-screen navigation
- **WHEN** the user chooses the exit action
- **THEN** TrailMate returns to the normal route detail tab layout
- **AND** the app bottom navigation is visible again
