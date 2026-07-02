## ADDED Requirements

### Requirement: Navigation start requests runtime permissions
The system SHALL request foreground location permission when a user starts active track navigation from a selected route, and on Android 13+ it SHALL include notification permission in the same start flow.

#### Scenario: User starts navigation without required permission
- **WHEN** a selected route is visible on the Navigation tab and foreground location permission is not granted
- **THEN** the app requests foreground location permission before starting the tracking foreground service

#### Scenario: Notification permission is requested on Android 13 plus
- **WHEN** a selected route is visible on the Navigation tab on Android 13 or newer
- **THEN** the start flow includes `POST_NOTIFICATIONS` in the runtime permission request

### Requirement: Navigation start launches real tracking service
The system SHALL start `TrackingForegroundService` only after foreground location permission is granted, using a service launcher boundary rather than fake GPS, simulated progress, or direct UI-owned service intent construction.

#### Scenario: Permission is already granted
- **WHEN** the user taps the navigation start action and foreground location permission is already granted
- **THEN** the app starts the real tracking foreground service

#### Scenario: Permission is granted after request
- **WHEN** the user grants foreground location permission from the navigation start request
- **THEN** the app starts the real tracking foreground service

#### Scenario: Notification permission is denied on Android 13 plus
- **WHEN** the user grants foreground location but denies notification permission from the navigation start request on Android 13 or newer
- **THEN** the app does not start the tracking foreground service and explains that notification permission is needed to show the running navigation state

### Requirement: Permission denied keeps route viewing available
The system SHALL show Chinese fallback copy when foreground location permission is denied, explaining that continuous location and track navigation cannot start while keeping the selected route visible.

#### Scenario: User denies foreground location
- **WHEN** the user denies the navigation start location permission request
- **THEN** the selected route remains visible and the app shows that location permission is needed for continuous navigation

### Requirement: Active state can stop foreground tracking service
The system SHALL expose a stop action after the foreground tracking service has been started from the navigation flow.

#### Scenario: User stops active tracking support
- **WHEN** the tracking foreground service has been started from the Navigation tab
- **THEN** the user can stop it from the Navigation tab and the app sends the service stop command

### Requirement: Scope excludes background location and simulated navigation
The system SHALL NOT request background location, create fake track points, simulate route progress, or mark the route as truly GPS-matched in this slice.

#### Scenario: Navigation service start scope remains honest
- **WHEN** the service is started from the Navigation tab
- **THEN** the UI and domain state do not create simulated track points, fabricated progress, or a completed navigation record
