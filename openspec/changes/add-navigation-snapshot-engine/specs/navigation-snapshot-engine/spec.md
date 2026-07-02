## ADDED Requirements

### Requirement: Engine computes a navigation snapshot
The system SHALL compute a navigation snapshot from route geometry, session state, a location sample, battery level, and optional prior off-route evidence.

#### Scenario: On-route sample produces progress data
- **WHEN** a location sample projects onto the planned route with acceptable GPS accuracy
- **THEN** the engine returns a snapshot with completed/remaining route data, remaining elevation, next waypoint, nearest exit, GPS accuracy, battery level, and no deviation alert

### Requirement: Engine evaluates off-route evidence
The system SHALL use GPS accuracy, lateral distance, elapsed time, and consecutive samples before escalating off-route status.

#### Scenario: Poor GPS accuracy does not create off-route warning
- **WHEN** a location sample has accuracy worse than the configured threshold
- **THEN** the engine returns a snapshot that preserves the session navigation state and does not create nearest route point guidance

#### Scenario: Sustained deviation creates confirmed off-route snapshot
- **WHEN** consecutive accurate samples remain beyond the suspected distance threshold for the configured confirmation duration
- **THEN** the engine returns a snapshot in confirmed off-route state with deviation distance and nearest route point guidance

### Requirement: Nearest route point guidance stays conservative
The system SHALL express nearest route point guidance as compass direction and distance only.

#### Scenario: Off-route guidance avoids safe-route claims
- **WHEN** the engine reports nearest route point guidance
- **THEN** the guidance contains a compass direction label and distance to the route point, without generating rerouting instructions or safe straight-line commands

### Requirement: Engine supports reverse navigation progress
The system SHALL respect the navigation session direction when calculating completed distance, remaining distance, remaining elevation, and next waypoint.

#### Scenario: Reverse direction calculates remaining data from current progress
- **WHEN** a session is navigating in reverse direction
- **THEN** the snapshot uses reverse route progress for remaining distance, remaining elevation, and next waypoint

### Requirement: Engine is side-effect free
The system MUST NOT start GPS, request permissions, start a foreground service, persist track points, or mutate route/session data while computing a snapshot.

#### Scenario: Snapshot calculation has no Android side effects
- **WHEN** the engine calculates a snapshot
- **THEN** all outputs are returned as values and no Android service, permission, storage, network, or UI side effect is performed
